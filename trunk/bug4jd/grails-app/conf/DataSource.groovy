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

import org.bug4j.server.util.DerbyUtil

dataSource {
    driverClassName = "org.h2.Driver"
    pooled = true
    username = "sa"
    password = ""
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
            switch ('oracle') {
                case 'h2':
                    dbCreate = "create-drop"
                    url = "jdbc:h2:mem:testDb;MVCC=TRUE"
                    break;
                case 'oracle':
                    driverClassName = "oracle.jdbc.OracleDriver"
                    dbCreate = "update" // one of 'create', 'create-drop', 'update', 'validate', ''
                    url = "jdbc:oracle:thin:@127.0.0.1:1521:orcl"
                    username = "bug4j"
                    password = "bug4j"
                    break;
                case 'derby':
                    new DerbyUtil().startup()
                    dbCreate = "update"
                    url = "jdbc:derby://localhost:1528/\${catalina.home}/bug4jdb;create=true"
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
            dbCreate = "update"
            url = "jdbc:h2:mem:testDb;MVCC=TRUE"
        }
    }
    production {
        dataSource {
            new DerbyUtil().startup()
            dbCreate = "update"
            url = "jdbc:derby://localhost:1528/\${catalina.home}/bug4jdb;create=true"
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
