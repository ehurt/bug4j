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

package org.bug4j.gwt.admin.client.data;

import java.io.Serializable;

public class User implements Serializable {
    public static final boolean SUPPORTS_LDAP = false;
    private String _userName;
    private String _email;
    private boolean _admin;
    private boolean _builtIn = true;
    private boolean _enabled;
    private Long _lastSignedIn;

    public User() {
    }

    public User(String userName, String email, boolean admin, boolean builtIn, boolean enabled, Long lastSignedIn) {
        _userName = userName;
        _email = email;
        _admin = admin;
        _builtIn = builtIn;
        _enabled = enabled;
        _lastSignedIn = lastSignedIn;
    }

    public String getUserName() {
        return _userName;
    }

    public void setUserName(String userName) {
        _userName = userName;
    }

    public String getEmail() {
        return _email;
    }

    public void setEmail(String email) {
        _email = email;
    }

    public boolean isAdmin() {
        return _admin;
    }

    public void setAdmin(boolean admin) {
        _admin = admin;
    }

    public boolean isBuiltIn() {
        return _builtIn;
    }

    public void setBuiltIn(boolean builtIn) {
        _builtIn = builtIn;
    }

    public boolean isEnabled() {
        return _enabled;
    }

    public void setEnabled(boolean enabled) {
        _enabled = enabled;
    }

    public Long getLastSignedIn() {
        return _lastSignedIn;
    }

    public void setLastSignedIn(Long lastSignedIn) {
        _lastSignedIn = lastSignedIn;
    }
}
