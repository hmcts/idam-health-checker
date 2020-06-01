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
 * A helper class for easier migration from slf4j-logback to pure logback.
 * Used by lombok's {@link lombok.CustomLog CustomLog}.
 */
@UtilityClass
public class LogHelper {

    private static LoggerContext loggerContext = generateLoggerContext();

    private static LoggerContext generateLoggerContext() {
        // not the most elegant solution as it supports profiles supplied only as a system property
        final String activeProfilesProperty = (String) System.getProperties().get("spring.profiles.active");
        final String[] activeProfiles = activeProfilesProperty == null ? null : activeProfilesProperty.split(",");

        try {
            LoggerContext context = new LoggerContext();
            JoranConfigurator configurator = new JoranConfigurator();

            configurator.setContext(context);
            context.reset();

            final String configResourceName;

            if (activeProfiles == null) {
                // fall-back to console and file only, for safety
                configResourceName = "custom-logback-console-file.xml";
            } else {
                final List<String> activeProfilesList = Arrays.asList(activeProfiles);
                if (activeProfilesList.contains("insightconsole"))
                    configResourceName = "custom-logback-insight-console-file.xml";
                else if (activeProfilesList.contains("live"))
                    configResourceName = "custom-logback-insight.xml";
                else if (activeProfilesList.contains("consoleonly"))
                    configResourceName = "custom-logback-console-file.xml";
                else
                    throw new RuntimeException("Cannot match Spring profiles to logback configs.");
            }

            // Get a configuration file from classpath
            URL configurationUrl = Thread.currentThread().getContextClassLoader().getResource(configResourceName);
            if (configurationUrl == null) {
                throw new IllegalStateException("Unable to find custom logback configuration file");
            }

            configurator.doConfigure(configurationUrl);
            StatusPrinter.printInCaseOfErrorsOrWarnings(context);
            // this must be output directly in case there are problems with logging
            System.out.println("Loaded " + configResourceName + " custom logback profile."); //NOSONAR
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

}
