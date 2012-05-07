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

class UserController {
    static defaultAction = "list"

    def index() {
        redirect(action: "list", params: params)
    }

    def list() {

        final users = User.list(params)
        int count = User.count
        return [
                users: users,
                total: count
        ]
    }

    def create() {
        final User user = new User(params)
        render(view: 'edit', model: [userInstance: user])
    }

    def edit() {
        final id = params.id
        final User user = User.get(id)
        return [
                userInstance: user
        ]
    }

    def save() {
        def userInstance
        final id = params.id
        if (id) {
            userInstance = User.get(id)
            userInstance.properties = params
        } else {
            userInstance = new User(params)
        }
        if (!userInstance.save(flush: true)) {
            render(view: "edit", model: [userInstance: userInstance])
            return
        }

        flash.message = id ?
                        message(code: 'default.updated.message', args: [message(code: 'user.label', default: 'User'), userInstance.username]) :
                        message(code: 'default.created.message', args: [message(code: 'user.label', default: 'User'), userInstance.username])
        redirect(action: "list", id: userInstance.id)
    }

    def delete() {
        final id = params.id
        final userInstance = User.get(id)
        userInstance.delete()
        flash.message = message(code: 'default.deleted.message', args: [message(code: 'user.label', default: 'User'), userInstance.username])
        redirect(action: 'list')
    }
}
