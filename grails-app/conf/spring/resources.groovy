// Place your Spring DSL code here
beans = {
    'apiAuthentificationFilter'(cytomine.web.APIAuthentificationFilters) {
        // properties
    }
    'multipartResolver'(be.cytomine.CytomineMultipartHttpServletRequest) {
        maxUploadSize = 51200000
    }
}