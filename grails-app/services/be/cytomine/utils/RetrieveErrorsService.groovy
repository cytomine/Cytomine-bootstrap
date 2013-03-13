package be.cytomine.utils

class RetrieveErrorsService {

    def grailsApplication
    def messageSource

    def initMethods() {
        grailsApplication.domainClasses.each {domainClass ->//iterate over the domainClasses
            if (domainClass.clazz.name.contains("be.cytomine")) {//only add it to the domains in my plugin

                domainClass.metaClass.retrieveErrors = {
                    def list = delegate?.errors?.allErrors?.collect {messageSource.getMessage(it, null)}
                    return list?.join('\n')
                }
            }
        }
    }
}
