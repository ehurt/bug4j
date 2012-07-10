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

import grails.converters.JSON
import groovy.sql.Sql
import org.bug4j.server.util.DateUtil

import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException
import javax.sql.DataSource

import org.bug4j.*

class BugController {

    def bugService
    DataSource dataSource
    def springSecurityService

    def index() {
        if (!params.sort) params.sort = 'bug_id'
        if (!params.order) params.order = 'desc'
        if (!params.max) params.max = 20
        if (!params.offset) params.offset = 0

        def selectedApp = getApp()
        def queryParams = [app: selectedApp]
        def queryCond = ''
        def filter = ''

        if (params.from) {
            Date fromDate = DateUtil.interpretDate(params.from, DateUtil.TimeAdjustType.BEGINNING_OF_DAY)
            if (fromDate) {
                queryCond += " and h.dateReported>=:fromDate"
                queryParams += [fromDate: fromDate]
                filter += "from:${params.from} "
            }
        }

        if (params.to) {
            Date toDate = DateUtil.interpretDate(params.to, DateUtil.TimeAdjustType.END_OF_DAY)
            if (toDate) {
                queryCond += " and h.dateReported<=:toDate"
                queryParams += [toDate: toDate]
                filter += "to:${params.to} "
            }
        }

        if (selectedApp.multiHost) {
            if (params.includeSingleHost) {
                filter += "showSingleHost "
            } else {
                queryCond += " and b.multiReport = true"
            }
        }

        if (params.includeIgnored) {
            filter += "includeIgnored "
        } else {
            queryCond += " and b.ignore = false"
        }

        final sql = """
                select
                    b.id as bug_id,
                    b.title as bug_title,
                    count(h.id) as hitCount,
                    min(h.dateReported) as firstHitDate,
                    max(h.dateReported) as lastHitDate,
                    b.hot
                from Bug b, Hit h
                where b.app=:app
                and b=h.bug
                ${queryCond}
                group by b.id,b.title,b.hot
                order by ${params.sort} ${params.order}
                """
        final List list = Bug.executeQuery(sql, queryParams, [
                max: params.max,
                offset: params.offset,
        ])

        final countSql = """
                select count(distinct b.id) as total
                from Bug b, Hit h
                where b.app=:app
                and b=h.bug
                ${queryCond}
                """
        final total = Bug.executeQuery(countSql, queryParams)
        final bugs = list.collect {
            return [
                    'bug_id': it[0],
                    'bug_title': it[1],
                    'hitCount': it[2],
                    'firstHitDate': it[3],
                    'lastHitDate': it[4],
                    hot: it[5]
            ]
        }

        return [
                app: selectedApp,
                bugs: bugs,
                total: total[0],
                filter: filter,
        ]
    }

    def filter() {
        final reParams = [
                sort: params.sort,
                order: params.order,
                max: params.max,
                offset: params.offset,
                a: params.a,
        ]

        if (params.filterFrom) reParams += [from: params.filterFrom]
        if (params.filterTo) reParams += [to: params.filterTo]
        if (params.filterIncludeSingleHost) reParams += [includeSingleHost: params.filterIncludeSingleHost]
        if (params.filterIncludeIgnored) reParams += [includeIgnored: params.filterIncludeIgnored]

        redirect(action: 'index', params: reParams)
    }

    def hits() {
        final bugid = params.id as Long

        final bug = Bug.get(bugid)
        final hits = Hit.findAllByBug(bug, params)
        hits*.loadStack()
        render(template: 'hits', model: [hits: hits])
    }

    def hit() {
        final hitid = params.hitid
        final Hit hit = Hit.get(hitid)
        hit.loadStack()

        render(template: 'hit', model: [hit: hit])
    }

    def bug() {
        final id = params.id
        final bug = Bug.get(id)
        def hits = Hit.findAllByBug(bug, params)
        hits*.loadStack()

        return [
                bug: bug,
                hits: hits,
        ]
    }

    def stack() {
        final hitId = params.id
        final hit = Hit.get(hitId)
        render(template: 'hit', model: [hit: hit])
    }

    def hitInfo() {
        final hitId = params.id
        final hit = Hit.get(hitId)
        render(template: 'hit', model: [hit: hit, hitTab: 'info'])
    }

    def clientSession() {
        final id = params.id
        final clientSession = ClientSession.get(id)
        final hits = Hit.findAllByClientSession(clientSession)
        return [
                clientSession: clientSession,
                hits: hits,
        ]
    }

    private App getApp() {
        App app = null

        String appCode = params.a
        if (appCode) {
            app = App.findByCode(appCode)
        }

        if (!app) {
            appCode = session.a
            if (appCode) {
                app = App.findByCode(appCode)
            }
        }

        if (!app) {
            app = App.list(max: 1).first()
        }

        session.a = app?.code

        return app
    }

    def getTimelineData() {
        final sql = new Sql(dataSource.connection)
        try {
            final bugId = params.id as long
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
            def timeLineData = [
                    cols: [
                            [id: 'date', label: 'Date', type: 'date'],
                            [id: 'hits', label: 'Hits', type: 'number'],
                    ],
                    rows: rows,
            ]
            render timeLineData as JSON
        } finally {
            sql.close()
        }
    }

    def getBugInfo = {
        final bugId = params.id as long
        final sql = new Sql(dataSource.connection)
        try {
            def row = sql.firstRow("select count(*), count(distinct reported_by), min(date_reported), max(date_reported) from hit h where h.BUG_ID=${bugId}")
            def bugInfo = [
                    count: row[0],
                    reportedByCount: "${row[1]} ${row[1] <= 1 ? 'user' : 'users'}",
                    minDateReported: DateUtil.formatDate((Date) row[2]),
                    maxDateReported: DateUtil.formatDate((Date) row[3]),
            ]
            // Derby does not support two count(distinct) so we must query this separately
            row = sql.firstRow("select count(distinct REMOTE_ADDR) from hit h where h.BUG_ID=${bugId}")
            bugInfo.remoteAddrCount = "${row[0]} ${row[0] <= 1 ? 'host' : 'hosts'}"

            bugInfo.reportedBy = sql.rows("select distinct reported_by from hit h where h.BUG_ID=${bugId}").collect {it[0]}.join(', ')
            bugInfo.remoteAddr = sql.rows("select distinct remote_addr from hit h where h.BUG_ID=${bugId}").collect {it[0]}.join(', ')
            final bug = Bug.get(bugId)
            final comments = Comment.findAllByBug(bug, [sort: 'dateAdded', order: 'asc'])

            render(template: 'bugInfo',
                    model: [
                            bug: bug,
                            bugId: bugId,
                            bugData: bugInfo,
                            comments: comments
                    ])

        } finally {
            sql.close()
        }
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
                        bugId: bugId,
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
                        bugId: bug.id,
                        comments: comments
                ])
    }

    def ignore = {
        final bugId = params.id
        final bug = Bug.get(bugId)
        bug.ignore = true
        bug.save();
    }

    def unignore = {
        final bugId = params.id
        final bug = Bug.get(bugId)
        bug.ignore = false
        bug.save();
    }

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
                redirect(action: 'bug', params: [id: bugId])
                return;
            } catch (PatternSyntaxException e) {
                flash.error = e.getMessage()
            }
        } else {
            if (!pat) {
                final String charsToEscape = '\\[].^$?*+'.collect {'\\' + it}.sum()
                pat = bug.title.replaceAll("([${charsToEscape}])", '\\\\$1')
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
