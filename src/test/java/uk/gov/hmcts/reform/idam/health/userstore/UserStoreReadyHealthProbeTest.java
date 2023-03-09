package uk.gov.hmcts.reform.idam.health.userstore;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;

@RunWith(MockitoJUnitRunner.class)
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