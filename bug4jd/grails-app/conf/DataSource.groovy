/*
 * Copyright 2013 Cedric Dandoy
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

import org.bug4j.server.util.DerbyUtil

dataSource {
    pooled = true
//    loggingSql = true
}
hibernate {
    cache.use_second_level_cache = true
    cache.use_query_cache = false
    cache.region.factory_class = 'net.sf.ehcache.hibernate.EhCacheRegionFactory'
}
// environment specific settings
environments {
    development {
        dataSource {
            switch ('derby') {
                case 'h2':
                    driverClassName = "org.h2.Driver"
                    dbCreate = "create-drop"
                    url = "jdbc:h2:mem:testDb;MVCC=TRUE"
                    username = "sa"
                    password = ""
                    break;
                case 'oracle':
                    driverClassName = "oracle.jdbc.OracleDriver"
                    dbCreate = "update" // one of 'create', 'create-drop', 'update', 'validate', ''
                    url = "jdbc:oracle:thin:@127.0.0.1:1521:orcl"
                    username = "bug4j"
                    password = "bug4j"
                    break;
                case 'derby':
                    driverClassName = "org.apache.derby.jdbc.ClientDriver"
                    new DerbyUtil().startup()
                    dbCreate = "update"
                    final userHome = System.getProperty("user.home")
                    final bug4jHome = new File(userHome, ".bug4j")
                    if (!bug4jHome.isDirectory()) {
                        if (!bug4jHome.mkdirs()) {
                            log.error("Failed to create ${bug4jHome}")
                        }
                    }
                    url = "jdbc:derby://localhost:1528/${bug4jHome}/bug4jdb;create=true"
                    username = "bug4j"
                    password = "bug4j"
                    pooled = true
                    properties {
                        maxActive = -1
                        minEvictableIdleTimeMillis = 1800000
                        timeBetweenEvictionRunsMillis = 1800000
                        numTestsPerEvictionRun = 3
                        testOnBorrow = true
                        testWhileIdle = true
                        testOnReturn = true
                        validationQuery = "VALUES 1"
                    }
            }
        }
    }
    test {
        dataSource {
            driverClassName = "org.h2.Driver"
            dbCreate = "update"
            url = "jdbc:h2:mem:testDb;MVCC=TRUE"
            username = "sa"
            password = ""
        }
    }
    production {
        dataSource {
            driverClassName = "org.apache.derby.jdbc.ClientDriver"
            new DerbyUtil().startup()
            dbCreate = "update"
            final userHome = System.getProperty("user.home")
            final bug4jHome = new File(userHome, ".bug4j")
            if (!bug4jHome.isDirectory()) {
                if (!bug4jHome.mkdirs()) {
                    log.error("Failed to create ${bug4jHome}")
                }
            }
            url = "jdbc:derby://localhost:1528/${bug4jHome}/bug4jdb;create=true"
            username = "bug4j"
            password = "bug4j"
            pooled = true
            properties {
                maxActive = -1
                minEvictableIdleTimeMillis = 1800000
                timeBetweenEvictionRunsMillis = 1800000
                numTestsPerEvictionRun = 3
                testOnBorrow = true
                testWhileIdle = true
                testOnReturn = true
                validationQuery = "VALUES 1"
            }
        }
    }
}
