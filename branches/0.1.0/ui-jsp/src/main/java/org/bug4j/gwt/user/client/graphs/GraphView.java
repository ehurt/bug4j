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

package org.bug4j.gwt.user.client.graphs;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.visualizations.corechart.LineChart;
import org.bug4j.gwt.user.client.BugModel;
import org.bug4j.gwt.user.client.event.ApplicationChangedEvent;
import org.bug4j.gwt.user.client.event.ApplicationChangedEventHandler;
import org.bug4j.gwt.user.client.event.BugListChanged;
import org.bug4j.gwt.user.client.event.BugListChangedHandler;

public abstract class GraphView {
    private SimpleLayoutPanel _simpleLayoutPanel;
    private final Label _anchor;
    protected final BugModel _bugModel;

    public GraphView(BugModel bugModel, String label) {
        _bugModel = bugModel;
        _anchor = createAnchor(label);

        final EventBus eventBus = _bugModel.getEventBus();
        eventBus.addHandler(ApplicationChangedEvent.TYPE, new ApplicationChangedEventHandler() {
            @Override
            public void onApplicationChanged(ApplicationChangedEvent event) {
                addOrReplaceGraph();
            }
        });
        eventBus.addHandler(BugListChanged.TYPE, new BugListChangedHandler() {
            @Override
            public void onBugListChanged(BugListChanged event) {
                addOrReplaceGraph();
            }
        });
    }

    public Label getAnchor() {
        return _anchor;
    }

    private Label createAnchor(String label) {
        final Label anchor = new Label(label);
        anchor.setStylePrimaryName("topGraphView-label");
        return anchor;
    }

    public final Widget createWidget() {
        return createLazyContent();
    }

    private Widget createLazyContent() {
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                addOrReplaceGraph();
            }
        };
        VisualizationUtils.loadVisualizationApi(runnable, LineChart.PACKAGE);

        _simpleLayoutPanel = new SimpleLayoutPanel();
        return _simpleLayoutPanel;
    }

    protected abstract void addOrReplaceGraph();

    protected final void setGraphWidget(Widget widget) {
        final Widget oldWidget = _simpleLayoutPanel.getWidget();
        if (oldWidget != null) {
            _simpleLayoutPanel.remove(oldWidget);
        }
        _simpleLayoutPanel.setWidget(widget);
    }
}
