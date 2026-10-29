package com.github.fecrol.assertzilla.unit;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.github.fecrol.assertzilla.ConfigurationLoader;
import com.github.fecrol.assertzilla.PlaywrightConfiguration;
import com.github.fecrol.assertzilla.PlaywrightConfigurationLoader;
import com.microsoft.playwright.options.ColorScheme;
import io.smallrye.config.Config;
import io.smallrye.config.SmallRyeConfigBuilder;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.*;
import org.slf4j.LoggerFactory;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@Tags({
        @Tag("unit")
})
@DisplayName("Playwright Configuration Loader Tests")
public class PlaywrightConfigurationLoaderTests { 

    private Logger logger;
    private ListAppender<ILoggingEvent> appender;
    private ConfigurationLoader playwrightConfigurationLoader;

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
        playwrightConfigurationLoader.loadConfiguration(null);

        // then
        Assertions.assertThat(appender.list).satisfiesOnlyOnce(event -> {
            Assertions.assertThat(event.getLevel()).isEqualTo(Level.INFO);
            Assertions
                    .assertThat(event.getFormattedMessage())
                    .isEqualToIgnoringCase("Playwright configuration playwright.config.yml loaded successfully.");
        });
    }

    @Test
    @DisplayName("Environment specific configuration should load successfully when found in resources")
    void environmentSpecificConfigurationIndicatedViaEnvironmentVariablesShouldLoadSuccessfullyWhenFoundInResources() {
        // given
        String playwrightEnvSpecificConfig = "env-specific";

        // when
        playwrightConfigurationLoader.loadConfiguration(playwrightEnvSpecificConfig);

        // then
        Assertions.assertThat(appender.list).satisfiesOnlyOnce(event -> {
            Assertions.assertThat(event.getLevel()).isEqualTo(Level.INFO);
            Assertions
                    .assertThat(event.getFormattedMessage())
                    .isEqualToIgnoringCase(String.format(
                            "Playwright configuration playwright-%s.config.yml loaded successfully.",
                            playwrightEnvSpecificConfig
                    ));
        });
    }

    @Test
    @DisplayName("Default and environment specific configurations should both load when found in resources")
    void defaultAndEnvironmentSpecificConfigurationsShouldBothLoadWhenFoundInResources() {
        // given
        String playwrightEnvSpecificConfig = "env-specific";

        // when
        playwrightConfigurationLoader.loadConfiguration(playwrightEnvSpecificConfig);

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
                                    playwrightEnvSpecificConfig
                            ));
                }
        );
    }

    @Test
    @DisplayName("Warning message should be logged when environment specific configuration is not found in resources")
    void warningMessageShouldBeLoggedWhenEnvironmentSpecificConfigurationIsNotFoundInResources() {
        // given
        String nonExistentConfiguration = "non-existent";

        // when
        playwrightConfigurationLoader.loadConfiguration(nonExistentConfiguration);

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

    @Test
    @DisplayName("Loaded configuration should map to POJO model successfully")
    void loadedConfigurationShouldMapToPojoModelSuccessfully() {
        // given
        Config configuration = playwrightConfigurationLoader.loadConfiguration(null);

        // then
        assertDoesNotThrow(() -> configuration.getConfigMapping(PlaywrightConfiguration.class));
    }

    @Test
    @DisplayName("Default configuration values should be set successfully")
    void defaultConfigurationValuesShouldBeSetSuccessfully() {
        // given
        Config configuration = playwrightConfigurationLoader.loadConfiguration(null);

        // when
        PlaywrightConfiguration playwrightConfiguration = configuration.getConfigMapping(PlaywrightConfiguration.class);
        PlaywrightConfiguration.Playwright.Browser browserFromConfig = playwrightConfiguration.playwright().browser();

        // then
        SoftAssertions.assertSoftly($ -> {
            $.assertThat(browserFromConfig.type()).isEqualToIgnoringCase("chromium");
            $.assertThat(browserFromConfig.launchOptions().headless()).isTrue();
            $.assertThat(browserFromConfig.launchOptions().ignoreAllDefaultArgs()).isFalse();
            $.assertThat(browserFromConfig.launchOptions().slowMo()).isEqualTo(0);
            $.assertThat(browserFromConfig.launchOptions().timeout()).isEqualTo(30000);
            $.assertThat(browserFromConfig.contextOptions().colorScheme()).isEqualTo(ColorScheme.LIGHT);
            $.assertThat(browserFromConfig.contextOptions().defaultNavigationTimeout()).isEqualTo(30000);
            $.assertThat(browserFromConfig.contextOptions().defaultTimeout()).isEqualTo(30000);
        });
    }

    @Test
    @DisplayName("Default configuration values should be overridable by environment specific configuration values")
    void defaultConfigurationValuesShouldBeOverridable() {
        // given
        Config configuration = playwrightConfigurationLoader.loadConfiguration("override-default-properties");

        // when
        PlaywrightConfiguration playwrightConfiguration = configuration.getConfigMapping(PlaywrightConfiguration.class);
        PlaywrightConfiguration.Playwright.Browser browserFromConfig = playwrightConfiguration.playwright().browser();

        // then
        SoftAssertions.assertSoftly($ -> {
            $.assertThat(browserFromConfig.type()).isEqualToIgnoringCase("firefox");
            $.assertThat(browserFromConfig.launchOptions().headless()).isFalse();
            $.assertThat(browserFromConfig.launchOptions().ignoreAllDefaultArgs()).isTrue();
            $.assertThat(browserFromConfig.launchOptions().slowMo()).isEqualTo(20000);
            $.assertThat(browserFromConfig.launchOptions().timeout()).isEqualTo(10000);
            $.assertThat(browserFromConfig.contextOptions().colorScheme()).isEqualTo(ColorScheme.DARK);
            $.assertThat(browserFromConfig.contextOptions().defaultNavigationTimeout()).isEqualTo(0);
            $.assertThat(browserFromConfig.contextOptions().defaultTimeout()).isEqualTo(60000);
        });
    }
}
