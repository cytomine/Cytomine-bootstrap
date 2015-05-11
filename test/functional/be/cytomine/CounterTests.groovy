package be.cytomine

import be.cytomine.image.ImageInstance
import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.ontology.ReviewedAnnotation
import be.cytomine.ontology.UserAnnotation
import be.cytomine.project.Project
import be.cytomine.test.BasicInstanceBuilder
import grails.util.Holders

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 8/02/11
 * Time: 9:01
 * To change this template use File | Settings | File Templates.
 */
class CounterTests {


    def counterService = Holders.getGrailsApplication().getMainContext().getBean("counterService")

   void testCounter() {

       //create 1 project p
       Project project = BasicInstanceBuilder.getProjectNotExist(true)

       //check if project p has 0 for each counter
       refreshCounter()
       checkCounterAnnotation(project,0,0,0,0)

       //add 1 images
       ImageInstance image = BasicInstanceBuilder.getImageInstanceNotExist(project,true)

       //check if project p has 0 for each counter, 1 for image
       //check if image has 0 for annotations counter
       refreshCounter()
       checkCounterAnnotation(project,1,0,0,0)
       checkCounterAnnotation(image,0,0,0)

       //add 3 annotations on image (2user 2 review, 2 algo)
       println  "create user annotation 1"
       UserAnnotation ua1 = BasicInstanceBuilder.getUserAnnotationNotExist(project,image,true)
       UserAnnotation ua2 = BasicInstanceBuilder.getUserAnnotationNotExist(project,image,true)
       println  "create algo annotation 1"
       AlgoAnnotation aa1 = BasicInstanceBuilder.getAlgoAnnotationNotExist(image,true)
       println  "create algo annotation 2"
       AlgoAnnotation aa2 = BasicInstanceBuilder.getAlgoAnnotationNotExist(image,true)
       println  "create reviewed annotation 1"
       ReviewedAnnotation ra1 = BasicInstanceBuilder.getReviewedAnnotationNotExist(image,true)
       println  "create reviewed annotation 2"
       ReviewedAnnotation ra2 = BasicInstanceBuilder.getReviewedAnnotationNotExist(image,true)


       //check if project p has 2 annotations for each class, 1 for image
       //check if image has 2 annotations for each clas
       refreshCounter()
       checkCounterAnnotation(project,1,2,2,2)
       checkCounterAnnotation(image,2,2,2)

       //remove 1 annotations of each class
       ua2.delete(flush:true)
       aa2.delete(flush:true)
       ra2.delete(flush:true)

       //check if project p has 1 annotations for each class, 1 for image
        //check if image has 1 annotations for each class
       refreshCounter()
       checkCounterAnnotation(project,1,1,1,1)
       checkCounterAnnotation(image,1,1,1)

       //remove image
       image.deleted = new Date()
       BasicInstanceBuilder.saveDomain(image)

       //check if project p has 0 annotations for each class, 0 for image
       refreshCounter()
       checkCounterAnnotation(project,0,0,0,0)

       //restore image
       image.deleted = null
       BasicInstanceBuilder.saveDomain(image)
       refreshCounter()
       checkCounterAnnotation(project,1,1,1,1)
       checkCounterAnnotation(image,1,1,1)

   }

    private void checkCounterAnnotation(ImageInstance image, long user, long algo, long reviewed) {
        image.refresh()
        assert image.countImageAnnotations == user
        assert image.countImageJobAnnotations == algo
        assert image.countImageReviewedAnnotations == reviewed
    }

    private void checkCounterAnnotation(Project project, long image, long user, long algo, long reviewed) {
        project.refresh()
        assert project.countImages == image
        assert project.countAnnotations == user
        assert project.countJobAnnotations == algo
        assert project.countReviewedAnnotations == reviewed
    }

    private void refreshCounter() {
        counterService.refreshCounter()
    }
}
