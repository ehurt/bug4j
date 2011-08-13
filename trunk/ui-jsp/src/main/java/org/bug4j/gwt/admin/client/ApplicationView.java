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

package org.bug4j.gwt.admin.client;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import org.bug4j.gwt.admin.client.app.ApplicationPackagesView;

/**
 */
public class ApplicationView extends AdminView {
    private final String _applicationName;

    protected ApplicationView(String applicationName) {
        super(applicationName);
        _applicationName = applicationName;
    }

    public String getApplicationName() {
        return _applicationName;
    }

    @Override
    protected Label createLabel(String labelText) {
        final Label ret = super.createLabel(labelText);
        ret.addStyleName("admin-label-app");
        return ret;
    }

    @Override
    protected Widget createWidget() {
        final Label applicationLabel;
        final TabLayoutPanel tabPanel;
        {
            applicationLabel = new Label(_applicationName);
            applicationLabel.setStyleName("admin-appview-label");
        }

        {
            tabPanel = new TabLayoutPanel(2, Style.Unit.EM);
            tabPanel.add(new ApplicationPackagesView(this).createWidget(), "Packages");
        }

        final DockLayoutPanel dockLayoutPanel;
        {
            dockLayoutPanel = new DockLayoutPanel(Style.Unit.EM);
            dockLayoutPanel.addNorth(applicationLabel, 2.5);
            dockLayoutPanel.add(tabPanel);
        }
        return dockLayoutPanel;
    }
}
