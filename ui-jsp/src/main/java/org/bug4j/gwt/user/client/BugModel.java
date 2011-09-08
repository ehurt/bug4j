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

import org.bug4j.gwt.common.client.data.AppPkg;
import org.bug4j.gwt.user.client.util.PropertyListener;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BugModel {
    private String _application;
    private List<AppPkg> _appPkgs;
    private final List<PropertyListener> _propertyListeners = new ArrayList<PropertyListener>();

    public BugModel() {
    }

    public void firePropertyChange(String property, @Nullable Object oldValue, @Nullable Object newValue) {
        if (!equals(oldValue, newValue)) {
            for (PropertyListener applicationListener : _propertyListeners) {
                applicationListener.propertyChanged(property, oldValue, newValue);
            }
        }
    }

    private static boolean equals(Object object1, Object object2) {
        if (object1 == object2) {
            return true;
        }
        if (object1 == null || object2 == null) {
            return false;
        }
        return object1.equals(object2);
    }

    public void addPropertyListener(PropertyListener listener) {
        _propertyListeners.add(listener);
    }

    public String getApplication() {
        return _application;
    }

    public void setApplication(final String application, List<AppPkg> appPkgs) {
        final String oldApplication = _application;
        _application = application;
        _appPkgs = appPkgs;
        firePropertyChange(PropertyListener.APPLICATION, oldApplication, application);
    }

    public List<AppPkg> getAppPkgs() {
        return _appPkgs;
    }
}
