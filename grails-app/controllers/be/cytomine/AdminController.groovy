package be.cytomine

import grails.plugins.springsecurity.Secured
import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass
import be.cytomine.ontology.ReviewedAnnotation

@Secured(['ROLE_ADMIN'])
class AdminController {

    def grailsApplication

    def index() { }

    def check() {
        try {
            checkDependance()

        } catch(Exception e) {
            println e
        }

    }

    private def checkDependance() {
        def allErrors = []
        grailsApplication.getDomainClasses().each { domain ->


            def columnDep = getDependencyColumn(domain)
            allErrors.addAll(checkServiceMethod(domain,columnDep))

            def columnDepHasMany = getDependencyColumnHasMany(domain)
            allErrors.addAll(checkServiceMethodInverse(domain,columnDepHasMany))

        }

        println "***********************************************************************"
        println "***********************************************************************"
        println "***********************************************************************"
        println "There are ${allErrors.size()} dependency conflict!"

        allErrors.sort().each {
            println it
        }

        if (!allErrors.isEmpty()) {
            throw new Exception(allErrors.join(","))
        }


    }

    def responseService

    private def getDependencyColumn(def domain) {
        def columnDep = []

        def domainClass = grailsApplication.getDomainClasses().find {it.name == domain.name}
        println "****** domainClass=$domainClass"

        def d = new DefaultGrailsDomainClass(domainClass.referenceInstance.class)
        d.persistentProperties.each {
            if(it.type.toString().contains("be.cytomine.")) {
                columnDep << [name:it.name, type:getClassName(it.type.toString())]
            }
        }
        columnDep
    }

    private def getDependencyColumnHasMany(def domain) {
        def columnDep = []

        def domainClass = grailsApplication.getDomainClasses().find {it.name == domain.name}

        def d = new DefaultGrailsDomainClass(domainClass.referenceInstance.class)
        d.persistentProperties.each {
            if(it.type.toString().contains("java.util.Set") || it.type.toString().contains("java.util.SortedSet") || it.type.toString().contains("java.util.List")) {
                columnDep << [name:it.name, type:getClassName(it.type.toString())]
            }
        }
        columnDep
    }


    private def checkServiceMethod(def domain, def columns) {
        //special domain that we should delete manually (not with deleteDependentXXX method)
        def domainToSkip = ["Command","AddCommand","EditCommand","DeleteCommand","CommandHistory","RedoStackItem","UndoStackItem"]
        if(domainToSkip.contains(domain.name)) {
            return []
        }


        def allErrors = []
        def fixColumn = []

        columns.each {
            println it.type
            if(it.type=="AnnotationDomain") {
                fixColumn << [name:"UserAnnotation", type:"UserAnnotation"]
                fixColumn << [name:"AlgoAnnotation", type:"AlgoAnnotation"]
                fixColumn << [name:"ReviewedAnnotation", type:"ReviewedAnnotation"]
            }else if(it.type=="User") {
                fixColumn << [name:"SecUser", type:"SecUser"]
            }else if(it.type=="UserJob") {
                fixColumn << [name:"SecUser", type:"SecUser"]
            }else {
                fixColumn << it
            }
        }

        def methodExpected = "deleteDependent"+domain.name
        println "****** methodExpected=" + methodExpected

        fixColumn.each {
            String name = it.name
            String type = it.type

            println "****** name=" + name
            println grailsApplication.getServiceClasses().collect{it.name}
            def serviceClass = grailsApplication.getServiceClasses().find {it.name.toLowerCase() == type.toLowerCase()}

            if(!serviceClass) {
                allErrors << "Service ${type}Service must exist and must contains $methodExpected($type,transaction)!!!"
            } else {

                println "****** serviceClass=" + serviceClass
                def allServiceMethods = serviceClass.metaClass.methods*.name

                //if there is no delete method, don't throw error
                boolean deleteMethodExist = false

                boolean isFind = false
                allServiceMethods.each {
                    //println it + " vs " + methodExpected + " => " + (it==methodExpected)
                    if(it==methodExpected) {
                        isFind = true
                    }

                    if(it.startsWith("delete")) {
                        deleteMethodExist = true
                    }
                }

                if(deleteMethodExist && !isFind) {
                    allErrors << "Service ${type}Service must implement $methodExpected($type,transaction)!!!"
                }


            }

        }
        allErrors
    }



    private def checkServiceMethodInverse(def domain, def columns) {
        //special domain that we should delete manually (not with deleteDependentXXX method)
        def domainToSkip = ["Command","AddCommand","EditCommand","DeleteCommand","CommandHistory","RedoStackItem","UndoStackItem"]
        if(domainToSkip.contains(domain.name)) {
            return []
        }


        def allErrors = []
        def fixColumn = []

        columns.each {
            println it.type
            if(it.type=="AnnotationDomain") {
                fixColumn << [name:it.name, type:"UserAnnotation"]
                fixColumn << [name:it.name, type:"AlgoAnnotation"]
                fixColumn << [name:it.name, type:"ReviewedAnnotation"]
            }else if(it.type=="User") {
                fixColumn << [name:it.name, type:"SecUser"]
            }else if(it.type=="UserJob") {
                fixColumn << [name:it.name, type:"SecUser"]
            }else {
                fixColumn << it
            }
        }



        fixColumn.each {

            def methodExpected = "deleteDependentHasMany" + it.name.substring(0,1).toUpperCase() + it.name.substring(1)
            println "****** methodExpected=" + methodExpected
            String name = it.name
            String type = domain.name

            println "****** name=" + name
            println grailsApplication.getServiceClasses().collect{it.name}
            def serviceClass = grailsApplication.getServiceClasses().find {it.name.toLowerCase() == type.toLowerCase()}

            if(!serviceClass) {
                allErrors << "Service ${type}Service must exist and must contains $methodExpected($type,transaction)!!!"
            } else {

                println "****** serviceClass=" + serviceClass
                def allServiceMethods = serviceClass.metaClass.methods*.name

                //if there is no delete method, don't throw error
                boolean deleteMethodExist = false

                boolean isFind = false
                allServiceMethods.each {
                    //println it + " vs " + methodExpected + " => " + (it==methodExpected)
                    if(it==methodExpected) {
                        isFind = true
                    }

                    if(it.startsWith("delete")) {
                        deleteMethodExist = true
                    }
                }

                if(deleteMethodExist && !isFind) {
                    allErrors << "Service ${type}Service must implement $methodExpected($type,transaction)!!!"
                }


            }

        }
        allErrors
    }















    private def getPropertyType(domainInstance, propertyName) {
         def domainClass = grailsApplication.getDomainClass(domainInstance.class.name)
         def prop = domainClass.properties.find { it.name == propertyName }
         return prop?.type
    }

    private String getClassName(String fullPath) {
    //be.cytomine.image.Image
        String[] array = fullPath.split("\\.")  //[be,cytomine,image,Image]
        //log.info array.length
        return array[array.length - 1] // Image
    }
}
