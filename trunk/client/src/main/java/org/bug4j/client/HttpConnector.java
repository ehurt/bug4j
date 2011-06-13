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

package org.bug4j.client;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

class HttpConnector {
    final HttpClient _httpClient = new DefaultHttpClient();
    private Settings _settings;

    public HttpConnector(Settings settings) {
        _settings = settings;
    }

    private String send(String endpoint, String... nameValuePairs) {
        final NameValuePair[] args = new NameValuePair[nameValuePairs.length / 2];
        for (int i = 0; i < nameValuePairs.length; i += 2) {
            final String name = nameValuePairs[i];
            final String value = nameValuePairs[i + 1];
            args[i / 2] = new BasicNameValuePair(name, value);
        }
        return send(endpoint, args);
    }

    private String send(String endpoint, NameValuePair... nameValuePairs) {
        String ret = null;

        try {
            final List<NameValuePair> nameValuePairList = Arrays.asList(nameValuePairs);
            final UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(nameValuePairList, "UTF-8");
            final String serverUri = _settings.getServer();
            final HttpPost httpPost = new HttpPost(serverUri + endpoint);
            httpPost.setEntity(urlEncodedFormEntity);

            final HttpResponse httpResponse = _httpClient.execute(httpPost);
            final StatusLine statusLine = httpResponse.getStatusLine();
            final int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) {
                final HttpEntity entity = httpResponse.getEntity();
                ret = EntityUtils.toString(entity);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Error while contacting the server");
        }
        return ret;
    }

    public boolean reportHit(String hash) {
        final String response = send("/in",
                "a", _settings.getApplicationName(),
                "v", _settings.getApplicationVersion(),
                "h", hash
        );
        return response.equals("New");
    }

    public void reportBug(String message, String[] stackLines) {
        final String stackText = toText(stackLines);
        final String applicationName = _settings.getApplicationName();
        final String applicationVersion = _settings.getApplicationVersion();
        send("/bug",
                "a", applicationName,
                "v", applicationVersion,
                "m", message,
                "s", stackText
        );
    }

    private String toText(String[] stackLines) {
        final StringBuilder stringBuilder = new StringBuilder();
        for (String stackLine : stackLines) {
            stringBuilder.append(stackLine);
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }
}
