/*******************************************************************************
* Copyright (c) 2012-2014 Codenvy, S.A.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* Codenvy, S.A. - initial API and implementation
*******************************************************************************/
package com.codenvy.ide.ext.datasource.client.editdatasource;

/**
 * Interface for a factory of {@link EditDatasourcesPresenter}.
 * 
 * @author "Mickaël Leduque"
 */
public interface EditDatasourcesPresenterFactory {

    /**
     * Create an instance of {@link EditDatasourcesPresenter}.
     * 
     * @return an instance.
     */
    EditDatasourcesPresenter createEditDatasourcesPresenter();
}
