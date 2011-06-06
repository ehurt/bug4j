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

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StackPathHashCalculator {
    private static final Pattern STACK_PATTERN = Pattern.compile("\tat ([^()]*)\\((.*)\\)");

    public StackPathHashCalculator() {
    }

    public static String analyze(List<String> stackLines) {
        final MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            return null;
        }

        final Iterator<String> iterator = stackLines.iterator();
        if (!iterator.hasNext()) {
            return null;
        }

        try {
            final String messageLine = iterator.next();
            final String exceptionClass = getExceptionClass(messageLine);
            messageDigest.update(exceptionClass.getBytes("UTF-8"));
            analyze(exceptionClass, iterator, messageDigest);
            final byte[] bytes = messageDigest.digest();

            final BigInteger bigInt = new BigInteger(1, bytes);
            final String ret = bigInt.toString(36);
            return ret;
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    protected static void analyze(String exceptionClass, Iterator<String> iterator, MessageDigest messageDigest) throws UnsupportedEncodingException {
        while (iterator.hasNext()) {
            final String stackLine = iterator.next();
            if (stackLine.startsWith("Caused by: ")) {
                final String substring = stackLine.substring("Caused by: ".length());
                exceptionClass = getExceptionClass(substring);
                messageDigest.update(exceptionClass.getBytes("UTF-8"));
            } else {
                final Matcher matcher = STACK_PATTERN.matcher(stackLine);
                if (matcher.matches()) {
                    final String methodCall = matcher.group(1);
                    if (!methodCall.startsWith("sun.reflect.")) {
                        messageDigest.update(methodCall.getBytes("UTF-8"));
                    }
                }
            }
        }
    }

    static String getExceptionClass(String messageLine) {
        final String ret;
        final int pos = messageLine.indexOf(':');
        if (pos < 0) {
            ret = messageLine;
        } else {
            ret = messageLine.substring(0, pos);
        }
        return ret;
    }
}
