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
package org.bug4j.server

import org.bug4j.Bug
import org.bug4j.Hit

class HitController {

    def index() {
        final bugId = params.id
        if (!params.sort) params.sort = 'id'
        if (!params.order) params.order = 'desc'
        if (!params.max) params.max = 50
        if (!params.offset) params.offset = 0

        final bug = Bug.get(bugId)
        final hits = Hit.findAllByBug(bug, params)
        final total = Hit.countByBug(bug, params)

        return [
                bug: bug,
                hits: hits,
                total: total,
        ]
    }
}
