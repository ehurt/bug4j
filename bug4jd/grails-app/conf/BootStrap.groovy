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

import org.bug4j.*

class BootStrap {

    def init = { servletContext ->
        if (!App.count) {
            final app = new App(label: 'bug4jDemo', code: 'bug4jDemo')
            final appPackages = new AppPackages(app: app, packageName: 'org.bug4j')
            app.addToAppPackages(appPackages)
            app.save()
        }

        if (!User.count) {
            final user = new User(username: 'bug4j', password: 'bug4j', enabled: true)
            final role = new Role(authority: Role.ADMIN)
            final userRole = new UserRole(user: user, role: role)
            user.save(true)
            role.save(true)
            userRole.save(true)
        }
    }

    def destroy = {
    }
}
