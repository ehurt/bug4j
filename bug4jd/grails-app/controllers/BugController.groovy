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
import org.bug4j.Hit
import org.bug4j.server.util.DateUtil

import java.text.DateFormat

import static org.bug4j.server.util.DateUtil.TimeAdjustType

class BugController {

    def index() {
        if (!params.sort) params.sort = 'bug_id'
        if (!params.order) params.order = 'desc'
        if (!params.max) params.max = 10
        if (!params.offset) params.offset = 0

        def selectedApp = getApp()
        def queryParams = [app: selectedApp]
        def queryCond = ''
        def filter = [display: '']
        final today = DateUtil.adjustToDayBoundary(new Date(), TimeAdjustType.BEGINNING_OF_DAY)

        Date fromDate = null
        Date toDate = null
        if (params.applyFilter) {
            if (params.applyFilter.from) fromDate = DateUtil.interpretDate(params.applyFilter.from, TimeAdjustType.BEGINNING_OF_DAY)
            if (params.applyFilter.to) toDate = DateUtil.interpretDate(params.applyFilter.to, TimeAdjustType.END_OF_DAY)
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
        println "Total: ${total}"
        final bugs = list.collect {
            return [
                    'bug_id': it[0],
                    'bug_title': it[1],
                    'hitCount': it[2],
                    'firstHitDate': it[3],
                    'lastHitDate': it[4],
            ]
        }
        def reparam = filterMap(params, ['sort', 'order', 'max', 'offset', 'appCode', 'fromDay', 'toDay'])
        return [
                app: selectedApp,
                bugs: bugs,
                total: total[0],
                filter: filter,
                params: reparam
        ]
    }

    def hits() {
        final bugid = params.bugid

        if (!params.max) params.max = 100
        if (!params.sort) params.sort = 'dateReported'
        if (!params.order) params.order = 'desc'

        final bug = Bug.get(bugid)
        final hits = Hit.findAllByBug(bug, [max: 100, sort: "dateReported", order: "desc", offset: 0])
        render(template: 'hits', model: [hits: hits])
    }

    def hit() {
        final hitid = params.hitid
        final Hit hit = Hit.get(hitid)

        render(template: 'hit', model: [hit: hit])
    }

    private App getApp() {
        App app = null

        String appCode = params.appCode
        if (appCode) {
            app = App.findByCode(appCode)
        }

        if (!app) {
            appCode = session.appCode
            if (appCode) {
                app = App.findByCode(appCode)
            }
        }

        if (!app) {
            app = App.list(max: 1).first()
        }

        session.appCode = app?.code

        return app
    }

    private static def filterMap(Map map, List keys) {
        def ret = [:]
        for (Object key : keys) {
            final value = map.get(key)
            if (value) {
                ret.put(key, value)
            }
        }
        return ret
    }

}
