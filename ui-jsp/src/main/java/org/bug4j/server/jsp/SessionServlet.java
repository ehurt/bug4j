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
import org.bug4j.server.store.Store;
import org.bug4j.server.store.StoreFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class SessionServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(SessionServlet.class);

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doit(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doit(request, response);
    }

    private void doit(HttpServletRequest request, HttpServletResponse response) {

        response.setContentType("text/plain");
        try {
            final String app = request.getParameter("a");
            final String version = request.getParameter("v");
//            final String user = request.getParameter("u");
            final long now = System.currentTimeMillis();
            final String remoteAddr = request.getRemoteAddr();

            final Store store = StoreFactory.getStore();
            if (store.doesAppExist(app)) {
                final long sessionId = store.createSession(app, version, now, remoteAddr);
                final PrintWriter writer = response.getWriter();
                writer.println(sessionId);
            } else {
                response.sendError(403, "Unknown application");
            }
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
