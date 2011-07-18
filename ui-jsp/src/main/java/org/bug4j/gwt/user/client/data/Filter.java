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

package org.bug4j.gwt.user.client.data;

import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

public class Filter implements Serializable {
    private String _title;
    private Integer _hitWithinDays = 7;
    private boolean _reportedByMultiple = false;

    public Filter() {
    }

    public Filter(Filter filter) {
        filter.copyTo(this);
    }

    public void copyTo(Filter that) {
        that.setHitWithinDays(this.getHitWithinDays());
        that.setTitle(this.getTitle());
        that.setReportedByMultiple(this.isReportedByMultiple());
    }

    public boolean isFiltering() {
        return hasHitWithinDays() || hasTitle() || isReportedByMultiple();
    }

    public boolean hasHitWithinDays() {
        return _hitWithinDays != null;
    }

    public Integer getHitWithinDays() {
        return _hitWithinDays;
    }

    public void setHitWithinDays(@Nullable Integer hitWithinDays) {
        _hitWithinDays = hitWithinDays;
    }

    public void clear() {
        setHitWithinDays(null);
        setTitle(null);
        setReportedByMultiple(false);
    }

    public boolean hasTitle() {
        return _title != null;
    }

    public String getTitle() {
        return _title;
    }

    public void setTitle(@Nullable String title) {
        _title = title;
    }

    public boolean isReportedByMultiple() {
        return _reportedByMultiple;
    }

    public void setReportedByMultiple(boolean reportedByMultiple) {
        _reportedByMultiple = reportedByMultiple;
    }
}