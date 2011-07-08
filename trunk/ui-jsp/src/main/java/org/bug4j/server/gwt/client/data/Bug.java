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

package org.bug4j.server.gwt.client.data;

import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

public class Bug implements Serializable {
    private long _id;
    private String _title;
    private int _hitCount;
    @Nullable
    private Long _maxHit;
    @Nullable
    private Long _lastReadHit;

    public Bug() {
    }

    public Bug(long id, String title, int hitCount) {
        _id = id;
        _title = title;
        _hitCount = hitCount;
    }

    public Bug(long id, String title, int hitCount, long maxHit, Long lastReadHit) {
        _id = id;
        _title = title;
        _hitCount = hitCount;
        _maxHit = maxHit;
        _lastReadHit = lastReadHit;
    }

    @Override
    public String toString() {
        return _id + "-" + _title;
    }

    public long getId() {
        return _id;
    }

    public String getTitle() {
        return _title;
    }

    public int getHitCount() {
        return _hitCount;
    }

    @SuppressWarnings({"ConstantConditions"})
    public boolean isRead() {
        if (_maxHit == null) {
            return true;
        }
        if (_lastReadHit == null) {
            return false;
        }
        if (_lastReadHit < _maxHit) {
            return false;
        }
        return true;
    }

    public long getLastReadHit() {
        return _lastReadHit == null ? 0 : _lastReadHit;
    }

    public void setRead(boolean read) {
        _lastReadHit = read ? Long.MAX_VALUE : null;
    }
}
