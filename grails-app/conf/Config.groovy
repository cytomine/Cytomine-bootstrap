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
grails.project.groupId = appName // change this to alter the default package name and Maven publishing destination
grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [ html: ['text/html','application/xhtml+xml'],
        json: ['application/json','text/json'],
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

mail.error.port = 587
mail.error.starttls = true

// set per-environment serverURL stem for creating absolute links
environments {
    production {
        grails.serverURL = "http://beta.cytomine.be"
    }
    development {
        grails.serverURL = "http://localhost:8080"
        grails.converters.default.pretty.print = true
        grails.plugins.springsecurity.useBasicAuth = true
    }
    test {
        grails.serverURL = "http://localhost:8090"
        grails.plugins.springsecurity.useBasicAuth = true
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
    System.setProperty('mail.smtp.port', mail.error.port.toString())
    System.setProperty('mail.smtp.starttls.enable',  mail.error.starttls.toString())

    println "Log4j consoleLevel"

    appenders {
        rollingFile name:"logfile", maxFileSize:'300kB',
                layout:pattern(conversionPattern: "%d{[ dd.MM.yy HH:mm:ss.SSS]} [%t] %-5p %c %x - %m%n"),
                file:"/tmp/cytomine.log"
        appender new org.apache.log4j.net.SMTPAppender(name:'mail', to:'cytomine.ulg@gmail.com', from:'cytomine.ulg@gmail.com', subject:'[Application Error]',
                SMTPHost:'smtp.gmail.com', SMTPUsername:'cytomine.ulg@gmail.com', SMTPPassword: 'C3=8wj9R',
                layout: pattern(conversionPattern: '%d{[ dd.MM.yy HH:mm:ss.SSS]} [%t] %n%-5p %n%c %n%C %n %x %n %m%n %n'),
                threshold:org.apache.log4j.Level.ERROR)
    }

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

    error 'org.codehaus.groovy.grails.plugins.springsecurity'

    info   'be.cytomine' ,'org.hibernate'




    environments {
        production {
            root {
                info 'stdout','appLog',"logfile"
                error  'mail'
                additivity = true
            }
        }
        development {
            root {
                info 'stdout','appLog',"logfile"
                additivity = true
            }
        }
        test {
            root {
                info 'stdout','appLog',"logfile"
                additivity = true
            }
        }
    }
    //debug "org.hibernate.SQL"
    debug 'be.cytomine'
    debug 'grails.app'
    debug 'grails.app.service'
    debug 'grails.app.controller'

    //UNCOMMENT THESE 2 LINES TO SEE SQL REQUEST AND THEIR PARAMETERS VALUES
    //debug 'org.hibernate.SQL'
    //trace 'org.hibernate.type'
}


// Added by the Spring Security Core plugin:
grails.plugins.springsecurity.userLookup.userDomainClassName = 'be.cytomine.security.SecUser'
grails.plugins.springsecurity.userLookup.passwordPropertyName = 'password'
grails.plugins.springsecurity.userLookup.authorityJoinClassName = 'be.cytomine.security.SecUserSecRole'
grails.plugins.springsecurity.authority.className = 'be.cytomine.security.SecRole'
grails.plugins.springsecurity.projectClass = 'be.cytomine.project.Project'

grails.plugins.springsecurity.controllerAnnotations.staticRules = [
        '/securityInfo/**': ['ROLE_ADMIN']
]
