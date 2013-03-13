package be.cytomine.image

import be.cytomine.security.SecUser

import javax.activation.MimetypesFileTypeMap

/**
 * TODOSTEVBEN:: doc + refactoring + security (?)
 */
class ConvertImagesService {

    def zipService
    def fileSystemService
    def grailsApplication

    static String MRXS_EXTENSION = "mrxs"
    static String VMS_EXTENSION = "vms"

    static transactional = true

    def convertUploadedFile(UploadedFile uploadedFile, SecUser currentUser) {
        //Check if file mime is allowed
        def allMime = UploadedFile.allowedMime.plus(UploadedFile.mimeToConvert).plus(UploadedFile.zipMime)
        if (!allMime.contains(uploadedFile.getExt())) {
            log.info uploadedFile.getFilename() + " : FORMAT NOT ALLOWED"
            uploadedFile.setStatus(UploadedFile.ERROR_FORMAT)
            uploadedFile.setConverted(false)
            uploadedFile.save(flush : true)
            return [uploadedFile]
        }

        if (UploadedFile.zipMime.contains(uploadedFile.getExt())) {
            return handleCompressedFile(uploadedFile, currentUser)
        } else {
            return handleSingleFile(uploadedFile, currentUser)
        }
    }

    private def handleSpecialFile(UploadedFile uploadedFile, SecUser currentUser, def pathsAndExtensions) {

        UploadedFile mainUploadedFile = null //mrxs or vms file
        def uploadedFiles = [] //nested files

        pathsAndExtensions.each { it ->
            if (it.extension == MRXS_EXTENSION || it.extension == VMS_EXTENSION) {
                mainUploadedFile = createNewUploadedFile(uploadedFile, it, currentUser, null)
            }
        }

        if (!mainUploadedFile) return null //ok, it's not a special file

        //create nested file
        mainUploadedFile.setStatus(UploadedFile.TO_DEPLOY)
        mainUploadedFile.setConverted(true)
        mainUploadedFile.save()
        uploadedFiles << mainUploadedFile
        pathsAndExtensions.each { it ->
            if (it.extension != MRXS_EXTENSION && it.extension != VMS_EXTENSION) {
                UploadedFile nestedUploadedFile = createNewUploadedFile(uploadedFile, it, currentUser, "application/octet-stream")
                nestedUploadedFile.setStatus(UploadedFile.CONVERTED)
                nestedUploadedFile.setParent(mainUploadedFile)
                nestedUploadedFile.setConverted(true)
                if (nestedUploadedFile.validate()) {
                    nestedUploadedFile.save()
                } else {
                    nestedUploadedFile.errors.each {
                        log.error it
                    }
                }

                uploadedFiles << nestedUploadedFile
            }
        }

        return uploadedFiles
    }

    private def handleCompressedFile(UploadedFile uploadedFile, SecUser currentUser) {
        /* Unzip the archive within the target */
        String destPath = zipService.uncompress(uploadedFile.getAbsolutePath())


        /* List files from the archive */
        def pathsAndExtensions = fileSystemService.getAbsolutePathsAndExtensionsFromPath(destPath)
        uploadedFile.setStatus(UploadedFile.UNCOMPRESSED)
        uploadedFile.save()

        def specialFiles = handleSpecialFile(uploadedFile, currentUser, pathsAndExtensions)
        if (specialFiles) return specialFiles

        //it looks like we have a set of "single file"
        def uploadedFiles = []
        pathsAndExtensions.each { it ->

            UploadedFile new_uploadedFile = createNewUploadedFile(uploadedFile, it, currentUser, null)
            uploadedFiles << new_uploadedFile
            if (new_uploadedFile.validate()){
                UploadedFile converted_uploadedFile = handleSingleFile(new_uploadedFile, currentUser)
                if (converted_uploadedFile != new_uploadedFile && converted_uploadedFile.getStatus() == UploadedFile.TO_DEPLOY) {
                    uploadedFiles << converted_uploadedFile
                }
            } else {
                uploadedFile.errors.each {
                    log.error it
                }
            }
        }

        return uploadedFiles
    }

    private def createNewUploadedFile(UploadedFile parentUploadedFile, def pathAndExtension, SecUser currentUser, String contentType){
        String absolutePath = pathAndExtension.absolutePath
        String extension = pathAndExtension.extension
        println "createNewUploadedFile $absolutePath"
        String filename = absolutePath.substring(parentUploadedFile.getPath().length(), absolutePath.length())
        if (!contentType) {
            MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
            contentType = mimeTypesMap.getContentType(absolutePath)
        }
        return new UploadedFile(
                originalFilename: filename,
                filename : filename,
                path : parentUploadedFile.getPath(),
                ext : extension,
                size : new File(absolutePath).size(),
                contentType : contentType,
                projects : parentUploadedFile.getProjects(),
                storages: parentUploadedFile.getStorages(),
                user : currentUser
        )
    }

    private UploadedFile handleSingleFile(UploadedFile uploadedFile, SecUser currentUser) {

        //Check if file must be converted or not...
        if (!UploadedFile.mimeToConvert.contains(uploadedFile.getExt())) {
            log.info uploadedFile.getFilename() + " : TO_DEPLOY"
            uploadedFile.setStatus(UploadedFile.TO_DEPLOY)
            uploadedFile.setConverted(false)
            uploadedFile.save()
            return uploadedFile
            /*uploadedFile.setConvertedExt(uploadedFile.getExt())
            uploadedFile.setConvertedFilename(uploadedFile.getFilename())*/
        } else {
            log.info "convert $uploadedFile"
            //..if yes. Convert it
            String convertFileName = uploadedFile.getFilename()
            convertFileName = convertFileName[0 .. (convertFileName.size() - uploadedFile.getExt().size() - 2)]
            convertFileName = convertFileName + "_converted.tif"

            String originalFilenameFullPath = [ uploadedFile.getPath(), uploadedFile.getFilename()].join(File.separator)
            String convertedFilenameFullPath = [ uploadedFile.getPath(), convertFileName].join(File.separator)


            try {

                Boolean success = vipsify(originalFilenameFullPath, convertedFilenameFullPath)
                if (success) {
                    UploadedFile convertUploadedFile = new UploadedFile(
                            originalFilename: uploadedFile.getOriginalFilename(),
                            filename : convertFileName,
                            path : uploadedFile.getPath(),
                            ext : "tiff",
                            size : new File(convertedFilenameFullPath).size(),
                            contentType : "image/tiff",
                            projects : uploadedFile.getProjects(),
                            storages: uploadedFile.getStorages(),
                            user : currentUser,
                            status: UploadedFile.TO_DEPLOY
                    )
                    uploadedFile.setConverted(true)
                    uploadedFile.setStatus(UploadedFile.CONVERTED)
                    /*uploadedFile.setConvertedFilename(convertFileName)
                    uploadedFile.setConvertedExt("tiff")*/
                    uploadedFile.save()
                    convertUploadedFile.save()
                    return convertUploadedFile
                } else {
                    uploadedFile.setConverted(false)
                    uploadedFile.setStatus(UploadedFile.ERROR_CONVERT)
                    return uploadedFile
                }

            } catch (Exception e) {
                e.printStackTrace()
                uploadedFile.setStatus(UploadedFile.ERROR_FORMAT)
                uploadedFile.save()
                return uploadedFile
            }

        }

    }

    private def vipsify(String originalFilenameFullPath, String convertedFilenameFullPath) {
        //1. Look for vips executable
        def executable = "/usr/bin/vips"
        if (System.getProperty("os.name").contains("OS X")) {
            executable = "/usr/local/bin/vips"
        }
        log.info "vips is in : $executable"
        //2. Create command
        def convertCommand = """im_vips2tiff "$originalFilenameFullPath" "$convertedFilenameFullPath":jpeg:95,tile:256x256,pyramid"""
        log.info "$executable $convertCommand"
        //3. Execute
        def ant = new AntBuilder()   // create an antbuilder
        ant.exec(outputproperty:"cmdOut",
                errorproperty: "cmdErr",
                resultproperty:"cmdExit",
                failonerror: "true",
                executable: executable) {
            arg(line:convertCommand)
        }
        log.info "return code:  ${ant.project.properties.cmdExit}"
        log.info "stderr:         ${ant.project.properties.cmdErr}"
        log.info "stdout:        ${ ant.project.properties.cmdOut}"
        return ant.project.properties.cmdExit == "0"
    }
}
