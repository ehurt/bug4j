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

package org.dandoy.bug4j.client;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.dandoy.bug4j.common.StackAnalyzer;
import org.dandoy.bug4j.common.TextToLines;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Bug4jTest {
    private static final Logger LOGGER = Logger.getLogger(Bug4jTest.class);

    @Test
    public void testLog4j() throws Exception {
        try {
            final FileInputStream fileInputStream = new FileInputStream("c:\\bogus");
            fileInputStream.close();
        } catch (IOException e) {
            LOGGER.error("Failed to do something", e);
        }

        try {
            doSomethingBad();
        } catch (Exception e) {
            LOGGER.error("Something else happened.", e);
        }

        LogManager.shutdown();
        Client.shutdown();
        Assert.assertEquals(2, Client.getReported());

    }

    @Test
    public void testUseDefaultExceptionHandler() throws Exception {
        Bug4jUncaughtExceptionHandler.install();

        final Thread thread = new Thread() {
            @Override
            public void run() {
                doSomethingBad();
            }
        };

        thread.start();
        thread.join();

        Client.shutdown();
        Assert.assertEquals(1, Client.getReported());
    }

    @Test
    public void testDirectCall() throws Exception {
        try {
            final FileInputStream fileInputStream = new FileInputStream("c:\\bogus");
            fileInputStream.close();
        } catch (IOException e) {
            Client.report("Failed to do something", e);
        }

        Client.shutdown();
        Assert.assertEquals(1, Client.getReported());
    }

    @Test
    public void testForceNew() throws Exception {
        final IllegalStateException e = new IllegalStateException("oh?");
        e.setStackTrace(new StackTraceElement[]{
                new StackTraceElement("org.dandoy.bug4j.someClass", "someMethod", "someClass.java", (int) (Math.random() * 10000)),
                new StackTraceElement("org.dandoy.bug4j.someClass", "someMethod", "someClass.java", (int) (Math.random() * 10000)),
                new StackTraceElement("org.dandoy.bug4j.someClass", "someMethod", "someClass.java", (int) (Math.random() * 10000)),
                new StackTraceElement("org.dandoy.bug4j.someClass", "someMethod", "someClass.java", (int) (Math.random() * 10000)),
                new StackTraceElement("org.dandoy.bug4j.someClass", "someMethod", "someClass.java", (int) (Math.random() * 10000)),
                new StackTraceElement("org.dandoy.bug4j.someClass", "someMethod", "someClass.java", (int) (Math.random() * 10000)),
                new StackTraceElement("org.dandoy.bug4j.someClass", "someMethod", "someClass.java", (int) (Math.random() * 10000)),
                new StackTraceElement("org.dandoy.bug4j.someClass", "someMethod", "someClass.java", (int) (Math.random() * 10000))
        });
        Client.report("Forcing a new exception", e);

        Client.shutdown();
        Assert.assertEquals(1, Client.getReported());
    }

    private void doSomethingBad() {
        try {
            doSomethingNotTooGood();
        } catch (Exception e) {
            throw new IllegalStateException("oh oh!", e);
        }
    }

    private void doSomethingNotTooGood() {
        throw new NullPointerException("aie!");
    }
}