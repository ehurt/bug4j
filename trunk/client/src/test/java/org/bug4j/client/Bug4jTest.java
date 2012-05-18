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

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

/**
 * This is a test
 */
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

        try {
            doSomethingBad();
        } catch (Exception e) {
            LOGGER.warn("just a warning", e);
        }

        LogManager.shutdown();
        Bug4jAgent.shutdown();
        Assert.assertEquals(2, Bug4jAgent.getReported());
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

        Bug4jAgent.shutdown();
        Assert.assertEquals(1, Bug4jAgent.getReported());
    }

    @Test
    public void testDirectCall() throws Exception {
        try {
            final FileInputStream fileInputStream = new FileInputStream("c:\\bogus");
            fileInputStream.close();
        } catch (IOException e) {
            Bug4jAgent.report("Failed to do something", e);
        }

        Bug4jAgent.shutdown();
        Assert.assertEquals(1, Bug4jAgent.getReported());
    }

    @Test
    public void testDirectCallNoMessage() throws Exception {
        try {
            final FileInputStream fileInputStream = new FileInputStream("c:\\bogus");
            fileInputStream.close();
        } catch (IOException e) {
            Bug4jAgent.report(e);
        }

        Bug4jAgent.shutdown();
        Assert.assertEquals(1, Bug4jAgent.getReported());
    }

    @Test
    public void testForceNew() throws Exception {
        final IllegalStateException e = new IllegalStateException("oh, c 'est d\u00e9j\u00e0 cass\u00e9?");
        e.setStackTrace(new StackTraceElement[]{
                new StackTraceElement("org.bug4j.SomeClass", buildRandomMethodName(), "SomeClass.java", (int) (Math.random() * 10000)),
                new StackTraceElement("org.bug4j.SomeClass", buildRandomMethodName(), "SomeClass.java", (int) (Math.random() * 10000)),
                new StackTraceElement("org.bug4j.SomeClass", buildRandomMethodName(), "SomeClass.java", (int) (Math.random() * 10000)),
                new StackTraceElement("org.bug4j.SomeClass", buildRandomMethodName(), "SomeClass.java", (int) (Math.random() * 10000)),
                new StackTraceElement("org.bug4j.SomeClass", buildRandomMethodName(), "SomeClass.java", (int) (Math.random() * 10000)),
                new StackTraceElement("org.bug4j.SomeClass", buildRandomMethodName(), "SomeClass.java", (int) (Math.random() * 10000)),
                new StackTraceElement("org.bug4j.SomeClass", buildRandomMethodName(), "SomeClass.java", (int) (Math.random() * 10000)),
                new StackTraceElement("org.bug4j.SomeClass", buildRandomMethodName(), "SomeClass.java", (int) (Math.random() * 10000))
        });
        Bug4jAgent.report("Forcing a new exception", e);

        Bug4jAgent.shutdown();
        Assert.assertEquals(1, Bug4jAgent.getReported());
    }

    @Test
    public void testSameTitle() throws Exception {
        final Random random = new Random();
        {
            final IllegalStateException e = new IllegalStateException("SameTitle");
            e.setStackTrace(new StackTraceElement[]{
                    new StackTraceElement("org.bug4j.SomeClass", "someMethod", "SomeClass.java", 100),
                    new StackTraceElement("org.bug4j.SomeClass", "someMethod", "SomeClass.java", random.nextInt()),
                    new StackTraceElement("org.bug4j.SomeClass", "someMethod", "SomeClass.java", random.nextInt()),
                    new StackTraceElement("org.bug4j.SomeClass", "someMethod", "SomeClass.java", random.nextInt()),
            });
            Bug4jAgent.report("testSameTitle", e);
        }

        {
            final IllegalStateException e = new IllegalStateException("SameTitle");
            e.setStackTrace(new StackTraceElement[]{
                    new StackTraceElement("org.bug4j.SomeClass", "someMethod", "SomeClass.java", 100),
                    new StackTraceElement("org.bug4j.someOtherClass", "someOtherMethod", "SomeClass.java", random.nextInt()),
                    new StackTraceElement("org.bug4j.someOtherClass", "someOtherMethod", "SomeClass.java", random.nextInt()),
                    new StackTraceElement("org.bug4j.someOtherClass", "someMethod", "SomeClass.java", random.nextInt()),
            });
            Bug4jAgent.report("testSameTitle", e);
        }
        Bug4jAgent.shutdown();
        Assert.assertEquals(2, Bug4jAgent.getReported());
    }

    /**
     * Verifies that we don't report the same error twice
     */
    @Test
    public void testNoReReport() throws Exception {
        {
            final IllegalStateException e = new IllegalStateException("SameTitle");
            e.setStackTrace(new StackTraceElement[]{
                    new StackTraceElement("org.bug4j.SomeClass", "testSameTitle", "SomeClass.java", 100),
                    new StackTraceElement("org.bug4j.SomeClass", "testSameTitle", "SomeClass.java", 200),
                    new StackTraceElement("org.bug4j.SomeClass", "testSameTitle", "SomeClass.java", 300),
                    new StackTraceElement("org.bug4j.SomeClass", "testSameTitle", "SomeClass.java", 400),
            });
            for (int i = 0; i < 10; i++) {
                Bug4jAgent.report("testSameTitle", e);
            }
        }

        Bug4jAgent.shutdown();
        Assert.assertEquals(1, Bug4jAgent.getReported());
    }

    @Test
    public void testNoStack() throws Exception {
        Bug4jAgent.report("Just a message", null);
        Bug4jAgent.shutdown();
    }

    @Test
    public void testLongTitle() throws Exception {
        Bug4jAgent.report(StringUtils.repeat("This_is_a_long_title", 100), null);
        Bug4jAgent.shutdown();
    }

    @Test
    @Ignore
    public void testCreateMany() throws Exception {
        for (int i = 0; i < 1000; i++) {
            testForceNew();
        }
    }

    private String buildRandomMethodName() {
        final StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            final char c = (char) (Math.random() * 26 + 'a');
            stringBuilder.append(c);
        }
        return stringBuilder.toString();
    }

    private void doSomethingBad() {
        try {
            doSomethingNotTooGood();
        } catch (Exception e) {
            throw new IllegalStateException("oh oh!", e);
        }
    }

    private void doSomethingNotTooGood() {
        new ArrayList<String>().get(1);
    }
}