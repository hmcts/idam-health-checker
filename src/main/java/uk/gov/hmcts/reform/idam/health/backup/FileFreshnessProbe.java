package uk.gov.hmcts.reform.idam.health.backup;

import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import uk.gov.hmcts.reform.idam.health.probe.HealthProbe;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import static java.text.MessageFormat.format;

@Slf4j
public class FileFreshnessProbe extends HealthProbe {

    private final String probeName;
    private final Path checkPath;
    private final long fileExpiryMs;

    private FileSystemInfo fileSystemInfo;
    private Clock clock;

    public FileFreshnessProbe(String probeName, String path, long fileExpiryMs) {
        this.probeName = probeName;
        this.checkPath = Paths.get(path);
        this.fileExpiryMs = fileExpiryMs;
        this.fileSystemInfo = new FileSystemInfo();
        this.clock = Clock.systemDefaultZone();
    }

    @Override
    public Logger getLogger() {
        return log;
    }

    @Override
    public boolean probe() {

        try {
            if (fileSystemInfo.exists(checkPath)) {

                LocalDateTime updateTime = fileSystemInfo.lastModifiedTime(checkPath);
                if (LocalDateTime.now(clock).isBefore(updateTime.plus(fileExpiryMs, ChronoUnit.MILLIS))) {
                    log.debug("{}: Path {} modified at {}", getName(), checkPath, updateTime);
                    return true;
                } else {
                    return handleError(format("Path {0} modified at {1}", checkPath, updateTime));
                }

            } else {
                return handleError(format("Nothing at path {0}", checkPath));
            }

        } catch (Exception e) {
            return handleException(e);
        }

    }

    @VisibleForTesting
    void changeClock(Clock clock) {
        this.clock = clock;
    }

    @VisibleForTesting
    void setFileSystemInfo(FileSystemInfo fileSystemInfo) {
        this.fileSystemInfo = fileSystemInfo;
    }

    @Override
    public String getName() {
        return probeName;
    }

    public class FileSystemInfo {

        public boolean exists(Path path) {
            return Files.exists(path);
        }

        public LocalDateTime lastModifiedTime(Path path) throws IOException {
            return LocalDateTime.ofInstant(Files.getLastModifiedTime(path).toInstant(), ZoneId.systemDefault());
        }
    }

}
