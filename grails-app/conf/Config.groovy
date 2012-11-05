import grails.converters.JSON

// locations to search for config files that get merged into the main config
// config files can either be Java properties files or ConfigSlurper scripts

// grails.config.locations = [ "classpath:${appName}-config.properties",
//                             "classpath:${appName}-config.groovy",
//                             "file:${userHome}/.grails/${appName}-config.properties",
//                             "file:${userHome}/.grails/${appName}-config.groovy"]

// if(System.properties["${appName}.config.location"]) {
//    grails.config.locations << "file:" + System.properties["${appName}.config.location"]
// }


JSON.use('default')
grails.config.locations = ["file:${userHome}/.grails/cytomineconfig.properties"]
grails.project.groupId = appName // change this to alter the default package name and Maven publishing destination
grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [ html: ['text/html','application/xhtml+xml'],
        json: ['application/json','text/json'],
        jsonp: 'application/javascript',
        xml: ['text/xml', 'application/xml'],
        png : 'image/png',
        jpg : 'image/jpeg',
        text: 'text/plain',
        js: 'text/javascript',
        rss: 'application/rss+xml',
        atom: 'application/atom+xml',
        css: 'text/css',
        csv: 'text/csv',
        all: '*/*',
        form: 'application/x-www-form-urlencoded',
        multipartForm: 'multipart/form-data'
]
cytomine.maxRequestSize = 102400

// URL Mapping Cache Max Size, defaults to 5000
//grails.urlmapping.cache.maxsize = 1000

// The default codec used to encode data with ${}
grails.views.default.codec = "none" // none, html, base64
grails.views.gsp.encoding = "UTF-8"
grails.converters.encoding = "UTF-8"
grails.converters.json.default.deep = false
//grails.converters.json.date = "javascript"
//grails.converters.xml.date = "javascript"



// enable Sitemesh preprocessing of GSP pages
grails.views.gsp.sitemesh.preprocess = true
// scaffolding templates configuration
grails.scaffolding.templates.domainSuffix = 'Instance'

// Set to false to use the new Grails 1.2 JSONBuilder in the render method
grails.json.legacy.builder = false
// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true
// whether to install the java.util.logging bridge for sl4j. Disable for AppEngine!
grails.logging.jul.usebridge = true
// packages to include in Spring bean scanning
grails.spring.bean.packages = []

cytomine.jobdata.filesystem = false
cytomine.jobdata.filesystemPath = "algo/data/"

//mail.error.port = 587
//mail.error.starttls = true

// set per-environment serverURL stem for creating absolute links
environments {
    production {
        grails.serverURL = "http://beta.cytomine.be"
    }
    development {
        grails.serverURL = "http://localhost:8080"  //BS : http://139.165.108.140:9090
        grails.converters.default.pretty.print = true
        grails.plugins.springsecurity.useBasicAuth = true
    }
    test {
        grails.serverURL = "http://localhost:8090"
        grails.plugins.springsecurity.useBasicAuth = true
        grails.plugins.springsecurity.basic.realmName = "Cytomine log"
    }
    perf {
        grails.serverURL = "http://localhost:8080"
        grails.plugins.springsecurity.useBasicAuth = true
        grails.plugins.springsecurity.basic.realmName = "Cytomine log"
    }
}
coverage {
    enableByDefault = false
    xml = true
}
elasticSearch {
  /**
   * Date formats used by the unmarshaller of the JSON responses
   */
  date.formats = ["yyyy-MM-dd'T'HH:mm:ss'Z'"]
  /**
   * Hosts for remote ElasticSearch instances.
   * Will only be used with the "transport" client mode.
   * If the client mode is set to "transport" and no hosts are defined, ["localhost", 9300] will be used by default.
   */
  client.hosts = [
          [host:'localhost', port:9300]
  ]

  /**
   * Default mapping property exclusions
   *
   * No properties matching the given names will be mapped by default
   * ie, when using "searchable = true"
   *
   * This does not apply for classes using mapping by closure
   */
  defaultExcludedProperties = ["password"]

  /**
   * Determines if the plugin should reflect any database save/update/delete automatically
   * on the ES instance. Default to false.
   */
  disableAutoIndex = false

  /**
   * Should the database be indexed at startup.
   *
   * The value may be a boolean true|false.
   * Indexing is always asynchronous (compared to Searchable plugin) and executed after BootStrap.groovy.
   */
  bulkIndexOnStartup = true

  /**
   *  Max number of requests to process at once. Reduce this value if you have memory issue when indexing a big amount of data
   *  at once. If this setting is not specified, 500 will be use by default.
   */
  maxBulkRequest = 500
}

environments {
  development {
    /**
     * Possible values : "local", "node", "transport"
     * If set to null, "node" mode is used by default.
     */
    elasticSearch.client.mode = 'local'
  }
  test {
      elasticSearch {
          client.mode = 'local'
          index.store.type = 'memory' // store local node in memory and not on disk
      }
  }
  production {
    elasticSearch.client.mode = 'node'
  }
}
// log4j configuration
log4j = {
    // Example of changing the log pattern for the default console
    // appender:
    //
    //appenders {
    //    console name:'stdout', layout:pattern(conversionPattern: '%c{2} %m%n')
    //}
//   System.setProperty('mail.smtp.port', mail.error.port.toString())
//   System.setProperty('mail.smtp.starttls.enable',  mail.error.starttls.toString())

    println "Log4j consoleLevel"

    appenders {
        rollingFile  name:'infoLog', file:'/tmp/cytomine-info.log', threshold: org.apache.log4j.Level.INFO, maxFileSize:1024
        rollingFile  name:'warnLog', file:'/tmp/cytomine-warn.log', threshold: org.apache.log4j.Level.WARN, maxFileSize:1024
        rollingFile  name:'errorLog', file:'/tmp/cytomine-error.log', threshold: org.apache.log4j.Level.ERROR, maxFileSize:1024
        rollingFile  name:'custom', file:'/tmp/cytomine-custom.log', maxFileSize:1024
    }


    /*appenders {
	    'null' name:'stacktrace'
        rollingFile name:"logfile", maxFileSize:'300kB',
                layout:pattern(conversionPattern: "%d{[ dd.MM.yy HH:mm:ss.SSS]} [%t] %-5p %c %x - %m%n"),
                file:"/tmp/cytomine.log"
//       appender new org.apache.log4j.net.SMTPAppender(name:'mail', to:'cytomine.ulg@gmail.com', from:'cytomine.ulg@gmail.com', subject:'[Application Error]',
//               SMTPHost:'smtp.gmail.com', SMTPUsername:'cytomine.ulg@gmail.com', SMTPPassword: 'C3=8wj9R',
//               layout: pattern(conversionPattern: '%d{[ dd.MM.yy HH:mm:ss.SSS]} [%t] %n%-5p %n%c %n%C %n %x %n %m%n %n'),
//               threshold:org.apache.log4j.Level.ERROR)
    }*/

    error  'org.codehaus.groovy.grails.web.servlet',  //  controllers
            'org.codehaus.groovy.grails.web.pages', //  GSP
            'org.codehaus.groovy.grails.web.sitemesh', //  layouts
            'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
            'org.codehaus.groovy.grails.web.mapping', // URL mapping
            'org.codehaus.groovy.grails.commons', // core / classloading
            'org.codehaus.groovy.grails.plugins', // plugins
            'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
            'org.springframework',
            'net.sf.ehcache.hibernate',
            'org.hibernate.engine.StatefulPersistenceContext.ProxyWarnLog'

    error 'org.springframework.security.web.context', 'org.hibernate.engine','net.sf.hibernate.impl.SessionImpl'




    environments {
        production {
            root {
                info 'appLog',"logfile"
//               error  'mail'
                additivity = true
            }
        }
        development {
            root {
                info 'appLog',"logfile", stdout
                additivity = true
            }
        }
        test {
            root {
                info 'appLog',"logfile", stdout
                additivity = true
            }
        }
        perf {
            root {
                info 'appLog',"logfile", stdout
                additivity = true
            }
        }
    }
    //debug "org.hibernate.SQL"
    /*debug 'be.cytomine'
   debug 'grails.app'
   debug 'grails.app.services'
   debug 'grails.app.controllers*/

    //UNCOMMENT THESE 2 LINES TO SEE SQL REQUEST AND THEIR PARAMETERS VALUES
//   debug 'org.hibernate.SQL'
//   trace 'org.hibernate.type'
}


// Added by the Spring Security Core plugin:
grails.plugins.springsecurity.userLookup.userDomainClassName = 'be.cytomine.security.SecUser'
grails.plugins.springsecurity.userLookup.passwordPropertyName = 'password'
grails.plugins.springsecurity.userLookup.authorityJoinClassName = 'be.cytomine.security.SecUserSecRole'
grails.plugins.springsecurity.authority.className = 'be.cytomine.security.SecRole'
grails.plugins.springsecurity.projectClass = 'be.cytomine.project.Project'
grails.plugins.springsecurity.rememberMe.parameter = 'remember_me'
grails.plugins.springsecurity.controllerAnnotations.staticRules = [
        '/securityInfo/**': ['ROLE_ADMIN']
]


grails.plugins.dynamicController.mixins = [
        'com.burtbeckwith.grails.plugins.appinfo.IndexControllerMixin':
                'com.burtbeckwith.appinfo_test.AdminManageController',

        'com.burtbeckwith.grails.plugins.appinfo.Log4jControllerMixin' :
                'com.burtbeckwith.appinfo_test.AdminManageController',

        'com.burtbeckwith.grails.plugins.appinfo.SpringControllerMixin' :
                'com.burtbeckwith.appinfo_test.AdminManageController',

        'com.burtbeckwith.grails.plugins.appinfo.MemoryControllerMixin' :
                'com.burtbeckwith.appinfo_test.AdminManageController',

        'com.burtbeckwith.grails.plugins.appinfo.PropertiesControllerMixin' :
                'com.burtbeckwith.appinfo_test.AdminManageController',

        'com.burtbeckwith.grails.plugins.appinfo.ScopesControllerMixin' :
                'com.burtbeckwith.appinfo_test.AdminManageController',

        'com.burtbeckwith.grails.plugins.appinfo.ThreadsControllerMixin' :
                'com.burtbeckwith.appinfo_test.AdminManageController',

        'app.info.custom.example.MyConfigControllerMixin' :
                'com.burtbeckwith.appinfo_test.AdminManageController'
]


grails.plugins.springsecurity.controllerAnnotations.staticRules = [
        '/admin/manage/**': ['ROLE_ADMIN']
]
