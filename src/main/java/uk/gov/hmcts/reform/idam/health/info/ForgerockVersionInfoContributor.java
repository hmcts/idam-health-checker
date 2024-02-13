package uk.gov.hmcts.reform.idam.health.info;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

/**
 * Makes the git commit of the cnp_idam_packer branch available. The commit details are deployed as
 * a file on each host.
 */
@Slf4j
@Component
public class ForgerockVersionInfoContributor implements InfoContributor {

    private final static String UNKNOWN = "unknown";

    private FileAccess fileAccess = new FileAccess();

    @Value("${forgerock.info.path}")
    private String versionFilePath;

    @Override
    public void contribute(Info.Builder builder) {
        try {
            String frVersion = UNKNOWN;
            String frUpdateTime = UNKNOWN;
            Path filePath = Paths.get(versionFilePath);
            if (fileAccess.exists(filePath)) {
                String content = fileAccess.content(filePath);
                if (content != null) {
                    frVersion = content;
                }
                LocalDateTime lastModifiedTime = fileAccess.lastModifiedTime(filePath);
                if (lastModifiedTime != null) {
                    frUpdateTime = lastModifiedTime.format(DateTimeFormatter.ISO_DATE_TIME);
                }
            }
            builder.withDetail("forgerock", ImmutableMap.of("commit", frVersion, "modifiedtime", frUpdateTime, "path", versionFilePath));
        } catch (Exception e) {
            log.error("{}: {} [{}]", ForgerockVersionInfoContributor.class.getSimpleName(), e.getMessage(), e.getClass().getSimpleName());
        }
    }

    @VisibleForTesting
    void setFileAccess(FileAccess fileAccess) {
        this.fileAccess = fileAccess;
    }

    @VisibleForTesting
    void setVersionFilePath(String versionFilePath) {
        this.versionFilePath = versionFilePath;
    }

    public class FileAccess {

        public boolean exists(Path path) {
            return Files.exists(path);
        }

        public LocalDateTime lastModifiedTime(Path path) throws IOException {
            return LocalDateTime.ofInstant(Files.getLastModifiedTime(path).toInstant(), ZoneId.systemDefault());
        }

        public String content(Path path) throws IOException {
            return Files.lines(path).collect(Collectors.joining(" "));
        }

    }
}
