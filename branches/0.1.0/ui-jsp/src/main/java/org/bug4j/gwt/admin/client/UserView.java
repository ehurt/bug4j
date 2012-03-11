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

import com.google.gwt.cell.client.*;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.text.shared.AbstractSafeHtmlRenderer;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionChangeEvent;
import org.bug4j.gwt.admin.client.data.User;
import org.bug4j.gwt.common.client.AdvancedAsyncCallback;
import org.bug4j.gwt.common.client.BaseDialog;

import java.util.*;

/**
 */
public class UserView extends DockLayoutPanel {

    private Button _deleteButton;
    private MultiSelectionModel<User> _selectionModel;
    private CellTable<User> _cellTable;

    protected UserView() {
        super(Style.Unit.EM);
        final Widget buttonPanel = createButtonPanel();
        addNorth(buttonPanel, 2.5);

        _cellTable = createCellTable();

        final ScrollPanel scrollPanel = new ScrollPanel(_cellTable);
        {
            final Style style = scrollPanel.getElement().getStyle();
            style.setPadding(10, Style.Unit.PX);
        }

        add(scrollPanel);

        whenSelectionChanges();
    }

    private CellTable<User> createCellTable() {
        final CellTable<User> cellTable = new CellTable<User>();

        cellTable.setWidth("100%", true);
        final Label emptyTableWidget = new Label("No Users");
        emptyTableWidget.getElement().getStyle().setFontSize(20, Style.Unit.PT);
        cellTable.setEmptyTableWidget(emptyTableWidget);

        _selectionModel = new MultiSelectionModel<User>(new ProvidesKey<User>() {
            @Override
            public Object getKey(User user) {
                return user.getUserName();
            }
        });
        cellTable.setSelectionModel(_selectionModel, DefaultSelectionEventManager.<User>createCheckboxManager());
        _selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                whenSelectionChanges();
            }
        });

        {
            Column<User, Boolean> checkColumn = new Column<User, Boolean>(new CheckboxCell(true, false)) {
                @Override
                public Boolean getValue(User user) {
                    // Get the value from the selection model.
                    return _selectionModel.isSelected(user);
                }
            };
            cellTable.addColumn(checkColumn, SafeHtmlUtils.fromSafeConstant("<br/>"));
            cellTable.setColumnWidth(checkColumn, 40, Style.Unit.PX);
        }

        {   // Username
            final TextColumn<User> userNameColumn = new TextColumn<User>() {
                @Override
                public String getValue(User object) {
                    return object.getUserName();
                }
            };
            cellTable.addColumn(userNameColumn, "Username");
        }

        if (User.USER_EMAIL) {   // email
            final TextInputCell textInputCell = new TextInputCell();
            final Column<User, String> emailColumn = new Column<User, String>(textInputCell) {
                @Override
                public String getValue(User user) {
                    return user.getEmail();
                }
            };
            emailColumn.setFieldUpdater(new FieldUpdater<User, String>() {
                @Override
                public void update(int index, User user, String value) {
                    user.setEmail(value);
                    whenUserChanges(user);
                }
            });
            cellTable.addColumn(emailColumn, "Email");
        }

        if (User.SUPPORTS_LDAP) {   // Authentication
            final SelectionCell selectionCell = new SelectionCell(Arrays.asList("built-in", "ldap"));
            final Column<User, String> column = new Column<User, String>(selectionCell) {
                @Override
                public String getValue(User user) {
                    return user.isBuiltIn() ? "built-in" : "ldap";
                }
            };
            column.setFieldUpdater(new FieldUpdater<User, String>() {
                @Override
                public void update(int index, User user, String value) {
                    user.setBuiltIn("built-in".equals(value));
                    whenUserChanges(user);
                }
            });
            cellTable.addColumn(column, "Authentication");
        }

        {   // Admin
            Column<User, Boolean> column = new Column<User, Boolean>(new CheckboxCell(true, false)) {
                @Override
                public Boolean getValue(User user) {
                    return user.isAdmin();
                }
            };
            column.setFieldUpdater(new FieldUpdater<User, Boolean>() {
                @Override
                public void update(int index, User user, Boolean value) {
                    user.setAdmin(value);
                    whenUserChanges(user);
                }
            });
            cellTable.addColumn(column, "Administrator");
        }

        {   // Admin
            Column<User, Boolean> column = new Column<User, Boolean>(new CheckboxCell(true, false)) {
                @Override
                public Boolean getValue(User user) {
                    return user.isEnabled();
                }
            };
            column.setFieldUpdater(new FieldUpdater<User, Boolean>() {
                @Override
                public void update(int index, User user, Boolean value) {
                    user.setEnabled(value);
                    whenUserChanges(user);
                }
            });
            cellTable.addColumn(column, "Enabled");
        }

        {
            cellTable.addColumn(new TextColumn<User>() {
                @Override
                public String getValue(User object) {
                    final Long lastSignedIn = object.getLastSignedIn();
                    final String s;
                    if (lastSignedIn != null) {
                        s = DateTimeFormat
                                .getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_MEDIUM)
                                .format(new Date(lastSignedIn));
                    } else {
                        s = "";
                    }
                    return s;
                }
            }, "Last Signed In");
        }

        {
            final ClickableTextCell clickableTextCell = new ClickableTextCell(new AbstractSafeHtmlRenderer<String>() {
                @Override
                public SafeHtml render(String object) {
                    return new SafeHtmlBuilder()
                            .appendHtmlConstant("<a href=\"#\">Reset Password</a>")
                            .toSafeHtml();
                }
            });

            final Column<User, String> column = new Column<User, String>(clickableTextCell) {
                @Override
                public String getValue(User object) {
                    return "";
                }
            };

            column.setFieldUpdater(new FieldUpdater<User, String>() {
                @Override
                public void update(int index, User user, String value) {
                    whenResetPassword(user);
                }
            });

            cellTable.addColumn(column);

            cellTable.setColumnWidth(column, 300, Style.Unit.PX);
        }

        refreshData();
        return cellTable;
    }

    private void refreshData() {
        AdminService.App.getInstance().getUsers(new AdvancedAsyncCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> result) {
                _cellTable.setRowData(result);
            }
        });
    }

    private void whenUserChanges(User user) {
        AdminService.App.getInstance().updateUser(user, new AdvancedAsyncCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
            }
        });
    }

    private void whenSelectionChanges() {
        final Set<User> selectedSet = _selectionModel.getSelectedSet();
        _deleteButton.setEnabled(!selectedSet.isEmpty());
    }

    private Widget createButtonPanel() {
        final Grid grid = new Grid(1, 2);
        grid.setCellPadding(2);
        final Button newUserButton = new Button("New User");
        newUserButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                whenNewUser();
            }
        });
        grid.setWidget(0, 0, newUserButton);
        _deleteButton = new Button("Delete");
        _deleteButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                whenDelete();
            }
        });
        grid.setWidget(0, 1, _deleteButton);
        return grid;
    }

    private void whenNewUser() {
        final UserDialog userDialog = new UserDialog();
        userDialog.show();
        userDialog.addCloseHandler(new CloseHandler<PopupPanel>() {
            @Override
            public void onClose(CloseEvent<PopupPanel> popupPanelCloseEvent) {
                if (!popupPanelCloseEvent.isAutoClosed()) {
                    final User user = new User(
                            userDialog.getUserName(),
                            userDialog.getEmail(),
                            userDialog.isAdmin(),
                            userDialog.isBuiltInt(),
                            true,
                            null
                    );
                    final String password = userDialog.getPassword();
                    AdminService.App.getInstance().createUser(user, password, new AdvancedAsyncCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            refreshData();
                        }
                    });
                }
            }
        });
    }

    private void whenDelete() {
        final Set<User> selectedSet = _selectionModel.getSelectedSet();
        final HashSet<String> userNames = new HashSet<String>(selectedSet.size());
        for (User user : selectedSet) {
            userNames.add(user.getUserName());
        }
        AdminService.App.getInstance().deleteUsers(userNames, new AdvancedAsyncCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                refreshData();
            }
        });
    }

    private void whenResetPassword(User user) {
        AdminService.App.getInstance().resetPassword(user, new AdvancedAsyncCallback<String>() {
            @Override
            public void onSuccess(final String newPassword) {
                final BaseDialog baseDialog = new BaseDialog("Password Reset") {

                    @Override
                    protected Widget createContent() {
                        final Grid grid = new Grid(1, 2);
                        grid.setWidget(0, 0, new Label("New Password:"));
                        grid.setWidget(0, 1, new Label(newPassword));

                        return grid;
                    }

                    @Override
                    protected void whenOk() {
                        hide();
                    }

                    @Override
                    protected Widget createButtons() {
                        final Widget ret = super.createOkButton();
                        ret.setWidth("300px");
                        return ret;
                    }
                };
                baseDialog.show();

            }
        });
    }
}
