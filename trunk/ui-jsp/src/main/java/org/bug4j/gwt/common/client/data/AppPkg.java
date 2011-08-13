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

package org.bug4j.gwt.common.client.data;

import java.io.Serializable;

public class AppPkg implements Serializable, Comparable<AppPkg> {
    private String _pkg;

    public AppPkg() {
    }

    public AppPkg(String pkg) {
        _pkg = pkg;
    }

    public String getPkg() {
        return _pkg;
    }

    public void setPkg(String pkg) {
        _pkg = pkg;
    }

    @Override
    public int compareTo(AppPkg that) {
        return this._pkg.trim().compareTo(that._pkg.trim());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AppPkg) {
            final AppPkg that = (AppPkg) obj;
            if (this.compareTo(that) == 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return _pkg.trim().hashCode();
    }
}
