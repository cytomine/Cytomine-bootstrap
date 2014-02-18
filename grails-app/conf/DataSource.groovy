dataSource {
  pooled = true
  driverClassName = "org.postgresql.Driver"
//    driverClassName = "com.p6spy.engine.spy.P6SpyDriver" // use this driver to enable p6spy logging
  username = "postgres"

  dialect = org.hibernatespatial.postgis.PostgisDialect
}
hibernate {
  cache.use_second_level_cache = true
  cache.use_query_cache = true


    //CLUSTER
    cache.provider_class = 'net.sf.ehcache.hibernate.EhCacheProvider'
//    cache.provider_class = 'net.sf.ehcache.hibernate.SingletonEhCacheProvider'
//    hibernate.cache.region.factory_class = 'net.sf.ehcache.hibernate.SingletonEhCacheRegionFactory'

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
}
