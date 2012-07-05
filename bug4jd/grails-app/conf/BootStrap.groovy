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

import org.apache.commons.io.IOUtils
import org.bug4j.*

class BootStrap {
    def extensionService

    def init = { servletContext ->

        // Install the default ~/.bug4j/bug4j-config.groovy
        try {
            final userHome = new File(System.getProperty('user.home'))
            if (userHome.isDirectory()) {
                final File bug4j = new File(userHome, '.bug4j')
                bug4j.mkdir()
                final File configFile = new File(bug4j, 'bug4j-config.groovy')
                if (!configFile.exists()) {
                    final inputStream = getClass().getClassLoader().getResourceAsStream('org/bug4j/server/resources/bug4j-config.groovy.txt')
                    if (inputStream) {
                        configFile.withOutputStream {OutputStream outputStream ->
                            IOUtils.copy(inputStream, outputStream)
                        }
                    }
                }
            }
        } catch (IOException ignore) {
        }

        final catalinaHome = System.getProperty('catalina.home')
        final catalinaHomeFile = new File(catalinaHome, 'extensions')
        extensionService.init(catalinaHomeFile)

        if (!App.count) {
            final app = new App(label: 'bug4jDemo', code: 'bug4jDemo')
            final appPackages = new AppPackages(app: app, packageName: 'org.bug4j')
            app.addToAppPackages(appPackages)
            app.save()
        }

        if (!User.count) {
            new Role(authority: Role.ANONYMOUS).save()
            final userRole = new Role(authority: Role.USER).save()
            final adminRole = new Role(authority: Role.ADMIN).save(true)

            final defaultUser = new User(username: 'bug4j', password: 'bug4j', enabled: true).save()
            UserRole.create(defaultUser, userRole)
            UserRole.create(defaultUser, adminRole)
        }
    }

    def destroy = {
    }
}
