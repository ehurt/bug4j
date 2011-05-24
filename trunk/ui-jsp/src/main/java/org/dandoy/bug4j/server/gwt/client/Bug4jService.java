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

package org.dandoy.bug4j.server.gwt.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import org.dandoy.bug4j.server.gwt.client.data.Bug;

import java.util.List;

@RemoteServiceRelativePath("Bug4jService")
public interface Bug4jService extends RemoteService {
    List<Bug> getBugs(String sortBy);

    /**
     * Utility/Convenience class.
     * Use Bug4jService.App.getInstance() to access static instance of BugServiceAsync
     */
    public static class App {
        private static Bug4jServiceAsync ourInstance = GWT.create(Bug4jService.class);

        public static synchronized Bug4jServiceAsync getInstance() {
            return ourInstance;
        }
    }
}
