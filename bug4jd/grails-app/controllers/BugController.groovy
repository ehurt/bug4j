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

class BugController {

    def index() {
        if (!params.sort) {
            params.sort = 'id'
            params.order = 'desc'
        }
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        Application application = getApplication()
        final bugs = Bug.findAllByApplication(application, params)
        return [
                application: application,
                bugs: bugs,
                total: Bug.count
        ]
    }

    private Application getApplication() {
        Application application = null
        final appCode = session.appCode

        if (appCode) {
            application = Application.findByCode(appCode)
        }

        if (!application) {
            application = Application.list(max: 1).first()
        }

        session.appCode = application?.code

        return application
    }
}
