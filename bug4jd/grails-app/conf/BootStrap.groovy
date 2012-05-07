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
import org.bug4j.ApplicationPackages

class BootStrap {

    def init = { servletContext ->
        if (!Application.count) {
            final application = new Application(label: 'bug4jDemo', code: 'bug4jDemo')
            final applicationPackages = new ApplicationPackages(application: application, packageName: 'org.bug4j.demo')
            application.addToApplicationPackages(applicationPackages)
            application.save()
        }
    }

    def destroy = {
    }
}
