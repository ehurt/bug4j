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

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.visualizations.corechart.LineChart;
import org.bug4j.gwt.user.client.Bug4j;
import org.bug4j.gwt.user.client.bugs.DisplaysBugs;
import org.bug4j.gwt.user.client.util.PropertyListener;
import org.jetbrains.annotations.Nullable;

public abstract class GraphView implements DisplaysBugs {
    protected final Bug4j _bug4j;
    private SimpleLayoutPanel _simpleLayoutPanel;
    private final Label _anchor;

    public GraphView(Bug4j bug4j, String label) {
        _bug4j = bug4j;
        _anchor = createAnchor(label);
        _bug4j.addPropertyListener(new PropertyListener<String>() {
            @Override
            public void propertyChanged(String key, @Nullable String value) {
                if (PropertyListener.APPLICATION.equals(key)) {
                    whenBugListChanges();
                }
            }
        });
    }

    @Override
    public final Bug4j getBug4J() {
        return _bug4j;
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

    @Override
    public final void redisplay() {
    }

    @Override
    public final void whenBugListChanges() {
        addOrReplaceGraph();
    }
}
