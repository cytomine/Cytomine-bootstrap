package cytomine.web

import be.cytomine.project.Project
import be.cytomine.ontology.Annotation
import be.cytomine.image.ImageInstance

/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 20/07/11
 * Time: 12:17
 */
class CountersService {

    def updateCounters() {
        Project.list().each { project ->
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
        }
    }
}
