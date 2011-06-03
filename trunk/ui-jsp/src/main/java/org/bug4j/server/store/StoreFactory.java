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

import org.bug4j.server.store.jdbc.JdbcStore;

public final class StoreFactory {
    private static Store _store;

    private StoreFactory() {
    }

    public static void createJdbcStore() {
        _store = JdbcStore.getInstance();
    }

    public static void setStore(Store store) {
        _store = store;
    }

    public static Store getStore() {
        return _store;
    }
}
