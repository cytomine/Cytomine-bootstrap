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

    def makeDirectory(String path) {
        def mkdirCommand = "mkdir -p " + path
        def proc = mkdirCommand.execute()
        proc.waitFor()
        return proc.exitValue()
    }
}
