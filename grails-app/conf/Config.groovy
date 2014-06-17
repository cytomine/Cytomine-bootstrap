import grails.converters.JSON
import grails.util.Environment

// locations to search for config files that get merged into the main config
// config files can either be Java properties files or ConfigSlurper scripts

// grails.config.locations = [ "classpath:${appName}-config.properties",
//                             "classpath:${appName}-config.groovy",
//                             "file:${userHome}/.grails/${appName}-config.properties",
//                             "file:${userHome}/.grails/${appName}-config.groovy"]

// if(System.properties["${appName}.config.location"]) {
//    grails.config.locations << "file:" + System.properties["${appName}.config.location"]
// }

grails.databinding.convertEmptyStringsToNull = false
JSON.use('default')
grails.config.locations = ["file:${userHome}/.grails/cytomineconfig.groovy"]
println "###########################################################################"
println "###########################################################################"
println grails.config.locations
println "###########################################################################"
println "###########################################################################"
grails.project.groupId = appName // change this to alter the default package name and Maven publishing destination
grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [
        json: ['application/json','text/json'],
        jsonp: 'application/javascript',
        html: ['text/html','application/xhtml+xml'],
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
cytomine.maxRequestSize = 10485760
storage_path="/Users/stevben/cytomine_storage" //default path for image locations
iipImageServer="http://localhost:8081" //default path for iip image server

rabbitmq.username = 'admin'
rabbitmq.password = 'mypass'
rabbitmq.hostname = 'rabbitmq'
// URL Mapping Cache Max Size, defaults to 5000
//grails.urlmapping.cache.maxsize = 1000

// The default codec used to encode data with ${}
grails.views.default.codec = "none" // none, html, base64
grails.views.gsp.encoding = "UTF-8"
grails.converters.encoding = "UTF-8"
grails.converters.json.default.deep = false
//grails.converters.json.date = "javascript"
//grails.converters.xml.date = "javascript"

grails.doc.title="Cytomine"
grails.doc.subtitle="Documentation"
grails.doc.authors="Vincent Botta, Marée Raphaël, Loïc Rollus, Benjamin Stévens"
grails.doc.license="Commercial"
grails.doc.copyright="University of liège"
grails.doc.footer="www.cytomine.be"


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
    scratch {
        grails.serverURL = "http://localhost:8080"
        grails.uploadURL = "http://localhost:9090"

        grails.imageServerURL = "http://localhost:9080"
        grails.converters.default.pretty.print = true
        grails.plugins.springsecurity.useBasicAuth = false
        grails.resources.adhoc.patterns = ['/images/*', '/css/*', '/js/*', '/plugins/*']
    }
    production {
        //grails.serverURL = "http://shareview.ecampus.ulg.ac.be"
        grails.serverURL = "http://localhost:8086"
        // "http://localhost:8080"
        grails.uploadURL = "http://localhost:9090"
//        grails.converters.default.pretty.print = true
        grails.plugins.springsecurity.useBasicAuth = false
        grails.resources.adhoc.patterns = ['/images/*', '/css/*', '/js/*', '/plugins/*']
    }
    development {
        grails.serverURL = "http://localhost:8080"
        //if test with VM windows 7
        //grails.serverURL = "http://10.0.2.2:8080"
        grails.uploadURL = "http://upload.cytomine.be"
        //grails.uploadURL = "http://localhost:9090"
        grails.imageServerURL = "http://localhost:9080"
        grails.converters.default.pretty.print = true
        grails.plugins.springsecurity.useBasicAuth = false
        grails.resources.adhoc.patterns = ['/images/*', '/css/*', '/js/*', '/plugins/*']
    }
    test {
        grails.serverURL = "http://localhost:8090"
        grails.plugins.springsecurity.useBasicAuth = true
        grails.plugins.springsecurity.basic.realmName = "Cytomine log"
        grails.resources.adhoc.patterns = ['/images/*', '/css/*', '/js/*', '/plugins/*']
    }
    testrun {
        grails.serverURL = "http://localhost:8090"
        grails.uploadURL = "http://localhost:9090"
        grails.imageServerURL = "http://localhost:9080"
        grails.converters.default.pretty.print = true
        grails.plugins.springsecurity.useBasicAuth = false
        grails.resources.adhoc.patterns = ['/images/*', '/css/*', '/js/*', '/plugins/*']
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
        grails.resources.processing.enabled = false;
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
        console name: 'stdout', layout: pattern(conversionPattern: '%d{dd-MM-yyyy HH:mm:ss,SSS} %5p %c{1} - %m%n')
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
    // debug 'org.springframework.security'



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
                info 'appLog',"logfile", 'stdout'
                additivity = true
            }
        }
        cluster {
            root {
                info 'appLog',"logfile", 'stdout'
                additivity = true
            }
        }
        test {
            root {
                info 'appLog',"logfile", 'stdout'
                additivity = true
            }
        }
        perf {
            root {
                info 'appLog',"logfile", 'stdout'
                additivity = true
            }
        }
    }



//    debug "org.hibernate.SQL"
//    debug 'be.cytomine'
    /*
   debug 'grails.app'
   debug 'grails.app.services'
   debug 'grails.app.controllers
    debug   'grails.plugins.springsecurity'
    debug   'grails.plugins.springsecurity'
    debug   'org.codehaus.groovy.grails.plugins.springsecurity'
    debug   'org.springframework.security'
    debug   'org.jasig.cas.client' */

    //UNCOMMENT THESE 2 LINES TO SEE SQL REQUEST AND THEIR PARAMETERS VALUES
//    debug 'org.hibernate.SQL'
//    trace 'org.hibernate.type'
}
grails.plugins.springsecurity.securityConfigType = "InterceptUrlMap"
grails.plugins.springsecurity.interceptUrlMap = [
        '/admin/**':    ['ROLE_ADMIN','ROLE_SUPER_ADMIN'],
        '/admincyto/**':    ['ROLE_ADMIN','ROLE_SUPER_ADMIN'],
        '/monitoring/**':    ['ROLE_ADMIN','ROLE_SUPER_ADMIN'],
        '/j_spring_security_switch_user': ['ROLE_ADMIN','ROLE_SUPER_ADMIN'],
        '/securityInfo/**': ['ROLE_ADMIN','ROLE_SUPER_ADMIN'],
        '/api/**':      ['IS_AUTHENTICATED_REMEMBERED'],
        '/lib/**':      ['IS_AUTHENTICATED_ANONYMOUSLY'],
        '/css/**':      ['IS_AUTHENTICATED_ANONYMOUSLY'],
        '/images/**':   ['IS_AUTHENTICATED_ANONYMOUSLY'],
        '/*':           ['IS_AUTHENTICATED_REMEMBERED'], //if cas authentication, active this      //beta comment
        '/login/**':    ['IS_AUTHENTICATED_ANONYMOUSLY'],
        '/logout/**':   ['IS_AUTHENTICATED_ANONYMOUSLY'],
        '/status/**':   ['IS_AUTHENTICATED_ANONYMOUSLY']
]
/* Read CAS/LDAP config. A bad thing with Grails external config is that all config data from config properties file
   is set AFTER ldap/cas config. So we read config data from file directly and we force flag (active)
   def flag = readFromConfigFile()
   if(flag) grails.config.flag = true
 */
Properties props = new Properties()
File propsFile = new File("${userHome}/.grails/cytomineconfig.groovy")

if(propsFile.exists()) {
    props.load(propsFile.newDataInputStream())
}

println "cas.active="+props.getProperty("grails.plugins.springsecurity.cas.active")
if(props.getProperty("grails.plugins.springsecurity.cas.active").toString()=="true") {
    println("enable CAS")
    grails.plugins.springsecurity.cas.useSingleSignout = true
    grails.plugins.springsecurity.cas.active = true
    grails.plugins.springsecurity.ldap.active = true
    grails.plugins.springsecurity.logout.afterLogoutUrl ='https://www.intranet.ulg.ac.be/logout?url=http://shareview.ecampus.ulg.ac.be'

} else {
    println("disable CAS")
    grails.plugins.springsecurity.cas.useSingleSignout = false
    grails.plugins.springsecurity.cas.active = false
    grails.plugins.springsecurity.ldap.active = false
    grails.plugins.springsecurity.interceptUrlMap.remove('/*')
}
grails.plugins.springsecurity.cas.loginUri = '/login'
grails.plugins.springsecurity.cas.serverUrlPrefix = 'https://www.intranet.ulg.ac.be/cas'

//allow an admin to connect as a other user
grails.plugins.springsecurity.useSwitchUserFilter = true

environments {
    development {
        //grails.plugins.springsecurity.cas.serviceUrl = 'http://localhost:8080/j_spring_cas_security_check'
        grails.plugins.springsecurity.cas.serviceUrl = 'http://shareview.ecampus.ulg.ac.be/j_spring_cas_security_check'
        //grails.serverURL = "http://shareview.ecampus.ulg.ac.be"
    }
    test {
        grails.plugins.springsecurity.cas.serviceUrl = 'http://localhost:8080/j_spring_cas_security_check'
    }
    testrun {
        grails.plugins.springsecurity.cas.serviceUrl = 'http://localhost:8090/j_spring_cas_security_check'
    }
    production {
        grails.plugins.springsecurity.cas.serviceUrl = 'http://shareview.ecampus.ulg.ac.be/j_spring_cas_security_check'
    }
}




grails.plugins.springsecurity.auth.loginFormUrl = '/'

grails.plugins.springsecurity.ldap.search.base = 'dc=ulg,dc=ac,dc=be'
grails.plugins.springsecurity.ldap.context.managerDn = 'uid=x000126,ou=specialusers,dc=ulg,dc=ac,dc=be'
grails.plugins.springsecurity.ldap.context.managerPassword = 'R5fH3qcY65nUdR3'
grails.plugins.springsecurity.ldap.context.server = 'ldap://ldap.ulg.ac.be:389'
grails.plugins.springsecurity.ldap.authorities.groupSearchBase =
        'uid=x000126,ou=specialusers,dc=ulg,dc=ac,dc=be'
grails.plugins.springsecurity.ldap.mapper.userDetailsClass= 'inetOrgPerson'// 'org.springframework.security.ldap.userdetails.InetOrgPerson'
grails.plugins.springsecurity.ldap.mapper.usePassword= false
grails.plugins.springsecurity.ldap.authorities.ignorePartialResultException = true
grails.plugins.springsecurity.ldap.authorities.retrieveDatabaseRoles = true
//
//grails.plugins.springsecurity.cas.useSingleSignout = false
//grails.plugins.springsecurity.cas.active = false
//grails.plugins.springsecurity.ldap.active = false


//CAS
//grails.plugins.springsecurity.cas.useSingleSignout = false //false for beta
//grails.plugins.springsecurity.cas.active = false //false for beta
//grails.plugins.springsecurity.cas.loginUri = '/login'
//grails.plugins.springsecurity.cas.serverUrlPrefix = 'https://www.intranet.ulg.ac.be/cas'
//grails.plugins.springsecurity.cas.serviceUrl = 'http://shareview.ecampus.ulg.ac.be/j_spring_cas_security_check'
//grails.plugins.springsecurity.logout.afterLogoutUrl ='https://www.intranet.ulg.ac.be/logout?url=http://shareview.ecampus.ulg.ac.be'
//grails.plugins.springsecurity.auth.loginFormUrl = '/'


//LDAP
//grails.plugins.springsecurity.ldap.active = false //false for beta
//grails.plugins.springsecurity.ldap.search.base = 'dc=ulg,dc=ac,dc=be'
//grails.plugins.springsecurity.ldap.context.managerDn = 'uid=x000126,ou=specialusers,dc=ulg,dc=ac,dc=be'
//grails.plugins.springsecurity.ldap.context.managerPassword = 'R5fH3qcY65nUdR3'
//grails.plugins.springsecurity.ldap.context.server = 'ldap://ldap.ulg.ac.be:389'
//grails.plugins.springsecurity.ldap.authorities.groupSearchBase =
//    'uid=x000126,ou=specialusers,dc=ulg,dc=ac,dc=be'
//grails.plugins.springsecurity.ldap.mapper.userDetailsClass= 'inetOrgPerson'// 'org.springframework.security.ldap.userdetails.InetOrgPerson'
//grails.plugins.springsecurity.ldap.mapper.usePassword= false
//grails.plugins.springsecurity.ldap.authorities.ignorePartialResultException = true
//grails.plugins.springsecurity.ldap.authorities.retrieveDatabaseRoles = true

// Added by the Spring Security Core plugin:
grails.plugins.springsecurity.userLookup.userDomainClassName = 'be.cytomine.security.SecUser'
grails.plugins.springsecurity.userLookup.passwordPropertyName = 'password'
grails.plugins.springsecurity.userLookup.authorityJoinClassName = 'be.cytomine.security.SecUserSecRole'
grails.plugins.springsecurity.authority.className = 'be.cytomine.security.SecRole'
grails.plugins.springsecurity.authority.nameField = 'authority'
grails.plugins.springsecurity.projectClass = 'be.cytomine.project.Project'
grails.plugins.springsecurity.rememberMe.parameter = 'remember_me'



//
//grails.plugins.springsecurity.controllerAnnotations.staticRules = [
//        '/securityInfo/**': ['ROLE_ADMIN'],
//       // '/monitoring/**': ['ROLE_ADMIN'],       //do not work...
//        //'/j_spring_security_switch_user': ['ROLE_SWITCH_USER', 'IS_AUTHENTICATED_FULLY'] //do not work...
//]


//grails.resources.debug=true

grails.plugins.dynamicController.mixins = [
        'com.burtbeckwith.grails.plugins.appinfo.IndexControllerMixin':       'com.burtbeckwith.appinfo_test.AdminManageController',
        'com.burtbeckwith.grails.plugins.appinfo.HibernateControllerMixin':   'com.burtbeckwith.appinfo_test.AdminManageController',
        'com.burtbeckwith.grails.plugins.appinfo.Log4jControllerMixin' :      'com.burtbeckwith.appinfo_test.AdminManageController',
        'com.burtbeckwith.grails.plugins.appinfo.SpringControllerMixin' :     'com.burtbeckwith.appinfo_test.AdminManageController',
        'com.burtbeckwith.grails.plugins.appinfo.MemoryControllerMixin' :     'com.burtbeckwith.appinfo_test.AdminManageController',
        'com.burtbeckwith.grails.plugins.appinfo.PropertiesControllerMixin' : 'com.burtbeckwith.appinfo_test.AdminManageController',
        'com.burtbeckwith.grails.plugins.appinfo.ScopesControllerMixin' :     'com.burtbeckwith.appinfo_test.AdminManageController'
]
/*grails.plugins.springsecurity.controllerAnnotations.staticRules = [
        '/admin/manage/**': ['ROLE_ADMIN']
]*/






/** RABBITMQ */
rabbitmq {
    connectionfactory {
        username = rabbitmq.username
        password = rabbitmq.password
        hostname = rabbitmq.hostname
        consumers = 5
    }
    queues = {
        cytomineQueue()
        helloWorldQueue()
        jobQueue()
        detectSampleQueue()
        //myOtherQueueName autoDelete: false, durable: true, exclusive: false, arguments: [arg1: 'val1', arg2: 'val2']
    }
}

// Uncomment and edit the following lines to start using Grails encoding & escaping improvements

/* remove this line 
// GSP settings
grails {
    views {
        gsp {
            encoding = 'UTF-8'
            htmlcodec = 'xml' // use xml escaping instead of HTML4 escaping
            codecs {
                expression = 'html' // escapes values inside null
                scriptlet = 'none' // escapes output from scriptlets in GSPs
                taglib = 'none' // escapes output from taglibs
                staticparts = 'none' // escapes output from static template parts
            }
        }
        // escapes all not-encoded output at final stage of outputting
        filteringCodecForContentType {
            //'text/html' = 'html'
        }
    }
}
remove this line */


//be.cytomine.api.doc

grails.plugins.restapidoc.docVersion = "0.1"

grails.plugins.restapidoc.basePath = "http://localhost:8080"

grails.plugins.restapidoc.customClassName = "be.cytomine.api.doc.CustomResponseDoc"

grails.plugins.restapidoc.controllerPrefix = "Rest"

grails.plugins.restapidoc.grailsDomainDefaultType = "long"


grails.plugins.restapidoc.defaultParamsQueryMultiple = [
        [name:"max",description:"Pagination: Number of record per page (default 0 = no pagination)",type:"int"],
        [name:"offset",description:"Pagination: Offset of first record (default 0 = first record)",type:"int"]
]

grails.plugins.restapidoc.defaultErrorAll = [
        "400": "Bad Request: missing parameters or bad message format",
        "401": "Unauthorized: must be auth",
        "403": "Forbidden: role error",
        "404": "Object not found"
]

grails.plugins.restapidoc.defaultErrorGet = [
        "400": "Bad Request: missing parameters or bad message format",
        "401": "Unauthorized: must be auth",
        "403": "Forbidden: role error",
        "404": "Object not found"
]

grails.plugins.restapidoc.defaultErrorPost = [
        "409": "Object already exist"
]

grails.plugins.restapidoc.defaultErrorPut = [
        "409": "Object already exist"
]



cytomine.customUI.global = [
        dashboard: ["ALL"],
        project: ["ALL"],
        ontology: ["ROLE_USER","ROLE_ADMIN"],
        storage : ["ROLE_USER","ROLE_ADMIN"],
        activity : ["ROLE_USER","ROLE_ADMIN"],
        feedback : ["ROLE_USER","ROLE_ADMIN"],
        explore : ["ALL"],
        admin : ["ROLE_ADMIN"],
        help : ["ALL"]
]

cytomine.customUI.project = [
        "project-annotations-tab":["ADMIN_PROJECT":true,"USER_PROJECT":true,"GUEST_PROJECT":true],
        "project-properties-tab":["ADMIN_PROJECT":true,"USER_PROJECT":true,"GUEST_PROJECT":true],
        "project-jobs-tab":["ADMIN_PROJECT":true,"USER_PROJECT":true,"GUEST_PROJECT":false],
        "project-configuration-tab":["ADMIN_PROJECT":true,"USER_PROJECT":false,"GUEST_PROJECT":false],
]



environments {
    cluster {
        grails {
            cache {
                enabled = false
                ehcache {
                    ehcacheXmlLocation = 'classpath:ehcache.xml' // conf/ehcache.xml
                    reloadable = false
                }
            }
        }

    }
}

//cytomine.client = "NO"

//
//if (Environment.getCurrent().name.equals("cluster"))  {
//    test "org.gebish:geb-junit4:0.9.2"
//    test "org.seleniumhq.selenium:selenium-support:2.39.0"
//    test "org.seleniumhq.selenium:selenium-firefox-driver:2.39.0"
//    test "org.seleniumhq.selenium:selenium-chrome-driver:2.39.0"
//}