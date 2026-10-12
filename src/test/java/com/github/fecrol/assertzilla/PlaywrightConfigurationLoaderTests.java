package com.github.fecrol.assertzilla;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.smallrye.config.SmallRyeConfigBuilder;
import org.assertj.core.api.Assertions;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.*;
import org.slf4j.LoggerFactory;

import static org.hamcrest.Matchers.*;

@Tags({
        @Tag("unit")
})
@DisplayName("Playwright Configuration Loader Tests")
public class PlaywrightConfigurationLoaderTests {

    private Logger logger;
    private ListAppender<ILoggingEvent> appender;
    private ConfigurationLoader playwrightConfigurationLoader;

    private static final String PLAYWRIGHT_ENV_CONFIG_SET_VIA_SYSTEM_PROPERTIES = "properties";

    @BeforeEach
    void beforeEach() {
        logger = (Logger) LoggerFactory.getLogger(PlaywrightConfigurationLoader.class);
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        SmallRyeConfigBuilder configurationBuilder = new SmallRyeConfigBuilder().addDefaultSources().addDefaultInterceptors();
        playwrightConfigurationLoader = new PlaywrightConfigurationLoader(configurationBuilder);
    }

    @AfterEach
    void afterEach() {
        logger.detachAppender(appender);
        appender.stop();
    }

    @Test
    @DisplayName("Default configuration should load successfully when found in resources")
    void defaultConfigurationShouldLoadSuccessfullyWhenFoundInResources() {
        // when
        playwrightConfigurationLoader.loadConfiguration();

        // then
        Assertions.assertThat(appender.list).satisfiesOnlyOnce(event -> {
            Assertions.assertThat(event.getLevel()).isEqualTo(Level.INFO);
            Assertions
                    .assertThat(event.getFormattedMessage())
                    .isEqualToIgnoringCase("Playwright configuration playwright.config.yml loaded successfully.");
        });
    }

    @Test
    @DisplayName("Environment specific configuration indicated via environment variables should load successfully when found in resources")
    void environmentSpecificConfigurationIndicatedViaEnvironmentVariablesShouldLoadSuccessfullyWhenFoundInResources() {
        // given
        String playwrightEnvConfigFromEnvVars = System.getenv("PLAYWRIGHT_ENV_CONFIG");
        MatcherAssert.assertThat(playwrightEnvConfigFromEnvVars, is(not(emptyOrNullString())));

        // when
        playwrightConfigurationLoader.loadConfiguration();

        // then
        Assertions.assertThat(appender.list).satisfiesOnlyOnce(event -> {
            Assertions.assertThat(event.getLevel()).isEqualTo(Level.INFO);
            Assertions
                    .assertThat(event.getFormattedMessage())
                    .isEqualToIgnoringCase(String.format(
                            "Playwright configuration playwright-%s.config.yml loaded successfully.",
                            playwrightEnvConfigFromEnvVars
                    ));
        });
    }

    @Test
    @DisplayName("Environment specific configuration indicated via system properties should load successfully when found in resources")
    void environmentSpecificConfigurationIndicatedViaSystemPropertiesShouldLoadSuccessfullyWhenFoundInResources() {
        // given
        System.setProperty("playwright.env.config", PLAYWRIGHT_ENV_CONFIG_SET_VIA_SYSTEM_PROPERTIES);

        // when
        playwrightConfigurationLoader.loadConfiguration();

        // then
        Assertions.assertThat(appender.list).satisfiesOnlyOnce(event -> {
            Assertions.assertThat(event.getLevel()).isEqualTo(Level.INFO);
            Assertions
                    .assertThat(event.getFormattedMessage())
                    .isEqualToIgnoringCase(String.format(
                            "Playwright configuration playwright-%s.config.yml loaded successfully.",
                            PLAYWRIGHT_ENV_CONFIG_SET_VIA_SYSTEM_PROPERTIES
                    ));
        });
    }

    @Test
    @DisplayName("Default and environment specific configurations should both load when found in resources")
    void defaultAndEnvironmentSpecificConfigurationsShouldBothLoadWhenFoundInResources() {
        // given
        System.setProperty("playwright.env.config", PLAYWRIGHT_ENV_CONFIG_SET_VIA_SYSTEM_PROPERTIES);

        // when
        playwrightConfigurationLoader.loadConfiguration();

        // then
        Assertions.assertThat(appender.list).satisfiesExactlyInAnyOrder(
                defaultConfigEvent -> {
                    Assertions.assertThat(defaultConfigEvent.getLevel()).isEqualTo(Level.INFO);
                    Assertions
                            .assertThat(defaultConfigEvent.getFormattedMessage())
                            .isEqualToIgnoringCase("Playwright configuration playwright.config.yml loaded successfully.");
                },
                envSpecificConfigEvent -> {
                    Assertions.assertThat(envSpecificConfigEvent.getLevel()).isEqualTo(Level.INFO);
                    Assertions
                            .assertThat(envSpecificConfigEvent.getFormattedMessage())
                            .isEqualToIgnoringCase(String.format(
                                    "Playwright configuration playwright-%s.config.yml loaded successfully.",
                                    PLAYWRIGHT_ENV_CONFIG_SET_VIA_SYSTEM_PROPERTIES
                            ));
                }
        );
    }

    @Test
    @DisplayName("Environment specific configuration indicated via system properties should override the value set via environment variables")
    void environmentSpecificConfigurationIndicatedViaSystemPropertiesShouldOverrideTheValueSetViaEnvironmentVariables() {
        // given
        String playwrightEnvConfigFromEnvVars = System.getenv("PLAYWRIGHT_ENV_CONFIG");
        MatcherAssert.assertThat(playwrightEnvConfigFromEnvVars, is(not(emptyOrNullString())));
        System.setProperty("playwright.env.config", PLAYWRIGHT_ENV_CONFIG_SET_VIA_SYSTEM_PROPERTIES);

        // when
        playwrightConfigurationLoader.loadConfiguration();

        // then
        Assertions.assertThat(appender.list).satisfiesOnlyOnce(event -> {
            Assertions.assertThat(event.getLevel()).isEqualTo(Level.INFO);
            Assertions
                    .assertThat(event.getFormattedMessage())
                    .isEqualToIgnoringCase(String.format(
                            "Playwright configuration playwright-%s.config.yml loaded successfully.",
                            PLAYWRIGHT_ENV_CONFIG_SET_VIA_SYSTEM_PROPERTIES
                    ));
        });
    }

    @Test
    @DisplayName("Warning message should be logged when environment specific configuration is not found in resources")
    void warningMessageShouldBeLoggedWhenEnvironmentSpecificConfigurationIsNotFoundInResources() {
        // given
        String nonExistentConfiguration = "does_not_exist";
        System.setProperty("playwright.env.config", nonExistentConfiguration);

        // when
        playwrightConfigurationLoader.loadConfiguration();

        // then
        Assertions.assertThat(appender.list).satisfiesOnlyOnce(event -> {
            Assertions.assertThat(event.getLevel()).isEqualTo(Level.WARN);
            Assertions
                    .assertThat(event.getFormattedMessage())
                    .isEqualToIgnoringCase(String.format(
                            "Playwright configuration playwright-%s.config.yml not found under resources.",
                            nonExistentConfiguration
                    ));
        });
    }
}
