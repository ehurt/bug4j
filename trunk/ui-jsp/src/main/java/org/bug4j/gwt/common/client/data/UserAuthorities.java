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

package org.bug4j.gwt.common.client.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 */
public class UserAuthorities implements Serializable {
    private String _userName;
    private Collection<String> _authorities = new ArrayList<String>();

    public UserAuthorities() {
    }

    public UserAuthorities(String userName, Collection<String> authorities) {
        _userName = userName;
        _authorities = authorities;
    }

    public String getUserName() {
        return _userName;
    }

    public Collection<String> getAuthorities() {
        return Collections.unmodifiableCollection(_authorities);
    }

    public boolean isAdmin() {
        return _authorities.contains("admin");
    }
}
