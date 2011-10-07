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

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import org.bug4j.gwt.user.client.BugModel;
import org.bug4j.gwt.user.client.data.Filter;
import org.bug4j.gwt.user.client.event.BugListChanged;
import org.bug4j.gwt.user.client.graphs.TopGraphView;

/**
 * The tabbed panel with bugs and graphs
 */
public class AllBugsView extends TabLayoutPanel {
    public AllBugsView(final BugModel bugModel, Filter filter) {
        super(2, Style.Unit.EM);

        final BugView bugView = new BugView(bugModel, filter);
        final TopGraphView topGraphView = new TopGraphView(bugModel);

        final Widget topGraphViewWidget = topGraphView.createWidget();
        add(bugView, "Bugs");
        add(topGraphViewWidget, "Charts");

        // Refresh the data when the tab selection changes
        addSelectionHandler(new SelectionHandler<Integer>() {
            @Override
            public void onSelection(SelectionEvent<Integer> integerSelectionEvent) {
                bugModel.getEventBus().fireEvent(new BugListChanged());
            }
        });

        // TODO: Find a better fix
        if (getUserAgent().contains("msie")) {
            // IE bug: The bug title does not appear (tested in ie8)
            selectTab(1);
            final Scheduler scheduler = Scheduler.get();
            scheduler.scheduleDeferred(new Scheduler.ScheduledCommand() {
                @Override
                public void execute() {
                    selectTab(0);
                }
            });
        }
    }

    private static native String getUserAgent() /*-{
        return navigator.userAgent.toLowerCase();
    }-*/;
}
