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

import org.apache.log4j.Logger;
import org.dandoy.bug4j.common.StackAnalyzer;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

public class ClientTest {
    private static final Logger LOGGER = Logger.getLogger(ClientTest.class);

    @Test
    public void test() throws Exception {
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

        Client.shutdown();
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

    @Test
    public void testAnalyzer() throws Exception {
        try {
            doSomethingBad();
        } catch (Exception e) {
            e.printStackTrace();
            final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            final PrintStream printStream = new PrintStream(byteArrayOutputStream);
            e.printStackTrace(printStream);
            final String stackTrace = byteArrayOutputStream.toString();
            final String[] lines = stackTrace.split("\r?\n");
            final List<String> stackLines = Arrays.asList(lines);

            final StackAnalyzer stackAnalyzer = new StackAnalyzer();
            final String analyzed = stackAnalyzer.analyze(stackLines);
            System.out.println("analyzed = " + analyzed);
        }
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
        // We have to start the client otherwise our shutdown might be enqueued before the exception
        Client.start();

        thread.start();
        thread.join();

        Client.shutdown();
    }
}