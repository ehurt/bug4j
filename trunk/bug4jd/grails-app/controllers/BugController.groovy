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

class BugController {

    def index() {
        if (!params.sort) params.sort = 'bug_id'
        if (!params.order) params.order = 'desc'
        if (!params.max) params.max = 10
        if (!params.offset) params.offset = 0

        final App selectedApp = getApp()
        final list = App.withCriteria() {
            eq("id", selectedApp.id)
            projections {
                bugs {
                    groupProperty("id", "bug_id")
                    groupProperty("title", "bug_title")
                    hits {
                        count("id", "hitCount")
                        min("dateReported", "firstHitDate")
                        max("dateReported", "lastHitDate")
                    }
                }
            }
            maxResults(params.max as int)
            firstResult(params.offset as int)
            order(params.sort, params.order)
        }

        final bugs = list.collect {
            [
                    'bug_id': it[0],
                    'bug_title': it[1],
                    'hitCount': it[2],
                    'firstHitDate': it[3],
                    'lastHitDate': it[4],
            ]
        }
        return [
                app: app,
                bugs: bugs,
                total: Bug.countByApp(selectedApp)
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

        String appCode = params.app
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
}
