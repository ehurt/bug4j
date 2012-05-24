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

import org.bug4j.App
import org.bug4j.Bug
import org.bug4j.ClientSession
import org.bug4j.Hit
import org.bug4j.server.util.DateUtil

class BugController {

    def userPreferenceService

    def index() {
        if (!params.sort) params.sort = 'bug_id'
        if (!params.order) params.order = 'desc'
        if (!params.max) params.max = 10
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

        boolean includeSingleHost = false
        if (params.includeSingleHost) {
            includeSingleHost = true
            filter += "showSingleHost "
        }
        if (!includeSingleHost) {
            queryCond += " and b.multiReport = true"
        }

        final sql = """
                select
                    b.id as bug_id,
                    b.title as bug_title,
                    count(h.id) as hitCount,
                    min(h.dateReported) as firstHitDate,
                    max(h.dateReported) as lastHitDate
                from Bug b, Hit h
                where b.app=:app
                and b=h.bug
                ${queryCond}
                group by b.id,b.title
                order by ${params.sort} ${params.order}
                """
        final List list = Bug.executeQuery(sql, queryParams, [max: params.max, offset: params.offset])

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
            ]
        }

        def showHits
        if (params.sh) {
            showHits = params.sh == 'y'
            userPreferenceService.setPreference('showHits', showHits)
        } else {
            showHits = userPreferenceService.getBooleanPreference('showHits')
        }

        return [
                app: selectedApp,
                bugs: bugs,
                total: total[0],
                showHits: showHits,
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
                sh: params.sh,
        ]

        if (params.filterFrom) reParams += [from: params.filterFrom]
        if (params.filterTo) reParams += [to: params.filterTo]
        if (params.filterIncludeSingleHost) reParams += [includeSingleHost: params.filterIncludeSingleHost]

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
}
