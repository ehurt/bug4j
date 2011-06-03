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

package org.bug4j.server.gwt.client.util;

import java.util.ArrayList;
import java.util.List;

public final class TextToLines {
    private TextToLines() {
    }

    public static String[] toArray(String text) {
        final List<String> ret = toList(text);
        return ret.toArray(new String[ret.size()]);
    }

    public static List<String> toList(String text) {
        final List<String> ret = new ArrayList<String>();
        final int length = text.length();
        int from = 0;
        int to = 0;
        while (to < length) {
            final char c = text.charAt(to);
            if (c == '\r') {
                if (to + 1 < length && text.charAt(to + 1) == '\n') {
                    final String line = text.substring(from, to);
                    ret.add(line);
                    to++;
                    from = to + 1;
                }
            } else if (c == '\n') {
                final String line = text.substring(from, to);
                ret.add(line);
                from = to + 1;
            }
            to++;
        }
        if (from < to) {
            final String line = text.substring(from, to);
            ret.add(line);
        }
        return ret;
    }
}
