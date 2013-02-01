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
        def domainToSkip = ["Command","AddCommand","EditCommand","DeleteCommand","CommandHistory","RedoStackItem","UndoStackItem"]

        def fixDomainName =
            ["AnnotationDomain" :
                    [[name:"UserAnnotation", type:"UserAnnotation"],[name:"AlgoAnnotation", type:"AlgoAnnotation"],[name:"ReviewedAnnotation", type:"ReviewedAnnotation"]],
             "User" : [[name:"SecUser", type:"SecUser"]],
             "UserJob" : [[name:"SecUser", type:"SecUser"]],
             "retrievalProjects" : [[name:"projects", type:"projects"]]
        ]


        def allErrors = []
        grailsApplication.getDomainClasses().each { domain ->
            if(!domainToSkip.contains(domain.name)) {
                def columnDep = getDependencyColumn(domain,["be.cytomine."])
                allErrors.addAll(checkServiceMethod(domain,columnDep,fixDomainName))

                def columnDepHasMany = getDependencyColumn(domain,["java.util.Set","java.util.SortedSet","java.util.List"])
                allErrors.addAll(checkServiceMethodInverse(domain,columnDepHasMany,fixDomainName))
            }
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

    private def getDependencyColumn(def domain, def dependancyFilter) {
        def columnDep = []

        def domainClass = grailsApplication.getDomainClasses().find {it.name == domain.name}

        def d = new DefaultGrailsDomainClass(domainClass.referenceInstance.class)
        d.persistentProperties.each { prop ->
            boolean contains = false
            dependancyFilter.each { filter ->
                contains = contains || prop.toString().contains(filter)
            }
            if(contains) {
                columnDep << [name:prop.name, type:getClassName(prop.type.toString())]
            }
        }
        columnDep
    }

    private boolean deleteDependentMissingMethod(def serviceClass, def methodExpected) {
        def allServiceMethods = serviceClass.metaClass.methods*.name
        //if there is no delete method, don't throw error
        boolean deleteMethodExist = false

        boolean isFind = false
        allServiceMethods.each {
            if(it==methodExpected) {
                isFind = true
            }

            if(it.startsWith("delete")) {
                deleteMethodExist = true
            }
        }

        return deleteMethodExist && !isFind


    }


    private def checkServiceMethod(def domain, def columns, def fixDomainName) {
        //special domain that we should delete manually (not with deleteDependentXXX method)
        def allErrors = []
        def fixColumn = []

        columns.each {
            if(fixDomainName.keySet().contains(it.type)) {
                fixDomainName.get(it.type).each {
                    fixColumn << it
                }
            } else {
                fixColumn << it
            }
        }

        def methodExpected = "deleteDependent"+domain.name

        fixColumn.each {
            String name = it.name
            String type = it.type

            def serviceClass = grailsApplication.getServiceClasses().find {it.name.toLowerCase() == type.toLowerCase()}

            if(!serviceClass) {
                allErrors << "Service ${type}Service must exist and must contains $methodExpected($type,transaction)!!!"
            } else {
                if(deleteDependentMissingMethod(serviceClass,methodExpected)) {
                    allErrors << "Service ${type}Service must implement $methodExpected($type,transaction)!!!"
                }
            }
        }
        allErrors
    }



    private def checkServiceMethodInverse(def domain, def columns,def fixDomainName) {

        def allErrors = []
        def fixColumn = []

        columns.each {
            if(fixDomainName.keySet().contains(it.type)) {
                fixDomainName.get(it.type).each {
                    fixColumn << it
                }
            } else {
                fixColumn << it
            }
        }

        fixColumn.each {

            def methodExpected1 = "deleteDependentHasMany" + it.name.substring(0,1).toUpperCase() + it.name.substring(1)
            def methodExpected2 = "deleteDependentHasMany" + domain.name
            String name = it.name.substring(0,it.name.size()-1)
            String type = domain.name

            def serviceClass = grailsApplication.getServiceClasses().find {it.name.toLowerCase() == type.toLowerCase()}

            if(!serviceClass) {
                allErrors << "Service ${type}Service must exist and must contains $methodExpected1($type,transaction)!!!"
                allErrors << "Service ${name}Service must exist and must contains $methodExpected2($type,transaction)!!!"
            } else {
                if(deleteDependentMissingMethod(serviceClass,methodExpected1)) {
                    allErrors << "Service ${type}Service must implement $methodExpected1($type,transaction)!!!"
                }
                if(deleteDependentMissingMethod(serviceClass,methodExpected2)) {
                    allErrors << "Service ${name}Service must implement $methodExpected2($type,transaction)!!!"
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
