package uk.gov.hmcts.reform.idam.healthchecker.health.ds;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ReflectionUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DSReplicationHealthIndicatorTest {

    @Autowired
    DSReplicationHealthIndicator indicator;

    File returnZero;
    File returnOne;

    @Before
    public void setup() throws Exception {
        returnZero = createTempFile ("return-zero.sh", "echo 0");
        returnOne = createTempFile ("return-one.sh", "echo 1");
    }

    @After
    public void teardown() throws Exception {
        returnOne.deleteOnExit();
        returnZero.deleteOnExit();
    }

    @Test
    public void checkInvalidResponseReturnsDown() throws Exception {
        setScript(returnOne.getAbsolutePath());
        Health health = indicator.health();
        assertEquals("DOWN", health.getStatus().toString());
    }

    @Test
    public void checkValidResponseReturnsUp() throws Exception {
        setScript(returnZero.getAbsolutePath());
        Health health = indicator.health();
        assertEquals("UP", health.getStatus().toString());
    }

    @Test
    public void checkMissingFileReturnsDown() throws Exception {
        setScript("/not/here/script.sh");
        Health health = indicator.health();
        assertEquals("DOWN", health.getStatus().toString());
    }

    private void setScript(String path) {
        Field field = ReflectionUtils.findField(DSReplicationHealthIndicator.class, "script");
        ReflectionUtils.makeAccessible(field);
        ReflectionUtils.setField(field, indicator, path);
    }

    private File createTempFile(String name, String contents) throws IOException {
        File f = File.createTempFile(name, ".tmp");
        BufferedWriter bw = new BufferedWriter(new FileWriter(f));
        bw.write(contents);
        bw.close();

        f.setExecutable(true);
        f.setReadable(true);
        f.setWritable(true);

        return f;
    }
}
