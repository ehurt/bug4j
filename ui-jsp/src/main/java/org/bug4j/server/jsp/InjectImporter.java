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

package org.bug4j.server.jsp;

import org.bug4j.gwt.admin.client.data.User;
import org.bug4j.server.processor.BugProcessor;
import org.bug4j.server.store.Store;

/**
 * An importer that re-dedigests the hits
 */
public class InjectImporter extends Importer {
    private final Store _store;

    public InjectImporter(Store store) {
        _store = store;
    }

    @Override
    protected void whenUser(String userName, String password, String email, boolean admin, boolean external, boolean disabled) {
        if (!_store.doesUserExist(userName)) {
            final User user = new User(userName, email, admin, !external, !disabled, null);
            _store.createUserWithEncryptedPassword(user, password);
        }
    }

    @Override
    protected void whenApp(String appName) {
        if (!_store.doesAppExist(appName)) {
            _store.createApplication(appName);
        }
    }

    @Override
    protected void endPackages() {
        _store.setPackages(_appName, _packages);
    }

    @Override
    protected void whenHit(String app, Long sessionId, long bugId, String title, long hitId, long dateReported, String appVer, String user, String message, String stack, Long buildDate, boolean devBuild, Integer buildNumber) {
        final long newBugId = BugProcessor.process(_store, sessionId, app, appVer, buildDate, devBuild, buildNumber, message, dateReported, user, stack);
        System.out.println(bugId + "/" + hitId + "->" + newBugId);
    }
}
