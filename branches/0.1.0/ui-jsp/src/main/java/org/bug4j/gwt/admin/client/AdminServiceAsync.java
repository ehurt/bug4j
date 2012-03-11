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

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.bug4j.gwt.admin.client.data.User;
import org.bug4j.gwt.common.client.data.AppPkg;

import java.util.Collection;
import java.util.List;

/**
 */
public interface AdminServiceAsync {

    void getUsers(AsyncCallback<List<User>> async);

    void updateUser(User user, AsyncCallback<Void> async);

    void createUser(User user, String password, AsyncCallback<Void> async);

    void getRandomPassword(AsyncCallback<String> async);

    void setPackages(String app, List<AppPkg> packages, AsyncCallback<Void> async);

    void createApplication(String applicationName, AsyncCallback<Void> async);

    void deleteApplication(String applicationName, AsyncCallback<Void> async);

    void deleteUsers(Collection<String> userNames, AsyncCallback<Void> async);

    void resetPassword(User user, AsyncCallback<String> async);
}
