dataSource {
  pooled = true
  driverClassName = "org.postgresql.Driver"
  username = "postgres"
  password = "postgres"
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
      dbCreate = "create-drop"
      url="jdbc:postgresql://localhost:5432/cytomine"
    }
  }
  test {
    dataSource {
      dbCreate = "create-drop"
      url="jdbc:postgresql://localhost:5432/cytomine"
    }
  }
  production {
    dataSource {
      dbCreate = "update"
      url = "jdbc:mysql://127.0.0.1:3306/cytomine?useUnicode=yes&characterEncoding=UTF-8"
    }
  }
}
