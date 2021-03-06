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

import grails.plugins.springsecurity.Secured
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

        final expandPreferences = UserPreference.
                findAllByKeyLike('expand.%').
                collectEntries { [it.key, it.value] }

        final hits = Hit.findAllByBug(bug, [sort: 'id', order: 'desc', max: 10])
        final totalHits = Hit.countByBug(bug)
        final strainInfos = Strain.executeQuery("""
            select r.id as strainId, min(h.dateReported) as minDate, max(h.dateReported) as maxDate
            from Strain r, Stack a, Hit h
            where r.bug=:bug
            and a.strain=r
            and h.stack=a
            group by r.id
            order by r.id desc""", [bug: bug])

        Stack firstStack = null
        if (strainInfos) {
            final firstStrainId = strainInfos[0][0] as long
            StackText.withTransaction {
                final strain = Strain.get(firstStrainId)
                firstStack = Stack.findByStrain(strain)
                final stackText = StackText.get(firstStack.stackTextId)
                stackText.readStackString()
            }
        }

        return [
                bug: bug,
                bugInfo: bugInfo,
                timelineData: timelineData,
                strainInfos: strainInfos,
                firstStack: firstStack,
                hits: hits,
                totalHits: totalHits,
                expandPreferences: expandPreferences,
                offset: offset,
                hasNext: hasNext,
        ]
    }

    @Secured(['ROLE_USER'])
    def authIndex = {
        redirect(action: 'index', params: params)
    }

    @Secured(['ROLE_USER'])
    def ignore = {
        final bugId = params.id
        final bug = Bug.get(bugId)
        bug.ignore = true
        bug.save()
        redirect(action: 'index', params: [id: bugId])
    }

    @Secured(['ROLE_USER'])
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

    def setExpandedState = {
        final section = params.section
        final state = params.state
        final expandState = session.expandState
        if (!expandState) {
            expandState = [:]
            session.expandState = expandState
        }
        expandState[section] = state
        render(text: '100')
    }

    @Secured(['ROLE_USER'])
    def addComment = {
        final bugId = params.bugId as long
        final bug = Bug.get(bugId)
        final newComment = params.newComment
        if (newComment) {
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
        }

        final comments = Comment.findAllByBug(bug, [sort: 'dateAdded', order: 'asc'])
        render(template: "comments",
                model: [
                        bug: bug,
                        comments: comments
                ])
    }

    /**
     * User started adding a comment then cancels
     */
    def cancelComment = {
        final bugId = params.bugId as long
        final bug = Bug.get(bugId)
        final comments = Comment.findAllByBug(bug, [sort: 'dateAdded', order: 'asc'])
        render(template: "comments",
                model: [
                        bug: bug,
                        comments: comments
                ])
    }

    @Secured(['ROLE_USER'])
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
