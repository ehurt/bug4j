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

package org.bug4j.server.gwt.client.settings;

import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import org.bug4j.server.gwt.client.Bug4j;
import org.bug4j.server.gwt.client.Bug4jService;

import java.util.List;

/**
 */
public class ApplicationDialog extends BaseDialog {

    private ListBox _listBox;

    public ApplicationDialog(Bug4j bug4j) {
        super("Application", bug4j);
    }

    @Override
    protected Widget createContent() {
        _listBox = new ListBox();
        _listBox.setVisibleItemCount(2);
        Bug4jService.App.getInstance().getApplications(new AsyncCallback<List<String>>() {
            @Override
            public void onFailure(Throwable caught) {
                Window.alert(caught.getMessage());
            }

            @Override
            public void onSuccess(List<String> appNames) {
                _listBox.clear();
                _listBox.setVisibleItemCount(Math.max(2, appNames.size()));
                for (String appName : appNames) {
                    _listBox.addItem(appName);
                }
            }
        });
        final SimpleLayoutPanel simpleLayoutPanel = new SimpleLayoutPanel();
        simpleLayoutPanel.add(_listBox);
        simpleLayoutPanel.setHeight("300px");

        _listBox.addDoubleClickHandler(new DoubleClickHandler() {
            @Override
            public void onDoubleClick(DoubleClickEvent event) {
                whenOk();
            }
        });
        return simpleLayoutPanel;
    }

    @Override
    protected void whenOk() {
        final int selectedIndex = _listBox.getSelectedIndex();
        final String application = _listBox.getItemText(selectedIndex);
        Bug4jService.App.getInstance().setDefaultApplication(application, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                Window.alert(caught.getMessage());
            }

            @Override
            public void onSuccess(Void result) {
                final Bug4j bug4j = getBug4j();
                bug4j.setApplication(application);
                ApplicationDialog.this.hide(false);
            }
        });
    }
}
