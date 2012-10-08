package cytomine.web

import be.cytomine.ontology.Annotation
import be.cytomine.social.SharedAnnotation

/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 20/07/11
 * Time: 12:17
 */
class CountersService {

    def sessionFactory

    def updateCounters() {
        /*Project.list().each { project ->
            println "update counter for project " + project.name
            def images = project.imagesinstance()
            def users = project.users()
            println "users = " + users
            if (images.size() > 0) {
                long totalCountAnnotations = Annotation.countByImageInList(images)
                println "totalCountAnnotations = " + totalCountAnnotations
                long totalCountAnnotationsByUser = Annotation.countByImageInListAndUserInList(images, users)
                println "totalCountAnnotationsByUser = " + totalCountAnnotationsByUser
                project.countAnnotations = totalCountAnnotationsByUser
                project.countJobAnnotations = totalCountAnnotations - totalCountAnnotationsByUser
            }
            else {
                project.setCountAnnotations(0L)
                project.setCountJobAnnotations(0L)
            }
            project.setCountImages(ImageInstance.countByProject(project))
            if (!project.validate()) {
                project.errors.each {
                    println it
                }
            } else {
                println "Project OK : " + project
                project.save(flush : true)
            }
            project.imagesinstance().each { image ->
                long totalCountAnnotations = Annotation.countByImage(image)
                long totalCountAnnotationsByUser = Annotation.countByImageAndUserInList(image, users)
                image.setCountImageAnnotations(totalCountAnnotationsByUser)
                image.setCountImageJobAnnotations(totalCountAnnotations - totalCountAnnotationsByUser)
                image.save(flush : true)
            }

        }*/



        sessionFactory.getCurrentSession().clear();
        def connection = sessionFactory.currentSession.connection()

        try {
            def statement = connection.createStatement()
            statement.execute("update annotation set count_comments = 0;")
        } catch (org.postgresql.util.PSQLException e) {
            println e
        }


        Collection<Annotation> annotations = (Collection<Annotation>) SharedAnnotation.list().collect { it.annotation }.unique()
        annotations.each { annotation ->
            long countComments = (long) SharedAnnotation.countByAnnotation(annotation)

            annotation.setCountComments(countComments)
            annotation.save()
        }
    }
}
