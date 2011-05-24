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

package org.dandoy.bug4j.server.gwt.client.bugs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import org.dandoy.bug4j.server.gwt.client.Bug4jService;
import org.dandoy.bug4j.server.gwt.client.data.Bug;

import java.util.List;

public class BugView {

    public BugView() {
    }

    public Widget createWidget() {
        final CellTable<Bug> bugCellTable = createTable();
        final ScrollPanel scrollPanel = new ScrollPanel(bugCellTable);
        return scrollPanel;
    }

    private CellTable<Bug> createTable() {
        final CellTable<Bug> cellTable = new CellTable<Bug>();
        cellTable.addColumn(BugViewColumn.ID, "ID");
        cellTable.setColumnWidth(BugViewColumn.TITLE, "300px");
        cellTable.addColumn(BugViewColumn.TITLE, "Title");
        cellTable.addColumn(BugViewColumn.HIT, "#");
        cellTable.getColumnSortList().push(new ColumnSortList.ColumnSortInfo(BugViewColumn.HIT, false));

        AsyncDataProvider<Bug> dataProvider = new AsyncDataProvider<Bug>() {
            @Override
            protected void onRangeChanged(HasData<Bug> display) {
                final ColumnSortList sortList = cellTable.getColumnSortList();
                final String sortBy = BugViewColumn.sortBy(sortList);
                Bug4jService.App.getInstance().getBugs(sortBy, new AsyncCallback<List<Bug>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("Failed");
                    }

                    @Override
                    public void onSuccess(List<Bug> result) {
                        cellTable.setRowData(result);
                    }
                });

            }
        };
        dataProvider.addDataDisplay(cellTable);
        cellTable.addColumnSortHandler(new ColumnSortEvent.AsyncHandler(cellTable));

        return cellTable;
    }

}
