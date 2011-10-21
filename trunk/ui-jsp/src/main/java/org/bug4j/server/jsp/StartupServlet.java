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

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.bug4j.server.migration.Migrator;
import org.bug4j.server.store.Store;
import org.bug4j.server.store.StoreFactory;
import org.bug4j.server.store.jdbc.JdbcStore;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

public class StartupServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(StartupServlet.class);

    @Override
    public void init(ServletConfig config) throws ServletException {
        migrate();

        printUrlWhenReady();

        scheduleTasks();
    }

    @Override
    public void destroy() {
        final Store store = StoreFactory.getStore();
        store.close();
    }

    private void printUrlWhenReady() {
        new Thread() {
            @Override
            public void run() {
                try {
                    printURL();

                } catch (Exception ignored) {
                }
            }

        }.start();
    }

    /**
     * Prints the URL on standard output.
     * We may have been started on a different port or even deployed on a different path
     * so we ping ourselves to find out if the application has been started where we think
     * it is.
     * If we can't get http://localhost:8063/static/bug4j.css then we don't print anything.
     */
    private static void printURL() throws Exception {
        for (int i = 0; i < 10; i++) {
            Thread.sleep(500);
            if (ping()) {
                System.out.println("\n\nbug4j started on http://localhost:8063/");
                return;
            }
        }
    }

    private static boolean ping() throws Exception {
        boolean ret = false;
        final DefaultHttpClient httpClient = new DefaultHttpClient();
        final HttpGet request = new HttpGet("http://localhost:8063/static/bug4j.css");
        final HttpResponse httpResponse = httpClient.execute(request);
        final StatusLine statusLine = httpResponse.getStatusLine();
        final int statusCode = statusLine.getStatusCode();
        if (statusCode == 200) {
            ret = true;
        }
        return ret;
    }

    private static void migrate() {
        final Migrator migrator = Migrator.getInstance();

        migrator.preOpenDB();

        try {
            StoreFactory.createJdbcStore();

            migrator.postOpenDB();
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * Run tasks periodically.
     * I don't want to introduce a dependency on Quartz just yet
     */
    private void scheduleTasks() {
        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000 * 60); // Let the system fully startup before to run the scheduled tasks
                    //noinspection InfiniteLoopStatement
                    while (true) {
                        runSheduledTasks();
                        Thread.sleep(1000 * 60 * 60);
                    }
                } catch (InterruptedException ignored) {
                }
                LOGGER.info("Task scheduler exits");
            }
        }.start();
    }

    private void runSheduledTasks() {
        LOGGER.info("Running scheduled tasks");
        final JdbcStore jdbcStore = JdbcStore.getInstance();
        jdbcStore.updateExtinctStatus();
        LOGGER.info("Done with scheduled tasks");
    }
}
