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

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Makes sure that files that contain "*.nocache.*" are not cached.
 */
public class NoCacheFilter implements Filter {
    public void destroy() {
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        final HttpServletRequest httpRequest = (HttpServletRequest) request;
        final String requestUri = httpRequest.getRequestURI();

        final HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (requestUri.contains(".nocache.")) {
            httpResponse.addHeader("Cache-Control", "no-cache");
        }

        chain.doFilter(request, response);
    }

    public void init(FilterConfig config) throws ServletException {
    }
}
