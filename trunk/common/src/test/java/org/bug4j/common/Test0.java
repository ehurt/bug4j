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

import org.junit.Test;

import java.math.BigInteger;
import java.security.MessageDigest;

public class Test0 {
    @Test
    public void test() throws Exception {
        final MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        messageDigest.update("Hello World".getBytes("UTF-8"));
        final byte[] bytes = messageDigest.digest();

        BigInteger bigInt = new BigInteger(1, bytes);
        String v1 = bigInt.toString(36);
        System.out.println("v1 = " + v1);
    }

    @Test
    public void testX() throws Exception {
        dothis();
    }

    private void dothis() {
        try {
            doThat();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void doThat() {
        throw new NullPointerException();
    }
}
