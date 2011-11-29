package be.cytomine.data

import be.cytomine.image.AbstractImage
import be.cytomine.image.ImageInstance

class ExportController {

    def exportimages = {
        def records = ""
        def currentProject = null
        AbstractImage.listOrderByPath().each { image ->
            def project = ImageInstance.findByBaseImage(image).getProject()
            if (currentProject != null && currentProject != project) records += "\n]"
            if (currentProject != project) records += "\nstatic def " + project.getName().replace("-", "").replace("_", "") + "_DATA = ["
            def record = "\n["
            record += 'filename :"' + image.getPath() + '",'
            record += 'name : "' + image.getFilename() + '",'
            record += 'study : "' + project.getName() + '",'
            record += 'extension : "' + image.getMime().getExtension() + '",'
            record += 'width : "' + image.getWidth() + '",'
            record += 'height : "' + image.getHeight() + '",'
            record += 'magnification : "' + image.getMagnification() + '",'
            record += 'resolution : "' + image.getResolution() + '",';
            record += 'slidename : "' + image.getSlide().getName() + '"'
            record += "]"
            records += record
            records += ","
            currentProject = project
        }
        records += "\n]"
        render(contentType: "application/json", text: "${records}")
    }

}
