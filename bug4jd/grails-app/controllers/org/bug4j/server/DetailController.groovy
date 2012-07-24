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

import groovy.sql.Sql
import org.bug4j.server.util.DateUtil

import javax.sql.DataSource

import org.bug4j.*

class DetailController {
    def springSecurityService
    DataSource dataSource
    def userPreferenceService
    def bugService

    def index() {
        Bug bug = null
        def bugId = params.id

        def offset = null
        def hasNext = false

        if (bugId) {
            bug = Bug.get(bugId)
        } else {
            final appCode = params.a
            final sort = params.sort
            final order = params.order
            offset = params.offset as Integer
            def next = params.next as Integer
            if (next) {
                offset += next
            }

            if (appCode && sort && order != null) {
                final app = App.findByCode(appCode)
                def queryParams
                def queryCond
                def filter
                def sql
                (sql, queryParams) = bugService.queryBugsParamsAndCondAndFilter(
                        app, params.from, params.to, params.includeSingleHost,
                        params.includeIgnored, params.sort, params.order, params.max, params.offset
                )
                def bugs = Bug.executeQuery(sql, queryParams, [
                        offset: offset,
                        max: 2,
                ])
                if (bugs) {
                    bugId = bugs[0][0] as Long
                    bug = Bug.get(bugId)
                    if (bugs[1]) {
                        hasNext = true
                    }
                }
            }
        }

        if (!bug) {
            render(text: 'Invalid request')
            return
        }

        final bugInfo = getBugInfo(bug)
        final timelineData = getTimelineData(bug)
        final collapsed = getCollapsed()

        final expandPreferences = UserPreference.
                findAllByKeyLike('expand.%').
                collectEntries { [it.key, it.value] }

        final hits = Hit.findAllByBug(bug, [sort: 'id', order: 'desc', max: 10])
        final totalHits = Hit.countByBug(bug)

        return [
                bug: bug,
                bugInfo: bugInfo,
                timelineData: timelineData,
                hits: hits,
                totalHits: totalHits,
                collapsed: collapsed,
                expandPreferences: expandPreferences,
                offset: offset,
                hasNext: hasNext,
        ]
    }

    def ignore = {
        final bugId = params.id
        final bug = Bug.get(bugId)
        bug.ignore = true
        bug.save()
        redirect(action: 'index', params: [id: bugId])
    }

    def unignore = {
        final bugId = params.id
        final bug = Bug.get(bugId)
        bug.ignore = false
        bug.save()
        redirect(action: 'index', params: [id: bugId])
    }

    private def getBugInfo(Bug bug) {
        long bugId = bug.id
        final sql = new Sql(dataSource)
        try {
            def ret = [:]
            def row = sql.firstRow("select count(*), count(distinct reported_by), min(date_reported), max(date_reported) from hit h where h.BUG_ID=${bugId}")
            def minDateReported = DateUtil.fixDate(row[2])
            final maxDateReported = DateUtil.fixDate(row[3])
            ret += [
                    count: row[0],
                    reportedByCount: "${row[1]} ${row[1] <= 1 ? 'user' : 'users'}",
                    minDateReported: DateUtil.formatDate((Date) minDateReported),
                    maxDateReported: DateUtil.formatDate((Date) maxDateReported),
            ]
            // Derby does not support two count(distinct) so we must query this separately
            row = sql.firstRow("select count(distinct REMOTE_ADDR) from hit h where h.BUG_ID=${bugId}")
            ret += [remoteAddrCount: "${row[0]} ${row[0] <= 1 ? 'host' : 'hosts'}"]
            ret += [reportedBy: sql.rows("select distinct reported_by from hit h where h.BUG_ID=${bugId}").collect {it[0]}.join(', ')]
            ret += [remoteAddr: sql.rows("select distinct remote_addr from hit h where h.BUG_ID=${bugId}").collect {it[0]}.join(', ')]
            ret += [comments: Comment.findAllByBug(bug, [sort: 'dateAdded', order: 'asc'])]

            return ret
        } finally {
            sql.close()
        }
    }

    private Map getTimelineData(Bug bug) {
        long bugId = bug.id
        final sql = new Sql(dataSource)
        try {
            Map<Date, Long> hitDays = [:]
            def tonight = DateUtil.adjustToDayBoundary(new Date(), DateUtil.TimeAdjustType.END_OF_DAY)
            hitDays[tonight] = 0
            sql.eachRow("select date_reported from hit h where h.BUG_ID=${bugId}") {
                final dateReported = DateUtil.adjustToDayBoundary(it.DATE_REPORTED, DateUtil.TimeAdjustType.BEGINNING_OF_DAY)
                Long hitCount = hitDays.get(dateReported)
                if (!hitCount) {
                    hitCount = 0;
                    if (!hitDays[dateReported - 1]) {
                        hitDays[dateReported - 1] = 0
                    }
                    if (!hitDays[dateReported + 1]) {
                        hitDays[dateReported + 1] = 0
                    }
                }
                hitCount++
                hitDays.put(dateReported, hitCount)
            }
            def rows = hitDays.collect {Date key, Long value ->
                def date = "Date(${1900 + key.getYear()}, ${key.getMonth()}, ${key.getDate()})"
                return [c: [[v: date], [v: value]]]
            }
            return [
                    cols: [
                            [id: 'date', label: 'Date', type: 'date'],
                            [id: 'hits', label: 'Hits', type: 'number'],
                    ],
                    rows: rows,
            ]
        } finally {
            sql.close()
        }
    }

    private String getCollapsed() {
        String collapsed = session.detailCollapsed
        if (!collapsed) {
            collapsed = userPreferenceService.getStringPreference('detailCollapsed')
            session.detailCollapsed = collapsed
        }
        return collapsed
    }

    def setExpandedState = {
        final section = params.section
        final state = params.state
        userPreferenceService.setPreference("expand." + section, state)
        render(text: '100')
    }

    def addComment = {
        final bugId = params.bugId as long
        final newComment = params.newComment
        final bug = Bug.get(bugId)
        final username = springSecurityService.principal.username
        final comment = new Comment(
                text: newComment,
                dateAdded: new Date(),
                addedBy: username
        )
        comment.bug = bug

        if (!comment.validate()) {
            final errors = comment.errors
            println errors
        }
        comment.save(flush: true)

        final comments = Comment.findAllByBug(bug, [sort: 'dateAdded', order: 'asc'])
        render(template: "comments",
                model: [
                        bug: bug,
                        comments: comments
                ])
    }

    def removeComment = {
        final commentId = params.id as long
        final comment = Comment.get(commentId)
        final bug = comment.bug
        final username = springSecurityService.principal.username
        if (comment.addedBy == username) {
            comment.delete();
        }
        final comments = Comment.findAllByBug(bug, [sort: 'dateAdded', order: 'asc'])
        render(template: "comments",
                model: [
                        bug: bug,
                        comments: comments
                ])
    }
}
