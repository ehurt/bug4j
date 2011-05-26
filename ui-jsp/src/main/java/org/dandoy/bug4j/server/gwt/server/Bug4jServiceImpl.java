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

package org.dandoy.bug4j.server.gwt.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.apache.log4j.Logger;
import org.dandoy.bug4j.server.gwt.client.Bug4jService;
import org.dandoy.bug4j.server.gwt.client.data.Bug;
import org.dandoy.bug4j.server.gwt.client.data.BugDetail;
import org.dandoy.bug4j.server.store.Store;
import org.dandoy.bug4j.server.store.StoreFactory;

import java.util.List;

public class Bug4jServiceImpl extends RemoteServiceServlet implements Bug4jService {
    private static final Logger LOGGER = Logger.getLogger(Bug4jServiceImpl.class);

    @Override
    public List<Bug> getBugs(final String sortBy) throws Exception {
        try {
            final Store store = StoreFactory.getStore();
            return store.getBugs(null, 0, Integer.MAX_VALUE, sortBy);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new Exception(e.getMessage(), e);
        }
    }

    @Override
    public BugDetail getBug(long bugId) throws Exception {
        final BugDetail ret;
        try {
            final Store store = StoreFactory.getStore();
            ret = store.getBug(bugId);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new Exception(e.getMessage(), e);
        }
        return ret;
    }
}