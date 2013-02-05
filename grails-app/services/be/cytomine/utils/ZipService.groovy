package be.cytomine.utils

class ZipService {

    def fileSystemService

    def uncompress(String absolutePath) {
        long timestamp = new Date().getTime()
        String parentPath = new File(absolutePath).getParent()
        String destPath = parentPath.endsWith("/") ?  parentPath :  parentPath + "/" + timestamp.toString()

        /* Create and temporary directory which will contains the archive content */
        fileSystemService.makeLocalDirectory(destPath)

        /* Get extension of filename in order to choose the uncompressor */
        String ext = FilesUtils.getExtensionFromFilename(absolutePath).toLowerCase()
        /* Unzip */
        if (ext == 'zip') {
            def ant = new AntBuilder()
            ant.unzip(src : absolutePath,
                    dest : destPath,
                    overwrite : false)
        }

        return destPath
    }
}
