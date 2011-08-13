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
import org.bug4j.gwt.user.client.Bug4j;
import org.bug4j.gwt.user.client.bugs.DisplaysBugs;

public class TopGraphView implements DisplaysBugs {
    private final Bug4j _bug4j;
    private SimpleLayoutPanel _simpleLayoutPanel;
    private GraphView _graphView;

    public TopGraphView(Bug4j bug4j) {
        _bug4j = bug4j;
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
                new HotBugsGraphView(_bug4j),
                new AllTimeBugsGraphView(_bug4j)
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

    @Override
    public void whenBugListChanges() {
        if (_graphView != null) {
            _graphView.whenBugListChanges();
        }
    }

    @Override
    public Bug4j getBug4J() {
        return _bug4j;
    }

    @Override
    public void redisplay() {
        if (_graphView != null) {
            _graphView.redisplay();
        }
    }
}
