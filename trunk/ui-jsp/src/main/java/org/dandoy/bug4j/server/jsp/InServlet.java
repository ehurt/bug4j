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

public class InServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(InServlet.class);

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doit(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doit(request, response);
    }

    private void doit(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/plain");
        final PrintWriter out = response.getWriter();

        final String app = request.getParameter("a");
        final String version = request.getParameter("v");
        final String hash = request.getParameter("h");

        final Store store = StoreFactory.getStore();
        long bugId = -1;
        try {
            bugId = store.find(app, hash);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        if (bugId >= 0) {
            store.reportHit(bugId, version);
            out.print("Old");
        } else {
            out.print("New");
        }
    }
}
