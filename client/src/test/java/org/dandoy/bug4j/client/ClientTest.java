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

import org.dandoy.bug4j.common.StackAnalyzer;
import org.dandoy.bug4j.common.TextToLines;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClientTest {

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
    public void testAnalyzer() throws Exception {
        try {
            doSomethingBad();
        } catch (Exception e) {
            e.printStackTrace();
            final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            final PrintStream printStream = new PrintStream(byteArrayOutputStream);
            e.printStackTrace(printStream);
            final String stackTrace = byteArrayOutputStream.toString();
            final String[] lines = TextToLines.toLines(stackTrace);
            final List<String> stackLines = Arrays.asList(lines);

            final StackAnalyzer stackAnalyzer = new StackAnalyzer();
            final String analyzed = stackAnalyzer.analyze(stackLines);
            System.out.println("analyzed = " + analyzed);
        }
    }
}