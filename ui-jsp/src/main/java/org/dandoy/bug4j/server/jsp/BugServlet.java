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

package org.dandoy.bug4j.server.jsp;

import org.apache.log4j.Logger;
import org.dandoy.bug4j.server.store.Store;
import org.dandoy.bug4j.server.store.StoreFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class BugServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(BugServlet.class);

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doit(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doit(request, response);
    }

    private void doit(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("text/plain");
        final PrintWriter out = response.getWriter();
        final long bugid = report(request);
        out.print(bugid);
    }

    private long report(HttpServletRequest request) {
        long bugid;
        try {
            final String app = request.getParameter("a");
            final String version = request.getParameter("v");
            final String hash = request.getParameter("h");
            final String title = request.getParameter("t");
            final String message = request.getParameter("m");
            final String exceptionMessage = request.getParameter("e");
            final String stackText = request.getParameter("s");

            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(String.format("bug:%s-%s-%s-%s-%s-%s", app, version, hash, title, message, exceptionMessage));
            }

            final Store store = StoreFactory.getStore();
            bugid = store.report(app, version, hash, title, message, exceptionMessage, stackText);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            bugid = -1;
        }
        return bugid;
    }
}
