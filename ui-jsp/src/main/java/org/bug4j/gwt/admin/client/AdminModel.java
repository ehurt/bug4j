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

package org.bug4j.gwt.admin.client;

import org.bug4j.gwt.admin.client.event.ApplicationsChangedEvent;
import org.bug4j.gwt.common.client.AdvancedAsyncCallback;
import org.bug4j.gwt.common.client.CommonModel;
import org.bug4j.gwt.common.client.CommonService;
import org.bug4j.gwt.common.client.data.UserAuthorities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Holds the list of applications in addition to the common model.
 */
public class AdminModel extends CommonModel {
    private final List<String> _applications;

    public AdminModel(UserAuthorities userAuthorities, List<String> applications) {
        super(userAuthorities);
        _applications = new ArrayList<String>(applications);
    }

    public void fireInitializationEvents() {
        super.fireInitializationEvents();
        fireApplicationsChangedEvent();
    }

    public void refreshApplications() {
        CommonService.App.getInstance().getApplications(new AdvancedAsyncCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> applications) {
                setApplications(applications);
            }
        });
    }

    private void fireApplicationsChangedEvent() {
        final List<String> applications = Collections.unmodifiableList(_applications);
        final ApplicationsChangedEvent event = new ApplicationsChangedEvent(applications);
        _eventBus.fireEvent(event);
    }

    private void setApplications(List<String> applications) {
        _applications.clear();
        _applications.addAll(applications);
        fireApplicationsChangedEvent();
    }

    public void delete(String application) {
        _applications.remove(application);
        fireApplicationsChangedEvent();
    }

    public void add(String application) {
        _applications.add(application);
        Collections.sort(_applications, String.CASE_INSENSITIVE_ORDER);
        fireApplicationsChangedEvent();
    }
}
