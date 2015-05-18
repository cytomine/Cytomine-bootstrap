/*
* Copyright (c) 2009-2015. Authors: see NOTICE file.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

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
//grails.project.fork = [
//        test: false,
//        run: false,
//        war: false,
//        console: false
//]

//UNCOMMENT TO HAVE AUTO RELOADING
grails.project.fork = [
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
]


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
        mavenLocal()
        mavenCentral()
        // For Geb snapshot
        mavenRepo "https://nexus.codehaus.org/content/repositories/snapshots"
        mavenRepo 'https://noams.artifactoryonline.com/noams/grails-jaxrs-plugin-snapshots'
        mavenRepo 'http://maven.restlet.org'
        mavenRepo "http://www.hibernatespatial.org/repository"
        mavenRepo "http://repository.ow2.org/nexus/content/repositories/public"
        mavenRepo "http://repository.ow2.org/nexus/content/repositories/ow2-legacy"
        mavenRepo "http://repo.grails.org/grails/core"
    }
    dependencies {
        test "org.spockframework:spock-grails-support:0.7-groovy-2.0"
        runtime 'postgresql:postgresql:9.1-901.jdbc4'
        runtime 'com.rabbitmq:amqp-client:3.4.4'
        compile "javax.validation:validation-api:1.1.0.Final"
        runtime "org.hibernate:hibernate-validator:5.0.3.Final"
        compile 'commons-beanutils:commons-beanutils:1.8.3'
    }
    plugins {
        compile ":mongodb:3.0.2"
        runtime ':hibernate4:4.3.5.5'
        build ':tomcat:7.0.54'
        compile ':cache:1.1.7'
        compile ':asset-pipeline:1.9.6'
        compile ":grails-melody:1.49.0"
        compile ":rest-api-doc:0.4.1"
        compile ":rest:0.8"
        compile ':spring-security-core:2.0-RC4'
        compile ":spring-security-acl:2.0-RC2"
        compile ':spring-security-appinfo:2.0-RC2'
        compile ":spring-security-cas:2.0-RC1"
        compile ":spring-security-ldap:2.0-RC2"
        runtime ':export:1.6'
        compile ":quartz:1.0.1"
        runtime ":quartz-monitor:0.3-RC3"
        runtime ":database-migration:1.3.8"
        runtime ":resources:1.2.8"
        compile ":executor:0.3"
        test ":code-coverage:2.0.3-3"
        compile ":mail:1.0.7"
        test(":spock:0.7") {
            exclude "spock-grails-support"
        }
        test ":geb:0.9.0"
        compile ':webxml:1.4.1'

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
