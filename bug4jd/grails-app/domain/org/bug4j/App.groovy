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



package org.bug4j

class App {

    String label
    String code

    static constraints = {
        label blank: false, unique: true
        code blank: false, unique: true
    }

    static hasMany = [
            appPackages: AppPackages,
            bugs: Bug,
            clientSessions: ClientSession,
            statCount: StatCount,
            stats: Stat
    ]

    static mapping = {
        table 'APP'
        appPackages cascade: 'all-delete-orphan'
        bugs cascade: 'all-delete-orphan'
        clientSessions cascade: 'all-delete-orphan'
        statCount cascade: 'all-delete-orphan'
        stats cascade: 'all-delete-orphan'
    }
}
