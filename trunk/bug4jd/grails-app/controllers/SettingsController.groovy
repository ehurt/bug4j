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
import org.bug4j.User
import org.bug4j.bug4jd.UserController

/**
 */
class SettingsController {
    def springSecurityService

    def index() {
        final User user = (User) springSecurityService.currentUser
        if (!user) {
            redirect(controller: 'login')
            return
        }
        if (params._action_index) {
            if (params.password != UserController.DUMMY_PASSWORD) user.password = params.password
            if (params.email) user.email = params.email
            if (user.save(validate: true)) {
                flash.message = 'Information updated'
            }
        }
        return [
                userInstance: user,
                displayPassword: UserController.DUMMY_PASSWORD,
        ]
    }
}
