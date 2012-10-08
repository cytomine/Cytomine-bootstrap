package cytomine.web

import be.cytomine.image.AbstractImage
import be.cytomine.image.UploadedFile
import be.cytomine.image.server.Storage

class RemoteCopyService {

    static transactional = false

    def copy(Storage storage, AbstractImage image, UploadedFile uploadedFile, boolean deleteAfterCopy) {
        def ant = new AntBuilder()
        def remoteFile = storage.getBasePath() + uploadedFile.getConvertedFilename()
        log.info "REMOTE FILE = " + remoteFile
        def remotePath = new File(remoteFile).getParent()
        log.info "REMOTE PATH = " + remotePath
        def localFile = uploadedFile.getPath() + "/" + uploadedFile.getConvertedFilename()
        log.info "LOCAL FILE = " + localFile
        def username = storage.getUsername()
        def ip = storage.getIp()
        def port = storage.getPort()
        def password = storage.getPassword()
        ant.sequential {
            sshexec(
                    host:storage.getIp(),
                    port:storage.getPort(),
                    username:storage.getUsername(),
                    password:storage.getPassword(),
                    command:"mkdir -p " + remotePath,
                    trust: true,
                    verbose: true
            )
            scp(file: localFile,
                    todir: username + "@" + ip + ":" + remoteFile,
                    password: "${password}",
                    port: port,
                    trust: true,
                    verbose: true
            )
        }


    }

}
