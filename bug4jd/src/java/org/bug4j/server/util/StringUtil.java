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
package org.bug4j.server.util;

import org.apache.commons.lang.StringUtils;
import org.bug4j.Bug;

/**
 */
public class StringUtil {

    /**
     * When the title is really long and without whitespaces it really messes up the ui.
     * This method inserts whitespaces in such case.
     */
    public static String chopLenghtyString(String title, int maxLen) {
        if (isBreakNeeded(title, maxLen)) {
            title = chopString(title, maxLen);
        }
        return title;
    }

    private static String chopString(String title, int maxLen) {
        final int length = title.length();
        final StringBuilder stringBuilder = new StringBuilder(length + 10);
        int len = 0;
        for (int i = 0; i < length; i++) {
            final char c = title.charAt(i);
            if (isBreakable(c)) {
                len = 0;
            } else {
                len++;
            }
            stringBuilder.append(c);
            if (len >= maxLen) {
                if (i + 1 < length) {
                    stringBuilder.append(' ');
                    len = 0;
                }
            }
        }
        return stringBuilder.toString();
    }

    private static boolean isBreakNeeded(String title, int maxLen) {
        if (title.length() <= maxLen) {
            return false;
        }
        int len = 0;
        for (int i = 0; i < title.length(); i++) {
            final char c = title.charAt(i);
            if (isBreakable(c)) {
                len = 0;
            } else {
                len++;
                if (len > maxLen) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isBreakable(char c) {
        return Character.isWhitespace(c);
    }

    public static String fixTitle(String title) {
        String ret;
        if (title != null) {
            final int length = title.length();
            final StringBuilder sb = new StringBuilder(length);

            int i = 0;
            // Skip leading spaces & other bad chars
            while (i < length) {
                final char c = title.charAt(i);
                if (32 < c && c < 127) {
                    break;
                }
                i++;
            }

            while (i < length) {
                // Keep good chars
                while (i < length) {
                    final char c = title.charAt(i++);
                    if (32 >= c || c >= 127) {
                        break;
                    }
                    sb.append(c);
                }
                // Skip bad chars, record a space for the last one
                while (i < length) {
                    final char c = title.charAt(i);
                    if (32 <= c && c < 127) {
                        sb.append(' ');
                        break;
                    }
                    i++;
                }
            }

            ret = sb.toString();
            ret = StringUtils.abbreviate(ret, Bug.TITLE_SIZE);
        } else {
            ret = null;
        }

        return ret;
    }
}
