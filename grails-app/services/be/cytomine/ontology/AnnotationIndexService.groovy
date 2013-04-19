package be.cytomine.ontology

import be.cytomine.Exception.InvalidRequestException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.image.ImageInstance
import be.cytomine.security.SecUser
import be.cytomine.utils.ModelService
import groovy.sql.Sql

class AnnotationIndexService {

    static transactional = true
    def modelService
    def dataSource

    def list(ImageInstance image) {
        String request = "SELECT user_id, image_id,count_annotation,count_reviewed_annotation  \n" +
                " FROM annotation_index \n" +
                " WHERE image_id = "+image.id
        def data = []
        new Sql(dataSource).eachRow(request) {
            data << [user:it[0],image: it[1], countAnnotation: it[2],countReviewedAnnotation: it[3]]
        }
        return data

    }

    def get(ImageInstance image, SecUser user) {
        AnnotationIndex.findByImageAndUser(image,user)
    }

    /**
     * Return the number of annotation created by this user for this image
     * If user is null, return the number of reviewed annotation for this image
     */
    def count(ImageInstance image, SecUser user) {
        println "count"
        println "image"
        String request
        if (user) {
            request = "SELECT count_annotation  \n" +
                    " FROM annotation_index \n" +
                    " WHERE image_id = "+image.id + " AND user_id = "+ user.id
        } else {
            request = "SELECT sum(count_reviewed_annotation)  \n" +
                    " FROM annotation_index \n" +
                    " WHERE image_id = "+image.id
        }

        long value = 0
        println request
        new Sql(dataSource).eachRow(request) {
            def val = it[0]
            val? value = val : 0
        }
        return value
    }


    def count(ImageInstance image, SecUser user, boolean reviewed) {

        def entry = AnnotationIndex.findByImageAndUser(image,user)
        if(!entry) {
            return 0
        } else {
            reviewed ? entry.countReviewedAnnotation : entry.countAnnotation
        }
    }
}
