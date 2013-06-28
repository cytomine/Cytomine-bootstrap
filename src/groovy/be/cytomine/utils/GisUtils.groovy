package be.cytomine.utils

/**
 * User: lrollus
 * Date: 17/10/12
 * GIGA-ULg
 * Utility class to deals with file
 */
class GisUtils {

    public static String PIXEL = 'pixels'
    public static String PIXELS2 = 'pixels²'
    public static String MM = 'mm'
    public static String MICRON2 = 'micron²'


    public static Integer PIXELv = 0
    public static Integer PIXELS2v = 1
    public static Integer MMv = 2
    public static Integer MICRON2v = 3


    public static Map<String,String> unit = [0:PIXEL,1:PIXELS2,2:MM,3:MICRON2]

    public static String retrieveUnit(def value) {
        return unit.get(value)
    }

}
