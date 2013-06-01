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

package org.bug4j

import org.hibernate.Hibernate

import java.sql.Blob
import javax.persistence.Column
import javax.persistence.Lob

class StackText {
    @Lob
    @Column(length = 102400)
    Blob text

    String textString

    static transients = ['textString']

    static constraints = {
    }

    static belongsTo = [
            stack: Stack
    ]

    static mapping = {
        text type: 'blob'
    }

    String readStackString() {
        if (!textString) {
            if (text) {
                final int length = (int) text.length()
                final bytes = text.getBytes(1, length)
                textString = new String(bytes)
            }
        }

        return textString
    }

    void writeStackString(String stackString) {
        if (stackString.length() > 102400) {
            stackString = stackString.substring(0, 102400 - 1)
        }
        text = Hibernate.createBlob(stackString.bytes)
    }
}
