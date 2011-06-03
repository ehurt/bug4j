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
import org.bug4j.server.gwt.client.data.Stack;
import org.bug4j.server.store.Store;
import org.bug4j.server.store.StoreFactory;

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

        final String s = doit(app, version, hash);
        out.print(s);
    }

    static String doit(String app, String version, String hash) {
        final String ret;
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(String.format("in :%s-%s-%s", app, version, hash));
        }

        final Store store = StoreFactory.getStore();
        final Stack stack = store.getStackByHash(app, hash);
        if (stack != null) {
            store.reportHitOnStack(app, version, stack);
            ret = "Old";
        } else {
            ret = "New";
        }
        return ret;
    }
}
