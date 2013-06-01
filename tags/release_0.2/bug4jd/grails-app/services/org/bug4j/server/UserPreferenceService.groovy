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
package org.bug4j.server

import org.bug4j.User
import org.bug4j.UserPreference

class UserPreferenceService {
    private static final DEFAULT_PREFS = [
            showHits: false,
    ]

    def springSecurityService

    String getStringPreference(String key) {
        String ret = DEFAULT_PREFS[key]
        final User user = (User) springSecurityService.getCurrentUser()
        if (user) {
            // TODO: This has not been tested
            final preference = user.preferences.find {it.key == key}
            if (preference) {
                ret = preference.value
            }
        }
        return ret
    }

    boolean getBooleanPreference(String key) {
        return 'true' == getStringPreference(key)
    }

    void setPreference(String key, Object value) {
        final User user = (User) springSecurityService.getCurrentUser()
        if (user) {
            // TODO: This has not been tested
            def preference = user.preferences.find {it.key == key}
            if (!preference) {
                preference = new UserPreference(key: key)
                preference.user = user
                user.addToPreferences(preference)
            }
            preference.value = value as String
            user.save(true)
        }
    }
}
