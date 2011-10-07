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

package org.bug4j.gwt.common.client;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import org.bug4j.gwt.common.client.data.UserAuthorities;
import org.bug4j.gwt.common.client.event.UserChangedEvent;

/**
 * The model common to the user and admin modules.
 */
public class CommonModel {
    protected EventBus _eventBus = new SimpleEventBus();
    private final UserAuthorities _userAuthorities;

    protected CommonModel(UserAuthorities userAuthorities) {
        _userAuthorities = userAuthorities;
    }

    public EventBus getEventBus() {
        return _eventBus;
    }

    private void fireUserChangedEvent() {
        _eventBus.fireEvent(new UserChangedEvent(_userAuthorities));
    }

    public String getUserName() {
        String ret = null;
        if (_userAuthorities != null) {
            ret = _userAuthorities.getUserName();
        }
        return ret;
    }

    public boolean isAdmin() {
        boolean ret = false;
        if (_userAuthorities != null) {
            ret = _userAuthorities.isAdmin();
        }
        return ret;
    }

    public void fireInitializationEvents() {
        fireUserChangedEvent();
    }
}
