package com.github.fecrol.assertzilla;

import io.smallrye.config.Config;
import io.smallrye.config.SmallRyeConfigBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        return null;
    }

    private List<String> getConfigsToLoad(String playwrightEnvConfig) {
        return  null;
    }

    @Override
    public Config loadConfiguration() {
        return null;
    }
}
