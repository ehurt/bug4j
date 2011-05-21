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

package org.dandoy.test.bug4j;

import org.apache.log4j.Logger;
import org.dandoy.bug4j.client.Client;

/**
 */
public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class);

    public static void main(String[] args) throws InterruptedException {
        Client.start(); // The client will eventually start automatically but if it starts after we call shutdown() then we will exit prematurely

        try {
            doit();
        } catch (Exception e) {
            LOGGER.error("Testing ... 123 ...", e);
        }

        Client.shutdown();
    }

    private static void doit() {
        throw new IllegalStateException("This is a test");
    }
}
