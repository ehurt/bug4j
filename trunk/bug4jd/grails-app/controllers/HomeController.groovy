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

class HomeController {
    def statsService

    def index() {
        def appCode = params.appCode
        int daysBack = params.daysBack ? params.daysBack as int : 7
        statsService.generateStats(false)

        def appStats = [:]

        def app = null
        final apps = App.list()
        if (apps) {
            if (appCode) {
                app = App.findByCode(appCode)
            } else {
                app = apps[0]
            }
            appStats.app = app
            appStats.bugs = statsService.getBugs(app, daysBack)
            appStats.hits = statsService.getHits(app, daysBack)
            appStats.bugCount = statsService.getBugCounts(app, daysBack)
            appStats.hitCount = statsService.getHitCounts(app, daysBack)
        }

        return [
                app: app,
                daysBack: daysBack,
                apps: apps,
                appStats: appStats,
        ]
    }
}
