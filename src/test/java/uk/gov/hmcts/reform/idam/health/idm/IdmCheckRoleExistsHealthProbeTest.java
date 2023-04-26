package uk.gov.hmcts.reform.idam.health.idm;

import com.google.common.collect.ImmutableMap;
import feign.Response;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.idam.health.am.AmProvider;

import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class IdmCheckRoleExistsHealthProbeTest {

    @Mock
    private IdmProvider idmProvider;

    @Mock
    private AmProvider amProvider;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private IdmHealthProbeProperties config;

    @InjectMocks
    private IdmCheckRoleExistsHealthProbe idmCheckRoleExistsHealthProbe;

    @Before
    public void setup() {
        given(config.getCheckRoleExists().getAmHost()).willReturn("test-am-host");
        given(config.getCheckRoleExists().getRoleId()).willReturn("test-citizen-id");
        given(config.getCheckRoleExists().getIdmClientId()).willReturn("test-idm-client-id");
        given(config.getCheckRoleExists().getIdmClientSecret()).willReturn("test-idm-client-secret");
        given(config.getCheckRoleExists().getIdmClientScope()).willReturn("test-idm-client-scope");
        given(config.getCheckRoleExists().getAmUser()).willReturn("test-am-user");
        given(config.getCheckRoleExists().getAmPassword()).willReturn("test-am-password");
    }

    @Test
    public void probe_roleExists() {
        Response response = mock(Response.class);
        given(response.status()).willReturn(HttpStatus.OK.value());
        given(amProvider.rootPasswordGrantAccessToken(
                eq("test-am-host"),
                eq("test-am-user"),
                eq("test-am-password"),
                eq("test-idm-client-id"),
                eq("test-idm-client-secret"),
                eq("test-idm-client-scope")
              )).willReturn(ImmutableMap.of("access_token", "test-access-token"));
        given(idmProvider.getRole(eq("bearer test-access-token"), eq("test-citizen-id"))).willReturn(response);

        assertTrue(idmCheckRoleExistsHealthProbe.probe());
    }

    @Test
    public void probe_roleDoesNotExist() {
        Response response = mock(Response.class);
        given(response.status()).willReturn(HttpStatus.NOT_FOUND.value());
        given(amProvider.rootPasswordGrantAccessToken(
                eq("test-am-host"),
                eq("test-am-user"),
                eq("test-am-password"),
                eq("test-idm-client-id"),
                eq("test-idm-client-secret"),
                eq("test-idm-client-scope")
        )).willReturn(ImmutableMap.of("access_token", "test-access-token"));
        given(idmProvider.getRole(eq("bearer test-access-token"), eq("test-citizen-id"))).willReturn(response);

        assertFalse(idmCheckRoleExistsHealthProbe.probe());
    }

    @Test
    public void probe_idmAccessTokenNotGranted() {
        given(amProvider.rootPasswordGrantAccessToken(
                eq("test-am-host"),
                eq("test-am-user"),
                eq("test-am-password"),
                eq("test-idm-client-id"),
                eq("test-idm-client-secret"),
                eq("test-idm-client-scope")
        )).willReturn(ImmutableMap.of("no_access_token", "nothing"));

        assertFalse(idmCheckRoleExistsHealthProbe.probe());

        verify(idmProvider, never()).getRole(any(), any());
    }

    @Test
    public void probe_amThrowsExceptionForGrant() {
        given(amProvider.rootPasswordGrantAccessToken(
                eq("test-am-host"),
                eq("test-am-user"),
                eq("test-am-password"),
                eq("test-idm-client-id"),
                eq("test-idm-client-secret"),
                eq("test-idm-client-scope")
        )).willThrow(new RuntimeException("test-exception"));

        assertFalse(idmCheckRoleExistsHealthProbe.probe());

        verify(idmProvider, never()).getRole(any(), any());
    }
}