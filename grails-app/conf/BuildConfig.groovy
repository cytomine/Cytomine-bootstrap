import grails.util.Environment
//grails.client = "be.cytomine.integration"
grails.servlet.version = "3.0"
grails.reload.enabled = true
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
//grails.project.war.file = "target/${appName}-${appVersion}.war"
grails.project.war.file = "target/${appName}.war"
//grails.project.dependency.resolver="ivy"
grails.project.dependency.resolver = "maven"

//UNCOMMENT TO HAVE WORKING TEST
grails.project.fork = [
        test: false,
        run: false,
        war: false,
        console: false
]

//UNCOMMENT TO HAVE AUTO RELOADING
/*grails.project.fork = [
        // configure settings for compilation JVM, note that if you alter the Groovy version forked compilation is required
        //  compile: [maxMemory: 256, minMemory: 64, debug: false, maxPerm: 256, daemon:true],
       // test: false,
        // configure settings for the test-app JVM, uses the daemon by default
        test: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, daemon:true],
        // configure settings for the run-app JVM
        run: [maxMemory: 1024*6, minMemory: 1024*2, debug: false, maxPerm: 512, forkReserve:false],
        // configure settings for the run-war JVM
        war: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, forkReserve:false],
        // configure settings for the Console UI JVM
        console: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256]
]*/

//grails.plugin.location."grails-jsondoc" = "/Users/stevben/Cytomine/github/grails-jsondoc"

//grails.plugin.location.restapidoc = "../RestApiDoc"

println "********************************************"
println grails
println grailsApplication

//if(grails.client=="be.cytomine.integration") {
//    grails.plugin.location.integration = "../Core-plugins/be.cytomine.integration"
//}
//grails.plugin.location."cookie-session" = "../grails-cookie-session-v2"

//grails.plugin.location."database-session" = "../grails-database-session"

grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
//        excludes 'ehcache'
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
        //mavenRepo "http://repo.spring.io/milestone/"



        //comment because not available
        //mavenRepo "http://www.terracotta.org/download/reflector/releases"

        mavenRepo "http://repository.ow2.org/nexus/content/repositories/public"

        mavenRepo "http://repository.ow2.org/nexus/content/repositories/ow2-legacy"

        mavenRepo "http://repo.grails.org/grails/core"


    }
    dependencies {
//        test "org.spockframework:spock-grails-support:0.7-groovy-2.0"
//        test "org.codehaus.geb:geb-spock:0.7.2"

        test "org.spockframework:spock-grails-support:0.7-groovy-2.0"



        //runtime "postgresql:postgresql:9.0-801.jdbc4"
        //   postgresql-9.0-801.jdbc4.jar

        //for cluster
//        runtime 'net.sf.ehcache:ehcache-core:2.6.6'
//        runtime 'net.sf.ehcache:ehcache-terracotta:2.6.6'

        runtime 'postgresql:postgresql:9.1-901.jdbc4'
//        runtime ('org.hibernatespatial:hibernate-spatial-postgis:1.1.1') {
//            exclude 'org.hibernate:hibernate-core:3.6.0.Final'
//        }
//        runtime 'com.vividsolutions:jts:1.13'
//        runtime 'org.postgis:postgis-jdbc:1.5.2'



//        environments {
//            cluster {

//
//            }
//        }
//       if (Environment.getCurrent().name.equals("cluster"))  {
        //for cache with terra 3.5.x
//        runtime 'net.sf.ehcache:ehcache-core:2.4.6'
//        runtime 'net.sf.ehcache:ehcache-terracotta:2.4.6'
//        //for session
//        runtime "org.terracotta:terracotta-toolkit-1.6-runtime:5.5.0"
//        runtime "org.terracotta.session:terracotta-session:1.3.5"

//      }
        compile 'commons-beanutils:commons-beanutils:1.8.3'
    }
    plugins {


        compile ":grails-melody:1.49.0"
        compile ":mongodb:3.0.2"
//        compile (':hibernate:3.6.10.17') {
//            excludes('hibernate-ehcache')
//        }
        runtime ':hibernate4:4.3.5.5'

        build ':tomcat:7.0.54'
        compile ':cache:1.1.7'
//        compile ':scaffolding:2.1.2'
        compile ':asset-pipeline:1.9.6'


        //cytomine.client

        compile ":rest-api-doc:0.4.1"

        compile ":rest:0.8"

//               build ":tomcat:$grailsVersion" //
//        runtime ":hibernate:$grailsVersion" //
//        runtime ":hibernate:3.6.10.1"

//        build ':tomcat:7.0.47'

//        runtime ':hibernate:3.6.10.6'

//        compile ':spring-security-core:2.0-RC2'
//        compile ":spring-security-acl:2.0-RC1"
//        compile ":spring-security-appinfo:2.0-RC2"
//		compile ":spring-security-cas:2.0-RC1"
//		compile ":spring-security-ldap:2.0-RC2"

        compile ':spring-security-core:2.0-RC4'
        //runtime ':spring-security-core:1.2.7.3'
        compile ":spring-security-acl:2.0-RC2"
        compile ':spring-security-appinfo:2.0-RC2'
        compile ":spring-security-cas:2.0-RC1"
        compile ":spring-security-ldap:2.0-RC2"

        runtime ':background-thread:1.6'
        runtime ':export:1.6'
        //runtime ':twitter-bootstrap:3.0.3'
        runtime ":rabbitmq:1.0.0"
        compile ":quartz:1.0.1"
        runtime ":quartz-monitor:0.3-RC3"
        runtime ":database-migration:1.3.8"
        runtime ":resources:1.2.8"
        //runtime ':jquery:1.8.3'
        compile ":executor:0.3"
        //test ':code-coverage:1.2.7'
        test ":code-coverage:2.0.3-3"
        compile ":mail:1.0.7"
        test(":spock:0.7") {
            exclude "spock-grails-support"
        }
        test ":geb:0.9.0"

        //compile ":cookie-session:2.0.15"


        compile ':webxml:1.4.1'
//        compile 'RobertFischer:database-session:1.2.3'
        // compile ":database-session:1.2.1"

        //CHANGE MADE FOR 2.4.2

        //cookie-session:2.0.15

        //export1.6

        //background-thread 1.6

        //acl
        //https://github.com/farko88/grails-spring-security-acl/commit/067fb06f19e0530c9414261a600df4f4538fcbb2

    }
}
// Remove the DisableOptimizationsTransformation jar before the war is bundled
//This jar is usefull for test coverage
grails.war.resources = { stagingDir ->
    delete(file:"${stagingDir}/WEB-INF/lib/DisableOptimizationsTransformation-0.1-SNAPSHOT.jar")
}
coverage {
    exclusions = [
            "**/be/cytomine/utils/bootstrap/**",
            "**/be/cytomine/data/**",
            "**/be/cytomine/processing/job/**",
            "**/be/cytomine/processing/image/filters/**",
            "**/be/cytomine/job/**",
            "**/twitter/bootstrap**"
    ]
}