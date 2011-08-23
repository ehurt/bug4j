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

package org.bug4j.server.store;

import org.bug4j.gwt.admin.client.data.User;

/**
 */
public class UserEx extends User {
    private String _password;

    public UserEx(String userName, String password, String email, boolean admin, boolean builtIn, boolean enabled, Long lastSignedIn) {
        super(userName, email, admin, builtIn, enabled, lastSignedIn);
        _password = password;
    }

    public String getPassword() {
        return _password;
    }
}
