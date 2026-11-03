package uk.gov.hmcts.reform.idam.health;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarFile;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

import static org.junit.jupiter.api.Assertions.*;

class PublishedArtifactTest {
    static final Path BUILD = Path.of(System.getProperty("verification.buildDir"));
    static final String VERSION = System.getProperty("verification.version");
    static final String COMMIT = System.getProperty("verification.commit");
    static final Path REPOSITORY = BUILD.resolve("ci-repository/com/github/hmcts/idam-health-checker/" + VERSION);

    static List<Path> files(Path directory, String suffix) throws Exception {
        try (var paths = Files.list(directory)) {
            return paths.filter(p -> p.getFileName().toString().endsWith(suffix)).toList();
        }
    }

    static Path executableJar() throws Exception {
        var jars = files(REPOSITORY, ".jar").stream()
                .filter(p -> !p.getFileName().toString().endsWith("-plain.jar")).toList();
        assertEquals(1, jars.size(), "Expected one published executable JAR");
        return jars.get(0);
    }

    @Test
    void publicationContainsTheBuiltJarAndCorrectMetadata() throws Exception {
        var published = executableJar();
        assertEquals(-1L, Files.mismatch(BUILD.resolve("libs/idam-health-checker-" + VERSION + ".jar"), published),
                "Published JAR differs from the built JAR");
        assertEquals(1, files(REPOSITORY, "-plain.jar").size(), "Missing library JAR");
        try (var jar = new JarFile(published.toFile())) {
            var manifest = jar.getManifest().getMainAttributes();
            assertEquals(VERSION, manifest.getValue("Implementation-Version"));
            assertEquals("org.springframework.boot.loader.launch.JarLauncher", manifest.getValue("Main-Class"));
            var properties = new Properties();
            try (InputStream stream = jar.getInputStream(jar.getJarEntry("BOOT-INF/classes/git.properties"))) {
                properties.load(stream);
            }
            assertEquals(COMMIT, properties.getProperty("git.commit.id"));
        }
        var poms = files(REPOSITORY, ".pom");
        assertEquals(1, poms.size(), "Expected one published POM");
        var factory = xmlFactory();
        var pom = factory.newDocumentBuilder().parse(poms.get(0).toFile()).getDocumentElement();
        for (var entry : java.util.Map.of("groupId", "com.github.hmcts", "artifactId", "idam-health-checker",
                "version", VERSION).entrySet()) {
            String value = null;
            for (var node = pom.getFirstChild(); node != null; node = node.getNextSibling()) {
                if (entry.getKey().equals(node.getNodeName())) {
                    value = node.getTextContent();
                }
            }
            assertEquals(entry.getValue(), value, entry.getKey());
        }
    }

    @Test
    void unitTestsExecutedSuccessfully() throws Exception {
        int executed = 0;
        for (var report : files(BUILD.resolve("test-results/test"), ".xml")) {
            var suite = xmlFactory().newDocumentBuilder().parse(report.toFile()).getDocumentElement();
            assertEquals("0", suite.getAttribute("failures"), report.toString());
            assertEquals("0", suite.getAttribute("errors"), report.toString());
            executed += Integer.parseInt(suite.getAttribute("tests")) - Integer.parseInt(suite.getAttribute("skipped"));
        }
        assertTrue(executed > 0, "No unit tests executed");
    }

    private static DocumentBuilderFactory xmlFactory() throws Exception {
        var factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return factory;
    }
}
