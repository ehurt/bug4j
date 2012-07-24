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

import org.bug4j.App
import org.bug4j.Bug

class BugController {

    public static final ArrayList<String> PARAM_NAMES = ['a', 'from', 'to', 'includeSingleHost', 'includeIgnored', 'sort', 'order', 'max', 'offset', 'max']
    def bugService

    def index() {
        if (!params.sort) params.sort = 'id'
        if (!params.order) params.order = 'desc'
        if (!params.max) params.max = 20
        if (!params.offset) params.offset = 0

        def selectedApp = getApp()
        def queryParams
        def queryCond
        def filter
        def sql
        (sql, queryParams, queryCond, filter) = bugService.queryBugsParamsAndCondAndFilter(
                selectedApp, params.from, params.to, params.includeSingleHost,
                params.includeIgnored, params.sort, params.order, params.max, params.offset)

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
                    'id': it[0],
                    'title': it[1],
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
                paramNames: PARAM_NAMES
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
