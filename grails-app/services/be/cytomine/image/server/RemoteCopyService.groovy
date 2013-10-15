package be.cytomine.image.server
/**
 * TODO: refactoring + doc + test
 */
class RemoteCopyService {

    static transactional = false

    def copy(String localFile,String remotePath,String remoteFile, Storage storage, boolean deleteAfterCopy) {
        def ant = new AntBuilder()
        def username = storage.getUsername()
        def ip = storage.getIp()
        def port = storage.getPort()
        def password = storage.getPassword()
        remoteFile = "\'$remoteFile\'"
        remotePath = "\'$remotePath\'"
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
