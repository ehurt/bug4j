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

import org.bug4j.common.TextToLines;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

class ReportableEvent {
    private final String _message;
    private final String[] _throwableStrRep;
    private final String _user;

    public ReportableEvent(String message, String[] throwableStrRep, String user) {
        _message = message;
        _throwableStrRep = throwableStrRep;
        _user = user;
    }

    public String getMessage() {
        return _message;
    }

    public String[] getThrowableStrRep() {
        return _throwableStrRep;
    }

    public String getUser() {
        return _user;
    }

    public static ReportableEvent createReportableEvent(String message, Throwable throwable) {
        final String[] throwableStrRep = createStringRepresentation(throwable);
        final String user = System.getProperty("user.name", null);
        return new ReportableEvent(message, throwableStrRep, user);
    }

    private static String[] createStringRepresentation(Throwable throwable) {
        String[] ret;
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final PrintStream printStream = new PrintStream(byteArrayOutputStream);
        try {
            throwable.printStackTrace(printStream);
        } finally {
            printStream.close();
        }
        final String stackText = byteArrayOutputStream.toString();
        ret = TextToLines.toLineArray(stackText);
        return ret;
    }
}
