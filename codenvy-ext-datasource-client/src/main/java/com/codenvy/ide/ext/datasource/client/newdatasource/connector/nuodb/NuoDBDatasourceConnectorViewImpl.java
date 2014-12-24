/*******************************************************************************
 * Copyright (c) 2012-2014 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.ext.datasource.client.newdatasource.connector.nuodb;

import java.util.Set;

import com.codenvy.ide.ext.datasource.client.newdatasource.NewDatasourceWizardMessages;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextInputCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.TextHeader;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.ProvidesKey;
import com.google.inject.Inject;

public class NuoDBDatasourceConnectorViewImpl extends Composite implements NuoDBDatasourceConnectorView {


    private static final String         TEXT_BOX_STYLE       = "gwt-TextBox";

    @UiField(provided = true)
    DataGrid<NuoDBBroker>               brokerList;

    @UiField
    Button                              addBrokerButton;

    @UiField
    Button                              deleteBrokersButton;

    @UiField
    TextBox                             dbName;

    @UiField
    TextBox                             usernameField;

    @UiField
    TextBox                             passwordField;

    @UiField
    RadioButton                         radioUserPref;

    @UiField
    RadioButton                         radioProject;

    @UiField
    ListBox                             projectsList;

    @UiField
    Button                              testConnectionButton;

    private ActionDelegate              delegate;
    private NuoActionDelegate           nuoDelegate;

    private NewDatasourceWizardMessages messages;

    protected String                    encryptedPassword;

    protected boolean                   passwordFieldIsDirty = false;

    protected Long                      runnerProcessId;


    @Inject
    public NuoDBDatasourceConnectorViewImpl(final NuoDBDatasourceViewImplUiBinder uiBinder,
                                            final DataGridResourcesInvisible dataGridResources,
                                            final NewDatasourceWizardMessages messages) {
        this.messages = messages;
        ProvidesKey<NuoDBBroker> keyProvider = new ProvidesKey<NuoDBBroker>() {
            @Override
            public Object getKey(final NuoDBBroker item) {
                return item.getId();
            }
        };
        brokerList = new DataGrid<NuoDBBroker>(20, dataGridResources, keyProvider);
        initWidget(uiBinder.createAndBindUi(this));

        // first column : host
        final TextInputCell hostCell = new StyledTextInputCell();
        Column<NuoDBBroker, String> hostColumn = new Column<NuoDBBroker, String>(hostCell) {
            @Override
            public String getValue(final NuoDBBroker broker) {
                return broker.getHost();
            }
        };
        hostColumn.setFieldUpdater(new FieldUpdater<NuoDBBroker, String>() {
            @Override
            public void update(final int index, final NuoDBBroker broker, final String value) {
                // update host value in model
                broker.setHost(value);
            }
        });

        brokerList.addColumn(hostColumn, new TextHeader("Host"));

        // second column : port
        final TextInputCell portCell = new StyledNumberInputCell();
        Column<NuoDBBroker, String> portColumn = new Column<NuoDBBroker, String>(portCell) {
            @Override
            public String getValue(final NuoDBBroker broker) {
                if (broker.getPort() != null) {
                    return Integer.toString(broker.getPort());
                } else {
                    return "";
                }
            }
        };
        portColumn.setFieldUpdater(new FieldUpdater<NuoDBBroker, String>() {
            @Override
            public void update(final int index, final NuoDBBroker broker, final String value) {
                try {
                    // update port value in model
                    int port = Integer.parseInt(value);
                    broker.setPort(port);
                } catch (final NumberFormatException e) {
                    // invalid input, cancel change
                    broker.setPort(null);
                    portCell.clearViewData(broker.getId());
                }
                brokerList.redraw();
            }
        });

        brokerList.addColumn(portColumn, new TextHeader("Port"));

        // manage selection
        final MultiSelectionModel<NuoDBBroker> selectionModel = new MultiSelectionModel<>(keyProvider);
        brokerList.setSelectionModel(selectionModel);

        radioUserPref.setValue(true);
        radioProject.setEnabled(false);
        projectsList.setEnabled(false);
        projectsList.setWidth("100px");

    }

    @Override
    public void setNuoDelegate(final NuoActionDelegate delegate) {
        nuoDelegate = delegate;
    }

    @Override
    public void setDelegate(final ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getDatabaseName() {
        return dbName.getText();
    }

    @Override
    public void bindBrokerList(final ListDataProvider<NuoDBBroker> dataProvider) {
        dataProvider.addDataDisplay(brokerList);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<NuoDBBroker> getBrokerSelection() {
        return ((MultiSelectionModel<NuoDBBroker>)brokerList.getSelectionModel()).getSelectedSet();
    }

    @Override
    public String getUsername() {
        return usernameField.getText();
    }

    @Override
    public String getPassword() {
        return passwordField.getText();
    }

    @Override
    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    @Override
    public void setDatabaseName(final String databaseName) {
        dbName.setValue(databaseName);
    }

    @Override
    public void setUsername(final String username) {
        usernameField.setValue(username);
    }

    @Override
    public void setPassword(final String password) {
        passwordField.setValue(password);
    }

    @UiHandler("testConnectionButton")
    void handleTestConnectionClick(final ClickEvent e) {
        delegate.onClickTestConnectionButton();
    }

    @UiHandler("addBrokerButton")
    void handleAddBrokerClick(final ClickEvent e) {
        nuoDelegate.onAddBroker();
    }

    @UiHandler("deleteBrokersButton")
    void handleDeleteBrokersClick(final ClickEvent e) {
        nuoDelegate.onDeleteBrokers();
    }

    private static class StyledTextInputCell extends TextInputCell {

        private static Template template = GWT.create(Template.class);

        @Override
        public void render(Context context, String value, SafeHtmlBuilder sb) {
            // Get the view data.
            Object key = context.getKey();
            ViewData viewData = getViewData(key);
            if (viewData != null && viewData.getCurrentValue().equals(value)) {
                clearViewData(key);
                viewData = null;
            }

            String s = (viewData != null) ? viewData.getCurrentValue() : value;
            if (s != null) {
                sb.append(template.input(s, TEXT_BOX_STYLE));
            } else {
                sb.appendHtmlConstant("<input type=\"text\" tabindex=\"-1\"></input>");
            }
        }
    }

    private static class StyledNumberInputCell extends StyledTextInputCell {
        private static final int ZERO = 48;
        private static final int NINE = 57;

        private static final int ZERO_NUM_LOCK = 96;
        private static final int NINE_NUM_LOCK = 105;

        private static final int BACK = 8;
        private static final int DEL = 46;

        private static final int INSERT = 45;
        private static final int ENTER = 13;
        private static final int WINDOWS = 27;

        private static final int LEFT_ARROW = 37;
        private static final int DOWN_ARROW = 40;

        @Override
        public void onBrowserEvent(Context context, Element parent, String value, NativeEvent event, ValueUpdater<String> valueUpdater) {
            if (event.getType().equals("keyup") || event.getType().equals("keydown")) {

                if(isSpecialKey(event) || isShiftInsertOrCtrlInsert(event)) {
                    return;
                }

                if (isNotDigitAndArrowKey(event) || isDigitKeyWithPressedShift(event)) {
                    event.preventDefault();
                }
            }
            super.onBrowserEvent(context, parent, value, event, valueUpdater);
        }

        private boolean isSpecialKey(NativeEvent e) {
            return e.getCtrlKey() || e.getMetaKey() || e.getKeyCode() == BACK || e.getKeyCode() == DEL;
        }

        private boolean isShiftInsertOrCtrlInsert(NativeEvent e) {
            return (e.getShiftKey() && e.getKeyCode() == INSERT) || (e.getCtrlKey() && e.getKeyCode() == INSERT);
        }

        private boolean isDigitKeyWithPressedShift(NativeEvent e) {
            return ((e.getKeyCode() >= ZERO && e.getKeyCode() <= NINE) || (e.getKeyCode() < ZERO_NUM_LOCK && e.getKeyCode() > NINE_NUM_LOCK))
                    && e.getShiftKey();
        }

        private boolean isNotDigitAndArrowKey(NativeEvent event) {
            return (event.getKeyCode() < ZERO || event.getKeyCode() > NINE) &&
                    (event.getKeyCode() < ZERO_NUM_LOCK || event.getKeyCode() > NINE_NUM_LOCK) &&
                    (event.getKeyCode() > DOWN_ARROW || event.getKeyCode() < LEFT_ARROW) &&
                    event.getKeyCode() != WINDOWS || event.getKeyCode() == INSERT ||
                    event.getKeyCode() == ENTER;
        }
    }

    interface NuoDBDatasourceViewImplUiBinder extends UiBinder<Widget, NuoDBDatasourceConnectorViewImpl> {
    }

    interface Template extends SafeHtmlTemplates {
        @Template("<input type=\"text\" value=\"{0}\" tabindex=\"-1\" class='{1}'></input>")
        SafeHtml input(final String value, final String className);
    }

    @Override
    public void onTestConnectionFailure(String errorMessage) {
        Window.alert(errorMessage);
    }

    @Override
    public void onTestConnectionSuccess() {
        Window.alert(messages.connectionTestSuccessMessage());
    }

    @Override
    public void setEncryptedPassword(String encryptedPassword, boolean resetPasswordField) {
        this.encryptedPassword = encryptedPassword;
        passwordFieldIsDirty = false;
        if (resetPasswordField) {
            passwordField.setText("");
        }
    }

    @UiHandler("passwordField")
    public void handlePasswordFieldChanges(ChangeEvent event) {
        passwordFieldIsDirty = true;
    }

    @Override
    public boolean isPasswordFieldDirty() {
        return passwordFieldIsDirty;
    }

    @Override
    public Long getRunnerProcessId() {
        return runnerProcessId;
    }

    @Override
    public void setRunnerProcessId(Long runnerProcessId) {
        this.runnerProcessId = runnerProcessId;
    }

}
