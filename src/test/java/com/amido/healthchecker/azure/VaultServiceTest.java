package com.amido.healthchecker.azure;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest()
@ActiveProfiles("dev")
public class VaultServiceTest {

    @Autowired
    @Qualifier("vaultService")
    VaultService service;

    @Before
    public void setup() throws Exception {

    }

    @Test
    public void shouldExtractParameterAsSystemProperty() throws Exception {
        service.loadSecret("SMOKE_TEST_USER_USERNAME", "smoke-test-user-username");
        String actual = System.getProperty("SMOKE_TEST_USER_USERNAME");

        assertThat(actual.equals("smoke@test.com"));
    }

    @Test(expected = IllegalStateException.class)
    public void missingValueThrowsException() throws Exception {
        service.loadSecret("SMOKE_TEST_USER_USERNAME", "not-present");
        String actual = System.getProperty("SMOKE_TEST_USER_USERNAME");

        assertThat(actual.equals("smoke@test.com"));
    }

}
