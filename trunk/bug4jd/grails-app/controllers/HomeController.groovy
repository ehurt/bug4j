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


import org.bug4j.Application
import org.bug4j.StatHitCount

class HomeController {
    def statsService

    def index() {
        int daysBack = params.daysBack ? params.daysBack : 300
        statsService.generateStats(false)

        def appStats = []
        final int nowInDays = System.currentTimeMillis() / 1000 / 60 / 60 / 24
        final int startInDays = nowInDays - daysBack

        Application.list().each {Application app ->
            def stats = (0..daysBack).collect {0}
            def statHitCounts = StatHitCount.findAllByApplicationAndDayGreaterThanEquals(app, startInDays)
            statHitCounts.each {StatHitCount statHitCount ->
                final day = statHitCount.day
                final hitCount = statHitCount.hitCount
                final daysAgo = nowInDays - day
                stats[daysBack - daysAgo] = hitCount
            }
            appStats += [app: app, stats: stats]
        }

        return [
                daysBack: daysBack,
                appStats: appStats,
        ]
    }
}
