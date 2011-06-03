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

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import org.dandoy.bug4j.server.gwt.client.Bug4j;
import org.dandoy.bug4j.server.gwt.client.Bug4jService;
import org.dandoy.bug4j.server.gwt.client.data.Bug;
import org.dandoy.bug4j.server.gwt.client.data.BugDetailInitialData;
import org.dandoy.bug4j.server.gwt.client.data.BugHitAndStack;
import org.dandoy.bug4j.server.gwt.client.util.TextToLines;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@SuppressWarnings({"GWTStyleCheck"})
public class BugDetailView {

    private long _bugId;
    private final DisplaysBugs _displaysBugs;
    private Label _label;
    private HTML _stack;
    private Label _hitIdWidget;
    private Label _versionWidget;
    private Label _reportedWidget;
    private int _currentHit;
    private List<Long> _bugHitIds = Collections.emptyList();
    private DockLayoutPanel _widget;

    public BugDetailView(DisplaysBugs displaysBugs) {
        _displaysBugs = displaysBugs;
    }

    public Widget createWidget() {
        _widget = new DockLayoutPanel(Style.Unit.EM);

        _widget.addNorth(createBugHeader(), 7);
        _widget.addNorth(createHitHeader(), 6);
        _widget.add(buildStackPanel());
        return _widget;
    }

    public Widget createBugHeader() {
        final VerticalPanel verticalPanel = new VerticalPanel();

        {
            verticalPanel.add(buildToolbar());
        }

        {
            _label = new Label();
            _label.addStyleName("BugDetailView-title");
            verticalPanel.add(_label);
        }

        {
            final HorizontalPanel hitPickPanel = new HorizontalPanel();
            final Anchor prev = new Anchor("<");
            prev.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    whenPrev();
                }
            });
            prev.addStyleName("BugDetailView-nav-button");
            hitPickPanel.add(prev);

            final Anchor dotdot = new Anchor("...");
            dotdot.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    whenDotDot();
                }
            });
            dotdot.addStyleName("BugDetailView-nav-button");
            hitPickPanel.add(dotdot);

            final Anchor next = new Anchor(">");
            next.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    whenNext();
                }
            });
            next.addStyleName("BugDetailView-nav-button");
            hitPickPanel.add(next);

            verticalPanel.add(hitPickPanel);
        }

        final SimpleLayoutPanel simpleLayoutPanel = new SimpleLayoutPanel();
        simpleLayoutPanel.addStyleName("BugDetailView-bug-header");
        simpleLayoutPanel.add(verticalPanel);

        return simpleLayoutPanel;
    }

    public Widget createHitHeader() {

        final Label hitIdLabel = new Label("Hit#:");
        hitIdLabel.addStyleName("BugDetailView-hit-label");
        _hitIdWidget = new Label();
        _hitIdWidget.addStyleName("BugDetailView-hit-value");

        final Label versionLabel = new Label("Version:");
        versionLabel.addStyleName("BugDetailView-hit-label");
        _versionWidget = new Label();
        _versionWidget.addStyleName("BugDetailView-hit-value");

        final Label reportedLabel = new Label("Reported:");
        reportedLabel.addStyleName("BugDetailView-hit-label");
        _reportedWidget = new Label();
        _reportedWidget.addStyleName("BugDetailView-hit-value");

        final Grid grid = new Grid(3, 2);
        grid.addStyleName("BugDetailView-hit-grid");
        grid.setWidget(0, 0, hitIdLabel);
        grid.setWidget(0, 1, _hitIdWidget);
        grid.setWidget(1, 0, versionLabel);
        grid.setWidget(1, 1, _versionWidget);
        grid.setWidget(2, 0, reportedLabel);
        grid.setWidget(2, 1, _reportedWidget);

        final SimpleLayoutPanel simpleLayoutPanel = new SimpleLayoutPanel();
        simpleLayoutPanel.add(grid);
        simpleLayoutPanel.addStyleName("BugDetailView-hit-header");

        return simpleLayoutPanel;
    }

    private ScrollPanel buildStackPanel() {
        _stack = new HTML();
        _stack.addStyleName("BugDetailView-hit-stack");
        final ScrollPanel scrollPanel = new ScrollPanel(_stack);
        scrollPanel.addStyleName("BugDetailView-hit-stack-sp");
        return scrollPanel;
    }

    private void whenPrev() {
        if (_currentHit > 0) {
            setCurrentHit(_currentHit - 1);
        }
    }

    private void whenDotDot() {
    }

    private void whenNext() {
        if (_currentHit + 1 < _bugHitIds.size()) {
            setCurrentHit(_currentHit + 1);
        }
    }

    private void setCurrentHit(final int hit) {
        final long hitId = _bugHitIds.get(hit);
        Bug4jService.App.getInstance().getBugHitAndStack(hitId, new AsyncCallback<BugHitAndStack>() {
            @Override
            public void onFailure(Throwable caught) {
                Window.alert(caught.getMessage());
            }

            @Override
            public void onSuccess(BugHitAndStack bugHitAndStack) {
                _currentHit = hit;
                display(bugHitAndStack);
            }
        });
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
        Bug4jService.App.getInstance().deleteBug(_bugId, new AsyncCallback<Void>() {
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

    public void displayBug(final long bugId) {
        _bugId = bugId;

        Bug4jService.App.getInstance().getBugDetailInitialData(Bug4j.APP, bugId, new AsyncCallback<BugDetailInitialData>() {
            @Override
            public void onFailure(Throwable caught) {
                Window.alert(caught.getMessage());
            }

            @Override
            public void onSuccess(BugDetailInitialData result) {
                final Bug bug = result.getBug();
                final String s = bugId + "-" + bug.getTitle();
                _label.setText(s);
                _bugHitIds = result.getBugHitIds();
                _currentHit = 0;
                final BugHitAndStack lastBugHitAndStack = result.getLastBugHitAndStack();
                if (lastBugHitAndStack != null) {
                    display(lastBugHitAndStack);
                }
                _widget.forceLayout();
            }
        });
    }

    private void display(BugHitAndStack bugHitAndStack) {
        _hitIdWidget.setText(Integer.toString(_bugHitIds.size() - _currentHit));
        _versionWidget.setText(bugHitAndStack.getAppVer());

        final DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_MEDIUM);
        final long tReported = bugHitAndStack.getDateReported();
        final String dateReportedText = dateTimeFormat.format(new Date(tReported));
        _reportedWidget.setText(dateReportedText);

        final String stack = bugHitAndStack.getStack();
        render(stack);
    }

    private void render(final String stackText) {
        Bug4j.withAppPackages(new AsyncCallback<List<String>>() {
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