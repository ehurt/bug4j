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

import org.apache.commons.lang.StringUtils
import org.bug4j.Bug

import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

class MergeController {
    def bugService

    def merge = {
        final bugId = params.id
        final String pat = params.pat
        flash.error = null

        final bug = Bug.get(bugId)
        List<Bug> matchingBugs = null
        int total = 0


        if (params.create) {
            try {
                final int mergedCount = bugService.merge(bug, pat)
                flash.message = "${mergedCount} bugs merged"
                redirect(controller: 'detail', params: [id: bugId])
                return;
            } catch (PatternSyntaxException e) {
                flash.error = e.getMessage()
            }
        } else {
            if (!pat) {
                String[] searchList = new String[BugService.PATTERN_CHARS.length]
                String[] replacementList = new String[BugService.PATTERN_CHARS.length]
                BugService.PATTERN_CHARS.eachWithIndex {char c, int i ->
                    searchList[i] = Character.toString(c)
                    replacementList[i] = '\\' + c
                }
                pat = StringUtils.replaceEach(bug.title, searchList, replacementList)
            }

            if (params.test) {
                try {
                    final pattern = Pattern.compile(pat)
                    final bugs = Bug.list(sort: 'id', order: 'asc')
                    matchingBugs = []
                    bugs.each {Bug matchingBug ->
                        final String title = matchingBug.title
                        final matcher = pattern.matcher(title)
                        if (matcher.matches()) {
                            matchingBugs.add(matchingBug)
                        }
                    }

                } catch (PatternSyntaxException e) {
                    flash.error = e.getMessage()
                }
            }
        }

        return [
                bug: bug,
                pat: pat,
                matchingBugs: matchingBugs,
                total: total,
        ]
    }
}
