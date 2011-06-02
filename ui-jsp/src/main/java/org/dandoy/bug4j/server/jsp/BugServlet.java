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
import org.dandoy.bug4j.common.FullStackHashCalculator;
import org.dandoy.bug4j.common.StackAnalyzer;
import org.dandoy.bug4j.common.StackPathHashCalculator;
import org.dandoy.bug4j.server.gwt.client.data.Stack;
import org.dandoy.bug4j.server.gwt.client.data.Strain;
import org.dandoy.bug4j.server.gwt.client.util.TextToLines;
import org.dandoy.bug4j.server.store.Store;
import org.dandoy.bug4j.server.store.StoreFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

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
        try {
            final String app = request.getParameter("a");
            final String version = request.getParameter("v");
            final String message = request.getParameter("m");
            final String exceptionMessage = request.getParameter("e");
            final String stackText = request.getParameter("s");

            doit(app, version, stackText);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    static void doit(String app, String version, String stackText) {

        final Store store = StoreFactory.getStore();
        final List<String> stackLines = TextToLines.toList(stackText);

        final List<String> appPackages = store.getPackages(app);
        final StackAnalyzer stackAnalyzer = new StackAnalyzer();
        stackAnalyzer.setApplicationPackages(appPackages);
        final String fullHash = FullStackHashCalculator.getTextHash(stackLines);

        Stack stack = store.getStackByHash(app, fullHash);
        if (stack == null) {
            final String strainHash = StackPathHashCalculator.analyze(stackLines);
            Strain strain = store.getStrainByHash(app, strainHash);
            if (strain == null) {
                final String title = stackAnalyzer.analyze(stackLines);
                final long bugId = store.createBug(app, title);
                strain = store.createStrain(app, bugId, strainHash);
            }
            stack = store.createStack(app, strain.getBugId(), strain.getStrainId(), fullHash, stackText);
        }
        store.reportHitOnStack(app, version, stack);
    }
}
