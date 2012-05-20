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

import java.text.DateFormat

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
        def filter = [display: '']
        final today = DateUtil.adjustToDayBoundary(new Date(), DateUtil.TimeAdjustType.BEGINNING_OF_DAY)

        Date fromDate = null
        Date toDate = null
        if (params.applyFilter) {
            if (params.applyFilter.from) fromDate = DateUtil.interpretDate(params.applyFilter.from, DateUtil.TimeAdjustType.BEGINNING_OF_DAY)
            if (params.applyFilter.to) toDate = DateUtil.interpretDate(params.applyFilter.to, DateUtil.TimeAdjustType.END_OF_DAY)
        } else {
            if (params.fromDay) {
                fromDate = today.minus(params.fromDay as int)
                queryCond += " and h.dateReported>=:fromDate"
                queryParams += [fromDate: fromDate]
            }

            if (params.toDay) {
                toDate = today.minus(params.toDay as int)
                queryCond += " and h.dateReported<=:toDate"
                queryParams += [toDate: toDate]
            }
        }

        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT);

        if (fromDate) {
            if (toDate) {
                filter.display = "between ${dateFormat.format(fromDate)} and ${dateFormat.format(toDate)}"
            } else {
                filter.display = "after ${dateFormat.format(fromDate)}"
            }
        } else {
            if (toDate) {
                filter.display = "before ${dateFormat.format(toDate)}"
            }
        }

        if (fromDate) filter.fromDate = dateFormat.format(fromDate)
        if (toDate) filter.toDate = dateFormat.format(toDate)

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
                filter: filter,
                showHits: showHits,
        ]
    }

    def hits() {
        final bugid = params.id

        final bug = Bug.get(bugid)
        final hits = Hit.findAllByBug(bug, params)
        render(template: 'hits', model: [hits: hits])
    }

    def hit() {
        final hitid = params.hitid
        final Hit hit = Hit.get(hitid)

        render(template: 'hit', model: [hit: hit])
    }

    def bug() {
        final id = params.id
        final bug = Bug.get(id)
        return [
                bug: bug
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
