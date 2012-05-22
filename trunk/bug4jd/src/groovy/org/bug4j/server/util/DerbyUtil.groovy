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
package org.bug4j.server.util

import org.apache.derby.drda.NetworkServerControl

class DerbyUtil {
    NetworkServerControl serverControl

    public DerbyUtil() {
        this(InetAddress.getByName("localhost"), 1528, "bug4j", "bug4j")
    }

    public DerbyUtil(InetAddress inetAddress, int port, String userName, String password) {
        serverControl = new NetworkServerControl(InetAddress.getByName("localhost"), 1528, "bug4j", "bug4j")
    }

    public boolean ping() {
        try {
            serverControl.ping()
            return true
        } catch (Exception ignore) {
            return false
        }
    }

    public boolean startup() {
        if (ping()) {
            return true
        }
        serverControl.start(new PrintWriter(System.out))
        for (i in 1..10) {
            if (ping()) {
                return true
            }
        }
        return false
    }
}
