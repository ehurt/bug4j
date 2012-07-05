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

class User {

    transient springSecurityService

    String username
    String password
    boolean externalAuthentication
    boolean enabled = true
    boolean accountExpired
    boolean accountLocked
    boolean passwordExpired
    String email
    Date lastSignedIn

    static constraints = {
        username(blank: false, unique: true)
        password(nullable: true)
        email(nullable: true, email: true)
        lastSignedIn(nullable: true)
        externalAuthentication(nullable: true)
    }

    static hasMany = [
            preferences: UserPreference,
            userRoles: UserRole,
    ]

    static mapping = {
        table 'BUG4J_USER'
        password column: '`password`'
        userRoles cascade: 'all-delete-orphan'
    }

    Set<Role> getAuthorities() {
        UserRole.findAllByUser(this).collect { it.role } as Set
    }

    def beforeInsert() {
        encodePassword()
    }

    def beforeUpdate() {
        if (isDirty('password')) {
            encodePassword()
        }
    }

    protected void encodePassword() {
        password = springSecurityService.encodePassword(password)
    }
}
