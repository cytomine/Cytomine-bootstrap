package cytomine.web

import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ZipService {

    static transactional = false

    def serviceMethod() {
        //todo
    }

    static Boolean zipDirectory(String srcDirPath, String destFilePath, List excludeDirs) {
        Boolean ret = true
        File rootFile = new File(srcDirPath)
        byte[] buf = new byte[1024]
        try {
            File parent = new File(destFilePath).parentFile
            parent.mkdirs()
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(destFilePath))

            File rec = new File(srcDirPath)
            rec.eachFileRecurse {File file ->

                if (file.isFile()) {
                    FileInputStream input = new FileInputStream(file)

                    // Store relative file path in zip file
                    String tmp = file.absolutePath.substring(rootFile.absolutePath.size() + 1)

                    // Add ZIP entry to output stream.
                    out.putNextEntry(new ZipEntry(tmp))

                    // Transfer bytes from the file to the ZIP file
                    int len
                    while ((len = input.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    // Complete the entry
                    out.closeEntry()
                    input.close()
                }
            }
            out.close()
        } catch (Exception e) {
            //logger.error "Encountered error when zipping file $srcDirPath, error is ${e.message}"
            ret = false
        }
        return ret
    }

}
