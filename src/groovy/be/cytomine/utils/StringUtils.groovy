package be.cytomine.utils

/**
 * Created by lrollus on 1/20/14.
 */
class StringUtils {


    static String splitCamelToBlank(String stringToSplit) {
        String result = ""
        for(int i=0;i<stringToSplit.size();i++) {
            //result = result + test.charAt(i)

            def car = stringToSplit[i]
            if(car != car.toUpperCase()) {
                result = result + car
            } else {
                result = result + " " + car.toLowerCase()
            }
        }
        return result
    }
}
