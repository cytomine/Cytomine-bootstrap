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
            if (images.size() > 0) project.setCountAnnotations(Annotation.countByImageInList(images))
            else project.setCountAnnotations(0L)
            project.setCountImages(ImageInstance.countByProject(project))
            project.save()
        }
        ImageInstance.list().each { image ->
            image.setCountImageAnnotations(Annotation.countByImage(image))
            image.save()
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
