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

import org.bug4j.server.gwt.client.data.*;

import java.util.List;
import java.util.Map;

public abstract class Store {
    protected Store() {
    }

    public abstract Bug getBug(String app, long bugId);

    public abstract List<Bug> getBugs(String app, Filter filter, int offset, int max, String orderBy);

    public abstract List<String> getPackages(String app);

    public abstract void close();

    public abstract void addPackage(String app, String appPackage);

    public abstract void deletePackage(String app, String appPackage);

    public abstract List<Hit> getHits(long bugId, int offset, int max, String orderBy);

    public abstract void deleteBug(long bugId);

    public abstract Map<Bug, int[]> getTopHits(String app, int daysBack, int max);

    public abstract Stack getStackByHash(String app, String fullHash);

    public abstract Strain getStrainByHash(String app, String strainHash);

    public abstract long createBug(String app, String title);

    public abstract Strain createStrain(String app, long bugid, String strainHash);

    public abstract Stack createStack(String app, long bugId, long strainId, String fullHash, String stackText);

    public abstract void reportHitOnStack(String app, String version, Stack stack);

    public abstract BugHit getLastHit(long bugId);

    public abstract String getStack(long hitId);

    public abstract BugHitAndStack getBugHitAndStack(long hitId);

    public abstract List<Long> getHitIds(long bugId);

    public abstract Long getBugIdByTitle(String app, String title);
}
