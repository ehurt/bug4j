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

package org.dandoy.bug4j.server.gwt.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import org.dandoy.bug4j.server.gwt.client.admin.PackagesView;
import org.dandoy.bug4j.server.gwt.client.bugs.BugView;

/**
 * Entry point classes define <code>onModuleLoad()</code>
 */
public class Bug4j implements EntryPoint {

    public static final Resources IMAGES = GWT.create(Resources.class);

    /**
     * This is the entry point method.
     */
    public void onModuleLoad() {

        final DockLayoutPanel dockLayoutPanel = new DockLayoutPanel(Style.Unit.EM);
        dockLayoutPanel.addNorth(new HTML("Bug4J"), 2);

        final TabLayoutPanel tabLayoutPanel = new TabLayoutPanel(2, Style.Unit.EM);
        tabLayoutPanel.add(new BugView().createWidget(), "Bugs");
        tabLayoutPanel.add(new PackagesView("My Application").createWidget(), "Packages");
        dockLayoutPanel.add(tabLayoutPanel);

        RootLayoutPanel.get().add(dockLayoutPanel);
        final Element loadingElement = DOM.getElementById("loading");
        DOM.removeChild(DOM.getParent(loadingElement), loadingElement);
    }
}
