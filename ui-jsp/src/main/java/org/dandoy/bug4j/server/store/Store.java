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

package org.dandoy.bug4j.server.store;

import org.dandoy.bug4j.server.gwt.client.data.Bug;
import org.dandoy.bug4j.server.gwt.client.data.BugDetail;
import org.dandoy.bug4j.server.gwt.client.data.Hit;

import java.util.List;

public abstract class Store {
    protected Store() {
    }

    public abstract long find(String app, String hash);

    public abstract long report(String app, String version, String hash, String title, String message, String exceptionMessage, String stackText);

    public abstract void reportHit(long bugId, String version);

    public abstract List<Bug> getBugs(String app, int offset, int max, String orderBy);

    public abstract BugDetail getBug(long id);

    public abstract List<String> getPackages(String app);

    public abstract void setPackages(String app, List<String> appPackages);

    public abstract void close();

    public abstract void addPackage(String app, String appPackage);

    public abstract void deletePackage(String app, String appPackage);

    public abstract List<Hit> getHits(long bugId, int offset, int max, String orderBy);

    public abstract void deleteBug(long bugId);
}
