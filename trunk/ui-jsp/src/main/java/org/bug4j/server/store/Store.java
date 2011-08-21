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

package org.bug4j.server.store;

import org.bug4j.gwt.admin.client.data.User;
import org.bug4j.gwt.user.client.data.*;
import org.bug4j.server.store.jdbc.BugCallback;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract class Store {
    protected Store() {
    }

    public abstract List<Bug> getBugs(String userName, String app, Filter filter, int offset, int max, String orderBy);

    public abstract void fetchBugs(String app, BugCallback bugCallback);

    public abstract List<String> getPackages(String app);

    public abstract void setPackages(String app, List<String> packages);

    public abstract void close();

    /**
     * @param orderBy i[d], a[pplication version] or d[ate reported], lowercase=ascending
     */
    public abstract List<BugHit> getHits(long bugId, @Nullable Filter filter, int offset, int max, String orderBy);

    public abstract void fetchHits(long bugId, HitsCallback hitsCallback);

    public abstract void deleteBug(long bugId);

    public abstract Map<Bug, int[]> getTopHits(String app, int daysBack, int max);

    public abstract Stack getStackByHash(String app, String fullHash);

    public abstract Strain getStrainByHash(String app, String strainHash);

    public abstract long createBug(String app, String title);

    public abstract Strain createStrain(long bugid, String strainHash);

    public abstract Stack createStack(long bugId, long strainId, String fullHash, String stackText);

    public abstract void reportHitOnStack(String app, String version, @Nullable String message, long dateReported, @Nullable String user, Stack stack);

    public abstract BugHit getLastHit(long bugId);

    public abstract String getStack(long hitId);

    public abstract BugHitAndStack getBugHitAndStack(long hitId);

    public abstract List<Long> getBugIdByTitle(String app, String title);

    public abstract List<String> getApplications();

    public abstract void setDefaultApplication(String remoteUser, String app);

    public abstract String getDefaultApplication(String remoteUser);

    public abstract Filter getDefaultFilter(String userName);

    public abstract void setDefaultFilter(String userName, Filter filter);

    public abstract void markRead(String userName, long bugId);

    public abstract List<BugCountByDate> getBugCountByDate(String app);

    public abstract List<User> getUsers();

    public abstract void deleteUser(String userName);

    public abstract void deleteUsers(Collection<String> userNames);

    public abstract void updateUser(User user);

    public abstract void createUser(User user, String password);

    public abstract void updatePassword(String userName, String oldPassword, String newPassword);

    public abstract void createApplication(String applicationName);

    public abstract void deleteApplication(String applicationName);

    public abstract void resetPassword(String userName, String randomPassword);
}
