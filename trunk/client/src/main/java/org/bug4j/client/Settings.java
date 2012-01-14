/*
 * Copyright 2012 Cedric Dandoy
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

class Settings {
    private String _serverUrl;
    private String _applicationName;
    private String _applicationVersion;
    private long _buildDate = System.currentTimeMillis();
    private boolean _devBuild;
    private Integer _buildNumber;
    private String _proxyHost;
    private int _proxyPort = 80;

    Settings() {
        _applicationName = "no-name";
        _applicationVersion = "1.0";
        _serverUrl = "http://report.bug4j.org:8063/bug4j/";
    }

    public String getServerUrl() {
        return _serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        _serverUrl = serverUrl;
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

    /**
     * @return the build date in millis
     */
    public long getBuildDate() {
        return _buildDate;
    }

    /**
     * Sets the build date.
     *
     * @param buildDate date in millis
     */
    public void setBuildDate(long buildDate) {
        _buildDate = buildDate;
    }

    /**
     * Sets the build date.
     * Accepted date formats:
     * <li>
     * <ul>yyyyMMddHHmmss20120109100603</ul>
     * <ul>yyyyMMddHHmmssSSS</ul>
     * <ul>y/M/d H:m:s</ul>
     * <ul>y/M/d H:m:s:S</ul>
     * </li>
     * For example: '20120109100603', '20120109100603177', '2012/01/09 10:06:03', '2012/01/09 10:06:03:177'
     */
    public void setBuildDate(String buildDate) {
        final SimpleDateFormat dateFormat;
        switch (buildDate.length()) {
            case 14:
                dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                break;
            case 17:
                dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
                break;
            case 19:
                dateFormat = new SimpleDateFormat("y/M/d H:m:s");
                break;
            case 23:
                dateFormat = new SimpleDateFormat("y/M/d H:m:s:S");
                break;
            default:
                throw new IllegalArgumentException("Invalid date format: \"" + buildDate + "\"");
        }
        try {
            final Date date = dateFormat.parse(buildDate);
            final long time = date.getTime();
            setBuildDate(time);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid date format: \"" + buildDate + "\"", e);
        }
    }

    /**
     * @return true if this is a developer build, false if this is an official build
     */
    public boolean isDevBuild() {
        return _devBuild;
    }

    /**
     * @see #isDevBuild()
     */
    public void setDevBuild(boolean devBuild) {
        _devBuild = devBuild;
    }

    public Integer getBuildNumber() {
        return _buildNumber;
    }

    public void setBuildNumber(int buildNumber) {
        _buildNumber = buildNumber;
    }

    public String getProxyHost() {
        return _proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        _proxyHost = proxyHost;
    }

    public int getProxyPort() {
        return _proxyPort;
    }

    public void setProxyPort(int proxyPort) {
        _proxyPort = proxyPort;
    }
}
