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

import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Enumeration;

public class Bug4jStarterTest {
    @Test
    public void testProperties() throws Exception {
        final Bug4jStarter bug4jStarter = new Bug4jStarter();
        final Settings settings = bug4jStarter.test_getSettings();
        Assert.assertEquals("http://127.0.0.1:8063/", settings.getServerUrl());
        Assert.assertEquals("bug4jDemo", settings.getApplicationName());
        Assert.assertEquals("1.0", settings.getApplicationVersion());
        Assert.assertEquals(new SimpleDateFormat("y/M/d H:m:s:S").parse("2012/01/09 10:06:03:177").getTime(), settings.getBuildDate());
        Assert.assertTrue(settings.isDevBuild());
        Assert.assertEquals(117, (int) settings.getBuildNumber());
        Assert.assertNull(settings.getProxyHost());
        Assert.assertEquals(80, settings.getProxyPort());
    }

    @Test
    public void testLog4j() throws Exception {
        final Logger logger = Logger.getRootLogger();
        final Enumeration allAppenders = logger.getAllAppenders();
        while (allAppenders.hasMoreElements()) {
            final Appender appender = (Appender) allAppenders.nextElement();
            if (appender instanceof Bug4jAppender) {
                final Bug4jAppender bug4jAppender = (Bug4jAppender) appender;
                final Bug4jStarter bug4jStarter = bug4jAppender.test_getBug4jStarter();
                final Settings settings = bug4jStarter.test_getSettings();
                Assert.assertEquals("http://127.0.0.1:8063/", settings.getServerUrl());
                Assert.assertEquals("bug4jDemo", settings.getApplicationName());
                Assert.assertEquals("1.0", settings.getApplicationVersion());
                Assert.assertEquals(new SimpleDateFormat("y/M/d H:m:s:S").parse("2012/01/09 10:07:03:177").getTime(), settings.getBuildDate());
                Assert.assertTrue(settings.isDevBuild());
                Assert.assertEquals(117, (int) settings.getBuildNumber());
                Assert.assertNull(settings.getProxyHost());
                Assert.assertEquals(80, settings.getProxyPort());
            }
        }
    }
}
