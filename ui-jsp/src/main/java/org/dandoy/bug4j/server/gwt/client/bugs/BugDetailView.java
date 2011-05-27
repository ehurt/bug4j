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
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import org.dandoy.bug4j.server.gwt.client.Bug4j;
import org.dandoy.bug4j.server.gwt.client.Bug4jService;
import org.dandoy.bug4j.server.gwt.client.data.BugDetail;
import org.dandoy.bug4j.server.gwt.client.util.TextToLines;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"GWTStyleCheck"})
public class BugDetailView {

    private HTML _stack;
    private HitsView _hitsView;
    private long _bugId;
    private final BugView _bugView;

    interface BugDetailViewUiBinder extends UiBinder<HTMLPanel, BugDetailView> {
    }

    private static BugDetailViewUiBinder _ourUiBinder = GWT.create(BugDetailViewUiBinder.class);
    @UiField
    SpanElement _id;
    @UiField
    SpanElement _title;
    @UiField
    SpanElement _message;
    @UiField
    SpanElement _exceptionMessage;

    public BugDetailView(BugView bugView) {
        _bugView = bugView;
    }

    public Widget createWidget() {
        final Widget rootElement = _ourUiBinder.createAndBindUi(this);
        final DockLayoutPanel ret = new DockLayoutPanel(Style.Unit.EM);
        ret.addNorth(buildToolbar(), 2);
        ret.addNorth(rootElement, 7);
        final TabLayoutPanel tabLayoutPanel = new TabLayoutPanel(2, Style.Unit.EM);
        _stack = new HTML();
        _stack.addStyleName("bug-detail-stack");
        tabLayoutPanel.add(_stack, "Stack Dump");
        _hitsView = new HitsView();
        tabLayoutPanel.add(_hitsView.createWidget(), "Hits");
        ret.add(tabLayoutPanel);
        return ret;
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
                _bugView.whenBugListChanges();
            }
        });
    }

    public void setBugDetail(BugDetail bugDetail) {
        _bugId = bugDetail.getId();
        _id.setInnerText(Long.toString(_bugId));
        _title.setInnerText(bugDetail.getTitle());
        _message.setInnerText(bugDetail.getMessage());
        _exceptionMessage.setInnerText(bugDetail.getExceptionMessage());
        final String stackText = bugDetail.getStackText();
        render(stackText);
        _hitsView.setBug(_bugId);
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


        final String[] lines = TextToLines.toLines(stackText);
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