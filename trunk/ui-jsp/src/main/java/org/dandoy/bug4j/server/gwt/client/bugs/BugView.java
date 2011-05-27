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
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import org.dandoy.bug4j.server.gwt.client.Bug4jService;
import org.dandoy.bug4j.server.gwt.client.data.Bug;
import org.dandoy.bug4j.server.gwt.client.data.BugDetail;

import java.util.List;

public class BugView {

    private SingleSelectionModel<Bug> _selectionModel;
    private BugDetailView _bugDetailView;

    public BugView() {
    }

    public Widget createWidget() {
        final CellTable<Bug> bugCellTable = createTable();
        final ScrollPanel scrollPanel = new ScrollPanel(bugCellTable);

        _bugDetailView = new BugDetailView();

        final SplitLayoutPanel splitLayoutPanel = new SplitLayoutPanel(5);
        splitLayoutPanel.addWest(scrollPanel, 500);
        splitLayoutPanel.add(_bugDetailView.getRootElement());
        return splitLayoutPanel;
    }

    private CellTable<Bug> createTable() {
        final CellTable<Bug> cellTable = new CellTable<Bug>();
        //noinspection GWTStyleCheck
        cellTable.addStyleName("bug-table");
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
                    public void onSuccess(List<Bug> bugs) {
                        cellTable.setRowData(bugs);
                        if (!bugs.isEmpty()) {
                            final Bug firstBug = bugs.get(0);
                            _selectionModel.setSelected(firstBug, true);
                        }
                    }
                });
            }
        };
        dataProvider.addDataDisplay(cellTable);
        cellTable.addColumnSortHandler(new ColumnSortEvent.AsyncHandler(cellTable));
        _selectionModel = new SingleSelectionModel<Bug>();
        cellTable.setSelectionModel(_selectionModel);

        _selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                whenTableSelectionChanges();
            }
        });

        return cellTable;
    }

    private void whenTableSelectionChanges() {
        final Bug bug = _selectionModel.getSelectedObject();
        final long bugId = bug.getId();
        Bug4jService.App.getInstance().getBug(bugId, new AsyncCallback<BugDetail>() {
            @Override
            public void onFailure(Throwable caught) {
                Window.alert(caught.getMessage());
            }

            @Override
            public void onSuccess(BugDetail bugDetail) {
                _bugDetailView.setBugDetail(bugDetail);
            }
        });
    }
}
