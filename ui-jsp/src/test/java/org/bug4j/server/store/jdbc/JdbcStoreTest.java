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

package org.bug4j.server.store.jdbc;

import org.bug4j.common.FullStackHashCalculator;
import org.bug4j.common.TextToLines;
import org.bug4j.gwt.user.client.data.Stack;
import org.bug4j.gwt.user.client.data.Strain;
import org.bug4j.server.processor.StackAnalyzer;
import org.bug4j.server.processor.StackPathHashCalculator;
import org.bug4j.server.store.Store;
import org.bug4j.server.store.TestingStore;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class JdbcStoreTest {

    private static final String STACK_TEXT = "java.lang.IllegalStateException: java.lang.IndexOutOfBoundsException: Index: 1, Size: 0\n" +
            "	at org.bug4j.common.Test0.dothis(Test0.java:44)\n" +
            "	at org.bug4j.common.Test0.testX(Test0.java:37)\n" +
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
            "	at org.bug4j.common.Test0.doThat(Test0.java:49)\n" +
            "	at org.bug4j.common.Test0.dothis(Test0.java:42)\n" +
            "	... 22 more";
    private static final String APP = "My Application";

    private static final String APP_VERSION = "1.3";

    private Store _store;

    @Before
    public void setUp() throws Exception {
        _store = TestingStore.createMemStore();
    }

    @After
    public void tearDown() throws Exception {
        _store.close();
    }

    @Test
    public void test() throws Exception {
        final List<String> stackLines = Arrays.asList(TextToLines.toLineArray(STACK_TEXT));
        final List<String> appPackages = _store.getPackages(APP);
        final StackAnalyzer stackAnalyzer = new StackAnalyzer();
        stackAnalyzer.setApplicationPackages(appPackages);
        final String fullHash = FullStackHashCalculator.getTextHash(stackLines);

        long bugid;
        Stack stack = _store.getStackByHash(APP, fullHash);
        if (stack == null) {
            final String strainHash = StackPathHashCalculator.analyze(stackLines);
            Strain strain = _store.getStrainByHash(APP, strainHash);
            if (strain == null) {
                final String title = stackAnalyzer.getTitle(stackLines);
                bugid = _store.createBug(APP, title);
                strain = _store.createStrain(bugid, strainHash);
            } else {
                bugid = strain.getBugId();
            }
            stack = _store.createStack(bugid, strain.getStrainId(), fullHash, STACK_TEXT);
        }
        final long dateReported = System.currentTimeMillis();
        _store.reportHitOnStack(null, APP_VERSION, null, dateReported, null, stack);
        bugid = stack.getBugId();
        System.out.println("bugid = " + bugid);
    }
}
