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

package org.bug4j.gwt.user.client.data;

import org.jetbrains.annotations.Nullable;

public class BugHitAndStack extends BugHit {
    private String _message;
    private String _stack;

    @SuppressWarnings({"UnusedDeclaration"})
    public BugHitAndStack() {
    }

    public BugHitAndStack(long hitId, String appVer, long dateReported, String user, String message, String stack) {
        this(hitId, appVer, dateReported, user, message, stack, null, false, null);
    }

    public BugHitAndStack(long hitId, String appVer, long dateReported, String user, String message, String stack, @Nullable Long dateBuilt, boolean devBuild, @Nullable Integer buildNumber) {
        super(hitId, appVer, dateReported, user, dateBuilt, devBuild, buildNumber);
        _message = message;
        _stack = stack;
    }

    public String getMessage() {
        return _message;
    }

    public String getStack() {
        return _stack;
    }
}
