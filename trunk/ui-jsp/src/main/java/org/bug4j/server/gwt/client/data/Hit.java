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

package org.bug4j.server.gwt.client.data;

import java.io.Serializable;

public class Hit implements Serializable {
    private long _id;
    private long _bugId;
    private String _appVer;
    private long _dateReported;

    @SuppressWarnings({"UnusedDeclaration"})
    public Hit() {
    }

    public Hit(long id, long bugId, String appVer, long dateReported) {
        _id = id;
        _bugId = bugId;
        _appVer = appVer;
        _dateReported = dateReported;
    }

    public long getId() {
        return _id;
    }

    public long getBugId() {
        return _bugId;
    }

    public String getAppVer() {
        return _appVer;
    }

    public long getDateReported() {
        return _dateReported;
    }
}
