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

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.bug4j.gwt.user.client.data.*;

import java.util.List;
import java.util.Map;

public interface Bug4jServiceAsync {

    void getBugs(String app, Filter filter, String sortBy, AsyncCallback<List<Bug>> async);

    void getHits(long bugId, Filter filter, int offset, int max, String orderBy, AsyncCallback<List<BugHit>> async);

    void deleteBug(long bugId, AsyncCallback<Void> async);

    void getTopHits(String app, int daysBack, int max, AsyncCallback<Map<Bug, int[]>> async);

    void getBugCountByDate(String app, AsyncCallback<List<BugCountByDate>> async);

    void getBugHitAndStack(long hitId, AsyncCallback<BugHitAndStack> async);

    void setDefaultApplication(String app, AsyncCallback<Void> async);

    void getDefaultApplication(AsyncCallback<String> async);

    void getDefaultFilter(AsyncCallback<Filter> async);

    void setDefaultFilter(Filter filter, AsyncCallback<Void> async);

    void markRead(long bugId, AsyncCallback<Void> async);

    void updatePassword(String oldPassword, String newPassword, AsyncCallback<Void> async);
}
