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

package org.bug4j.server.gwt.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.apache.log4j.Logger;
import org.bug4j.server.gwt.client.Bug4jService;
import org.bug4j.server.gwt.client.data.Bug;
import org.bug4j.server.gwt.client.data.BugHit;
import org.bug4j.server.gwt.client.data.BugHitAndStack;
import org.bug4j.server.gwt.client.data.Filter;
import org.bug4j.server.store.Store;
import org.bug4j.server.store.StoreFactory;
import org.jetbrains.annotations.Nullable;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

public class Bug4jServiceImpl extends RemoteServiceServlet implements Bug4jService {
    private static final Logger LOGGER = Logger.getLogger(Bug4jServiceImpl.class);

    @Override
    public String getUserName() throws Exception {
        final HttpServletRequest threadLocalRequest = getThreadLocalRequest();
        final String remoteUser = threadLocalRequest.getRemoteUser();
        return remoteUser;
    }

    @Override
    public List<String> getApplications() throws Exception {
        try {
            final Store store = StoreFactory.getStore();
            return store.getApplications();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Nullable
    public String getDefaultApplication() throws Exception {
        String ret = null;
        final String remoteUser = getUserName();
        final Store store = StoreFactory.getStore();
        try {
            ret = store.getDefaultApplication(remoteUser);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ret;
    }

    @Override
    public void setDefaultApplication(String app) throws Exception {
        final String remoteUser = getUserName();
        final Store store = StoreFactory.getStore();
        try {
            store.setDefaultApplication(remoteUser, app);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public Filter getDefaultFilter() throws Exception {
        try {
            final Store store = StoreFactory.getStore();
            return store.getDefaultFilter(getUserName());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new Exception(e.getMessage(), e);
        }
    }

    @Override
    public void setDefaultFilter(Filter filter) throws Exception {
        try {
            final Store store = StoreFactory.getStore();
            final String userName = getUserName();
            store.setDefaultFilter(userName, filter);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new Exception(e.getMessage(), e);
        }
    }

    @Override
    public List<Bug> getBugs(String app, Filter filter, final String sortBy) throws Exception {
        try {
            final Store store = StoreFactory.getStore();
            return store.getBugs(app, filter, 0, Integer.MAX_VALUE, sortBy);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new Exception(e.getMessage(), e);
        }
    }

    @Override
    public void deleteBug(long bugId) throws Exception {
        try {
            final Store store = StoreFactory.getStore();
            store.deleteBug(bugId);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<String> getPackages(String app) throws Exception {
        final Store store = StoreFactory.getStore();
        final List<String> ret;
        try {
            ret = store.getPackages(app);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new Exception(e.getMessage(), e);
        }
        return ret;
    }

    @Override
    public void setPackages(String app, List<String> packages) throws Exception {
        final Store store = StoreFactory.getStore();
        try {
            store.setPackages(app, packages);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public List<BugHit> getHits(long bugId, Filter filter, int offset, int max, String orderBy) throws Exception {
        try {
            final Store store = StoreFactory.getStore();
            return store.getHits(bugId, filter, offset, max, orderBy);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public Map<Bug, int[]> getTopHits(String app, int daysBack, int max) throws Exception {
        try {
            final Store store = StoreFactory.getStore();
            return store.getTopHits(app, daysBack, max);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public BugHitAndStack getBugHitAndStack(long hitId) throws Exception {
        final Store store = StoreFactory.getStore();
        return store.getBugHitAndStack(hitId);
    }

    @Override
    public void markRead(long bugId) throws Exception {
        final Store store = StoreFactory.getStore();
        try {
            store.markRead(getUserName(), bugId);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}