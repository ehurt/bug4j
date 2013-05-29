/*
 * Copyright 2013 Cedric Dandoy
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

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

/**
 * A log4j appender. When using bug4j with log4j, you can configure the settings in log4j.properties or log4j.xml.
 * For example:<pre>
 *     log4j.rootLogger=INFO, A1, BUG4J
 *     ...
 *     log4j.appender.BUG4J=org.bug4j.client.Bug4jAppender
 *     log4j.appender.BUG4J.serverUrl=http://bug4j.example.com:8063/bug4j
 *     log4j.appender.BUG4J.applicationName=My Application
 *     log4j.appender.BUG4J.applicationVersion=1.3
 * </pre>
 */
public class Bug4jAppender extends AppenderSkeleton {
    private final Bug4jStarter _bug4jStarter = new Bug4jStarter();
    private Level _minLevel = Level.WARN;

    public Bug4jAppender() {
    }

    @SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
    @Override
    protected void append(LoggingEvent event) {
        final Level level = event.getLevel();
        if (level.isGreaterOrEqual(_minLevel)) {
            final String message = event.getRenderedMessage();
            final ThrowableInformation throwableInformation = event.getThrowableInformation();
            final Throwable throwable = throwableInformation == null ? null : throwableInformation.getThrowable();
            final String[] throwableStrRep;
            if (throwable == null) {
                final LocationInfo locationInformation = event.getLocationInformation();
                throwableStrRep = new String[]{
                        message,
                        "\tat " + locationInformation.fullInfo
                };
            } else {
                throwableStrRep = TextUtils.createStringRepresentation(throwable);
            }
            Bug4jAgent.report(message, throwableStrRep);
        }
    }

    public void close() {
        // Cannot shutdown the client because it may be locked by the log4j shutdown
        // Client.shutdown();
    }

    public boolean requiresLayout() {
        return false;
    }

    @Override
    public void activateOptions() {
        _bug4jStarter.start();
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void setServerUrl(String serverUrl) {
        _bug4jStarter.setServerUrl(serverUrl);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void setApplicationName(String applicationName) {
        _bug4jStarter.setApplicationName(applicationName);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void setApplicationVersion(String applicationVersion) {
        _bug4jStarter.setApplicationVersion(applicationVersion);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void setBuildDate(String buildDate) {
        _bug4jStarter.setBuildDate(buildDate);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void setDevBuild(String devBuild) {
        _bug4jStarter.setDevBuild(Boolean.valueOf(devBuild));
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void setBuildNumber(int buildNumber) {
        _bug4jStarter.setBuildNumber(buildNumber);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void setReportLevel(String level) {
        _minLevel = Level.toLevel(level);
    }

    /**
     * Set the proxy.
     *
     * @param proxy the proxy host name and port separated by a ':'. <br/>For example "proxy.example.com:8080".
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public void setProxy(String proxy) {
        _bug4jStarter.setProxy(proxy);
    }

    protected Bug4jStarter test_getBug4jStarter() {
        return _bug4jStarter;
    }
}
