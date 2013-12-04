grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
//grails.project.war.file = "target/${appName}-${appVersion}.war"
grails.project.war.file = "target/${appName}.war"
//grails.project.dependency.resolver="ivy"
grails.project.dependency.resolver = "maven"
grails.project.fork = [ test: false, run: false, war: false, console: false ]
grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
        excludes 'httpclient'
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
        mavenRepo 'https://noams.artifactoryonline.com/noams/grails-jaxrs-plugin-snapshots'
        mavenRepo 'http://maven.restlet.org'
        mavenRepo "http://www.hibernatespatial.org/repository"
    }
    dependencies {
        test "org.spockframework:spock-grails-support:0.7-groovy-2.0"
        test "org.codehaus.geb:geb-spock:0.7.2"
        //runtime "postgresql:postgresql:9.0-801.jdbc4"
                         //   postgresql-9.0-801.jdbc4.jar

        runtime 'postgresql:postgresql:9.1-901.jdbc4'
//        runtime ('org.hibernatespatial:hibernate-spatial-postgis:1.1.1') {
//            exclude 'org.hibernate:hibernate-core:3.6.0.Final'
//        }
//        runtime 'com.vividsolutions:jts:1.13'
//        runtime 'org.postgis:postgis-jdbc:1.5.2'


    }
    plugins {
        // Uncomment these (or add new ones) to enable additional resources capabilities
        //runtime ":zipped-resources:1.0"
        //runtime ":cached-resources:1.0"
        //runtime ":yui-minify-resources:0.1.4"
        compile ":grails-melody:1.47.2"
//        compile ":svn:1.0.2"

//        build ":tomcat:7.0.42"

//        build ":tomcat:7.0.42"
//        compile ':hibernate:3.6.10.2'

//               build ":tomcat:$grailsVersion" //
//        runtime ":hibernate:$grailsVersion" //
//        runtime ":hibernate:3.6.10.1"

        build ':tomcat:7.0.42'
//        build ":jetty:2.0.3"
//        compile ':hibernate:3.6.10.3'
        runtime ':hibernate:3.6.10.4'


        runtime ':spring-security-core:1.2.7.3'
        runtime ':spring-security-acl:1.1.1'
        runtime ':spring-security-appinfo:1.0'
		compile ":spring-security-cas:1.0.5"
		compile ":spring-security-ldap:1.0.6"
        runtime ':background-thread:1.6'
        runtime ':export:1.5'
        //runtime ':fields:1.3'
        runtime ':twitter-bootstrap:2.0.4'
        runtime ":rabbitmq:1.0.0"
        runtime ":quartz:1.0-RC9"
        runtime ":quartz-monitor:0.3-RC1"
  //      runtime ':rest:0.7'
        runtime ':cache:1.0.1'
        runtime ":database-migration:1.3.8"
        runtime ":resources:1.2.RC2"
        runtime ':jquery:1.8.3'
//        runtime ":jawr:3.3.3"
        //compile ":grails-ant:0.1.3"
        compile ":executor:0.3"
		
 //       test ':code-coverage:1.2'
        test ':code-coverage:1.2.6'

//        compile (':jaxrs:0.8') {
//            excludes 'spring-core', 'spring-beans', 'spring-context', 'spring-web', 'spring-aop'
//        }
        compile ":apidocs:0.2"
//        compile ":apidocs:0.2"

//
//        test ':selenium-rc:1.0.2'
//        test ':functional-test:2.0.RC1'

        test(":spock:0.7") {
          exclude "spock-grails-support"
        }
        test ":geb:0.9.0"

    }
}
coverage {
	exclusions = [
            "**/be/cytomine/data/**",
            "**/be/cytomine/processing/job/**",
            "**/be/cytomine/processing/image/filters/**",
            "**/be/cytomine/job/**",
            "**/twitter/bootstrap**"
    ]
}

