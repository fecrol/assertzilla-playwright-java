package com.github.fecrol.assertzilla;

import com.google.common.base.Strings;
import io.smallrye.config.Config;
import io.smallrye.config.SmallRyeConfigBuilder;
import io.smallrye.config.source.yaml.YamlConfigSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class PlaywrightConfigurationLoader implements ConfigurationLoader {

    private final Logger LOGGER;
    private final String DEFAULT_CONFIGURATION_FILE_NAME;
    private final Function<String, String> ENV_SPECIFIC_CONFIGURATION_FILE_NAME;
    private SmallRyeConfigBuilder configurationBuilder;

    public PlaywrightConfigurationLoader(SmallRyeConfigBuilder configurationBuilder) {
        this.LOGGER = LoggerFactory.getLogger(PlaywrightConfigurationLoader.class);
        this.DEFAULT_CONFIGURATION_FILE_NAME = "playwright.config.yml";
        this.ENV_SPECIFIC_CONFIGURATION_FILE_NAME = (String testConfig) -> String.format("playwright-%s.config.yml", testConfig);
        this.configurationBuilder = configurationBuilder;
    }

    private String getPlaywrightEnvConfigArg() {
        String playwrightEnvConfig;
        playwrightEnvConfig = System.getenv("PLAYWRIGHT_ENV_CONFIG");
        String playwrightEnvConfigFromSystemProperties = System.getProperty("playwright.env.config");

        if (!Strings.isNullOrEmpty(playwrightEnvConfigFromSystemProperties)) {
            playwrightEnvConfig = playwrightEnvConfigFromSystemProperties;
        }

        return playwrightEnvConfig;
    }

    private List<String> getConfigsToLoad(String playwrightEnvConfig) {
        List<String> configsToLoad = new ArrayList<>();
        configsToLoad.add(DEFAULT_CONFIGURATION_FILE_NAME);

        if (!Strings.isNullOrEmpty(playwrightEnvConfig)) {
            configsToLoad.add(ENV_SPECIFIC_CONFIGURATION_FILE_NAME.apply(playwrightEnvConfig));
        }

        return configsToLoad;
    }

    @Override
    public Config loadConfiguration() {
        String playwrightEnvConfig = getPlaywrightEnvConfigArg();
        List<String> configsToLoad = getConfigsToLoad(playwrightEnvConfig);
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        for (String configToLoad : configsToLoad) {
            URL resourceAsUrl = classLoader.getResource(configToLoad);

            if (resourceAsUrl != null) {
                try {
                    YamlConfigSource yamlConfigurationSource = new YamlConfigSource(
                            resourceAsUrl,
                            YamlConfigSource.ORDINAL + 1
                    );
                    configurationBuilder.withSources(yamlConfigurationSource);
                    LOGGER.info("Playwright configuration {} loaded successfully.", configToLoad);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            } else {
                LOGGER.warn("Playwright configuration {} not found under resources.", configToLoad);
            }
        }

        return configurationBuilder.withMapping(PlaywrightConfiguration.class).build();
    }
}
