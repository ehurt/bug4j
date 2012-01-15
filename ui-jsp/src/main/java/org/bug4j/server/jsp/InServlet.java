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
import org.bug4j.gwt.user.client.data.Stack;
import org.bug4j.server.store.Store;
import org.bug4j.server.store.StoreFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static org.bug4j.common.ParamConstants.*;

public class InServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(InServlet.class);

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doit(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doit(request, response);
    }

    private void doit(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            response.setContentType("text/plain");
            final PrintWriter out = response.getWriter();

            final String app = request.getParameter(PARAM_APPLICATION_NAME);
            final String message = request.getParameter(PARAM_MESSAGE);
            final String user = request.getParameter(PARAM_USER);
            final String hash = request.getParameter(PARAM_HASH);
            final Long sessionId = getLongParameter(request, PARAM_SESSION_ID);

            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(String.format("in :%s-%s", app, hash));
            }

            final Store store = StoreFactory.getStore();
            if (store.doesAppExist(app)) {
                final Stack stack = store.getStackByHash(app, hash);
                if (stack != null) {
                    final long dateReported = System.currentTimeMillis();
                    store.reportHitOnStack(sessionId, message, dateReported, user, stack);
                    out.print("Old");
                } else {
                    out.print("New");
                }
            } else {
                response.sendError(403, "Unknown application");
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            response.sendError(500);
        }
    }

    static Long getLongParameter(HttpServletRequest request, String name) {
        Long ret = null;
        final String sessionValue = request.getParameter(name);
        if (sessionValue != null) {
            try {
                ret = Long.parseLong(sessionValue);
            } catch (NumberFormatException ignored) {
            }
        }
        return ret;
    }

    static Integer getIntParameter(HttpServletRequest request, String name) {
        Integer ret = null;
        final String sessionValue = request.getParameter(name);
        if (sessionValue != null) {
            try {
                ret = Integer.parseInt(sessionValue);
            } catch (NumberFormatException ignored) {
            }
        }
        return ret;
    }

    static boolean getBooleanParameter(HttpServletRequest request, String name) {
        boolean ret = false;
        final String sessionValue = request.getParameter(name);
        if ("Y".equals(sessionValue)) {
            ret = true;
        }
        return ret;
    }
}
