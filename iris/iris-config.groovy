
// THIS IS THE COMMON EXTERNAL CYTOMINE IRIS CONFIGURATION FILE
// use ConfigSlurper Syntax to configure the settings
import org.apache.log4j.DailyRollingFileAppender
println "loading general config..."

// Cytomine core settings
grails.cytomine = [
        image : [
                host : "IMS_URL"
        ],
        host : "http://CORE_URL",
        web : "http://www.cytomine.be", 
		apps: [
			iris : [
		        server : [
		                admin : [
		                        name : "Philipp Kainz",
		                        organization: "Medical University of Graz",
		                        email : "philipp.kainz@medunigraz.at"
		                ]
		        ],
		        // configure a demo project for this IRIS instance which will always be enabled to its users
		        // if none is specified, all projects will be disabled by default
		        demoProject : [
		                cmID : 151637920,
		        ],
				sync : [:]
			]
		]
	]
	
// backend and DB settings
grails.logging.jul.usebridge = true
grails.dbconsole.enabled = true
grails.dbconsole.urlRoot = '/admin/dbconsole'


// log4j configuration
def catalinaBase = System.properties.getProperty('catalina.base')
if (!catalinaBase) catalinaBase = '.'
def logDirectory = (catalinaBase + "/logs")

log4j = {
    appenders {
        console name: 'stdout', layout: pattern(conversionPattern: '%d [%t] %-5p %c{2} - %m%n')
        appender new DailyRollingFileAppender(
                name: 'dailyFileAppender',
                datePattern: "'.'yyyy-MM-dd",  // See the API for all patterns.
                fileName: (logDirectory+"/iris/iris.log"),
                layout: pattern(conversionPattern: '%d [%t] %-5p %c{2} %x - %m%n')
        )

        rollingFile name: "stacktrace", maxFileSize: 4096,
                file: (logDirectory+"/iris/iris-stacktrace.log")
    }

    root {
        info 'stdout', 'dailyFileAppender'
    }

    // common logging
    error 'org.codehaus.groovy.grails.web.servlet',        // controllers
            'org.codehaus.groovy.grails.web.pages',          // GSP
            'org.codehaus.groovy.grails.web.sitemesh',       // layouts
            'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
            'org.codehaus.groovy.grails.web.mapping',        // URL mapping
            'org.codehaus.groovy.grails.commons',            // core / classloading
            'org.codehaus.groovy.grails.plugins',            // plugins
            'org.codehaus.groovy.grails.orm.hibernate',      // hibernate integration
            'org.springframework',
            'org.hibernate',
            'net.sf.ehcache.hibernate'

//	trace 'org.hibernate.type.descriptor.sql.BasicBinder'

    // cytomine java client
    warn 'be.cytomine.client'

    environments {
        development {
            debug 	'grails.app.controllers',
					'grails.app.services',
					'be.cytomine.apps.iris'
					// 'org.hibernate.SQL',
					'grails.assets'

            debug 'grails.app.jobs'
        }
        production {
            // let the application run in debug log mode
            debug 	'grails.app.controllers',
                   	'grails.app.services',
                   	'be.cytomine.apps.iris',
                   	'grails.app.jobs'
        }
    }
}

println 'loaded general config.'
