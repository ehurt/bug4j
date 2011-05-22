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
import org.dandoy.bug4j.server.gwt.client.Bug4jService;
import org.dandoy.bug4j.server.gwt.client.bugs.BugEntry;
import org.dandoy.bug4j.server.gwt.client.util.ModelUtil;

import java.util.*;

public class Bug4jServiceImpl extends RemoteServiceServlet implements Bug4jService {
    @Override
    public List<BugEntry> getBugs(final String sortBy) {
        final ArrayList<BugEntry> ret = new ArrayList<BugEntry>(Arrays.asList(
                new BugEntry("app", "NPE at abc.java", 1, 3),
                new BugEntry("app", "NPE at asd.java", 2, 6),
                new BugEntry("app", "NPE at sdf.java", 3, 4),
                new BugEntry("app", "NPE at qwe.java", 4, 1)
        ));
        if (!sortBy.isEmpty()) {
            final String lcSortBy = sortBy.toLowerCase();
            Comparator<BugEntry> bugEntryComparator = new Comparator<BugEntry>() {
                @Override
                public int compare(BugEntry o1, BugEntry o2) {
                    if (lcSortBy.startsWith("i")) {
                        return ModelUtil.compareTo(o1.getId(), o2.getId());
                    } else if (lcSortBy.startsWith("t")) {
                        return o1.getTitle().compareToIgnoreCase(o2.getTitle());
                    } else if (lcSortBy.startsWith("h")) {
                        return o1.getHitCount() - o2.getHitCount();
                    } else {
                        return 0;
                    }
                }
            };
            if (Character.isUpperCase(sortBy.charAt(0))) {
                bugEntryComparator = Collections.reverseOrder(bugEntryComparator);
            }
            Collections.sort(ret, bugEntryComparator);
        }
        return ret;
    }
}