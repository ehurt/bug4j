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

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.InvocationException;

/**
 * An AsyncCallback class that handles session timeouts
 */
public abstract class AdvancedAsyncCallback<T> implements AsyncCallback<T> {
    @Override
    public void onFailure(Throwable caught) {
        if (caught instanceof InvocationException) {
            Window.open("j_spring_security_logout", "_self", "");
        } else {
            String message = caught.getMessage();
            if (message == null) {
                message = "Server error";
            }
            displayErrorMessage(caught, message);
        }
    }

    /**
     * Override this method if you want to display the error message differently.
     */
    protected void displayErrorMessage(Throwable caught, String message) {
        Window.alert(message);
    }
}
