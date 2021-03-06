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

class Bug {
    public static final int TITLE_SIZE = 512
    String title
    Date extinct
    Date unextinct
    boolean multiReport
    boolean ignore

    /**
     * The unique identified in an external 'bug database'
     */
    String reportedId

    /**
     * The value to display for an externally reported bug
     */
    String reportedLabel

    double hot

    static constraints = {
        title(blank: false, maxSize: TITLE_SIZE)
        extinct(nullable: true)
        unextinct(nullable: true)
        strains(nullable: true)
        reportedId(nullable: true)
        reportedLabel(nullable: true)
        hot(nullable: true)
    }

    static belongsTo = [
            app: App,
    ]

    static hasMany = [
            hits: Hit,
            strains: Strain,
            comments: Comment,
            mergePatterns: MergePattern,
    ]

    static mapping = {
        table 'BUG'
        title index: 'BUG_TITLE_IDX'
    }
}
