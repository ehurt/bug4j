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
import org.bug4j.App
import org.bug4j.server.util.DateUtil

import java.text.SimpleDateFormat

class HomeController {
    def statsService

    def index() {
        def appCode = params.a
        int daysBack = params.daysBack ? params.daysBack as int : 7

        def appStats = [:]

        def app = null
        final apps = App.list([sort: 'label', order: 'asc'])
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

        final simpleDateFormat = new SimpleDateFormat('M/d/y')
        def now = new Date()

        def dateLinks = [
                todayFrom: simpleDateFormat.format(DateUtil.adjustToDayBoundary(now, DateUtil.TimeAdjustType.BEGINNING_OF_DAY)),
                todayTo: simpleDateFormat.format(DateUtil.adjustToDayBoundary(now, DateUtil.TimeAdjustType.END_OF_DAY)),

                yesterdayFrom: simpleDateFormat.format(DateUtil.adjustToDayBoundary(now - 1, DateUtil.TimeAdjustType.BEGINNING_OF_DAY)),
                yesterdayTo: simpleDateFormat.format(DateUtil.adjustToDayBoundary(now - 1, DateUtil.TimeAdjustType.END_OF_DAY)),

                daysBackFrom: simpleDateFormat.format(DateUtil.adjustToDayBoundary(now - daysBack, DateUtil.TimeAdjustType.BEGINNING_OF_DAY)),
                daysBackTo: simpleDateFormat.format(DateUtil.adjustToDayBoundary(now, DateUtil.TimeAdjustType.END_OF_DAY)),

        ]

        return [
                app: app,
                daysBack: daysBack,
                apps: apps,
                appStats: appStats,
                dateLinks: dateLinks
        ]
    }

    @Secured(['ROLE_ADMIN'])
    def refreshStatistics() {
        def appCode = params.a
        final app = App.findByCode(appCode)
        statsService.generateStats(app, true)
        flash.message = 'Statistics have been re-generated'
        redirect(action: 'index')
    }
}
