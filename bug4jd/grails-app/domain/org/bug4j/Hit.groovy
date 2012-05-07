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

class Hit {
    Date dateReported
    String reportedBy
    String message

    static constraints = {
        reportedBy(blank: false)
        message(nullable: true)
        stack(nullable: true)
        clientSession(nullable: true)
    }

    static belongsTo = [
            bug: Bug,
            clientSession: ClientSession,
            stack: Stack,
    ]

    static hasOne = [
    ]
}
