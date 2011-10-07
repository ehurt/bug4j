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

package org.bug4j.gwt.admin.client.event;

import com.google.gwt.event.shared.GwtEvent;

import java.util.Collections;
import java.util.List;

/**
 * Fired when the list of applications changed
 */
public class ApplicationsChangedEvent extends GwtEvent<ApplicationsChangedEventHandler> {
    public static Type<ApplicationsChangedEventHandler> TYPE = new Type<ApplicationsChangedEventHandler>();
    private final List<String> _applications;

    public ApplicationsChangedEvent(List<String> applications) {
        _applications = Collections.unmodifiableList(applications);
    }

    public Type<ApplicationsChangedEventHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(ApplicationsChangedEventHandler handler) {
        handler.onApplicationsChanged(this);
    }

    public List<String> getApplications() {
        return _applications;
    }
}
