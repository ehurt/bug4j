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

package org.bug4j.server;

import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.Properties;

public final class Configuration {
    private static Configuration INSTANCE;
    private File _configFile;
    private Properties _properties;

    private Configuration() {
        final String catalinaHome = System.getProperty("catalina.home", null);
        _configFile = new File(catalinaHome, "/conf/bug4j.properties");

        _properties = readProperties();
    }

    public static synchronized Configuration getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Configuration();
        }
        return INSTANCE;
    }

    private Properties readProperties() {
        final Properties ret = new Properties();
        if (_configFile.exists()) {
            if (!_configFile.canWrite()) {
                throw new IllegalStateException("File is not writable:" + _configFile.getAbsolutePath());
            }
            try {
                final FileInputStream fileInputStream = new FileInputStream(_configFile);
                try {
                    final BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                    try {
                        ret.load(bufferedInputStream);
                    } finally {
                        bufferedInputStream.close();
                    }
                } finally {
                    fileInputStream.close();
                }

            } catch (IOException e) {
                throw new IllegalStateException("Failed to read " + _configFile.getAbsolutePath(), e);
            }
        }
        return ret;
    }

    public void writeProperties() {
        try {
            final FileOutputStream fileOutputStream = new FileOutputStream(_configFile);
            try {
                final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
                try {
                    _properties.store(bufferedOutputStream, null);
                } finally {
                    bufferedOutputStream.close();
                }
            } finally {
                fileOutputStream.close();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write to " + _configFile.getAbsolutePath());
        }
    }

    public String getProperty(String key, @Nullable String defaultValue) {
        return _properties.getProperty(key, defaultValue);
    }

    public void setProperty(String key, String value) {
        _properties.setProperty(key, value);
    }

    public int getIntProperty(String key, int defaultValue) {
        int ret = defaultValue;
        final String property = getProperty(key, null);
        if (property != null) {
            try {
                ret = Integer.parseInt(property);
            } catch (NumberFormatException ignored) {
            }
        }
        return ret;
    }

    public void setIntProperty(String key, int value) {
        final String stringValue = Integer.toString(value);
        setProperty(key, stringValue);
    }
}
