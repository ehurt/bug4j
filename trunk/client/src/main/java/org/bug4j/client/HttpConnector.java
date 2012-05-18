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

package org.bug4j.client;

import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.bug4j.common.ParamConstants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class HttpConnector {

    final HttpClient _httpClient;
    private final String _serverUri;
    private final String _applicationName;
    private final String _applicationVersion;
    private final long _buildDate;
    private final boolean _devBuild;
    private final Integer _buildNumber;
    private long _sessionId;

    private HttpConnector(String serverUrl, String proxyHost, int proxyPort, String applicationName, String applicationVersion, long buildDate, boolean devBuild, Integer buildNumber) {
        serverUrl = serverUrl.trim();
        if (!serverUrl.endsWith("/")) {
            serverUrl = serverUrl + "/";
        }
        _serverUri = serverUrl;
        _applicationName = applicationName;
        _applicationVersion = applicationVersion;
        _buildDate = buildDate;
        _devBuild = devBuild;
        _buildNumber = buildNumber;
        _httpClient = new DefaultHttpClient();
        if (proxyHost != null) {
            final HttpHost proxyHttpHost = new HttpHost(proxyHost, proxyPort);
            final HttpParams httpClientParams = _httpClient.getParams();
            httpClientParams.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxyHttpHost);
        }
    }

    public static HttpConnector createHttpConnector(String serverUrl, String proxyHost, int proxyPort, String applicationName, String applicationVersion, long buildDate, boolean devBuild, Integer buildNumber) {
        final HttpConnector httpConnector = new HttpConnector(
                serverUrl, proxyHost, proxyPort,
                applicationName, applicationVersion,
                buildDate, devBuild, buildNumber);
        httpConnector.createSession();

        return httpConnector;
    }

    private String send(String endpoint, String... nameValuePairs) {
        final List<NameValuePair> nameValuePairList = new ArrayList<NameValuePair>(nameValuePairs.length / 2);
        for (int i = 0; i < nameValuePairs.length; i += 2) {
            final String name = nameValuePairs[i];
            final String value = nameValuePairs[i + 1];
            if (value != null) {
                final BasicNameValuePair basicNameValuePair = new BasicNameValuePair(name, value);
                nameValuePairList.add(basicNameValuePair);
            }
        }
        return send(endpoint, nameValuePairList);
    }

    private String send(String endpoint, List<NameValuePair> nameValuePairs) {
        String ret = null;

        try {
            final UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(nameValuePairs, "UTF-8");
            final HttpPost httpPost = new HttpPost(_serverUri + endpoint);
            httpPost.setEntity(urlEncodedFormEntity);

            final HttpResponse httpResponse = _httpClient.execute(httpPost);
            final StatusLine statusLine = httpResponse.getStatusLine();
            final int statusCode = statusLine.getStatusCode();
            final HttpEntity entity = httpResponse.getEntity();
            final String response = EntityUtils.toString(entity);
            if (statusCode == 200) {
                ret = response;
            } else {
                System.err.println(response);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Error while contacting the server");
        }
        return ret;
    }

    public boolean reportHit(String message, String user, String hash) {
        final String response = send("br/in",
                ParamConstants.PARAM_SESSION_ID, Long.toString(_sessionId),
                ParamConstants.PARAM_APPLICATION_NAME, _applicationName,
                ParamConstants.PARAM_MESSAGE, message,
                ParamConstants.PARAM_USER, user,
                ParamConstants.PARAM_HASH, hash
        );
        return "New".equals(response);
    }

    public void reportBug(String message, String user, String[] stackLines) {
        final String stackText = toText(stackLines);
        send("br/bug",
                ParamConstants.PARAM_SESSION_ID, Long.toString(_sessionId),
                ParamConstants.PARAM_APPLICATION_NAME, _applicationName,
                ParamConstants.PARAM_MESSAGE, message,
                ParamConstants.PARAM_USER, user,
                ParamConstants.PARAM_STACK, stackText
        );
    }

    private String toText(String[] stackLines) {
        String ret = null;
        if (stackLines != null) {
            final StringBuilder stringBuilder = new StringBuilder();
            for (String stackLine : stackLines) {
                stringBuilder.append(stackLine);
                stringBuilder.append("\n");
            }
            ret = stringBuilder.toString();
        }
        return ret;
    }

    private void createSession() {
        final String response = send("br/ses"
                , ParamConstants.PARAM_APPLICATION_NAME, _applicationName
                , ParamConstants.PARAM_APPLICATION_VERSION, _applicationVersion
                , ParamConstants.PARAM_BUILD_DATE, Long.toString(_buildDate)
                , ParamConstants.PARAM_DEV_BUILD, _devBuild ? "Y" : null
                , ParamConstants.PARAM_BUILD_NUMBER, _buildNumber == null ? null : Integer.toString(_buildNumber)
        );
        if (response != null) {
            try {
                _sessionId = Long.parseLong(response.trim());
            } catch (NumberFormatException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }
}
