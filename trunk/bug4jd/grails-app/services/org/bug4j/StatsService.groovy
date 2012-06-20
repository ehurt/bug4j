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

import groovy.time.TimeCategory
import org.bug4j.server.util.DateUtil

class StatsService {

    def generateStats(boolean forceRecalc) {
        App.list().each {app ->
            generateStats(app, forceRecalc)
        }
    }

    def generateStats(App app, boolean forceRecalc) {
        use(TimeCategory) {
            final anHourAgo = 1.hour.ago
            app.withTransaction {
                boolean update = false
                Date since = new Date(0)
                if (forceRecalc) {
                    update = true
                } else {
                    final Stat stat = Stat.findByApp(app)
                    if (!stat || stat.dateLastGenerated < anHourAgo) {
                        update = true
                        if (stat) {
                            since = stat.dateLastGenerated
                        }
                    }
                }
                if (update) {
                    generateStats(app, since)
                }
            }
        }
    }

    def generateStats(App app, Date since) {
        try {
            Stat stat = Stat.findByApp(app)
            if (!stat) {
                stat = new Stat()
                stat.app = app
            }
            stat.dateLastGenerated = new Date()
            generateStatsCount(app, since)
            stat.save()
        } catch (Exception e) {
            log.error("Failed to generate statistics", e)
        }
    }

    def generateStatsCount = {App app, Date since ->
        final daysSince = DateUtil.toDays(since.time)
        StatCount.executeUpdate("delete StatCount where app=? and day>=?", [app, daysSince])
        Date adjustedSince = DateUtil.adjustToDayBoundary(since, DateUtil.TimeAdjustType.BEGINNING_OF_DAY)
        generateStatsBugCount(app, adjustedSince)
        generateStatsHitCount(app, adjustedSince)
        generateHot(app, adjustedSince)
    }

    private def generateStatsBugCount = {App app, Date adjustedSince ->
        final list = Hit.executeQuery("""select year(h.dateReported),month(h.dateReported),day(h.dateReported), count(distinct b.id)
                        from Hit h, Bug b
                        where b.app=?
                        ${app.multiHost ? 'and b.multiReport = true' : ''}
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
            final int thenInDays = calendar.timeInMillis / (1000L * 60 * 60 * 24)
            stats[thenInDays] = count
        }

        stats.each {
            final statHitCount = new StatCount(day: it.key, countValue: it.value, countType: 'B')
            statHitCount.app = app
            statHitCount.save(failOnError: true)
        }
    }

    private def generateStatsHitCount = {App app, Date adjustedSince ->
        final list = Hit.executeQuery("""select year(h.dateReported),month(h.dateReported),day(h.dateReported), count(*)
                        from Hit h, Bug b
                        where b.app=?
                        ${app.multiHost ? 'and b.multiReport = true' : ''}
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
            final statHitCount = new StatCount(day: it.key, countValue: it.value, countType: 'H')
            statHitCount.app = app
            statHitCount.save(failOnError: true)
        }
    }

    /**
     * Fill the stats for the bug graph
     */
    def getBugs(App app, int daysBack) {
        return getBugsOrHits(app, daysBack, 'B')
    }

    /**
     * Fill the stats for the hit graph
     */
    def getHits(App app, int daysBack) {
        return getBugsOrHits(app, daysBack, 'H')
    }

    /**
     * Fill the stats for the bug or hit graph
     */
    private def getBugsOrHits(App app, int daysBack, String countType) {
        final long dayInMs = 1000 * 60 * 60 * 24
        final int nowInDays = System.currentTimeMillis() / dayInMs
        final int startInDays = nowInDays - daysBack

        def ret = (0..daysBack).collect {0}
        def statCounts = StatCount.findAllByAppAndCountTypeAndDayGreaterThanEquals(app, countType, startInDays)
        statCounts.each {StatCount statHitCount ->
            final day = statHitCount.day
            final hitCount = statHitCount.countValue
            final daysAgo = nowInDays - day
            ret[daysBack - daysAgo] = hitCount
        }
        return ret
    }

    /**
     * Count the bugs today, yesterday and last X days average
     */
    def getBugCounts(App app, int daysBack) {
        return getBugOrHitCounts(app, daysBack, 'B')
    }

    /**
     * Count the hits today, yesterday and last X days average
     */
    def getHitCounts(App app, int daysBack) {
        return getBugOrHitCounts(app, daysBack, 'H')
    }

    private def getBugOrHitCounts(App app, int daysBack, String countType) {
        final int nowInDays = System.currentTimeMillis() / 1000 / 60 / 60 / 24

        def ret = [:]
        ret.today = StatCount.findByAppAndDayAndCountType(app, nowInDays, countType)?.countValue
        ret.yesterday = StatCount.findByAppAndDayAndCountType(app, nowInDays - 1, countType)?.countValue
        final statCountOverXDays = StatCount.findAllByAppAndCountTypeAndDayBetween(app, countType, nowInDays - daysBack, nowInDays)
        ret.total = statCountOverXDays.sum {it.countValue}
        ret.avg = ret.total ? ret.total / daysBack as int : null

        if (ret.avg) {
            if (ret.today) {
                ret.todayHot = true
            }
            if (ret.yesterday) {
                ret.yesterdayHot = true
            }
        }
        return ret
    }

    private def generateHot = {App app, Date adjustedSince ->
        long dayInMs = 1000L * 24 * 60 * 60
        long now = System.currentTimeMillis()
        long range = dayInMs * 30L
        long oldest = now - range
        double boost = 7

        // Count how many hosts have reported over the range
        final q = Hit.executeQuery('select count(h.remoteAddr) from Hit h where h.dateReported>:date',
                [date: new Date(now - range)])
        final long nbrHosts = (long) q[0]

        app.bugs.each {Bug bug ->
            double weight = 0
            def hosts = new HashSet<String>()
            Hit.findAllByBugAndDateReportedGreaterThan(bug, new Date(oldest)).each {Hit hit ->
                final dateReported = hit.dateReported.getTime()
                final msAgo = (now - dateReported) / range
                // = 1-1/(1+EXP(-(B1*2*$D$1)+$D$1))
                final thisWeight = 1D - 1D / (1 + Math.exp(-(msAgo * boost * 2) + boost))

//                println "   hit ${hit.id} - ${msAgo} - ${thisWeight} - ${hit.dateReported}"
                weight += thisWeight
                hosts.add(hit.remoteAddr)
            }
            if (nbrHosts) {
                weight *= hosts.size() / nbrHosts
            }
            bug.hot = weight
            bug.save()
        }
    }
}
