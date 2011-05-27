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
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import org.dandoy.bug4j.server.gwt.client.data.BugDetail;

public class BugDetailView {

    private HTML _stack;

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

    public BugDetailView() {
    }

    public Widget getRootElement() {
        final Widget rootElement = _ourUiBinder.createAndBindUi(this);
        final DockLayoutPanel ret = new DockLayoutPanel(Style.Unit.EM);
        ret.addNorth(rootElement, 7);
        final TabLayoutPanel tabLayoutPanel = new TabLayoutPanel(2, Style.Unit.EM);
        _stack = new HTML();
        _stack.addStyleName("bug-detail-stack");
        tabLayoutPanel.add(_stack, "Stack Dump");
        tabLayoutPanel.add(new HTML("HITS"), "Hits");
        ret.add(tabLayoutPanel);
        return ret;
    }

    public void setBugDetail(BugDetail bugDetail) {
        _id.setInnerText(Long.toString(bugDetail.getId()));
        _title.setInnerText(bugDetail.getTitle());
        _message.setInnerText(bugDetail.getMessage());
        _exceptionMessage.setInnerText(bugDetail.getExceptionMessage());
        _stack.setText(bugDetail.getStackText());
    }
}