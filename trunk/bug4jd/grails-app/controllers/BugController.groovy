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
import org.bug4j.Bug
import org.bug4j.Hit

class BugController {

    def index() {
        if (!params.sort) params.sort = 'id'
        if (!params.order) params.order = 'desc'
        if (!params.max) params.max = 100
        Application application = getApplication()
        final bugs = Bug.findAllByApplication(application, params)
        return [
                application: application,
                bugs: bugs,
                total: Bug.count
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

    private Application getApplication() {
        Application application = null

        String appCode = params.app
        if (appCode) {
            application = Application.findByCode(appCode)
        }

        if (!application) {
            appCode = session.appCode
            if (appCode) {
                application = Application.findByCode(appCode)
            }
        }

        if (!application) {
            application = Application.list(max: 1).first()
        }

        session.appCode = application?.code

        return application
    }
}
