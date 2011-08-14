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

package org.bug4j.server.jsp;

import org.apache.log4j.Logger;
import org.bug4j.server.migration.Migrator;
import org.bug4j.server.store.Store;
import org.bug4j.server.store.StoreFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

public class StartupServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(StartupServlet.class);

    @Override
    public void init(ServletConfig config) throws ServletException {
        final Migrator migrator = Migrator.getInstance();

        migrator.preOpenDB();

        try {
            StoreFactory.createJdbcStore();

            migrator.completeMigration();
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public void destroy() {
        final Store store = StoreFactory.getStore();
        store.close();
    }
}
