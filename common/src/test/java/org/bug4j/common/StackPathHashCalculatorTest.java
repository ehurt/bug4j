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

package org.bug4j.common;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class StackPathHashCalculatorTest {
    @Test
    public void testAnalyzeFull() throws Exception {
        final List<String> stack = Arrays.asList(
                "java.lang.IllegalStateException: java.lang.IndexOutOfBoundsException: Index: 1, Size: 0",
                "	at org.bug4j.common.Test0.dothis(Test0.java:44)",
                "	at org.bug4j.common.Test0.testX(Test0.java:37)",
                "	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)",
                "	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)",
                "	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)",
                "	at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:44)",
                "	at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:15)",
                "	at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:41)",
                "	at org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:20)",
                "	at org.junit.runners.BlockJUnit4ClassRunner.runNotIgnored(BlockJUnit4ClassRunner.java:79)",
                "	at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:71)",
                "	at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:49)",
                "	at org.junit.runners.ParentRunner$3.run(ParentRunner.java:193)",
                "	at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:52)",
                "	at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:191)",
                "	at org.junit.runners.ParentRunner.access$000(ParentRunner.java:42)",
                "	at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:184)",
                "	at org.junit.runners.ParentRunner.run(ParentRunner.java:236)",
                "	at org.junit.runner.JUnitCore.run(JUnitCore.java:157)",
                "	at com.intellij.rt.execution.junit.JUnitStarter.main(JUnitStarter.java:62)",
                "Caused by: java.lang.IndexOutOfBoundsException: Index: 1, Size: 0",
                "	at java.util.ArrayList.RangeCheck(ArrayList.java:547)",
                "	at java.util.ArrayList.get(ArrayList.java:322)",
                "	at org.bug4j.common.Test0.doThat(Test0.java:49)",
                "	at org.bug4j.common.Test0.dothis(Test0.java:42)",
                "	... 22 more"
        );
        Assert.assertEquals("ykn1pmves7846yx9ssisy9h9", StackPathHashCalculator.analyze(stack));
    }

    @Test
    public void testCompareSimple() throws Exception {
        final List<String> refStack = Arrays.asList(
                "java.lang.IllegalStateException: java.lang.IndexOutOfBoundsException: Index: 1, Size: 0",
                "	at org.bug4j.common.Test0.dothis(Test0.java:44)",
                "	at com.intellij.rt.execution.junit.JUnitStarter.main(JUnitStarter.java:62)",
                "Caused by: java.lang.IndexOutOfBoundsException: Index: 1, Size: 0",
                "	at java.util.ArrayList.RangeCheck(ArrayList.java:547)",
                "	at org.bug4j.common.Test0.dothis(Test0.java:42)",
                "	... 22 more"
        );
        final String reference = StackPathHashCalculator.analyze(refStack);

        {
            final List<String> stack = Arrays.asList(
                    "java.lang.IllegalStateException: java.lang.IndexOutOfBoundsException: Index: 1, Size: 0",
                    "	at org.bug4j.common.Test0.dothis(Test0.java:49)",                                             // different line
                    "	at com.intellij.rt.execution.junit.JUnitStarter.main(JUnitStarter.java:62)",
                    "Caused by: java.lang.IndexOutOfBoundsException: Index: 1, Size: 0",
                    "	at java.util.ArrayList.RangeCheck(ArrayList.java:547)",
                    "	at org.bug4j.common.Test0.dothis(Test0.java:33)",                                             // different line
                    "	... 22 more"
            );
            Assert.assertEquals(reference, StackPathHashCalculator.analyze(stack));
        }

        {
            final List<String> stack = Arrays.asList(
                    "java.lang.IllegalStateException: java.lang.IndexOutOfBoundsException: Index: 3, Size: 0",  // different message
                    "	at org.bug4j.common.Test0.dothis(Test0.java:49)",
                    "	at com.intellij.rt.execution.junit.JUnitStarter.main(JUnitStarter.java:62)",
                    "Caused by: java.lang.IndexOutOfBoundsException: Index: 3, Size: 0",                        // different message
                    "	at java.util.ArrayList.RangeCheck(ArrayList.java:547)",
                    "	at org.bug4j.common.Test0.dothis(Test0.java:42)",
                    "	... 22 more"
            );
            Assert.assertEquals(reference, StackPathHashCalculator.analyze(stack));
        }

        {
            final List<String> stack = Arrays.asList(
                    "java.lang.IllegalStateException: java.lang.IndexOutOfBoundsException: Index: 1, Size: 0",
                    "	at org.bug4j.common.Test0.dothis(Test0.java:44)",
                    "	at com.intellij.rt.execution.junit.JUnitStarter.main(JUnitStarter.java:62)",
                    "Caused by: java.lang.IndexOutOfBoundsException: Index: 1, Size: 0",
                    "	at java.util.ArrayList.RangeCheck(ArrayList.java:547)",
                    "	at org.bug4j.common.Test0.dothis(Test0.java:42)",
                    "	... 252 more"                                                                           // Different ... XX mode
            );
            Assert.assertEquals(reference, StackPathHashCalculator.analyze(stack));
        }

        {
            final List<String> stack = Arrays.asList(
                    "java.lang.IllegalStateException: java.lang.NullPointerException",                          // Different message (because of a different exception)
                    "	at org.bug4j.common.Test0.dothis(Test0.java:44)",
                    "	at com.intellij.rt.execution.junit.JUnitStarter.main(JUnitStarter.java:62)",
                    "Caused by: java.lang.NullPointerException",                                                // Different exception
                    "	at java.util.ArrayList.RangeCheck(ArrayList.java:547)",
                    "	at org.bug4j.common.Test0.dothis(Test0.java:42)",
                    "	... 22 more"
            );
            Assert.assertNotSame(reference, StackPathHashCalculator.analyze(stack));
        }

        {
            final List<String> stack = Arrays.asList(
                    "java.lang.IllegalStateException: java.lang.IndexOutOfBoundsException: Index: 1, Size: 0",
                    "	at xx.yy.Test0.dothis(Test0.java:44)",                                             // different package
                    "	at com.intellij.rt.execution.junit.JUnitStarter.main(JUnitStarter.java:62)",
                    "Caused by: java.lang.IndexOutOfBoundsException: Index: 1, Size: 0",
                    "	at java.util.ArrayList.RangeCheck(ArrayList.java:547)",
                    "	at org.bug4j.common.Test0.dothis(Test0.java:42)",
                    "	... 22 more"
            );
            Assert.assertNotSame(reference, StackPathHashCalculator.analyze(stack));
        }

    }
}
