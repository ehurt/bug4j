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
package org.bug4j.server.security

import org.bug4j.Role
import org.bug4j.User
import org.bug4j.UserRole
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.springframework.ldap.core.DirContextAdapter
import org.springframework.ldap.core.DirContextOperations
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.GrantedAuthorityImpl
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper
import org.springframework.security.core.userdetails.UsernameNotFoundException

class CustomUserDetailsContextMapper implements UserDetailsContextMapper {
    private static final boolean AUTO_CREATE_USERS = false

    private static final List NO_ROLES = [new GrantedAuthorityImpl(SpringSecurityUtils.NO_ROLE)]
    def springSecurityService

    @Override
    public CustomUserDetails mapUserFromContext(DirContextOperations ctx, String username, Collection<GrantedAuthority> authority) {
        User user = User.findByUsername(username)


        if (!user) {
            if (!AUTO_CREATE_USERS) {
                throw new UsernameNotFoundException('User not found', username)
            }
            User.withTransaction() {
                user = new User(
                        username: username,
                        password: 'ldap_authentication',
                        enabled: true
                )
                if (!user.validate()) {
                    log.error(user.errors)
                }
                user.save(flush: true)

                UserRole.create(user, Role.findByAuthority(Role.USER))
            }
        }
        def roles = User.withTransaction() {
            user.getAuthorities()
        }
        final Collection<GrantedAuthority> authorities = roles.collect { new GrantedAuthorityImpl(it.authority) }
        final CustomUserDetails userDetails = new CustomUserDetails(
                username, user.password, user.enabled,
                false, false, false, authorities, user.id, username)

        return userDetails
    }

    @Override
    public void mapUserToContext(UserDetails arg0, DirContextAdapter arg1) {
    }
}
