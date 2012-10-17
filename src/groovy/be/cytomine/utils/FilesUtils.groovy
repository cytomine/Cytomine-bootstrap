package be.cytomine.utils

/**
 * User: lrollus
 * Date: 17/10/12
 * GIGA-ULg
 * Utility class to deals with file
 */
class FilesUtils {

    /**
     * Get the extension of a filename
     */
    public static def getExtensionFromFilename = {filename ->
        def returned_value = ""
        def m = (filename =~ /(\.[^\.]*)$/)
        if (m.size() > 0) returned_value = ((m[0][0].size() > 0) ? m[0][0].substring(1).trim().toLowerCase() : "");
        return returned_value
    }

    /**
     * Convert the current filename to a valide filename (without bad char like '@','+',...)
     * All bad char are replaced with '_'
     * @param file File
     * @return Correct filename for this file
     */
    public static String correctFileName(def file) {
        String newFilename = file.originalFilename
        newFilename = newFilename.replace(" ", "_")
        newFilename = newFilename.replace("(", "_")
        newFilename = newFilename.replace(")", "_")
        newFilename = newFilename.replace("+", "_")
        newFilename = newFilename.replace("*", "_")
        newFilename = newFilename.replace("/", "_")
        newFilename = newFilename.replace("@", "_")
        return newFilename
    }

}
