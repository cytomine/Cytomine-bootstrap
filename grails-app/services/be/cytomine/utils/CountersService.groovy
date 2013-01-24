package be.cytomine.utils

import be.cytomine.image.ImageInstance
import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.ontology.ReviewedAnnotation
import be.cytomine.ontology.UserAnnotation
import be.cytomine.project.Project
import be.cytomine.social.SharedAnnotation

/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 20/07/11
 * Time: 12:17
 * Service to reset all value incr/decr by trigger
 * E.g. A project has count value for "user annotation", "algo annotation",...
 * Instead of doing this:
 * value = UserAnnotation.countByProject(project)
 * We can do something like this:
 * value = project.userAnnotationNumber
 * The value is incr/decr with a trigger on user annotation table
 *
 * We may sometimes need to reset counter with correct value if there are some service/database issue
 */
class CountersService {

    def sessionFactory

    /**
     * Update all counter with their correct value
     */
    def updateCounters() {

        Project.list().each { project ->
            //reset project counter
            log.info "update counter for project " + project.name
            def userAnnotations = UserAnnotation.countByProject(project)
            def algoAnnotations = AlgoAnnotation.countByProject(project)
            def reviewedAnnotations = ReviewedAnnotation.countByProject(project)
            def images = ImageInstance.findAllByProject(project)

            project.setCountAnnotations(userAnnotations)
            project.setCountJobAnnotations(algoAnnotations)
            project.setCountReviewedAnnotations(reviewedAnnotations)
            project.setCountImages(images.size())
            project.save(flush: true)

            images.each { image ->
                //reset image counter
                log.info "update counter for image " + image.id
                def userAnnotationsImage = UserAnnotation.countByProjectAndImage(project,image)
                def algoAnnotationsImage = AlgoAnnotation.countByProjectAndImage(project,image)
                def reviewedAnnotationsImage = ReviewedAnnotation.countByProjectAndImage(project,image)

                if (image.getCountImageAnnotations() != userAnnotationsImage || image.getCountImageJobAnnotations() != algoAnnotationsImage) {
                    image.setCountImageAnnotations(userAnnotationsImage)
                    image.setCountImageJobAnnotations(algoAnnotationsImage)
                    image.setCountImageReviewedAnnotations(reviewedAnnotationsImage)
                    image.save(flush: true)
                }

            }
        }

        //reset annotation comments
        sessionFactory.getCurrentSession().clear();
        def connection = sessionFactory.currentSession.connection()

        try {
            def statement = connection.createStatement()
            statement.execute("update user_annotation set count_comments = 0;")
        } catch (org.postgresql.util.PSQLException e) {
            log.info e
        }

        Collection<UserAnnotation> annotations = (Collection<UserAnnotation>) SharedAnnotation.list().collect { it.userAnnotation }.unique()
        annotations.each { annotation ->
            long countComments = (long) SharedAnnotation.countByUserAnnotation(annotation)

            annotation.setCountComments(countComments)
            annotation.save(flush: true)
        }
    }
}
