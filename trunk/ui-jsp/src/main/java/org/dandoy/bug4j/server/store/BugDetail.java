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

package org.dandoy.bug4j.server.store;

public class BugDetail extends Bug {
    private final String _message;
    private final String _exceptionMessage;
    private final String _stackText;

    public BugDetail(long id, String title, int hitCount, String message, String exceptionMessage, String stackText) {
        super(id, title, hitCount);
        _message = message;
        _exceptionMessage = exceptionMessage;
        _stackText = stackText;
    }

    public String getMessage() {
        return _message;
    }

    public String getExceptionMessage() {
        return _exceptionMessage;
    }

    public String getStackText() {
        return _stackText;
    }
}
