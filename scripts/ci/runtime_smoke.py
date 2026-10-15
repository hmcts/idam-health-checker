#!/usr/bin/env python3
"""Verify published artifacts and run the executable JAR against local fixtures."""
import base64
import contextlib
import datetime
import hashlib
import http.server
import json
import os
from pathlib import Path
import socket
import subprocess
import threading
import time
import urllib.error
import urllib.parse
import urllib.request
import xml.etree.ElementTree as ET
import zipfile

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / 'build/reports/runtime'
OUT.mkdir(parents=True, exist_ok=True)
JAVA = str(Path(os.environ['JAVA_HOME']) / 'bin/java') if os.environ.get('JAVA_HOME') else 'java'
VERSION = os.environ.get('RELEASE_VERSION', 'DEV-SNAPSHOT').removeprefix('refs/tags/')
COMMIT = subprocess.check_output(['git', 'rev-parse', 'HEAD'], cwd=ROOT, text=True).strip()
HTTP = urllib.request.build_opener(urllib.request.ProxyHandler({}))


def require(condition, message):
    if not condition:
        raise AssertionError(message)


def artifacts():
    suites = [ET.parse(p).getroot() for p in (ROOT / 'build/test-results/test').glob('TEST-*.xml')]
    executed = sum(int(s.get('tests', 0)) - int(s.get('skipped', 0)) for s in suites)
    require(executed > 0, 'No tests executed')
    require(all(int(s.get('failures', 0)) + int(s.get('errors', 0)) == 0 for s in suites), 'Test failures')
    jar = ROOT / f'build/libs/idam-health-checker-{VERSION}.jar'
    with zipfile.ZipFile(jar) as archive:
        manifest = archive.read('META-INF/MANIFEST.MF').decode()
        require(f'Implementation-Version: {VERSION}' in manifest, 'Incorrect JAR version')
        require('Main-Class: org.springframework.boot.loader.launch.JarLauncher' in manifest, 'Missing Boot launcher')
        require(f'git.commit.id={COMMIT}' in archive.read('BOOT-INF/classes/git.properties').decode(), 'Incorrect Git metadata')
    repo = ROOT / 'build/ci-repository/com/github/hmcts/idam-health-checker' / VERSION
    poms = list(repo.glob('*.pom'))
    require(len(poms) == 1, 'Expected one published POM')
    pom = ET.parse(poms[0]).getroot()
    ns = {'m': 'http://maven.apache.org/POM/4.0.0'}
    for key, expected in [('groupId', 'com.github.hmcts'), ('artifactId', 'idam-health-checker'), ('version', VERSION)]:
        require(pom.findtext('m:' + key, namespaces=ns) == expected, f'Incorrect published {key}')
    published = [p for p in repo.glob('*.jar') if not p.name.endswith('-plain.jar')]
    require(len(published) == 1, 'Expected one published executable JAR')
    require(hashlib.sha256(jar.read_bytes()).digest() == hashlib.sha256(published[0].read_bytes()).digest(), 'Published JAR differs from build')
    require(bool(list(repo.glob('*-plain.jar'))), 'Missing published library JAR')
    print(f'Publication verified; {executed} tests executed', flush=True)
    return published[0]


def get(port, path):
    try:
        response = HTTP.open(f'http://127.0.0.1:{port}{path}', timeout=2)
    except urllib.error.HTTPError as error:
        response = error
    with response:
        return response.code, json.load(response)


def wait_for(process, port, label, predicate):
    deadline = time.monotonic() + 60
    last = None
    while time.monotonic() < deadline:
        require(process.poll() is None, f'Application exited during {label}; inspect runtime logs')
        try:
            last = get(port, '/admin/health')
            if predicate(*last):
                (OUT / f'{label}.json').write_text(json.dumps(last, indent=2))
                return last[1]
        except (OSError, ValueError) as error:
            last = str(error)
        time.sleep(0.25)
    raise AssertionError(f'Timed out waiting for {label}: {last}')


@contextlib.contextmanager
def application(jar, profile, extra=()):
    with socket.socket() as sock:
        sock.bind(('127.0.0.1', 0))
        port = sock.getsockname()[1]
    version_file = OUT / 'forgerock-version'
    version_file.write_text('ci-forgerock-fixture')
    args = [JAVA, '-Xmx256m', '-jar', str(jar), f'--spring.profiles.active={profile}',
            '--server.address=127.0.0.1', f'--server.port={port}', '--azure.keyvault.uri=',
            f'--forgerock.info.path={version_file}', *extra]
    with (OUT / f'{profile}.log').open('w') as log:
        process = subprocess.Popen(args, cwd=ROOT, stdout=log, stderr=subprocess.STDOUT)
        try:
            yield process, port
        finally:
            process.terminate()
            try:
                process.wait(timeout=10)
            except subprocess.TimeoutExpired:
                process.kill()
                process.wait(timeout=5)


class AmStub(http.server.BaseHTTPRequestHandler):
    failing = threading.Event()
    seen = set()
    errors = []
    lock = threading.Lock()

    def log_message(self, *_):
        pass

    def reply(self, status, body):
        data = json.dumps(body).encode()
        self.send_response(status)
        self.send_header('Content-Type', 'application/json')
        self.send_header('Content-Length', str(len(data)))
        self.end_headers()
        self.wfile.write(data)

    def do_GET(self):
        if self.path not in ['/json/health/live', '/json/health/ready']:
            self.reply(404, {})
            return
        self.reply(503 if self.path.endswith('/ready') and self.failing.is_set() else 200, {})

    def do_POST(self):
        expected = {'grant_type': ['password'], 'scope': ['openid profile roles'],
                    'username': ['ci user+&'], 'password': ['ci pass+&=']}
        root = self.path == '/oauth2/access_token'
        if root:
            expected.update(client_id=['ci-client'], client_secret=['default'])
        valid_path = root or self.path == '/oauth2/realms/hmcts/access_token'
        form = urllib.parse.parse_qs(self.rfile.read(int(self.headers.get('Content-Length', 0))).decode())
        auth = 'Basic ' + base64.b64encode(b'ci-agent:ci-secret').decode()
        valid = valid_path and form == expected and self.headers.get('Content-Type', '').startswith('application/x-www-form-urlencoded')
        valid = valid and (root or self.headers.get('Authorization') == auth)
        with self.lock:
            if valid:
                self.seen.add(self.path)
            else:
                self.errors.append(f'Invalid form request: {self.path}, {form}')
        self.reply(200 if valid else 400, {'access_token': 'ci-token'} if valid else {})


def main():
    jar = artifacts()
    for profile, code, status in [('optimist', 200, 'UP'), ('pessimist', 503, 'DOWN')]:
        with application(jar, profile) as (process, port):
            wait_for(process, port, profile, lambda c, b: c == code and b.get('status') == status)
            info_code, info = get(port, '/admin/info')
            require(info_code == 200, 'Info endpoint unavailable')
            require(info['forgerock']['commit'] == 'ci-forgerock-fixture', 'ForgeRock info missing')
            require(COMMIT.startswith(info['git']['commit']['id']), 'Git info mismatch')
        print(f'{profile}: {code}/{status} and info verified', flush=True)
    server = http.server.ThreadingHTTPServer(('127.0.0.1', 0), AmStub)
    thread = threading.Thread(target=server.serve_forever, daemon=True)
    thread.start()
    extra = [f'--am.root=http://127.0.0.1:{server.server_port}',
             '--test.owner.username=ci user+&', '--test.owner.password=ci pass+&=',
             '--web.admin.client.name=ci-agent', '--web.admin.client.secret=ci-secret',
             '--am.healthprobe.root-password-grant.am-user=ci user+&',
             '--am.healthprobe.root-password-grant.am-password=ci pass+&=',
             '--am.healthprobe.root-password-grant.client-id=ci-client',
             '--am.healthprobe.root-password-grant.client-scope=openid profile roles']
    for probe in ['live', 'ready', 'password-grant', 'root-password-grant']:
        extra += [f'--am.healthprobe.{probe}.check-interval=250ms', f'--am.healthprobe.{probe}.freshness-interval=1s']
    components = ['amLiveScheduledHealthProbe', 'amReadyScheduledHealthProbe',
                  'amPasswordGrantScheduledHealthProbe', 'amRootPasswordGrantScheduledHealthProbe']
    try:
        with application(jar, 'am', extra) as (process, port):
            up = lambda c, b: c == 200 and b.get('status') == 'UP' and all(b.get('components', {}).get(k, {}).get('status') == 'UP' for k in components)
            body = wait_for(process, port, 'am-up', up)
            for name in components:
                for field in ['lastChecked', 'lastStatusChange']:
                    datetime.datetime.fromisoformat(body['components'][name]['details'][field].replace('Z', '+00:00'))
            AmStub.failing.set()
            body = wait_for(process, port, 'am-down', lambda c, b: c == 503 and b.get('status') == 'DOWN' and b['components']['amReadyScheduledHealthProbe']['status'] == 'DOWN')
            details = body['components']['amReadyScheduledHealthProbe']['details']
            require('503' in details['AMReadyHealthProbe'], 'Failure detail missing')
            datetime.datetime.fromisoformat(details['lastDetailUpdate'].replace('Z', '+00:00'))
            AmStub.failing.clear()
            wait_for(process, port, 'am-recovered', up)
            require(len(AmStub.seen) == 2 and not AmStub.errors, f'Form validation failed: {AmStub.errors}')
        print('AM: forms, JSON decoding, timestamps, failure and recovery verified', flush=True)
    finally:
        server.shutdown()
        server.server_close()
        thread.join(timeout=5)
    print('Runtime verification passed', flush=True)


if __name__ == '__main__':
    main()
