package be.cytomine

import be.cytomine.project.Project

abstract class CytomineDomain {

    def sequenceService
    Long id
    Date created
    Date updated

    static mapping = {
        id generator: "assigned"
    }

    static constraints = {
        created nullable: true
        updated nullable: true
    }

    public beforeInsert() {
        if (!created) {
            created = new Date()
        }
        if (id == null)
            id = sequenceService.generateID(this)
    }

    public beforeUpdate() {
        updated = new Date()
    }

    /**
     * Return domain project (annotation project, ...)
     * By default, a domain has no project.
     * You need to override getProject() in domain class
     * @return Domain project
     */
    public Project projectDomain() {
        return null;
    }


}
