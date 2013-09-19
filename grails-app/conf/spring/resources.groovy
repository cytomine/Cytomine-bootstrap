import be.cytomine.web.CytomineMultipartHttpServletRequest

// Place your Spring DSL code here
beans = {
    'apiAuthentificationFilter'(cytomine.web.APIAuthentificationFilters) {
        // properties
    }
    'multipartResolver'(CytomineMultipartHttpServletRequest) {
        // Max in memory 100kbytes
        maxInMemorySize=10240

        //100Gb Max upload size
        maxUploadSize=102400000000

    }

    springConfig.addAlias "springSecurityService", "springSecurityCoreSpringSecurityService"


}