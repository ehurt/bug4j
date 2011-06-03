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

package org.bug4j.server.gwt.client.data;

import java.io.Serializable;
import java.util.List;

public class BugDetailInitialData implements Serializable {
    private Bug _bug;
    private List<Long> _bugHitIds;
    private BugHitAndStack _lastStack;

    public BugDetailInitialData() {
    }

    public BugDetailInitialData(Bug bug, List<Long> bugHitIds, BugHitAndStack lastStack) {
        _bug = bug;
        _bugHitIds = bugHitIds;
        _lastStack = lastStack;
    }

    public Bug getBug() {
        return _bug;
    }

    public List<Long> getBugHitIds() {
        return _bugHitIds;
    }

    public BugHitAndStack getLastBugHitAndStack() {
        return _lastStack;
    }
}
