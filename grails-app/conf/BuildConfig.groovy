grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
//grails.project.war.file = "target/${appName}-${appVersion}.war"
grails.project.war.file = "target/${appName}.war"
grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    checksums true // Whether to verify checksums on resolve
    legacyResolve false // whether to do a secondary resolve on plugin installation, not advised and here for backwards compatibility

    repositories {
        grailsPlugins()
        grailsHome()
        grailsCentral()

        // uncomment the below to enable remote dependency resolution
        // from public Maven repositories
        //mavenLocal()
        //mavenCentral()
        //mavenRepo "http://snapshots.repository.codehaus.org"
        //mavenRepo "http://repository.codehaus.org"
        //mavenRepo "http://download.java.net/maven/2/"
        //mavenRepo "http://repository.jboss.com/maven2/"
        mavenLocal()
        mavenCentral()

        // For Geb snapshot
        mavenRepo "https://nexus.codehaus.org/content/repositories/snapshots"
    }
    dependencies {

    }
    plugins {
        // Uncomment these (or add new ones) to enable additional resources capabilities
        //runtime ":zipped-resources:1.0"
        //runtime ":cached-resources:1.0"
        //runtime ":yui-minify-resources:0.1.4"
        build ":tomcat:$grailsVersion"

        runtime ":hibernate:$grailsVersion"
        runtime ':spring-security-core:1.2.7.3'
        runtime ':spring-security-acl:1.1.1'
        runtime ':spring-security-appinfo:1.0'
        runtime ':background-thread:1.6'
        runtime ':export:1.5'
        runtime ':fields:1.3'

        runtime ':twitter-bootstrap:2.0.4'
        runtime ':quartz:1.0-RC5'
        runtime ':quartz-monitor:0.3-RC1'
        runtime ':rest:0.7'
        runtime ':cache:1.0.1'
        runtime ':database-migration:1.2.1'
        runtime ":resources:1.2.RC2"
        runtime ':jquery:1.8.3'

        test ':code-coverage:1.2'
        test ':selenium-rc:1.0.2'
        test ':functional-test:2.0.RC1'

    }
}
coverage {
	exclusions = [
            "**/be/cytomine/data/**",
            "**/be/cytomine/processing/job/**",
            "**/be/cytomine/processing/image/filters/**"
    ]
}

