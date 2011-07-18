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

package org.bug4j.gwt.user.client.data;

import java.io.Serializable;

public class BugHit implements Serializable {
    private long _hitId;
    private String _appVer;
    private long _dateReported;
    private String _user;

    public BugHit() {
    }

    public BugHit(long hitId, String appVer, long dateReported, String user) {
        _hitId = hitId;
        _appVer = appVer;
        _dateReported = dateReported;
        _user = user;
    }

    public long getHitId() {
        return _hitId;
    }

    public String getAppVer() {
        return _appVer;
    }

    public long getDateReported() {
        return _dateReported;
    }

    public String getUser() {
        return _user;
    }
}
