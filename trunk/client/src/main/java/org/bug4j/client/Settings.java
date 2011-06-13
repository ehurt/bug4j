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

public class Settings {
    private String _server;
    private String _applicationName;
    private String _applicationVersion;

    public Settings() {
        _applicationName = "no-name";
        _applicationVersion = "1.0";
        _server = "http://localhost:8063/bug4j/";
    }

    public static Settings getDefaultInstance() {
        final Settings ret = new Settings();
        ret.readSettings();
        return ret;
    }

    private void readSettings() {
        final Properties properties = readProperties();

        final String server = properties.getProperty("server");
        if (server != null) {
            _server = server;
        }

        final String appName = properties.getProperty("application.name");
        if (appName != null) {
            _applicationName = appName;
        }

        final String appVersion = properties.getProperty("application.version");
        if (appVersion != null) {
            _applicationVersion = appVersion;
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

    public String getServer() {
        return _server;
    }

    public void setServer(String server) {
        _server = server;
    }

    public String getApplicationName() {
        return _applicationName;
    }

    public void setApplicationName(String applicationName) {
        _applicationName = applicationName;
    }

    public String getApplicationVersion() {
        return _applicationVersion;
    }

    public void setApplicationVersion(String applicationVersion) {
        _applicationVersion = applicationVersion;
    }
}
