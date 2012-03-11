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

package org.bug4j.gwt.admin.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.apache.log4j.Logger;
import org.bug4j.gwt.admin.client.AdminService;
import org.bug4j.gwt.admin.client.data.User;
import org.bug4j.gwt.common.client.data.AppPkg;
import org.bug4j.gwt.common.client.data.UserException;
import org.bug4j.server.store.Store;
import org.bug4j.server.store.StoreFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 */
public class AdminServiceImpl extends RemoteServiceServlet implements AdminService {
    private static final Logger LOGGER = Logger.getLogger(AdminServiceImpl.class);

    private String getUserName() {
        final HttpServletRequest threadLocalRequest = getThreadLocalRequest();
        final String remoteUser = threadLocalRequest.getRemoteUser();
        return remoteUser;
    }

    @Override
    public List<User> getUsers() {
        final Store store = StoreFactory.getStore();
        try {
            return store.getUsers();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public void createUser(User user, String password) {
        final Store store = StoreFactory.getStore();
        try {
            store.createUser(user, password);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public void updateUser(User user) {
        final Store store = StoreFactory.getStore();
        try {
            store.updateUser(user);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public void deleteUsers(Collection<String> userNames) throws UserException {
        final String userName = getUserName();
        final boolean triedToDeleteSelf = userNames.remove(userName);
        final Store store = StoreFactory.getStore();
        try {
            store.deleteUsers(userNames);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new IllegalStateException(e.getMessage(), e);
        }
        if (triedToDeleteSelf) {
            throw new UserException(1, "You cannot delete yourself.");
        }
    }

    @Override
    public String getRandomPassword() {
        final String digits = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        final StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            final int pos = (int) (digits.length() * Math.random());
            final char c = digits.charAt(pos);
            stringBuilder.append(c);
        }
        return stringBuilder.toString();
    }

    @Override
    public String resetPassword(User user) throws Exception {
        final Store store = StoreFactory.getStore();
        try {
            final String userName = user.getUserName();
            final String randomPassword = getRandomPassword();
            store.resetPassword(userName, randomPassword);
            return randomPassword;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void createApplication(String applicationName) throws Exception {
        final Store store = StoreFactory.getStore();
        try {
            store.createApplication(applicationName);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void deleteApplication(String applicationName) throws Exception {
        final Store store = StoreFactory.getStore();
        try {
            store.deleteApplication(applicationName);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void setPackages(String app, List<AppPkg> pkgs) throws Exception {
        final Store store = StoreFactory.getStore();
        try {
            final ArrayList<String> packages = new ArrayList<String>();
            for (AppPkg pkg : pkgs) {
                packages.add(pkg.getPkg());
            }
            store.setPackages(app, packages);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}