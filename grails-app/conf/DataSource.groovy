dataSource {
  pooled = true
  driverClassName = "com.mysql.jdbc.Driver"
  username = "root"
  password = ""
  dialect = "org.hibernate.dialect.MySQL5InnoDBDialect"
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
      dbCreate = "create-drop" // one of 'create', 'create-drop','update'
      url = "jdbc:mysql://127.0.0.1:3306/cytominedev?useUnicode=yes&characterEncoding=UTF-8"
    }
  }
  test {
    dataSource {
      dbCreate = "update"
      url = "jdbc:mysql://127.0.0.1:3306/cytominetest?useUnicode=yes&characterEncoding=UTF-8"
    }
  }
  production {
    dataSource {
      dbCreate = "update"
      url = "jdbc:mysql://127.0.0.1:3306/cytomine?useUnicode=yes&characterEncoding=UTF-8"
    }
  }
}
