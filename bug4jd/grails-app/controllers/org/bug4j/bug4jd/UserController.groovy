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
package org.bug4j.bug4jd

import grails.plugins.springsecurity.Secured
import org.bug4j.Role
import org.bug4j.User
import org.bug4j.UserRole

@Secured(['ROLE_ADMIN'])
class UserController {
    public static final String DUMMY_PASSWORD = "***************"

    static defaultAction = "list"

    def index() {
        redirect(action: "list", params: params)
    }

    def list() {
        if (!params.sort) params.sort = 'username'
        if (!params.order) params.order = 'asc'
        if (!params.max) params.max = 10
        if (!params.offset) params.offset = 0

        final users = User.list(params)
        int count = User.count
        return [
                users: users,
                total: count
        ]
    }

    def create() {
        final User user = new User(params)
        render(view: 'edit', model: [
                userInstance: user,
                authorities: []
        ])
    }

    def edit() {
        final id = params.id
        final User user = User.get(id)

        return [
                userInstance: user,
                authorities: user.getAuthorities()*.authority,
        ]
    }

    def save() {
        def userInstance
        def authorities
        final id = params.id
        if (id) {
            userInstance = User.get(id)
            if (params.password == DUMMY_PASSWORD) {
                params.remove('password')
            }
            userInstance.properties = params
            authorities = userInstance.getAuthorities()*.authority
        } else {
            userInstance = new User(params)
            authorities = []
        }
        if (!userInstance.save(flush: true)) {
            render(view: "edit",
                    model: [
                            userInstance: userInstance,
                            authorities: authorities
                    ])
            return
        }
        UserRole.create(userInstance, Role.findByAuthority(Role.USER))

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

    def bulk() {

    }

    def bulkSave() {
        final String names = params.names
        final split = names.split("[\n\r;]")
        split.each {
            if (it) {
                String username = it
                String email = null
                final emailWithNameMatcher = it =~ /(.*)<(.*)>/
                if (emailWithNameMatcher.matches()) {
                    username = emailWithNameMatcher[0][2]
                    email = username
                }

                if (!email) {
                    if (username.contains('@')) {
                        email = username
                    }
                }

                if (!User.findByUsername(it)) {
                    final user = new User(username: username, externalAuthentication: true, enabled: true, email: email).save()
                    UserRole.create(user, Role.findByAuthority(Role.USER))
                }
            }
        }
        flash.message = 'Users created'
        redirect(action: 'list')
    }
}
