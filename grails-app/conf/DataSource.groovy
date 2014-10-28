dataSource {
  pooled = true
  driverClassName = "org.postgresql.Driver"
//    driverClassName = "com.p6spy.engine.spy.P6SpyDriver" // use this driver to enable p6spy logging
  username = "postgres"
  dialect = org.hibernatespatial.postgis.PostgisDialect
    properties {
        //specifies that this tc Server is enabled to be monitored using JMX
        jmxEnabled = true
        //number of connections that are created when the pool is started
        initialSize = 10
        //maximum number of active connections that can be allocated from this pool at the same time
        maxActive = 500
        //minimum number of established connections that should be kept in the pool at all times
        minIdle = 10
        //maximum number of connections that should be kept in the pool at all times
        maxIdle = 500
        //maximum number of milliseconds that the pool will wait
        maxWait = 30000
        //Time in milliseconds to keep this connection
        maxAge = 5 * 60000
        //number of milliseconds to sleep between runs of the idle connection validation/cleaner thread
        timeBetweenEvictionRunsMillis = 5000
        //minimum amount of time an object may sit idle in the pool before it is eligible for eviction
        minEvictableIdleTimeMillis = 60000
    }
}
hibernate {
//  cache.use_second_level_cache = true
//  cache.use_query_cache = true
//    cache.use_second_level_cache = false
//    cache.use_query_cache = false   // Changed to false to be enable the distributed cache
//    cache.provider_class = 'net.sf.ehcache.hibernate.SingletonEhCacheProvider'

    //CLUSTER
//    cache.provider_class = 'net.sf.ehcache.hibernate.EhCacheProvider'
//    cache.provider_class = 'net.sf.ehcache.hibernate.SingletonEhCacheProvider'
   // hibernate.cache.region.factory_class = 'net.sf.ehcache.hibernate.SingletonEhCacheRegionFactory'
    cache.use_second_level_cache = true
    cache.use_query_cache = false
    //cache.region.factory_class = 'net.sf.ehcache.hibernate.EhCacheRegionFactory' // Hibernate 3
    cache.region.factory_class = 'org.hibernate.cache.ehcache.EhCacheRegionFactory' // Hibernate 4
    singleSession = true // configure OSIV singleSession mode
}
// environment specific settings
environments {
    scratch {
        dataSource {
            dbCreate = "update"
//      url="jdbc:postgresql://139.165.144.107:5432/cytominedev"
//      password = 'postgres'
            url="jdbc:postgresql://localhost:5432/cytomineempty"
            password = "postgres"
        }
    }
  development {
    dataSource {
      dbCreate = "update"
//      url="jdbc:postgresql://139.165.144.107:5432/cytominedev"
//      password = 'postgres'
      url="jdbc:postgresql://localhost:5432/cytomineelearn"
      password = "postgres"

    }
  }
    cluster {
        dataSource {
            dbCreate = "update"
//      url="jdbc:postgresql://139.165.144.107:5432/cytominedev"
//      password = 'postgres'
            url="jdbc:postgresql://localhost:5432/cytomine"
            password = "postgres"

        }
    }
  test {
    dataSource {
      //loggingSql = true
      dbCreate = "create"
      url="jdbc:postgresql://localhost:5432/cytominetest"
      password = "postgres"
    }
  }
  production {
      dataSource {
          dbCreate = "update"
          url="jdbc:postgresql://localhost:5432/cytomine"
          password = "postgres"
      }
  }
  perf {
    dataSource {
        //loggingSql = true
        dbCreate = "update"
        url="jdbc:postgresql://localhost:5433/cytomineperf"
        password = "postgres"
      }
    }
    testrun {
        dataSource {
            //loggingSql = true
            dbCreate = "create"
            url="jdbc:postgresql://localhost:5432/cytominetestrun"
            password = "postgres"
        }
    }
}
grails {
    mongo {
        host = "localhost"
        port = 27017
        databaseName = "cytomine"
    }
}
environments {
    test {
        grails {
            mongo {
                databaseName = "cytominetest"
            }
        }
    }
}