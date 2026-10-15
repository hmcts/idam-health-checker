#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/../.."
./gradlew clean build publishMainPublicationToCiRepository --console=plain --warning-mode all
python3 scripts/ci/runtime_smoke.py
