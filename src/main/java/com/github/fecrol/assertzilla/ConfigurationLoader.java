package com.github.fecrol.assertzilla;

import io.smallrye.config.Config;

public interface ConfigurationLoader {

    Config loadConfiguration();
}
