/*
 * Copyright 2011 Cedric Dandoy
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.bug4j.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Builder class to start the bug4j client.
 * Example:<pre>
 *     new Bug4jStarter()
 *          .serverUrl("http://host-name:8063/bug4j")
 *          .start();
 * </pre>
 */
public final class Bug4jStarter {
    private final Settings _settings = new Settings();

    public Bug4jStarter() {
        readDefaultSettings();
    }

    private void readDefaultSettings() {
        final Properties properties = readProperties();

        final String server = properties.getProperty("server");
        if (server != null) {
            setServerUrl(server);
            System.err.println("bug4j warning: property 'server' is deprecated. Please use 'server.url' instead");
        }

        final String serverUrl = properties.getProperty("server.url");
        if (serverUrl != null) {
            setServerUrl(serverUrl);
        }

        final String appName = properties.getProperty("application.name");
        if (appName != null) {
            setApplicationName(appName);
        }

        final String appVersion = properties.getProperty("application.version");
        if (appVersion != null) {
            setApplicationVersion(appVersion);
        }

        final String anonymousReports = properties.getProperty("reports.anonymous");
        if (anonymousReports != null) {
            setAnonymousReports(Boolean.valueOf(anonymousReports));
        }
    }

    private static Properties readProperties() {
        final Properties ret = new Properties();
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final InputStream inputStream = classLoader.getResourceAsStream("bug4j.properties");
        if (inputStream != null) {
            try {
                try {
                    ret.load(inputStream);
                } finally {
                    inputStream.close();
                }
            } catch (IOException e) {
                final String message = e.getMessage();
                System.err.println(message);
            }
        }
        return ret;
    }

    /**
     * Sets the server URL.
     * The server url should be something like http://host-name:8063/bug4j
     */
    public Bug4jStarter setServerUrl(String serverUrl) {
        _settings.setServerUrl(serverUrl);
        return this;
    }

    /**
     * Sets the application name.
     */
    public Bug4jStarter setApplicationName(String applicationName) {
        _settings.setApplicationName(applicationName);
        return this;
    }

    /**
     * Sets the application version.
     * Example: "2011-06-15 08:32:17"
     */
    public Bug4jStarter setApplicationVersion(String applicationVersion) {
        _settings.setApplicationVersion(applicationVersion);
        return this;
    }

    /**
     * Determines if the user name can be sent with the exception.
     * The default is false
     */
    public Bug4jStarter setAnonymousReports(boolean anonymousReports) {
        _settings.setAnonymousReports(anonymousReports);
        return this;
    }

    /**
     * Start the bug4j thread
     */
    public void start() {
        Bug4jAgent.start(_settings);
    }
}
