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

package org.bug4j.demo;

import org.bug4j.client.Bug4jAgent;

import java.util.ArrayList;

/**
 * This class demonstrates how to manually call the bug4j agent.
 */
public class DemoManual {
    public static void main(String[] args) throws InterruptedException {
        try {
            System.out.println("\n\nCausing an exception...\n\n");
            foo(); // foo() will of course throw an exception

        } catch (Exception e) {
            e.printStackTrace();
            Bug4jAgent.report("Just pretending something failed in class DemoManual", e);
        }

        // The bug reports are enqueued and consumbed by a background thread.
        // Since our demo application exits right away the bug has probably not been sent yet.
        // Using Bug4jAgent.shutdown() will wait until all bugs have been sent.
        Bug4jAgent.shutdown();
    }

    private static void foo() {
        bar();
    }

    private static void bar() {
        final ArrayList<String> arrayList = new ArrayList<String>();
        boom(arrayList);
    }

    private static void boom(ArrayList<String> strings) {
        // Force a IndexOutOfBoundsException
        strings.get(1);
    }
}
