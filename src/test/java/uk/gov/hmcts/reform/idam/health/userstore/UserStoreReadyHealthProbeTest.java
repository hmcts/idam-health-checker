package uk.gov.hmcts.reform.idam.health.userstore;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
public class UserStoreReadyHealthProbeTest {

    @Mock
    private UserStoreProvider userStoreProvider;

    @InjectMocks
    private UserStoreReadyHealthProbe underTest;

    @Test
    public void testProbe_success() {
        assertTrue(underTest.probe());
    }

    @Test
    public void testProbe_failException() {
        doThrow(new RuntimeException("text-exception")).when(userStoreProvider).healthy();
        assertFalse(underTest.probe());
    }

}