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

package org.bug4j.gwt.user.client.bugs;

import com.google.gwt.dom.client.Style;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.*;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.*;
import org.bug4j.common.TextToLines;
import org.bug4j.gwt.common.client.data.AppPkg;
import org.bug4j.gwt.user.client.Bug4j;
import org.bug4j.gwt.user.client.Bug4jService;
import org.bug4j.gwt.user.client.data.Bug;
import org.bug4j.gwt.user.client.data.BugHit;
import org.bug4j.gwt.user.client.data.BugHitAndStack;
import org.bug4j.gwt.user.client.data.Filter;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@SuppressWarnings({"GWTStyleCheck"})
class BugDetailView {

    private static final int PAGE_SIZE = 200;
    private final DisplaysBugs _displaysBugs;
    private Label _label;
    private HTML _stack;
    private CellTable<BugHit> _cellTable;
    private Filter _filter;
    private Bug _bug;
    private final Set<Long> _unreadHits = new HashSet<Long>();
    @Nullable
    private BugHit _lastSelectedHit;
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
        final Label emptyTableWidget = new Label("No hits");
        emptyTableWidget.getElement().getStyle().setFontSize(20, Style.Unit.PT);
        _cellTable.setEmptyTableWidget(emptyTableWidget);

        _dateColumn.setSortable(true);
        _versionColumn.setSortable(true);
        _userColumn.setSortable(true);

        _cellTable.addColumn(_dateColumn, "Date");
        _cellTable.addColumn(_versionColumn, "Version");
        _cellTable.addColumn(_userColumn, "User");

        _cellTable.setRowStyles(new RowStyles<BugHit>() {
            @Override
            public String getStyleNames(BugHit bugHit, int rowIndex) {
                StringBuilder ret = new StringBuilder("BugDetailView-hit-cell ");
                if (_unreadHits.contains(bugHit.getHitId())) {
                    ret.append("BugDetailView-hit-cell-unread");
                } else {
                    ret.append("BugDetailView-hit-cell-read");
                }
                return ret.toString();
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
        if (_lastSelectedHit != null) {
            final long previousHitId = _lastSelectedHit.getHitId();
            if (previousHitId != bugHit.getHitId()) {
                _unreadHits.remove(previousHitId);
                if (_unreadHits.isEmpty()) {
                    _bug.setRead(true);
                    _displaysBugs.redisplay();
                }
            }
        }

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
        _lastSelectedHit = bugHit;
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

    public void clear() {
        _label.setText("");
        _cellTable.setRowCount(0);
        _stack.setHTML("");
    }

    public void displayBug(Filter filter, final Bug bug) {
        _filter = filter;
        _bug = bug;
        _lastSelectedHit = null;
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
                    setData(bugHits);
                }
            });
        } else {
            _cellTable.setRowCount(0);
        }
    }

    private void setData(List<BugHit> bugHits) {
        _unreadHits.clear();
        for (BugHit bugHit : bugHits) {
            final long hitId = bugHit.getHitId();
            if (hitId > _bug.getLastReadHit()) {
                _unreadHits.add(hitId);
            }
        }

        _cellTable.setRowData(bugHits);
        final SelectionModel<? super BugHit> selectionModel = _cellTable.getSelectionModel();
        if (!bugHits.isEmpty()) {
            final BugHit firstBugHit = bugHits.get(0);
            selectionModel.setSelected(firstBugHit, true);
        } else {
            selectionModel.setSelected(null, true);
        }
    }

    private void render(final String stackText) {
        final Bug4j bug4J = _displaysBugs.getBug4J();
        bug4J.withAppPackages(new AsyncCallback<List<AppPkg>>() {
            @Override
            public void onFailure(Throwable caught) {
                Window.alert(caught.getMessage());
            }

            @Override
            public void onSuccess(List<AppPkg> appPackages) {
                renderWithAppPackages(appPackages, stackText);
            }
        });
    }

    private void renderWithAppPackages(List<AppPkg> appPackages, String stackText) {
        final SafeHtml safeHtml = buildStack(appPackages, stackText);
        _stack.setHTML(safeHtml);
    }

    private static SafeHtml buildStack(List<AppPkg> appPkgs, String stackText) {
        final SafeHtmlBuilder safeHtmlBuilder = new SafeHtmlBuilder();

        List<String> lineStarts = new ArrayList<String>(appPkgs.size());
        for (AppPkg appPkg : appPkgs) {
            lineStarts.add("\tat " + appPkg.getPkg());
        }

        final String[] lines = toLineArray(stackText);
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

    /**
     * Transforms a text string into an array of Strings.
     * This code is "stolen" from {@link TextToLines} because sharing from another module did not work with GWT+IntelliJ.
     *
     * @param text input text
     * @return an array of lines
     */
    public static String[] toLineArray(String text) {
        final List<String> ret = new ArrayList<String>();
        final int length = text.length();
        int from = 0;
        int to = 0;
        while (to < length) {
            final char c = text.charAt(to);
            if (c == '\r') {
                if (to + 1 < length && text.charAt(to + 1) == '\n') {
                    final String line = text.substring(from, to);
                    ret.add(line);
                    to++;
                    from = to + 1;
                }
            } else if (c == '\n') {
                final String line = text.substring(from, to);
                ret.add(line);
                from = to + 1;
            }
            to++;
        }
        if (from < to) {
            final String line = text.substring(from, to);
            ret.add(line);
        }
        return ret.toArray(new String[ret.size()]);
    }

}