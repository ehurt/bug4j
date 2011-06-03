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

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

public class Bug4jAppender extends AppenderSkeleton {
    private Settings _settings = Settings.getDefaultInstance();

    public Bug4jAppender() {
    }

    @SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
    @Override
    protected void append(LoggingEvent event) {
        final Level level = event.getLevel();
        if (level == Level.ERROR) {
            final String message = event.getRenderedMessage();
            final ThrowableInformation throwableInformation = event.getThrowableInformation();
            if (throwableInformation != null) {
                final Throwable throwable = throwableInformation.getThrowable();
                final String exceptionMessage = throwable.getMessage();
                final String[] throwableStrRep = throwableInformation.getThrowableStrRep();
                final ReportableEvent reportableEvent = new ReportableEvent(message, exceptionMessage, throwableStrRep);
                Client.enqueue(reportableEvent);
            }
        }
    }

    @Override
    public void close() {
        // Cannot shutdown the client because it may be locked by the log4j shutdown
        // Client.shutdown();
    }

    @Override
    public boolean requiresLayout() {
        return false;
    }

    @Override
    public void activateOptions() {
        Client.start(_settings);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void setServer(String server) {
        _settings.setServer(server);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void setApplicationName(String applicationName) {
        _settings.setApplicationName(applicationName);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void setApplicationVersion(String applicationVersion) {
        _settings.setApplicationVersion(applicationVersion);
    }
}
