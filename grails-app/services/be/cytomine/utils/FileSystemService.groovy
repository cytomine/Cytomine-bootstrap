package be.cytomine.utils

class FileSystemService {

    def getAbsolutePathsAndExtensionsFromPath(String path) {
        def pathsAndExtensions = []
        new File(path).eachFileRecurse() { file ->
            if (!file.directory) {
                String absolutePath = file.getAbsolutePath()
                String extension = FilesUtils.getExtensionFromFilename(file.getAbsolutePath())
                pathsAndExtensions << [absolutePath : absolutePath, extension : extension]
            }

        }
        return pathsAndExtensions
    }

    def makeLocalDirectory(String path) {
        def mkdirCommand = "mkdir -p " + path
        def proc = mkdirCommand.execute()
        proc.waitFor()
        return proc.exitValue()
    }

    def makeRemoteDirectory(String ip, Integer port, String username, String password, String keyFile, String remotePath) {
        def ant = new AntBuilder()
        if (password != null) {
            ant.sshexec(
                    host:ip,
                    port:port,
                    username:username,
                    password : password,
                    command:"mkdir -p " + remotePath,
                    trust: true,
                    verbose: true
            )
        } else if (keyFile != null) {
            ant.sshexec(
                    host:ip,
                    port:port,
                    username:username,
                    keyFile : keyFile,
                    command:"mkdir -p " + remotePath,
                    trust: true,
                    verbose: true
            )
        } else{
            log.error "cannot make directory $remotePath on remove host with user $username"
        }

    }

    def deleteFile(String path) {
        def deleteCommand = "rm " + path
        log.info deleteCommand
        def proc = deleteCommand.execute()
        proc.waitFor()
        return proc.exitValue()
    }
}
