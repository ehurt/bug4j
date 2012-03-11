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

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import org.bug4j.gwt.admin.client.data.User;
import org.bug4j.gwt.common.client.data.AppPkg;
import org.bug4j.gwt.common.client.data.UserException;

import java.util.Collection;
import java.util.List;

/**
 */
@RemoteServiceRelativePath("AdminService")
public interface AdminService extends RemoteService {

    List<User> getUsers();

    void createUser(User user, String password);

    void updateUser(User user);

    void deleteUsers(Collection<String> userNames) throws UserException;

    String getRandomPassword();

    String resetPassword(User user) throws Exception;

    public void createApplication(String applicationName) throws Exception;

    public void deleteApplication(String applicationName) throws Exception;

    void setPackages(String app, List<AppPkg> packages) throws Exception;

    /**
     * Utility/Convenience class.
     * Use AdminService.App.getInstance() to access static instance of AdminServiceAsync
     */
    public static class App {
        private static final AdminServiceAsync ourInstance = (AdminServiceAsync) GWT.create(AdminService.class);

        public static AdminServiceAsync getInstance() {
            return ourInstance;
        }
    }
}
