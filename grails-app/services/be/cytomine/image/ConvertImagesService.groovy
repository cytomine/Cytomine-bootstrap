package be.cytomine.image

import be.cytomine.image.UploadedFile
import be.cytomine.security.SecUser

/**
 * TODOSTEVBEN:: doc + refactoring + security (?)
 */
class ConvertImagesService {

    static transactional = true

    def convertUploadedFile(UploadedFile uploadedFile, SecUser currentUser) {
//        SpringSecurityUtils.reauthenticate currentUser.getUsername(), null
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
            log.info "convert $uploadedFile"
            //..if yes. Convert it
            String convertFileName = uploadedFile.getFilename()
            convertFileName = convertFileName[0 .. (convertFileName.size() - uploadedFile.getExt().size() - 2)]
            convertFileName = convertFileName + "_converted.tif"
            String contextPath = uploadedFile.getPath().endsWith("/") ?  uploadedFile.getPath() :  uploadedFile.getPath() + "/"
            String originalFilenameFullPath = contextPath + uploadedFile.getFilename()
            String convertedFilenameFullPath = contextPath + convertFileName
            uploadedFile.setConvertedFilename(convertFileName)
            uploadedFile.setConvertedExt("tiff")



            try {
                //Check number of bits in channels (e.g //identify -verbose /tmp/cytominebuffer/18/1351541319227/JPCLN001.png | grep Depth)
                /*if (uploadedFile.getExt().toLowerCase() == "png") {
                    log.info "It's a PNG. 16 bits per channel ?"
                    String identifyCommand = """ -verbose "$originalFilenameFullPath" | grep Depth"""
                    def ant = new AntBuilder()   // create an antbuilder
                    ant.exec(outputproperty:"cmdOut",
                            errorproperty: "cmdErr",
                            resultproperty:"cmdExit",
                            failonerror: "true",
                            executable: "/usr/local/bin/identify") {
                        arg(line:identifyCommand)
                    }
                    String result = ant.project.properties.cmdOut //ex :  Depth: 16-bit
                    println "RESULT : $result"
                    if (result) {
                        String tmpFilename = "$originalFilenameFullPath.8bits.png"
                        String bitsString = result.split(":")[1].split("-")[0]
                        log.info "bitsString : $bitsString"
                        if (Integer.parseInt(bitsString) == 16) {
                            log.info "converting to 8 bits"
                            //IF 16 BITS => convert /tmp/cytominebuffer/18/1351541319227/JPCLN001.png -depth 8 /tmp/cytominebuffer/18/1351541319227/JPCLN001_8.png
                            ant = new AntBuilder()   // create an antbuilder
                            ant.exec(outputproperty:"cmdOut",
                                    errorproperty: "cmdErr",
                                    resultproperty:"cmdExit",
                                    failonerror: "true",
                                    executable: "/usr/local/bin/convert") {
                                arg(line:""" "$originalFilenameFullPath" -depth 8 "$tmpFilename" """)
                            }
                            originalFilenameFullPath = tmpFilename
                        }
                    }


                }*/
                //Convert

                //1. Look for vips executable

                def executable = "/usr/bin/vips"
                if (System.getProperty("os.name").contains("OS X")) {
                    executable = "/usr/local/bin/vips"
                }
                log.info "vips is in : $executable"
                def convertCommand = """im_vips2tiff "$originalFilenameFullPath" "$convertedFilenameFullPath":jpeg:95,tile:256x256,pyramid"""
                log.info "$executable $convertCommand"
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

                uploadedFile.setStatus(UploadedFile.CONVERTED)
                uploadedFile.save(flush : true)
                return true

            } catch (Exception e) {
                e.printStackTrace()
                uploadedFile.setStatus(UploadedFile.ERROR_FORMAT)
                uploadedFile.save(flush : true)
                return false
            }

        }
    }
}
