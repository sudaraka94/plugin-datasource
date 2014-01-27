/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 * [2012] - [2013] Codenvy, S.A.
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.ide.ext.datasource.client.newdatasource.connector.postgres;

import com.codenvy.ide.api.notification.NotificationManager;
import com.codenvy.ide.dto.DtoFactory;
import com.codenvy.ide.ext.datasource.client.DatasourceManager;
import com.codenvy.ide.ext.datasource.client.newdatasource.NewDatasourceWizard;
import com.codenvy.ide.ext.datasource.client.newdatasource.connector.AbstractNewDatasourceConnectorPage;
import com.codenvy.ide.ext.datasource.shared.DatabaseConfigurationDTO;
import com.codenvy.ide.ext.datasource.shared.DatabaseType;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class PostgresDatasourceConnectorPage extends AbstractNewDatasourceConnectorPage implements
                                                                                       PostgresDatasourceConnectorView.ActionDelegate {
    protected PostgresDatasourceConnectorView view;
    protected DtoFactory                      dtoFactory;
    protected NotificationManager             notificationManager;

    @Inject
    public PostgresDatasourceConnectorPage(final PostgresDatasourceConnectorView view,
                                           final NotificationManager notificationManager,
                                           final DtoFactory dtoFactory,
                                           final DatasourceManager datasourceManager,
                                           final EventBus eventBus) {
        super("PostgreSQL", null, "postgres", datasourceManager, eventBus);
        this.view = view;
        this.notificationManager = notificationManager;
        this.dtoFactory = dtoFactory;
    }


    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
    }


    protected DatabaseConfigurationDTO getConfiguredDatabase() {
        String datasourceId = wizardContext.getData(NewDatasourceWizard.DATASOURCE_NAME);
        DatabaseConfigurationDTO result =
                                          dtoFactory.createDto(DatabaseConfigurationDTO.class)
                                                    .withDatabaseName(view.getDatabaseName())
                                                    .withHostname(view.getHostname()).withPort(view.getPort())
                                                    .withUsername(view.getUsername())
                                                    .withPassword(view.getPassword())
                                                    .withDatabaseType(DatabaseType.POSTGRES)
                                                    .withDatasourceId(datasourceId);
        return result;
    }
}
