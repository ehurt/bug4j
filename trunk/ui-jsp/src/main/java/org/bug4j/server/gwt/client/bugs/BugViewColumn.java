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

package org.bug4j.server.gwt.client.bugs;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.TextColumn;
import org.bug4j.server.gwt.client.data.Bug;

public class BugViewColumn extends TextColumn<Bug> {
    public static final BugViewColumn ID = new BugViewColumn("I");
    public static final BugViewColumn TITLE = new BugViewColumn("T");
    public static final BugViewColumn HIT = new BugViewColumn("H");

    private final String _id;

    public BugViewColumn(String id) {
        _id = id;
        setSortable(true);
    }

    @Override
    public String getValue(Bug bugEntry) {
        if (ID._id.equals(_id)) {
            return Long.toString(bugEntry.getId());
        } else if (TITLE._id.equals(_id)) {
            return bugEntry.getTitle();
        } else if (HIT._id.equals(_id)) {
            return Integer.toString(bugEntry.getHitCount());
        } else {
            throw new IllegalStateException("Invalid id: " + _id);
        }
    }

    @Override
    public void render(Cell.Context context, Bug object, SafeHtmlBuilder sb) {
        super.render(context, object, sb);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public static String sortBy(ColumnSortList columnSortList) {
        final StringBuilder ret = new StringBuilder();
        final int size = columnSortList.size();
        for (int i = 0; i < size; i++) {
            final ColumnSortList.ColumnSortInfo columnSortInfo = columnSortList.get(i);
            final BugViewColumn bugViewColumn = (BugViewColumn) columnSortInfo.getColumn();
            final String id;
            if (columnSortInfo.isAscending()) {
                id = bugViewColumn._id.toLowerCase();
            } else {
                id = bugViewColumn._id.toUpperCase();
            }
            ret.append(id);
        }
        return ret.toString();
    }
}
