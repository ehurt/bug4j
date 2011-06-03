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

package org.bug4j.server.jsp;

import org.bug4j.common.FullStackHashCalculator;
import org.bug4j.server.gwt.client.data.Bug;
import org.bug4j.server.gwt.client.data.BugHit;
import org.bug4j.server.gwt.client.util.TextToLines;
import org.bug4j.server.store.Store;
import org.bug4j.server.store.StoreFactory;
import org.bug4j.server.store.TestingStore;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class ServletsTest {
    private static final String STACK_TEXT = "java.lang.IllegalStateException: java.lang.IndexOutOfBoundsException: Index: 1, Size: 0\n" +
            "	at org.dandoy.Test0.dothis(Test0.java:44)\n" +
            "	at org.dandoy.Test0.testX(Test0.java:37)\n" +
            "	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n" +
            "	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)\n" +
            "	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)\n" +
            "	at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:44)\n" +
            "	at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:15)\n" +
            "	at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:41)\n" +
            "	at org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:20)\n" +
            "	at org.junit.runners.BlockJUnit4ClassRunner.runNotIgnored(BlockJUnit4ClassRunner.java:79)\n" +
            "	at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:71)\n" +
            "	at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:49)\n" +
            "	at org.junit.runners.ParentRunner$3.run(ParentRunner.java:193)\n" +
            "	at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:52)\n" +
            "	at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:191)\n" +
            "	at org.junit.runners.ParentRunner.access$000(ParentRunner.java:42)\n" +
            "	at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:184)\n" +
            "	at org.junit.runners.ParentRunner.run(ParentRunner.java:236)\n" +
            "	at org.junit.runner.JUnitCore.run(JUnitCore.java:157)\n" +
            "	at com.intellij.rt.execution.junit.JUnitStarter.main(JUnitStarter.java:62)\n" +
            "Caused by: java.lang.IndexOutOfBoundsException: Index: 1+ Size: 0\n" +
            "	at java.util.ArrayList.RangeCheck(ArrayList.java:547)\n" +
            "	at java.util.ArrayList.get(ArrayList.java:322)\n" +
            "	at org.dandoy.Test0.doThat(Test0.java:49)\n" +
            "	at org.dandoy.Test0.dothis(Test0.java:42)\n" +
            "	... 22 more";

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
    public void test() throws Exception {
        final List<String> stackLines = TextToLines.toList(STACK_TEXT);

        { // Submit a new bug
            final String textHash = FullStackHashCalculator.getTextHash(stackLines);
            final String s = InServlet.doit(APP, APP_VERSION, textHash);
            assertEquals("New", s);

            BugServlet.doit(APP, APP_VERSION, STACK_TEXT);
        }

        { // Submit the same bug
            final String textHash = FullStackHashCalculator.getTextHash(stackLines);
            final String s = InServlet.doit(APP, APP_VERSION, textHash);
            assertEquals("Old", s);
        }

        {
            final List<Bug> bugs = _store.getBugs(APP, 0, 100, "i");
            assertEquals(1, bugs.size());
            final Bug bug = bugs.get(0);
            assertEquals("IndexOutOfBoundsException at Test0.java:49", bug.getTitle());
            assertEquals(2, bug.getHitCount());

            {
                final BugHit bugHit = _store.getLastHit(bug.getId());
                final String stack = _store.getStack(bugHit.getHitId());
                assertEquals(STACK_TEXT, stack);
            }

            {
                final Map<Bug, int[]> topHits = _store.getTopHits(APP, 7, 5);
                assertEquals(topHits.size(), 1);
                final Bug firstBug = topHits.keySet().iterator().next();
                assertEquals(bug.getId(), firstBug.getId());
                assertEquals(bug.getTitle(), firstBug.getTitle());
                final int[] hits = topHits.values().iterator().next();
                assertArrayEquals(new int[]{0, 0, 0, 0, 0, 0, 0, 2}, hits);
            }
        }
    }
}
