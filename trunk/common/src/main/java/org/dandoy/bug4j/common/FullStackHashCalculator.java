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

package org.dandoy.bug4j.common;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class FullStackHashCalculator {
    public static String getTextHash(List<String> stackLines) {
        final String ret;
        try {
            final MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            for (String stackLine : stackLines) {
                final byte[] lineBytes = stackLine.getBytes("UTF-8");
                messageDigest.update(lineBytes);
            }
            final byte[] bytes = messageDigest.digest();
            final BigInteger bigInt = new BigInteger(1, bytes);
            ret = bigInt.toString(36);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        return ret;
    }
}
