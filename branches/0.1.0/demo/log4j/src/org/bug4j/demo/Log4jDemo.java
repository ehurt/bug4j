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

package org.bug4j.demo;

import org.apache.log4j.Logger;
import org.bug4j.client.Bug4jAgent;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This demo program will create an exception and report it through log4j.
 * bug4j will handle the error and report a hit to the server.
 * See log4j.properties
 */
public class Log4jDemo {
    private static final Logger LOGGER = Logger.getLogger(Log4jDemo.class);

    public static void main(String[] args) throws Exception {
        try {
            System.out.println("\n\nCausing an exception...\n\n");
            calling();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            // The bug reports are enqueued and consumbed by a background thread.
            // Since our demo application exits right away the bug has probably not been sent yet.
            // Using Bug4jAgent.shutdown() will wait until all bugs have been sent.
            Bug4jAgent.shutdown();
        }
    }

    private static void calling() {
        elvis();
    }

    private static void elvis() {
        try {
            // Create the exception.
            final InputStream inputStream = new FileInputStream("/is/anybody/home?");
            inputStream.close();
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
