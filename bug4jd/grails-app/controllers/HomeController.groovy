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
import org.bug4j.StatHitCount

class HomeController {
    def statsService

    def index() {
        def appCode = params.appCode
        int daysBack = params.daysBack ? params.daysBack : 300
        statsService.generateStats(false)

        def appStats = [:]
        final int nowInDays = System.currentTimeMillis() / 1000 / 60 / 60 / 24
        final int startInDays = nowInDays - daysBack

        final apps = App.list()
        if (apps) {
            def app
            if (appCode) {
                app = App.findByCode(appCode)
            } else {
                app = apps[0]
            }
            appStats.app = app
            // Fill the stats for the hit graph
            def hits = (0..daysBack).collect {0}
            def statHitCounts = StatHitCount.findAllByAppAndDayGreaterThanEquals(app, startInDays)
            statHitCounts.each {StatHitCount statHitCount ->
                final day = statHitCount.day
                final hitCount = statHitCount.hitCount
                final daysAgo = nowInDays - day
                hits[daysBack - daysAgo] = hitCount
            }
            appStats.hits = hits

            // Count hits today
            def hitCount = [:]
            hitCount.today = StatHitCount.findByAppAndDay(app, nowInDays)?.hitCount
            hitCount.yesterday = StatHitCount.findByAppAndDay(app, nowInDays - 1)?.hitCount
            final statHitCountOver7Days = StatHitCount.findAllByAppAndDayBetween(app, nowInDays - 7, nowInDays)
            def hitsOver7Days = statHitCountOver7Days.sum {it.hitCount}
            hitCount.avg7days = hitsOver7Days ? hitsOver7Days / 7 as int : null

            if (hitCount.today && hitCount.avg7days) {
                hitCount.hitTodayHot = true
            }
            appStats.hitCount = hitCount
        }

        return [
                daysBack: daysBack,
                apps: apps,
                appStats: appStats,
        ]
    }
}
