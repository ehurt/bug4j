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

import org.bug4j.server.store.Store;
import org.bug4j.server.store.StoreFactory;
import org.bug4j.server.store.TestingStore;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BugProcessorTest {
    private static final String APP = "My Application";
    private static final String APP_VERSION = "1.3";

    private Store _store;

    @Before
    public void setUp() throws Exception {
        _store = new TestingStore();
        StoreFactory.setStore(_store);
    }

    @After
    public void tearDown() throws Exception {
        _store.close();
    }

    @Test
    public void testDedupeByTitle_1() throws Exception {
        // Initial exception
        final long bug_0 = BugProcessor.process(APP, APP_VERSION, null, null, "" +
                "java.lang.IllegalStateException: oh oh!\n" +
                "\tat org.bug4j.SomeClass.someMethod(SomeClass.java:100)\n" +
                "\tat org.bug4j.SomeClass.someMethod(SomeClass.java:200)\n" +
                "Caused by: java.lang.IndexOutOfBoundsException: Index: 1, Size: 0\n" +
                "\tat org.bug4j.SomeClass.someMethod(SomeClass.java:300)\n" +
                "\tat org.bug4j.SomeClass.someMethod(SomeClass.java:400)\n" +
                "\t... 22 more");

        // Same IllegalStateException but with a different code path. Should match the first one
        final long bug_1 = BugProcessor.process(APP, APP_VERSION, null, null, "" +
                "java.lang.IllegalStateException: oh oh!\n" +
                "\tat org.bug4j.SomeClass.someMethod(SomeClass.java:100)\n" +
                "\tat org.bug4j.SomeClass.someOtherMethod(SomeClass.java:500)\n" +
                "Caused by: java.lang.IndexOutOfBoundsException: Index: 1, Size: 0\n" +
                "\tat org.bug4j.SomeClass.someMethod(SomeClass.java:300)\n" +
                "\tat org.bug4j.SomeClass.someMethod(SomeClass.java:400)\n" +
                "\t... 22 more");
        Assert.assertEquals(bug_0, bug_1);

        // Same IllegalStateException bug with a different cause. Should be a different bug
        final long bug_2 = BugProcessor.process(APP, APP_VERSION, null, null, "" +
                "java.lang.IllegalStateException: oh oh!\n" +
                "\tat org.bug4j.SomeClass.someMethod(SomeClass.java:100)\n" +
                "\tat org.bug4j.SomeClass.someOtherMethod(SomeClass.java:500)\n" +
                "Caused by: java.lang.NullPointerException\n" +
                "\tat org.bug4j.SomeClass.someMethod(SomeClass.java:300)\n" +
                "\tat org.bug4j.SomeClass.someMethod(SomeClass.java:400)\n" +
                "\t... 22 more");
        Assert.assertNotSame(bug_0, bug_2);
    }
}
