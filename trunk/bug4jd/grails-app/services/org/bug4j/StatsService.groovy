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
package org.bug4j

class StatsService {

    def generateStats(boolean forceRecalc) {
        App.list().each {
            if (forceRecalc) {
                Stat.findByApp(it)?.delete()
            }
            generateStats(it)
        }
    }

    def generateStats(App app) {
        Stat stat = Stat.findByApp(app)
        if (!stat) {
            stat = new Stat(dateLastGenerated: new Date(0))
            stat.app = app
        }
        generateStatsHitCount(app, stat.dateLastGenerated)
        stat.dateLastGenerated = new Date()
        stat.save(failOnError: true)
    }

    def generateStatsHitCount = {App app, Date since ->
        final daysSince = toDays(since.time)
        Date adjustedSince = backToMidnight(since)
        StatHitCount.executeUpdate("delete StatHitCount where app=? and day>=?", [app, daysSince])

        final list = Hit.executeQuery("""select year(h.dateReported),month(h.dateReported),day(h.dateReported), count(*)
                        from Hit h, Bug b
                        where b.app=?
                        and b=h.bug
                        and h.dateReported >= ?
                        group by year(h.dateReported),month(h.dateReported),day(h.dateReported)""",
                [app, adjustedSince])

        Map<Integer, Integer> stats = [:]
        list.each {
            def yearReported = it[0] as int
            def monthReported = it[1] as int
            def dayReported = it[2] as int
            int count = it[3] as int
            final calendar = new GregorianCalendar(yearReported, monthReported - 1, dayReported)
            final int thenInDays = calendar.timeInMillis / 1000 / 60 / 60 / 24
            stats[thenInDays] = count
        }

        stats.each {
            final statHitCount = new StatHitCount(day: it.key, hitCount: it.value)
            statHitCount.app = app
            statHitCount.save(failOnError: true)
        }
    }

    private static int toDays(long timeInMillis) {
        timeInMillis / 1000 / 60 / 60 / 24
    }

    private static Date backToMidnight(Date date) {
        final calendarSince = date.toCalendar()
        calendarSince.set(Calendar.HOUR_OF_DAY, 0)
        calendarSince.set(Calendar.MINUTE, 0)
        calendarSince.set(Calendar.SECOND, 0)
        calendarSince.set(Calendar.MILLISECOND, 0)
        return calendarSince.getTime()
    }
}
