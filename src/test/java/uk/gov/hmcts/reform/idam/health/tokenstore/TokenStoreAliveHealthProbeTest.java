package uk.gov.hmcts.reform.idam.health.tokenstore;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
public class TokenStoreAliveHealthProbeTest {

    @Mock
    private TokenStoreProvider tokenStoreProvider;

    @InjectMocks
    private TokenStoreAliveHealthProbe underTest;

    @Test
    public void testProbe_success() {
        assertTrue(underTest.probe());
    }

    @Test
    public void testProbe_failException() {
        doThrow(new RuntimeException("text-exception")).when(tokenStoreProvider).alive();
        assertFalse(underTest.probe());
    }

}