package be.cytomine.ontology

import be.cytomine.AnnotationDomain

class ReviewedAnnotation extends AnnotationDomain{

    static hasMany = [ term: Term ]

    String parentClassName
    Long parentClassId
    int status

    static constraints = {
    }

    static mapping = {
          id generator: "assigned"
          columns {
              location type: org.hibernatespatial.GeometryUserType
          }
        term fetch: 'join'
     }

    @Override
    def terms() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    def termsId() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    def beforeInsert() {
        super.beforeInsert()
    }

    def beforeUpdate() {
        super.beforeUpdate()
    }
}
