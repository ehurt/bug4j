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

package org.dandoy.bug4j.client;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Settings {
    private static final Logger LOGGER = Logger.getLogger(Settings.class);
    private static final int DEFAULT_PORT = 8004;
    private static final String DEFAULT_HOST = "localhost";

    private final String _serverUri;
    private final String _appName;
    private final String _appVersion;

    public Settings(String appName, String appVersion, String serverUri) {
        _appName = appName;
        _appVersion = appVersion;
        _serverUri = serverUri;
    }

    public static Settings readSettings() {
        final Properties properties = readProperties();
        final String serverUri = properties.getProperty("server", "http://localhost:8080/bug4j/");
        final String appName = properties.getProperty("application.name");
        final String appVersion = properties.getProperty("application.version");
        final Settings ret = new Settings(appName, appVersion, serverUri);
        return ret;
    }

    private static int getIntProperty(Properties properties, String key, int defaultValue) {
        int ret = defaultValue;
        final String property = properties.getProperty(key, Integer.toString(defaultValue));
        try {
            ret = Integer.parseInt(property);
        } catch (NumberFormatException e) {
            LOGGER.error("Failed to parse the value of \"" + key + "\":" + property);
        }
        return ret;
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
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
        return ret;
    }

    public String getServerUri() {
        return _serverUri;
    }

    public String getApplicationName() {
        return _appName;
    }

    public String getApplicationVersion() {
        return _appVersion;
    }
}
