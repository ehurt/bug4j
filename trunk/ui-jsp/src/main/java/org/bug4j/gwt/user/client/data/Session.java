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

    public Session() {
    }

    public Session(long sessionId, String application, String version, long firstHit, String hostName) {
        _sessionId = sessionId;
        _application = application;
        _version = version;
        _firstHit = firstHit;
        _hostName = hostName;
    }

    public long getSessionId() {
        return _sessionId;
    }

    public void setSessionId(long sessionId) {
        _sessionId = sessionId;
    }

    public String getApplication() {
        return _application;
    }

    public void setApplication(String application) {
        _application = application;
    }

    public String getVersion() {
        return _version;
    }

    public void setVersion(String version) {
        _version = version;
    }

    public long getFirstHit() {
        return _firstHit;
    }

    public void setFirstHit(long firstHit) {
        _firstHit = firstHit;
    }

    public String getHostName() {
        return _hostName;
    }

    public void setHostName(String hostName) {
        _hostName = hostName;
    }
}
