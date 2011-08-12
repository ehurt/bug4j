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

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import org.bug4j.gwt.admin.client.data.User;

/**
 */
public class UserDialog extends DialogBox {

    private TextBox _userName = new TextBox();
    private TextBox _email = new TextBox();
    private ListBox _authentication = new ListBox();
    private CheckBox _administrator = new CheckBox();
    private final Label _error = new Label(" ");
    private final Label _password = new Label("...");

    public UserDialog() {
        _error.getElement().getStyle().setColor("red");

        final Grid grid = new Grid(5, 2);
        int pos = 0;

        grid.setWidget(pos, 0, new Label("Username:"));
        grid.setWidget(pos, 1, _userName);

        _userName.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                if (_email.getText().isEmpty()) {
                    final String username = _userName.getText();
                    if (username.contains("@")) {
                        _email.setText(username);
                    }
                }
            }
        });

        pos++;
        grid.setWidget(pos, 0, new Label("Password:"));
        grid.setWidget(pos, 1, _password);

        pos++;
        grid.setWidget(pos, 0, new Label("Email:"));
        grid.setWidget(pos, 1, _email);

        if (User.SUPPORTS_LDAP) {
            pos++;
            grid.setWidget(pos, 0, new Label("Authentication:"));
            _authentication.addItem("built-in");
            _authentication.addItem("ldap");
            grid.setWidget(pos, 1, _authentication);
        }

        pos++;
        grid.setWidget(pos, 0, new Label("Administrator:"));
        grid.setWidget(pos, 1, _administrator);

        final Button okButton = new Button("OK");
        okButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                whenOk();
            }
        });
        okButton.setWidth("75px");

        final Button cancelButton = new Button("Cancel");
        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                whenCancel();
            }
        });
        cancelButton.setWidth("75px");

        final FlowPanel buttonPanel = new FlowPanel();
        buttonPanel.add(wrapButton(cancelButton));
        buttonPanel.add(wrapButton(okButton));

        final FlowPanel content = new FlowPanel();
        content.add(_error);
        content.add(grid);
        content.add(buttonPanel);

        setWidget(content);

        setText("New User");
        center();

        AdminService.App.getInstance().getRandomPassword(new AsyncCallback<String>() {
            @Override
            public void onFailure(Throwable caught) {
                Window.alert(caught.getMessage());
            }

            @Override
            public void onSuccess(String result) {
                _password.setText(result);
            }
        });
    }

    private void whenOk() {
        if (_userName.getText().trim().isEmpty()) {
            _error.setText("Invalid Username");
        } else if (!_email.getText().contains("@")) {
            _error.setText("Invalid Email");
        } else {
            hide(false);
        }
    }

    private void whenCancel() {
        hide(true);
    }

    private Widget wrapButton(Button button) {
        final SimplePanel simplePanel = new SimplePanel(button);
        final Style style = simplePanel.getElement().getStyle();
        style.setFloat(Style.Float.RIGHT);
        style.setPadding(5, Style.Unit.PX);
        style.setPaddingTop(7, Style.Unit.PX);
        return simplePanel;
    }

    public String getUserName() {
        return _userName.getText();
    }

    public String getEmail() {
        return _email.getText();
    }

    public boolean isBuiltInt() {
        boolean ret = false;
        final int selectedIndex = _authentication.getSelectedIndex();
        if (selectedIndex >= 0) {
            final String itemText = _authentication.getItemText(selectedIndex);
            ret = "built-int".equals(itemText);
        }
        return ret;
    }

    public boolean isAdmin() {
        return _administrator.getValue();
    }

    public String getPassword() {
        return _password.getText();
    }

    @Override
    protected void onPreviewNativeEvent(Event.NativePreviewEvent event) {
        if (event.getTypeInt() == Event.ONKEYDOWN) {
            final NativeEvent nativeEvent = event.getNativeEvent();
            final Event event0 = Event.as(nativeEvent);
            final int keyCode = event0.getKeyCode();
            switch (keyCode) {
                case KeyCodes.KEY_ESCAPE:
                    whenCancel();
                    break;
                case KeyCodes.KEY_ENTER:
                    whenOk();
                    break;
            }
        }
    }
}
