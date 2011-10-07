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

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import org.bug4j.gwt.common.client.data.UserException;
import org.bug4j.gwt.user.client.data.*;

import java.util.List;
import java.util.Map;

@RemoteServiceRelativePath("Bug4jService")
public interface Bug4jService extends RemoteService {

    String getDefaultApplication();

    void setDefaultApplication(String app);

    Filter getDefaultFilter();

    void setDefaultFilter(Filter filter);

    List<Bug> getBugs(String app, Filter filter, String sortBy);

    void deleteBug(long bugId);

    List<BugHit> getHits(long bugId, int offset, int max, String orderBy);

    Map<Bug, int[]> getTopHits(String app, int daysBack, int max);

    BugHitAndStack getBugHitAndStack(long hitId);

    void markRead(long bugId);

    List<BugCountByDate> getBugCountByDate(String app);

    void updatePassword(String oldPassword, String newPassword) throws UserException;

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
