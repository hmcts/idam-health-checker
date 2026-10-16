package uk.gov.hmcts.reform.idam.health.info;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.info.Info;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ForgerockVersionInfoContributorTest {

    private static final long EPOCH_1AM = 3600;

    @Mock
    private ForgerockVersionInfoContributor.FileAccess fileAccess;

    private final ForgerockVersionInfoContributor contributor = new ForgerockVersionInfoContributor();

    @BeforeEach
    public void setup() {
        contributor.setVersionFilePath("test-path");
        contributor.setFileAccess(fileAccess);
    }

    @Test
    public void testContribute_noFileAtPath() {
        when(fileAccess.exists(any())).thenReturn(false);
        Info.Builder builder = new Info.Builder();
        contributor.contribute(builder);
        Map<String, String> result = getForgerockDetails(builder);
        assertThat(result.get("commit"), is("unknown"));
        assertThat(result.get("modifiedtime"), is("unknown"));
        assertThat(result.get("path"), is("test-path"));
    }

    @Test
    public void testContribute_fileAtPathNoContentsNoTime() throws IOException {
        when(fileAccess.exists(any())).thenReturn(true);
        when(fileAccess.lastModifiedTime(any())).thenReturn(null);
        when(fileAccess.content(any())).thenReturn(null);
        Info.Builder builder = new Info.Builder();
        contributor.contribute(builder);
        Map<String, String> result = getForgerockDetails(builder);
        assertThat(result.get("commit"), is("unknown"));
        assertThat(result.get("modifiedtime"), is("unknown"));
        assertThat(result.get("path"), is("test-path"));
    }

    @Test
    public void testContribute_fileAtPathStringContent() throws IOException {
        when(fileAccess.exists(any())).thenReturn(true);
        when(fileAccess.lastModifiedTime(any())).thenReturn(LocalDateTime.ofInstant(Instant.ofEpochSecond(EPOCH_1AM), ZoneId.systemDefault()));
        when(fileAccess.content(any())).thenReturn("test-content");
        Info.Builder builder = new Info.Builder();
        contributor.contribute(builder);
        Map<String, String> result = getForgerockDetails(builder);
        assertThat(result.get("commit"), is("test-content"));
        assertThat(result.get("modifiedtime"), is(LocalDateTime.ofInstant(Instant.ofEpochSecond(EPOCH_1AM), ZoneId.systemDefault()).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
        assertThat(result.get("path"), is("test-path"));
    }

    @Test
    public void testContribute_handleException() throws IOException {
        when(fileAccess.exists(any())).thenReturn(true);
        when(fileAccess.content(any())).thenThrow(new RuntimeException("test exception"));
        try {
            contributor.contribute(new Info.Builder());
            assert(true);
        } catch (Exception e) {
            fail();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> getForgerockDetails(Info.Builder builder) {
        return (Map<String, String>) builder.build().get("forgerock");
    }

}
