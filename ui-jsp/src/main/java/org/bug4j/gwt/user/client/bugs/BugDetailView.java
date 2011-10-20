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
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.*;
import org.bug4j.common.TextToLines;
import org.bug4j.gwt.common.client.AdvancedAsyncCallback;
import org.bug4j.gwt.common.client.data.AppPkg;
import org.bug4j.gwt.user.client.Bug4jService;
import org.bug4j.gwt.user.client.BugModel;
import org.bug4j.gwt.user.client.data.Bug;
import org.bug4j.gwt.user.client.data.BugHit;
import org.bug4j.gwt.user.client.data.BugHitAndStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * The view that shows hits at the top and a stack at the bottom.
 */
public class BugDetailView extends DockLayoutPanel {

    private static final int PAGE_SIZE = 200;
    private Anchor _anchor;
    private HTML _stack;
    private DataGrid<BugHit> _dataGrid;
    private Bug _bug;
    private final Set<Long> _unreadHits = new HashSet<Long>();
    @Nullable
    private BugHit _lastSelectedHit;
    private Object _clip;
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
    private final BugModel _bugModel;
    private ScrollPanel _stackScrollPanel;

    public BugDetailView(BugModel bugModel) {
        super(Style.Unit.EM);
        getElement().setId("org.bug4j.gwt.user.client.bugs.BugDetailView");

        _bugModel = bugModel;

        final Widget bugHeader = createBugHeader();
        addNorth(bugHeader, 3);

        final SplitLayoutPanel splitLayoutPanel = new SplitLayoutPanel();
        _dataGrid = buildDataGrid();
        splitLayoutPanel.addNorth(_dataGrid, 250);
        splitLayoutPanel.add(buildStackPanel());
        add(splitLayoutPanel);
    }

    private DataGrid<BugHit> buildDataGrid() {
        final DataGrid<BugHit> ret = new DataGrid<BugHit>(PAGE_SIZE) {
            @Override
            public void setRowData(int start, List<? extends BugHit> values) {
                final HeaderPanel headerPanel = (HeaderPanel) getWidget();
                final ScrollPanel scrollPanel = (ScrollPanel) headerPanel.getContentWidget();
                scrollPanel.scrollToTop();
                super.setRowData(start, values);
            }
        };

        ret.setKeyboardSelectionPolicy(HasKeyboardSelectionPolicy.KeyboardSelectionPolicy.BOUND_TO_SELECTION);
        ret.setWidth("100%");
        final Label emptyTableWidget = new Label("No hits");
        emptyTableWidget.getElement().getStyle().setFontSize(20, Style.Unit.PT);
        ret.setEmptyTableWidget(emptyTableWidget);
        ret.setRowCount(0);

        _dateColumn.setSortable(true);
        _versionColumn.setSortable(true);
        _userColumn.setSortable(true);

        ret.addColumn(_dateColumn, "Date");
        ret.addColumn(_versionColumn, "Version");
        ret.addColumn(_userColumn, "User");

        ret.setRowStyles(new RowStyles<BugHit>() {
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
        ret.getColumnSortList().push(new ColumnSortList.ColumnSortInfo(_dateColumn, false));
        ret.addColumnSortHandler(new ColumnSortEvent.AsyncHandler(ret));

        _selectionModel = new SingleSelectionModel<BugHit>();
        ret.setSelectionModel(_selectionModel);
        _selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                whenHitSelectionChanges();
            }
        });

        final AsyncDataProvider<BugHit> dataProvider = new AsyncDataProvider<BugHit>() {
            @Override
            protected void onRangeChanged(HasData<BugHit> display) {
                if (_bug != null) {
                    final long bugId = _bug.getId();

                    final StringBuilder sortBy = new StringBuilder();
                    final ColumnSortList sortList = _dataGrid.getColumnSortList();
                    for (int i = 0; i < sortList.size(); i++) {
                        final ColumnSortList.ColumnSortInfo columnSortInfo = sortList.get(i);
                        final Column<?, ?> column = columnSortInfo.getColumn();
                        final char c = column == _dateColumn ? 'd' :
                                       column == _versionColumn ? 'v' :
                                       column == _userColumn ? 'b' : 'X';
                        final boolean ascending = columnSortInfo.isAscending();
                        sortBy.append(ascending ? c : Character.toUpperCase(c));
                    }

                    Bug4jService.App.getInstance().getHits(bugId, 0, PAGE_SIZE, sortBy.toString(), new AdvancedAsyncCallback<List<BugHit>>() {
                        @Override
                        public void onSuccess(List<BugHit> bugHits) {
                            _lastSelectedHit = null;
                            _unreadHits.clear();
                            for (BugHit bugHit : bugHits) {
                                final long hitId = bugHit.getHitId();
                                if (hitId > _bug.getLastReadHit()) {
                                    _unreadHits.add(hitId);
                                }
                            }

                            _dataGrid.setRowData(0, bugHits);
                            _dataGrid.setRowCount(bugHits.size(), true);

                            final SelectionModel<? super BugHit> selectionModel = _dataGrid.getSelectionModel();
                            if (!bugHits.isEmpty()) {
                                final BugHit firstBugHit = bugHits.get(0);
                                selectionModel.setSelected(firstBugHit, true);
                            } else {
                                selectionModel.setSelected(null, true);
                            }
                        }
                    });
                }
            }
        };
        dataProvider.addDataDisplay(ret);
        return ret;
    }

    private void whenHitSelectionChanges() {
        final BugHit bugHit = _selectionModel.getSelectedObject();
        if (_lastSelectedHit != null) {
            final long previousHitId = _lastSelectedHit.getHitId();
            if (previousHitId != bugHit.getHitId()) {
                _unreadHits.remove(previousHitId);
                if (_unreadHits.isEmpty()) {
                    _bug.setRead(true);
                }
            }
        }

        if (bugHit != null) {
            final long hitId = bugHit.getHitId();
            Bug4jService.App.getInstance().getBugHitAndStack(hitId, new AdvancedAsyncCallback<BugHitAndStack>() {
                @Override
                public void onSuccess(BugHitAndStack bugHitAndStack) {
                    final String stack = bugHitAndStack.getStack();
                    renderStack(stack);
                }
            });
        }
        _lastSelectedHit = bugHit;
    }

    public Widget createBugHeader() {
        _anchor = new Anchor();
        _anchor.addStyleName("BugDetailView-title");

        final SimpleLayoutPanel simpleLayoutPanel = new SimpleLayoutPanel();
        simpleLayoutPanel.setWidget(_anchor);
        return simpleLayoutPanel;
    }

    private Widget buildStackPanel() {
        final DockLayoutPanel ret = new DockLayoutPanel(Style.Unit.EM) {
            @Override
            public void onResize() {
                super.onResize();
                zeroClipReposition(_clip);
            }

            @Override
            protected void onDetach() {
                zeroClipDetach(_clip);
            }
        };

        _stack = new HTML();
        _stack.addStyleName("BugDetailView-hit-stack");
        _stackScrollPanel = new ScrollPanel(_stack);
        _stackScrollPanel.addStyleName("BugDetailView-hit-stack-sp");

        final Anchor copy = new Anchor("Copy");
        copy.getElement().setId("copyId");
        final FlowPanel bottomButtons = new FlowPanel();
        bottomButtons.add(copy);

        ret.addSouth(bottomButtons, 1.5);
        ret.add(_stackScrollPanel);
        return ret;
    }

    public void clear() {
        _anchor.setText("");
        _dataGrid.setRowCount(0);
        _stack.setHTML("");
    }

    public void displayBug(final Bug bug) {
        _bug = bug;
        refreshTitle();
        refreshGrid();
    }

    private void refreshTitle() {
        final long bugId = _bug.getId();

        final String bugTitle = bugId + "-" + _bug.getTitle();
        _anchor.setText(bugTitle);
        final String url = createBugLink(bugId);
        _anchor.setHref(url);
    }

    private void refreshGrid() {
        final Range visibleRange = _dataGrid.getVisibleRange();
        _dataGrid.setVisibleRangeAndClearData(visibleRange, true);
    }

    private static String createBugLink(long bugId) {
        return Window.Location
                .createUrlBuilder()
                .setParameter("bug", Long.toString(bugId))
                .buildString();
    }

    private void renderStack(final String stackText) {
        setClipText(stackText);

        if (stackText != null) {
            _stackScrollPanel.setVerticalScrollPosition(0);
            _bugModel.getPackages(new AdvancedAsyncCallback<List<AppPkg>>() {
                @Override
                public void onSuccess(List<AppPkg> appPkgs) {
                    final SafeHtml safeHtml = buildStack(appPkgs, stackText);
                    _stack.setHTML(safeHtml);
                }
            });
        } else {
            _stack.setHTML("");
        }
    }

    private void setClipText(String stackText) {
        if (_clip == null) {
            // It would be cleaner to attach in a onAttach(){} but the element
            // does not have a size at that time so it wouldn't work.
            _clip = zeroClipAttach("copyId");
        }
        zeroClipSetText(_clip, stackText);
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

    private static native Object zeroClipAttach(String widgetId) /*-{
        var clip = new $wnd.ZeroClipboard.Client();
        clip.glue(widgetId);
        return clip;
    }-*/;

    private static native void zeroClipDetach(Object clip) /*-{
        clip.destroy()
    }-*/;

    private static native void zeroClipSetText(Object clip, String text) /*-{
        clip.setText(text)
    }-*/;

    private static native void zeroClipReposition(Object clip) /*-{
        clip.reposition();
    }-*/;
}
