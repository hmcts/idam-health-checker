package uk.gov.hmcts.reform.idam.health.util;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import lombok.experimental.UtilityClass;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * A helper class for easier migration from slf4j-logback to pure logback. Used by lombok's {@link lombok.CustomLog CustomLog}.
 */
@UtilityClass
public class LogHelper {

    private static LoggerContext loggerContext = generateLoggerContext();

    private static LoggerContext generateLoggerContext() {
        // this is hacky as it supports profiles supplied only as a system property
        String[] activeProfiles = ((String) System.getProperties().get("spring.profiles.active")).split(",");
        if (activeProfiles.length < 2)
            throw new RuntimeException("Expected at least 2 active profiles provided as system properties.");

        try {
            LoggerContext context = new LoggerContext();
            JoranConfigurator configurator = new JoranConfigurator();

            configurator.setContext(context);
            context.reset();

            final String configResourceName;
            final List<String> activeProfilesList = Arrays.asList(activeProfiles);

            if (activeProfilesList.contains("insightconsole"))
                configResourceName = "custom-logback-insight-console-file.xml";
            else if (activeProfilesList.contains("live"))
                configResourceName = "custom-logback-insight.xml";
            else if (activeProfilesList.contains("consoleonly"))
                configResourceName = "custom-logback-console-file.xml";
            else
                throw new RuntimeException("Cannot match Spring profiles to logback configs.");

            // Get a configuration file from classpath
            URL configurationUrl = Thread.currentThread().getContextClassLoader().getResource(configResourceName);
            if (configurationUrl == null) {
                throw new IllegalStateException("Unable to find custom logback configuration file");
            }

            configurator.doConfigure(configurationUrl);
            StatusPrinter.printInCaseOfErrorsOrWarnings(context);

            return context;
        } catch (JoranException e) {
            throw new RuntimeException("Unable to configure logger", e);
        }
    }

    /**
     * Returns a new logger associated with a given class. Used by lombok's {@link lombok.CustomLog CustomLog}.
     *
     * @param clazz the class
     * @return the newly created logger
     */
    @SuppressWarnings("unused")
    public Logger getLogger(Class<?> clazz) {
        return loggerContext.getLogger(clazz);
    }

//    /**
//     * Generates a logger context for logback taking Spring profiles into account.
//     *
//     * @param activeProfiles active Spring profiles
//     * @return a new context
//     */
//    private static LoggerContext loggerContext(@NonNull final String[] activeProfiles) {
//        LoggerContext loggerContext = new LoggerContext();
//        ContextInitializer contextInitializer = new ContextInitializer(loggerContext);
//        try {
//            final String logbackConfigurationResource;
//            final List<String> activeProfilesList = Arrays.asList(activeProfiles);
//
//            if (activeProfilesList.contains("insightconsole"))
//                logbackConfigurationResource = "custom-logback-insight-console-file.xml";
//            else if (activeProfilesList.contains("live"))
//                logbackConfigurationResource = "custom-logback-insight.xml";
//            else if (activeProfilesList.contains("consoleonly"))
//                logbackConfigurationResource = "custom-logback-console-file.xml";
//            else
//                throw new RuntimeException("Cannot match Spring profiles to logback configs.");
//
//            // Get a configuration file from classpath
//            URL configurationUrl = Thread.currentThread().getContextClassLoader().getResource(logbackConfigurationResource);
//            if (configurationUrl == null) {
//                throw new IllegalStateException("Unable to find custom logback configuration file");
//            }
//            // Ask context initializer to load configuration into context
//            contextInitializer.configureByResource(configurationUrl);
//
//            StatusPrinter.print(loggerContext);
//            return loggerContext;
//        } catch (JoranException e) {
//            throw new RuntimeException("Unable to configure logger", e);
//        }
//    }


    public static void main(String[] args) {
        Logger logger = getLogger(LogHelper.class);
        logger.info("elo");
    }
}
