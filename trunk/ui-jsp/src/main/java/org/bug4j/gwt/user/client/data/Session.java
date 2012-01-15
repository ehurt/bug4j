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

import java.io.Serializable;

public class Session implements Serializable {
    private long _sessionId;
    private String _application;
    private String _version;
    private long _firstHit;
    private String _hostName;
    private Long _dateBuilt;
    private boolean _devBuild;
    private Integer _buildNumber;

    public Session() {
    }

    public Session(long sessionId, String application, String version, long firstHit, String hostName, Long dateBuilt, boolean devBuild, Integer buildNumber) {
        _sessionId = sessionId;
        _application = application;
        _version = version;
        _firstHit = firstHit;
        _hostName = hostName;
        _dateBuilt = dateBuilt;
        _devBuild = devBuild;
        _buildNumber = buildNumber;
    }

    public long getSessionId() {
        return _sessionId;
    }

    public String getApplication() {
        return _application;
    }

    public String getVersion() {
        return _version;
    }

    public long getFirstHit() {
        return _firstHit;
    }

    public String getHostName() {
        return _hostName;
    }

    public Long getDateBuilt() {
        return _dateBuilt;
    }

    public boolean isDevBuild() {
        return _devBuild;
    }

    public Integer getBuildNumber() {
        return _buildNumber;
    }
}
