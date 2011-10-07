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

package org.bug4j.gwt.common.client.event;

import com.google.gwt.event.shared.GwtEvent;
import org.bug4j.gwt.common.client.data.UserAuthorities;

/**
 * Fired when the user name changes.
 * The user name actually never really changes, it only changes from null to something.
 */
public class UserChangedEvent extends GwtEvent<UserChangedEventHandler> {
    public static Type<UserChangedEventHandler> TYPE = new Type<UserChangedEventHandler>();
    private final UserAuthorities _userAuthorities;

    public UserChangedEvent(UserAuthorities userAuthorities) {
        _userAuthorities = userAuthorities;
    }

    public Type<UserChangedEventHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(UserChangedEventHandler handler) {
        handler.onUserChanged(this);
    }

    public UserAuthorities getUserAuthorities() {
        return _userAuthorities;
    }
}
