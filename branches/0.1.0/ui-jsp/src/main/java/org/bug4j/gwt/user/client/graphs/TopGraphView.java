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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import org.bug4j.gwt.user.client.BugModel;

public class TopGraphView {
    private SimpleLayoutPanel _simpleLayoutPanel;
    private GraphView _graphView;
    private final BugModel _bugModel;

    public TopGraphView(BugModel bugModel) {
        _bugModel = bugModel;
    }

    public Widget createWidget() {
        final SplitLayoutPanel ret = new SplitLayoutPanel();
        _simpleLayoutPanel = new SimpleLayoutPanel();
        ret.addWest(buildNavigationWidget(), 200);
        ret.add(_simpleLayoutPanel);
        return ret;
    }

    private Widget buildNavigationWidget() {
        final FlowPanel ret = new FlowPanel();
        final GraphView[] graphViews = {
                new HotBugsGraphView(_bugModel),
                new AllTimeBugsGraphView(_bugModel)
        };
        for (final GraphView graphView : graphViews) {
            final Label anchor = graphView.getAnchor();
            anchor.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    setView(graphView);
                }
            });
            ret.add(anchor);
        }
        setView(graphViews[0]);
        return ret;
    }

    private void setView(GraphView graphView) {
        if (_graphView != null) {
            final Label anchor = _graphView.getAnchor();
            anchor.setStyleDependentName("selected", false);
        }
        final Widget oldWidget = _simpleLayoutPanel.getWidget();
        if (oldWidget != null) {
            _simpleLayoutPanel.remove(oldWidget);
        }

        _graphView = graphView;

        if (_graphView != null) {
            final Widget widget = graphView.createWidget();
            _simpleLayoutPanel.setWidget(widget);
            final Label anchor = _graphView.getAnchor();
            anchor.setStyleDependentName("selected", true);
        }
    }
}
