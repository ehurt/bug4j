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

package org.bug4j.server.gwt.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import org.bug4j.server.gwt.client.admin.PackagesView;
import org.bug4j.server.gwt.client.bugs.BugView;
import org.bug4j.server.gwt.client.bugs.HotBugsGraphView;

import java.util.List;

/**
 * Entry point classes define <code>onModuleLoad()</code>
 */
public class Bug4j implements EntryPoint {
    public static final String APP = "My Application";
    public static final Resources IMAGES = GWT.create(Resources.class);
    private static List<String> _appPackages;

    /**
     * This is the entry point method.
     */
    public void onModuleLoad() {

        final DockLayoutPanel dockLayoutPanel = new DockLayoutPanel(Style.Unit.PX);
        final Widget html = new HTML("<img src=\"../icons/splat.png\"/><span class=\"logo\">Bug4J</span>");
        dockLayoutPanel.addNorth(html, 55);

        final TabLayoutPanel tabLayoutPanel = new TabLayoutPanel(2, Style.Unit.EM);
        tabLayoutPanel.add(new BugView().createWidget(), "Bugs");
        tabLayoutPanel.add(new HotBugsGraphView().createWidget(), "Top Chart");
        tabLayoutPanel.add(new PackagesView().createWidget(), "Setup");

        dockLayoutPanel.add(tabLayoutPanel);

        RootLayoutPanel.get().add(dockLayoutPanel);
        final Element loadingElement = DOM.getElementById("loading");
        DOM.removeChild(DOM.getParent(loadingElement), loadingElement);
    }

    public static synchronized void withAppPackages(final AsyncCallback<List<String>> asyncCallback) {
        if (_appPackages == null) {
            Bug4jService.App.getInstance().getPackages(Bug4j.APP, new AsyncCallback<List<String>>() {
                @Override
                public void onFailure(Throwable caught) {
                    asyncCallback.onFailure(caught);
                }

                @Override
                public void onSuccess(List<String> result) {
                    _appPackages = result;
                    asyncCallback.onSuccess(_appPackages);
                }
            });
        } else {
            asyncCallback.onSuccess(_appPackages);
        }
    }

    public static synchronized void clearAppPackages() {
        _appPackages = null;
    }
}
