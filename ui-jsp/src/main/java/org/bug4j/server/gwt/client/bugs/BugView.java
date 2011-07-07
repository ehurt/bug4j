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

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import org.bug4j.server.gwt.client.Bug4j;
import org.bug4j.server.gwt.client.Bug4jService;
import org.bug4j.server.gwt.client.data.Bug;
import org.bug4j.server.gwt.client.data.Filter;
import org.bug4j.server.gwt.client.util.PropertyListener;

import java.util.List;

public class BugView implements DisplaysBugs {

    private static final int PAGE_SIZE = 100;
    private SingleSelectionModel<Bug> _selectionModel;
    private BugDetailView _bugDetailView;
    private CellTable<Bug> _cellTable;
    private final Filter _filter = new Filter();
    private MenuItem _filterMenuItem;
    private final Bug4j _bug4j;

    public BugView(Bug4j bug4j) {
        _bug4j = bug4j;
        _bug4j.addApplicationListener(new PropertyListener<String>() {
            @Override
            public void propertyChanged(String key, String value) {
                refreshBugs();
            }
        });
    }

    public Widget createWidget() {
        final CellTable<Bug> bugCellTable = createTable();
        final ScrollPanel scrollPanel = new ScrollPanel(bugCellTable);
        final DockLayoutPanel dockLayoutPanel = new DockLayoutPanel(Style.Unit.EM);
        final MenuBar menuBar = new MenuBar();
        _filterMenuItem = menuBar.addItem("<<<filter>>>", new Command() {
            @Override
            public void execute() {
                whenFilter();
            }
        });
        updateFilterMenuItem();

        menuBar.addItem("Export...", new Command() {
            @Override
            public void execute() {
                whenExport();
            }
        });
        dockLayoutPanel.addNorth(menuBar, 2);

        dockLayoutPanel.add(scrollPanel);

        _bugDetailView = new BugDetailView(this);

        final SplitLayoutPanel splitLayoutPanel = new SplitLayoutPanel(5);
        splitLayoutPanel.addWest(dockLayoutPanel, 600);
        splitLayoutPanel.add(_bugDetailView.createWidget());
        return splitLayoutPanel;
    }

    private void whenExport() {
        final String moduleBaseURL = GWT.getModuleBaseURL();
        final String application = _bug4j.getApplication();
        if (application != null) {
            Window.open(moduleBaseURL + "../bug/export?a=" + application, "_self", "");
        }
    }

    private void whenFilter() {
        final FilterDialog filterDialog = new FilterDialog(_filter);
        filterDialog.addCloseHandler(new CloseHandler<PopupPanel>() {
            @Override
            public void onClose(CloseEvent<PopupPanel> popupPanelCloseEvent) {
                if (!popupPanelCloseEvent.isAutoClosed()) {
                    final Filter newFilter = filterDialog.getFilter();
                    newFilter.copyTo(_filter);
                    refreshBugs();
                    updateFilterMenuItem();
                }
            }
        });
        filterDialog.show();
    }

    private void updateFilterMenuItem() {
        _filterMenuItem.setText("Filter" + (_filter.isFiltering() ? "*" : ""));
    }

    public void whenBugListChanges() {
        refreshBugs();
    }

    @Override
    public Bug4j getBug4J() {
        return _bug4j;
    }

    private CellTable<Bug> createTable() {
        _cellTable = new CellTable<Bug>(PAGE_SIZE);
        final Label noDataLabel = new Label("No data");
        noDataLabel.getElement().getStyle().setFontSize(20, Style.Unit.PT);
        _cellTable.setEmptyTableWidget(noDataLabel);
        _cellTable.setWidth("100%", true);
        _cellTable.addStyleName("bug-table");
        _cellTable.addColumn(BugViewColumn.ID, "ID");
        _cellTable.addColumn(BugViewColumn.TITLE, "Title");
        _cellTable.addColumn(BugViewColumn.HIT, "#");

        _cellTable.setColumnWidth(BugViewColumn.ID, "5em");
        _cellTable.setColumnWidth(BugViewColumn.HIT, "5em");

        _cellTable.getColumnSortList().push(new ColumnSortList.ColumnSortInfo(BugViewColumn.HIT, false));

        AsyncDataProvider<Bug> dataProvider = new AsyncDataProvider<Bug>() {
            @Override
            protected void onRangeChanged(HasData<Bug> display) {
                refreshBugs();
            }
        };
        dataProvider.addDataDisplay(_cellTable);
        _cellTable.addColumnSortHandler(new ColumnSortEvent.AsyncHandler(_cellTable));
        _selectionModel = new SingleSelectionModel<Bug>();
        _cellTable.setSelectionModel(_selectionModel);

        _selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                whenTableSelectionChanges();
            }
        });

        return _cellTable;
    }

    private void refreshBugs() {
        final ColumnSortList sortList = _cellTable.getColumnSortList();
        final String sortBy = BugViewColumn.sortBy(sortList);
        final String application = _bug4j.getApplication();
        if (application != null) {
            Bug4jService.App.getInstance().getBugs(application, _filter, sortBy, new AsyncCallback<List<Bug>>() {
                @Override
                public void onFailure(Throwable caught) {
                    Window.alert(caught.getMessage());
                }

                @Override
                public void onSuccess(List<Bug> bugs) {
                    _cellTable.setRowData(bugs);
                    if (!bugs.isEmpty()) {
                        final Bug firstBug = bugs.get(0);
                        _selectionModel.setSelected(firstBug, true);
                    } else {
                        _selectionModel.setSelected(null, true);
                    }
                }
            });
        } else {
            _cellTable.setRowCount(0);
        }
    }

    private void whenTableSelectionChanges() {
        final Bug bug = _selectionModel.getSelectedObject();
        if (bug != null) {
            final long bugId = bug.getId();
            _bugDetailView.displayBug(_filter, bugId);
        } else {
            _bugDetailView.clear();
        }
    }
}
