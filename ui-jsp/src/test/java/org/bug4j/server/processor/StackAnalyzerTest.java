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

package org.bug4j.server.processor;

import org.apache.commons.io.IOUtils;
import org.bug4j.common.TextToLines;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 */
public class StackAnalyzerTest {

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

    @Test
    public void testTitle1() throws Exception {
        try {
            doSomethingBad();
        } catch (Exception e) {
            e.printStackTrace();
            final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            final PrintStream printStream = new PrintStream(byteArrayOutputStream);
            e.printStackTrace(printStream);
            final String stackTrace = byteArrayOutputStream.toString();
            final String[] lines = TextToLines.toLineArray(stackTrace);
            final List<String> stackLines = Arrays.asList(lines);

            final StackAnalyzer stackAnalyzer = new StackAnalyzer();
            final String title = stackAnalyzer.getTitle(stackLines);
            Assert.assertTrue(title.matches("IndexOutOfBoundsException at StackAnalyzerTest.java:[0-9]+"));
        }
    }

    @SuppressWarnings({"unchecked"})
    @Test
    public void testTitle2() throws IOException {

        final StackAnalyzer stackAnalyzer = new StackAnalyzer();
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final InputStream inputStream = classLoader.getResourceAsStream("org/bug4j/server/processor/StackAnalyzerTest.1.txt");
        try {
            final List lines = IOUtils.readLines(inputStream);
            final String title = stackAnalyzer.getTitle(lines);
            Assert.assertEquals("NullPointerException at QueryNodeDTO.java:106", title);
        } finally {
            inputStream.close();
        }
    }

    @Test
    public void testGetCause() throws Exception {
        try {
            doSomethingBad();
        } catch (Exception e) {
            e.printStackTrace();
            final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            final PrintStream printStream = new PrintStream(byteArrayOutputStream);
            e.printStackTrace(printStream);
            final String stackTrace = byteArrayOutputStream.toString();
            final String[] lines = TextToLines.toLineArray(stackTrace);
            final List<String> stackLines = Arrays.asList(lines);

            final StackAnalyzer stackAnalyzer = new StackAnalyzer();
            final List<String> causes = stackAnalyzer.getCauses(stackLines);
            Assert.assertEquals(2, causes.size());
            Assert.assertEquals("java.lang.IllegalStateException", causes.get(0));
            Assert.assertEquals("java.lang.IndexOutOfBoundsException", causes.get(1));
        }
    }
}