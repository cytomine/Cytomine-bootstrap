package cytomine.web

import be.cytomine.image.AbstractImage
import be.cytomine.image.server.Storage

class RemoteCopyService {

    static transactional = false

    def copy(Storage storage, AbstractImage image, localFile) {
        def ant = new AntBuilder()
        def remoteFile = storage.getBasePath() + image.getFilename()
        def username = storage.getUsername()
        def ip = storage.getIp()
        def port = storage.getPort()
        def password = storage.getPassword()
        ant.scp(file: localFile,
                todir: username + "@" + ip + ":" + remoteFile,
                password: "${password}",
                port: port,
                trust: true, verbose: true)
    }

}
