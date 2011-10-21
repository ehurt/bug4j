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
    private Long _bugId;
    private Integer _hitWithinDays = null;
    private boolean _includeSingleUserReports = true;
    private boolean _showExtinct = true;

    public Filter() {
    }

    public Filter(Filter filter) {
        filter.copyTo(this);
    }

    public void copyTo(Filter that) {
        that.setHitWithinDays(this.getHitWithinDays());
        that.setTitle(this.getTitle());
        that.setIncludeSingleUserReports(this.isIncludeSingleUserReports());
        that.setShowExtinct(this.isShowExtinct());
    }

    public boolean isFiltering() {
        return hasHitWithinDays() || hasTitle() || !isIncludeSingleUserReports() || !isShowExtinct();
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
        setIncludeSingleUserReports(true);
        setShowExtinct(true);
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

    public Long getBugId() {
        return _bugId;
    }

    public void setBugId(Long bugId) {
        _bugId = bugId;
    }

    public boolean isIncludeSingleUserReports() {
        return _includeSingleUserReports;
    }

    public void setIncludeSingleUserReports(boolean includeSingleUserReports) {
        _includeSingleUserReports = includeSingleUserReports;
    }

    public boolean isShowExtinct() {
        return _showExtinct;
    }

    public void setShowExtinct(boolean showExtinct) {
        _showExtinct = showExtinct;
    }
}
