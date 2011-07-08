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

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.*;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.*;
import org.bug4j.server.gwt.client.Bug4j;
import org.bug4j.server.gwt.client.Bug4jService;
import org.bug4j.server.gwt.client.data.Bug;
import org.bug4j.server.gwt.client.data.BugHit;
import org.bug4j.server.gwt.client.data.BugHitAndStack;
import org.bug4j.server.gwt.client.data.Filter;
import org.bug4j.server.gwt.client.util.TextToLines;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SuppressWarnings({"GWTStyleCheck"})
class BugDetailView {

    private static final int PAGE_SIZE = 200;
    private final DisplaysBugs _displaysBugs;
    private Label _label;
    private HTML _stack;
    private CellTable<BugHit> _cellTable;
    private Filter _filter;
    private Bug _bug;
    private static final TextColumn<BugHit> _dateColumn = new TextColumn<BugHit>() {
        @Override
        public String getValue(BugHit bugHit) {
            final long dateReported = bugHit.getDateReported();
            final DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_MEDIUM);
            final String ret = dateTimeFormat.format(new Date(dateReported));
            return ret;
        }
    };

    private static final TextColumn<BugHit> _versionColumn = new TextColumn<BugHit>() {
        @Override
        public String getValue(BugHit bugHit) {
            return bugHit.getAppVer();
        }
    };
    private static final TextColumn<BugHit> _userColumn = new TextColumn<BugHit>() {
        @Override
        public String getValue(BugHit bugHit) {
            return bugHit.getUser();
        }
    };
    private SingleSelectionModel<BugHit> _selectionModel;

    public BugDetailView(DisplaysBugs displaysBugs) {
        _displaysBugs = displaysBugs;
    }

    public Widget createWidget() {
        final DockLayoutPanel ret = new DockLayoutPanel(Style.Unit.EM);
        ret.addNorth(createBugHeader(), 3);

        final SplitLayoutPanel splitLayoutPanel = new SplitLayoutPanel();
        _cellTable = buildCellTable();
        splitLayoutPanel.addNorth(new ScrollPanel(_cellTable), 250);
        splitLayoutPanel.add(buildStackPanel());
        ret.add(splitLayoutPanel);
        return ret;
    }

    private CellTable<BugHit> buildCellTable() {
        _cellTable = new CellTable<BugHit>(PAGE_SIZE);
        _cellTable.setWidth("100%", true);

        _dateColumn.setSortable(true);
        _versionColumn.setSortable(true);
        _userColumn.setSortable(true);

        _cellTable.addColumn(_dateColumn, "Date");
        _cellTable.addColumn(_versionColumn, "Version");
        _cellTable.addColumn(_userColumn, "User");

        _cellTable.setRowStyles(new RowStyles<BugHit>() {
            @Override
            public String getStyleNames(BugHit row, int rowIndex) {
                String ret = "BugDetailView-hit-cell";
//                if (row.getHitId() % 2 == 0) {
//                    ret += " BugDetailView-hit-cell-unread";
//                }
                return ret;
            }
        });
        _cellTable.getColumnSortList().push(new ColumnSortList.ColumnSortInfo(_dateColumn, false));

        AsyncDataProvider<BugHit> dataProvider = new AsyncDataProvider<BugHit>() {
            @Override
            protected void onRangeChanged(HasData<BugHit> display) {
                refreshData();
            }
        };
        dataProvider.addDataDisplay(_cellTable);

        _cellTable.addColumnSortHandler(new ColumnSortEvent.AsyncHandler(_cellTable));
        _selectionModel = new SingleSelectionModel<BugHit>();
        _cellTable.setSelectionModel(_selectionModel);
        _selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                whenHitSelectionChanges();
            }
        });
        return _cellTable;
    }

    private void whenHitSelectionChanges() {
        final BugHit bugHit = _selectionModel.getSelectedObject();
        if (bugHit != null) {
            final long hitId = bugHit.getHitId();
            Bug4jService.App.getInstance().getBugHitAndStack(hitId, new AsyncCallback<BugHitAndStack>() {
                @Override
                public void onFailure(Throwable caught) {
                    Window.alert(caught.getMessage());
                }

                @Override
                public void onSuccess(BugHitAndStack bugHitAndStack) {
                    final String stack = bugHitAndStack.getStack();
                    render(stack);
                }
            });
        }

    }

    public Widget createBugHeader() {
        final VerticalPanel verticalPanel = new VerticalPanel();

/* Remove the "Delete button for now.
        {
            verticalPanel.add(buildToolbar());
        }
*/

        {
            _label = new Label();
            _label.addStyleName("BugDetailView-title");
            verticalPanel.add(_label);
        }

        final SimpleLayoutPanel simpleLayoutPanel = new SimpleLayoutPanel();
        simpleLayoutPanel.addStyleName("BugDetailView-bug-header");
        simpleLayoutPanel.add(verticalPanel);

        return simpleLayoutPanel;
    }

    private ScrollPanel buildStackPanel() {
        _stack = new HTML();
        _stack.addStyleName("BugDetailView-hit-stack");
        final ScrollPanel scrollPanel = new ScrollPanel(_stack);
        scrollPanel.addStyleName("BugDetailView-hit-stack-sp");
        return scrollPanel;
    }

    private Widget buildToolbar() {
        final HorizontalPanel toolbar = new HorizontalPanel();
        final Button delete = new Button("Delete");
        delete.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                whenDelete();
            }
        });
        toolbar.add(delete);
        return toolbar;
    }

    private void whenDelete() {
        Bug4jService.App.getInstance().deleteBug(_bug.getId(), new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                Window.alert(caught.getMessage());
            }

            @Override
            public void onSuccess(Void result) {
                _displaysBugs.whenBugListChanges();
            }
        });
    }

    public void clear() {
        _label.setText("");
        _cellTable.setRowCount(0);
        _stack.setHTML("");
    }

    public void displayBug(Filter filter, final Bug bug) {
        _filter = filter;
        _bug = bug;
        refreshData();
    }

    private void refreshData() {
        if (_bug != null) {
            final long bugId = _bug.getId();

            final String s = bugId + "-" + _bug.getTitle();
            _label.setText(s);

            final StringBuilder sortBy = new StringBuilder();
            final ColumnSortList sortList = _cellTable.getColumnSortList();
            for (int i = 0; i < sortList.size(); i++) {
                final ColumnSortList.ColumnSortInfo columnSortInfo = sortList.get(i);
                final Column<?, ?> column = columnSortInfo.getColumn();
                final char c = column == _dateColumn ? 'd' :
                               column == _versionColumn ? 'v' :
                               column == _userColumn ? 'b' : 'X';
                final boolean ascending = columnSortInfo.isAscending();
                sortBy.append(ascending ? c : Character.toUpperCase(c));
            }

            Bug4jService.App.getInstance().getHits(bugId, _filter, 0, PAGE_SIZE, sortBy.toString(), new AsyncCallback<List<BugHit>>() {
                @Override
                public void onFailure(Throwable caught) {
                    Window.alert(caught.getMessage());
                }

                @Override
                public void onSuccess(List<BugHit> bugHits) {
                    _cellTable.setRowData(bugHits);
                    final SelectionModel<? super BugHit> selectionModel = _cellTable.getSelectionModel();
                    if (!bugHits.isEmpty()) {
                        final BugHit firstBugHit = bugHits.get(0);
                        selectionModel.setSelected(firstBugHit, true);
                    } else {
                        selectionModel.setSelected(null, true);
                    }
                }
            });
        } else {
            _cellTable.setRowCount(0);
        }
    }

    private void render(final String stackText) {
        final Bug4j bug4J = _displaysBugs.getBug4J();
        bug4J.withAppPackages(new AsyncCallback<List<String>>() {
            @Override
            public void onFailure(Throwable caught) {
                Window.alert(caught.getMessage());
            }

            @Override
            public void onSuccess(List<String> appPackages) {
                renderWithAppPackages(appPackages, stackText);
            }
        });
    }

    private void renderWithAppPackages(List<String> appPackages, String stackText) {
        final SafeHtml safeHtml = buildStack(appPackages, stackText);
        _stack.setHTML(safeHtml);
    }

    private static SafeHtml buildStack(List<String> appPackages, String stackText) {
        final SafeHtmlBuilder safeHtmlBuilder = new SafeHtmlBuilder();

        List<String> lineStarts = new ArrayList<String>(appPackages);
        for (String appPackage : appPackages) {
            lineStarts.add("\tat " + appPackage);
        }

        final String[] lines = TextToLines.toArray(stackText);
        for (String line : lines) {
            boolean isImportant = false;
            if (line.startsWith("\t")) {
                for (int i = 0; !isImportant && i < lineStarts.size(); i++) {
                    final String lineStart = lineStarts.get(i);
                    if (line.startsWith(lineStart)) {
                        isImportant = true;
                    }
                }
            } else {
                isImportant = true;
            }

            safeHtmlBuilder.appendHtmlConstant(
                    "<div class=\"" + (isImportant ? "stack-highlight" : "stack-dim") + "\">"
            );
            safeHtmlBuilder.appendEscaped(line);
            safeHtmlBuilder.appendHtmlConstant("<br>");
            safeHtmlBuilder.appendHtmlConstant("</div>");
        }
        return safeHtmlBuilder.toSafeHtml();
    }
}