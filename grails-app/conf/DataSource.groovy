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
  cache.provider_class = 'net.sf.ehcache.hibernate.EhCacheProvider'
}
// environment specific settings
environments {
  development {
    dataSource {
      dbCreate = "update"
      url="jdbc:postgresql://localhost:5432/cytomine"
      password = "postgres"
    }
  }
  test {
    dataSource {
      //loggingSql = true
      dbCreate = "create-drop"
      url="jdbc:postgresql://localhost:5432/cytominetest"
      password = "postgres"
    }
  }
  production {
    dataSource {
      dbCreate = "update"
      url = "jdbc:postgresql://10.1.0.102:5432/cytomineprod"
      password = "cytomine_postgres"
    }
  }
  perf {
    dataSource {
        //loggingSql = true
        dbCreate = "update"
        url="jdbc:postgresql://localhost:5432/cytomineperf"
        password = "postgres"
      }
    }
}
