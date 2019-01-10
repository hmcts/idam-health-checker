package uk.gov.hmcts.reform.idam.health.backup;

import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.idam.health.probe.HealthProbe;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@Slf4j
public class FileFreshnessProbe implements HealthProbe {

    private final String probeName;
    private final Path checkPath;
    private final long fileExpiryMs;

    private FileSystemInfo fileSystemInfo;
    private Clock clock;

    public FileFreshnessProbe(String probeName, String path, long fileExpiryMs) {
        this.probeName = probeName;
        this.checkPath = Paths.get(path);
        this.fileExpiryMs = fileExpiryMs;
        this.fileSystemInfo = new DefaultFileSystemInfo();
        this.clock = Clock.systemDefaultZone();
    }

    @Override
    public boolean probe() {

        try {
            if (fileSystemInfo.exists(checkPath)) {

                LocalDateTime updateTime = fileSystemInfo.lastModifiedTime(checkPath);
                log.info("{}: Path {} modified at {}", probeName, checkPath, updateTime);
                return LocalDateTime.now(clock).isBefore(updateTime.plus(fileExpiryMs, ChronoUnit.MILLIS));

            } else {
                log.warn("{}: Nothing at path {}", probeName, checkPath);
            }

        } catch (Exception e) {
            log.error("{}: {} [{}]", probeName, e.getMessage(), e.getClass().getSimpleName());
        }

        return false;
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

    public interface FileSystemInfo {
        boolean exists(Path path);
        LocalDateTime lastModifiedTime(Path path) throws IOException;
    }

    private class DefaultFileSystemInfo implements FileSystemInfo {

        @Override
        public boolean exists(Path path) {
            return Files.exists(path);
        }

        @Override
        public LocalDateTime lastModifiedTime(Path path) throws IOException {
            return LocalDateTime.ofInstant(Files.getLastModifiedTime(path).toInstant(), ZoneId.systemDefault());
        }
    }

}
