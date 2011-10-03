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

package org.bug4j.gwt.user.client;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.bug4j.gwt.common.client.AdvancedAsyncCallback;
import org.bug4j.gwt.common.client.CommonService;
import org.bug4j.gwt.common.client.data.AppPkg;
import org.bug4j.gwt.user.client.event.ApplicationChangedEvent;

import java.util.List;

public class BugModel {
    private EventBus _eventBus = new SimpleEventBus();
    private String _application;
    private List<AppPkg> _appPkgs;

    public BugModel() {
    }

    public EventBus getEventBus() {
        return _eventBus;
    }

    public String getApplication() {
        return _application;
    }

    public void setApplication(final String application) {
        _application = application;
        _appPkgs = null;
        _eventBus.fireEvent(new ApplicationChangedEvent(_application));
    }

    public void getPackages(final AsyncCallback<List<AppPkg>> callback) {
        if (_appPkgs != null) {
            callback.onSuccess(_appPkgs);
        } else {
            CommonService.App.getInstance().getPackages(_application, new AdvancedAsyncCallback<List<AppPkg>>() {
                @Override
                public void onSuccess(List<AppPkg> result) {
                    _appPkgs = result;
                    callback.onSuccess(_appPkgs);
                }
            });
        }
    }
}
