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

public class ReportableEvent {
    private final String _message;
    private final String exceptionMessage;
    private final String[] throwableStrRep;

    public ReportableEvent(String message, String exceptionMessage, String[] throwableStrRep) {
        _message = message;
        this.exceptionMessage = exceptionMessage;
        this.throwableStrRep = throwableStrRep;
    }

    public String getMessage() {
        return _message;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public String[] getThrowableStrRep() {
        return throwableStrRep;
    }
}
