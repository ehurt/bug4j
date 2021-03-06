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

import org.apache.commons.lang.StringUtils

class Hit {
    public static final int MESSAGE_SIZE = 1024

    Date dateReported
    String reportedBy
    String message
    String remoteAddr

    static constraints = {
        reportedBy(nullable: true)
        message(nullable: true, maxSize: MESSAGE_SIZE)
        remoteAddr(nullable: true, maxSize: 256)
        stack(nullable: true)
        clientSession(nullable: true)
    }

    static belongsTo = [
            bug: Bug,
            clientSession: ClientSession,
            stack: Stack,
    ]

    static mapping = {
        bug index: 'HIT_BUG_IDX'
        stack index: 'HIT_STACK_IDX'
    }

    void setRemoteAddr(String remoteAddr) {
        if (remoteAddr) {
            remoteAddr = StringUtils.abbreviate(remoteAddr, 256)
        }
        this.remoteAddr = remoteAddr
    }

    /**
     * Derby requires a transaction to read blobs
     */
    void loadStack() {
        Hit.withTransaction {
            stack?.stackText?.readStackString()
        }
    }
}
