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

package org.bug4j.gwt.user.client.settings;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import org.bug4j.gwt.common.client.BaseDialog;
import org.bug4j.gwt.common.client.data.UserException;
import org.bug4j.gwt.user.client.Bug4jService;

/**
 */
public class UserDialog extends BaseDialog {

    private TextBox _oldPassword;
    private TextBox _newPassword;
    private TextBox _confirm;
    private Label _errorLabel;

    public UserDialog() {
        super("Settings");
    }

    @Override
    protected void whenOk() {
        final String newPassword = _newPassword.getText();
        final String confirm = _confirm.getText();
        if (!newPassword.equals(confirm)) {
            _confirm.setFocus(true);
            setError("Please confirm the password again");
            _confirm.setText("");
        } else {
            final String oldPassword = _oldPassword.getText();
            Bug4jService.App.getInstance().updatePassword(oldPassword, newPassword, new AsyncCallback<Void>() {
                @Override
                public void onFailure(Throwable caught) {
                    final String message = caught.getMessage();
                    setError(message);
                    if (caught instanceof UserException) {
                        final UserException userException = (UserException) caught;
                        final int type = userException.getType();
                        if (type == 1) {
                            _oldPassword.setText("");
                            _oldPassword.setFocus(true);
                        }
                    }
                }

                @Override
                public void onSuccess(Void result) {
                    hide(false);
                }
            });
        }
    }

    @Override
    protected Widget createContent() {
        _errorLabel = new Label("");
        _errorLabel.setStyleName("usersettings-error-label");
        _errorLabel.setVisible(false);

        _oldPassword = new PasswordTextBox();
        _newPassword = new PasswordTextBox();
        _confirm = new PasswordTextBox();

        final Grid grid = new Grid(3, 2);

        grid.setWidget(0, 0, new Label("Old password:"));
        grid.setWidget(0, 1, _oldPassword);

        grid.setWidget(1, 0, new Label("New password:"));
        grid.setWidget(1, 1, _newPassword);

        grid.setWidget(2, 0, new Label("Confirm new password:"));
        grid.setWidget(2, 1, _confirm);

        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                _oldPassword.setFocus(true);
            }
        });
        final FlowPanel flowPanel = new FlowPanel();
        flowPanel.add(_errorLabel);
        flowPanel.add(grid);
        return flowPanel;
    }

    private void setError(String error) {
        _errorLabel.setText(error);
        _errorLabel.setVisible(true);
    }
}
