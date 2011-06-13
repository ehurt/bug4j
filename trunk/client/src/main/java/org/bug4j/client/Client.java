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

@Deprecated
public class Client {
    private Client() {
    }

    public static synchronized void start() {
        Bug4jAgent.start();
    }

    public static synchronized void start(Settings settings) {
        Bug4jAgent.start(settings);
    }

    public synchronized static void shutdown() {
        Bug4jAgent.shutdown();
    }

    public static void report(String message, Throwable throwable) {
        Bug4jAgent.report(message, throwable);
    }

    public static void enqueue(ReportableEvent reportableEvent) {
        Bug4jAgent.enqueue(reportableEvent);
    }

    public static int getReported() {
        return Bug4jAgent.getReported();
    }
}
