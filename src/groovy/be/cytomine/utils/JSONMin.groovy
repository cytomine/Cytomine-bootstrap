package be.cytomine.utils

/**
 * User: lrollus
 * Date: 19/09/12
 * GIGA-ULg
 * Class to min JSON key
 * 
 */
class JSONMin {

    private static volatile JSONMin instance
    private long index

    Map<String, String> conversion = new HashMap<String, String>()

    private JSONMin() {

    }

    public String convert(String key) {
        String compressKey = conversion.get(key)
        if(!compressKey) {
            compressKey = getFirstCar(key,2) + getNextId()
            conversion.put(key,compressKey);
        }
        return compressKey;
    }

    public static JSONMin getInstance() {
        if(!instance)
            instance = new JSONMin()
        return instance
    }

    private synchronized long getNextId() {
        return index++;
    }

    public static getFirstCar(String key, int number) {
        return key.substring(0,number<=key.length()? number : key.length())
    }
//
//    JSONMin jsonMin = JSONMin.getInstance()
//
//    jsonMin.put('class',project.class)
//    jsonMin.put('id',project.id)
//    jsonMin.put('name',project.id)
//    println jsonMin.data
//    return jsonMin.data

}
