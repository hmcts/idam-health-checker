package uk.gov.hmcts.reform.idam.health;


import com.microsoft.applicationinsights.autoconfigure.initializer.SpringBootTelemetryInitializer;
import com.microsoft.applicationinsights.extensibility.TelemetryInitializer;
import com.microsoft.applicationinsights.extensibility.TelemetryProcessor;
import com.microsoft.applicationinsights.telemetry.RequestTelemetry;
import com.microsoft.applicationinsights.telemetry.Telemetry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class ApplicationConfig {

    @Value("${spring.application.instance:instance}")
    private String appInstance;

    @Bean
    public TaskScheduler taskScheduler(){
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(5);
        threadPoolTaskScheduler.setThreadNamePrefix("ProbeScheduler");
        return threadPoolTaskScheduler;
    }

    @Bean
    public TelemetryInitializer telemetryInitializer() {
        return new SpringBootTelemetryInitializer() {
            @Override
            public void initialize(Telemetry telemetry) {
                super.initialize(telemetry);
                telemetry.getContext().getCloud().setRoleInstance(appInstance);
            }
        };
    }

    @Bean
    @Profile("pessimist")
    public TelemetryProcessor pessimistTelemetryProcessor() {
        return new TelemetryProcessor() {
            @Override
            public boolean process(Telemetry telemetry) {
                if (telemetry instanceof RequestTelemetry) {
                    if (!((RequestTelemetry) telemetry).isSuccess()) {
                        return false;
                    }
                }
                return true;
            }
        };
    }

}
