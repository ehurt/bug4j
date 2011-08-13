/*
 * Copyright 2011 Cedric Dandoy
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

package org.bug4j.gwt.common.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.apache.log4j.Logger;
import org.bug4j.gwt.common.client.CommonService;
import org.bug4j.gwt.common.client.data.AppPkg;
import org.bug4j.gwt.common.client.data.UserAuthorities;
import org.bug4j.server.store.Store;
import org.bug4j.server.store.StoreFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 */
public class CommonServiceImpl extends RemoteServiceServlet implements CommonService {
    private static final Logger LOGGER = Logger.getLogger(CommonServiceImpl.class);

    @Override
    public String getUserName() {
        final HttpServletRequest threadLocalRequest = getThreadLocalRequest();
        final String remoteUser = threadLocalRequest.getRemoteUser();
        final Principal userPrincipal = threadLocalRequest.getUserPrincipal();
        final Authentication authentication = (UsernamePasswordAuthenticationToken) userPrincipal;
        final Collection<GrantedAuthority> authorities = authentication.getAuthorities();
        return remoteUser;
    }

    @Override
    public UserAuthorities getUserAuthorities() {
        final HttpServletRequest threadLocalRequest = getThreadLocalRequest();
        final String remoteUser = threadLocalRequest.getRemoteUser();
        final Principal userPrincipal = threadLocalRequest.getUserPrincipal();
        final Authentication authentication = (UsernamePasswordAuthenticationToken) userPrincipal;
        final Collection<GrantedAuthority> authorities = authentication.getAuthorities();
        final Collection<String> authorityNames = new ArrayList<String>();
        for (GrantedAuthority authority : authorities) {
            final String authorityName = authority.getAuthority();
            authorityNames.add(authorityName);
        }
        return new UserAuthorities(remoteUser, authorityNames);
    }

    @Override
    public List<String> getApplications() throws Exception {
        try {
            final Store store = StoreFactory.getStore();
            return store.getApplications();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<AppPkg> getPackages(String app) throws Exception {
        final Store store = StoreFactory.getStore();
        final List<AppPkg> ret;
        try {
            final List<String> packages = store.getPackages(app);
            ret = new ArrayList<AppPkg>();
            for (String pkg : packages) {
                ret.add(new AppPkg(pkg));
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new Exception(e.getMessage(), e);
        }
        return ret;
    }

}