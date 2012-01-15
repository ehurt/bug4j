/*
 * Copyright 2012 Cedric Dandoy
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
import org.bug4j.server.processor.BugProcessor;
import org.bug4j.server.store.Store;
import org.bug4j.server.store.StoreFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.bug4j.common.ParamConstants.*;

public class BugServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(BugServlet.class);

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doit(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doit(request, response);
    }

    private void doit(HttpServletRequest request, HttpServletResponse response) {

        response.setContentType("text/plain");
        try {
            final String app = request.getParameter(PARAM_APPLICATION_NAME);
            final String message = request.getParameter(PARAM_MESSAGE);
            final long dateReported = System.currentTimeMillis();
            final String user = request.getParameter(PARAM_USER);
            final String stackText = request.getParameter(PARAM_STACK);
            final long sessionId = InServlet.getLongParameter(request, PARAM_SESSION_ID);

            final Store store = StoreFactory.getStore();
            BugProcessor.process(store, sessionId, app, message, dateReported, user, stackText);
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
