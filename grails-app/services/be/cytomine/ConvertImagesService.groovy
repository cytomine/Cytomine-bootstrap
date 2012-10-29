package be.cytomine

import be.cytomine.image.UploadedFile
import be.cytomine.security.SecUser
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils


class ConvertImagesService {

    static transactional = true

    def convertUploadedFile(UploadedFile uploadedFile, SecUser currentUser) {
        SpringSecurityUtils.reauthenticate currentUser.getUsername(), null
        //Check if file mime is allowed
        if (!UploadedFile.allowedMime.plus(UploadedFile.mimeToConvert).contains(uploadedFile.getExt())) {
            log.info uploadedFile.getFilename() + " : FORMAT NOT ALLOWED"
            uploadedFile.setStatus(UploadedFile.ERROR_FORMAT)
            uploadedFile.save(flush : true)
            return true
        }

        //Check if file must be converted or not...
        if (!UploadedFile.mimeToConvert.contains(uploadedFile.getExt())) {
            log.info uploadedFile.getFilename() + " : CONVERTED"
            uploadedFile.setStatus(UploadedFile.CONVERTED)
            uploadedFile.setConvertedExt(uploadedFile.getExt())
            uploadedFile.setConvertedFilename(uploadedFile.getFilename())
            uploadedFile.save(flush : true)
            return true
        } else {

            //..if yes. Convert it
            String convertFileName = uploadedFile.getFilename()
            convertFileName = convertFileName[0 .. (convertFileName.size() - uploadedFile.getExt().size() - 2)]
            convertFileName = convertFileName + "_converted.tif"
            String contextPath = uploadedFile.getPath().endsWith("/") ?  uploadedFile.getPath() :  uploadedFile.getPath() + "/"
            String originalFilenameFullPath = contextPath + uploadedFile.getFilename()
            String convertedFilenameFullPath = contextPath + convertFileName
            def convertCommand = """im_vips2tiff "$originalFilenameFullPath" "$convertedFilenameFullPath":jpeg:95,tile:256x256,pyramid"""
            log.info convertCommand
            /*def convertCommand = """"$originalFilenameFullPath" -define tiff:tile-geometry=256x256 -compress jpeg 'ptif:$convertedFilenameFullPath'"""*/
            log.info convertCommand
            try {
                def ant = new AntBuilder()   // create an antbuilder
                ant.exec(outputproperty:"cmdOut",
                        errorproperty: "cmdErr",
                        resultproperty:"cmdExit",
                        failonerror: "true",
                        executable: 'vips') {
                    arg(line:convertCommand)
                }
                log.info "return code:  ${ant.project.properties.cmdExit}"
                log.info "stderr:         ${ant.project.properties.cmdErr}"
                log.info "stdout:        ${ ant.project.properties.cmdOut}"
                /*def proc = convertCommand.execute()
                proc.waitFor()*/
                //if (ant.project.properties.cmdExit == 0) { //success
                uploadedFile.setStatus(UploadedFile.CONVERTED)
                /*} else {
                    uploadFile.setStatus(UploadedFile.ERROR_FORMAT)
                }*/
                uploadedFile.setConvertedFilename(convertFileName)
                uploadedFile.setConvertedExt("tif")
                uploadedFile.save(flush : true)

            } catch (Exception e) {
                e.printStackTrace()
                uploadedFile.setStatus(UploadedFile.ERROR_FORMAT)
                uploadedFile.save(flush : true)
            }
            return true
        }
    }
}
