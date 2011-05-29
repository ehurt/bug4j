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

package org.dandoy.bug4j.server.gwt.client.admin;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import org.dandoy.bug4j.server.gwt.client.Bug4j;
import org.dandoy.bug4j.server.gwt.client.Bug4jService;

import java.util.List;

@SuppressWarnings({"GWTStyleCheck"})
public class PackagesView {

    private TextBox _textBox;
    private VerticalPanel _verticalPanel;

    public PackagesView() {
    }

    public Widget createWidget() {

        // vertical panel of packages
        _verticalPanel = new VerticalPanel();
        final ScrollPanel scrollPanel = new ScrollPanel(_verticalPanel);

        final DecoratorPanel decoratorPanel = new DecoratorPanel();
        final VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.add(new HTML("<H2>Packages</H2>"));
        verticalPanel.add(scrollPanel);
        decoratorPanel.add(verticalPanel);

        refreshList();

        return decoratorPanel;
    }

    private void refreshList() {
        final int widgetCount = _verticalPanel.getWidgetCount();
        for (int i = 0; i < widgetCount; i++) {
            _verticalPanel.remove(0);
        }

        Bug4jService.App.getInstance().getPackages(Bug4j.APP, new AsyncCallback<List<String>>() {
            @Override
            public void onFailure(Throwable caught) {
                Window.alert(caught.getMessage());
            }

            @Override
            public void onSuccess(List<String> appPackages) {
                for (final String appPackage : appPackages) {
                    final Widget packageBox = buildPackageBox(appPackage);
                    _verticalPanel.add(packageBox);
                }
                _verticalPanel.add(buildAddBox());
                _textBox.setFocus(true);
            }
        });
    }

    private Widget buildPackageBox(final String appPackage) {
        final HorizontalPanel ret;

        final PushButton deleteButton = new PushButton(new Image(Bug4j.IMAGES.delete()));
        deleteButton.setStylePrimaryName("pkg-button");
        deleteButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                whenDelete(appPackage);
            }
        });
        final Label label = new Label();
        label.setText(appPackage);
        label.addStyleName("app-pkg-name");

        ret = new HorizontalPanel();
        ret.add(deleteButton);
        ret.add(label);

        return ret;
    }

    private Widget buildAddBox() {
        final HorizontalPanel ret;

        _textBox = new TextBox();

        final PushButton addButton = new PushButton(new Image(Bug4j.IMAGES.add()));
        addButton.setStylePrimaryName("pkg-button");
        addButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                whenAdd();
            }
        });

        ret = new HorizontalPanel();
        ret.add(addButton);
        ret.add(_textBox);
        return ret;
    }

    private void whenAdd() {
        Bug4j.clearAppPackages();
        Bug4jService.App.getInstance().addPackage(Bug4j.APP, _textBox.getText(), new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                Window.alert(caught.getMessage());
            }

            @Override
            public void onSuccess(Void result) {
                refreshList();
            }
        });
    }

    private void whenDelete(String appPackage) {
        Bug4j.clearAppPackages();
        Bug4jService.App.getInstance().deletePackage(Bug4j.APP, appPackage, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                Window.alert(caught.getMessage());
            }

            @Override
            public void onSuccess(Void result) {
                refreshList();
            }
        });
    }
}
