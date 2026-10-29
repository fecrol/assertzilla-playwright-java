package com.github.fecrol.assertzilla;

import com.microsoft.playwright.options.*;
import io.smallrye.config.WithDefault;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface PlaywrightConfiguration {

    Playwright playwright();


    interface Playwright {

        Optional<CreateOptions> createOptions();

        interface CreateOptions {
            Map<String, String> env();
        }

        Browser browser();

        interface Browser {

            @WithDefault("chromium")
            String type();

            LaunchOptions launchOptions();

            interface LaunchOptions {

                Optional<List<String>> args();

                Optional<Path> artifactsDir();

                Optional<String> channel();

                Optional<Boolean> chromiumSandbox();

                Optional<Path> downloadsPath();

                Map<String, String> env();

                Optional<Path> executablePath();

                Map<String, String> firefoxUserPrefs();

                Optional<Boolean> handleSIGHUP();

                Optional<Boolean> handleSIGINT();

                Optional<Boolean> handleSIGTERM();

                @WithDefault("true")
                boolean headless();

                @WithDefault("false")
                boolean ignoreAllDefaultArgs();

                Optional<List<String>> ignoreDefaultArgs();

                Optional<Proxy> proxy();

                @WithDefault("0")
                double slowMo();

                @WithDefault("30000")
                double timeout();

                Optional<Path> tracesDir();
            }

            ContextOptions contextOptions();

            interface ContextOptions {

                Optional<Boolean> acceptDownloads();

                Optional<String> baseUrl();

                Optional<Boolean> bypassCsp();

                Optional<List<ClientCertificate>> clientCertificates();

                interface ClientCertificate {

                    String origin();

                    Optional<Path> certPath();

                    Optional<Path> keyPath();

                    Optional<Path> pfxPath();

                    byte[] cert();

                    byte[] key();

                    byte[] pfx();

                    Optional<String> passphrase();
                }

                @WithDefault("light")
                ColorScheme colorScheme();

                Optional<Contrast> contrast();

                Optional<Double> deviceScaleFactor();

                Map<String, String> extraHttpHeaders();

                Optional<ForcedColors> forcedColors();

                Optional<Geolocation> geolocation();

                interface Geolocation {

                    double latitude();

                    double longitude();

                    Optional<Double> accuracy();
                }

                Optional<Boolean> hasTouch();

                Optional<HttpCredentials> httpCredentials();

                interface HttpCredentials {

                    String username();

                    String password();

                    Optional<String> origin();

                    Optional<HttpCredentialsSend> send();
                }

                Optional<Boolean> ignoreHttpsErrors();

                Optional<Boolean> isMobile();

                Optional<Boolean> javaScriptEnabled();

                Optional<String> locale();

                Optional<Boolean> offline();

                Optional<List<String>> permissions();

                Optional<Proxy> proxy();

                Optional<HarContentPolicy> recordHarContent();

                Optional<HarMode> recordHarMode();

                Optional<Boolean> recordHarOmitContent();

                Optional<Path> recordHarPath();

                Optional<String> recordHarUrlFilter();

                Optional<Path> recordVideoDir();

                Optional<ScreenRelatedSize> recordVideoSize();

                Optional<ReducedMotion> reducedMotion();

                Optional<ScreenRelatedSize> screenSize();

                Optional<ServiceWorkerPolicy> serviceWorkers();

                Optional<String> storageState();

                Optional<Path> storageStatePath();

                Optional<Boolean> strictSelectors();

                Optional<String> timezoneId();

                Optional<String> userAgent();

                Optional<ScreenRelatedSize> viewportSize();

                interface ScreenRelatedSize {

                    int width();

                    int height();
                }

                @WithDefault("30000")
                double defaultNavigationTimeout();

                @WithDefault("30000")
                double defaultTimeout();
            }

            interface Proxy {

                String server();

                Optional<String> bypass();

                Optional<String> username();

                Optional<String> password();
            }
        }
    }
}
