package jsondoc.utils

/**
 * Created by lrollus on 1/10/14.
 */
class MappingRules {

    static String FIRSTCHARPATH = "/api"

    static String DEFAULTFORMAT = "json"

    Map<String,MappingRulesEntry> rules = new TreeMap<String,MappingRulesEntry>()

    public void addRule(String controllerName, String actioName, String path, String verb) {
        String key = (controllerName+"."+actioName).toUpperCase()
        key = key.replace("CONTROLLER","")

        String shortPath = path
        if(shortPath.startsWith(FIRSTCHARPATH)) {
            shortPath = shortPath.substring(FIRSTCHARPATH.size())
        }
        shortPath = shortPath.replace("{format}",DEFAULTFORMAT)

        rules.put(key,new MappingRulesEntry(path:shortPath,verb:verb))
    }

    public MappingRulesEntry getRule(String controllerName, String actionName) {
        String key = (controllerName+"."+actionName).toUpperCase()
        key = key.replace("CONTROLLER","")
        rules.get(key)
    }

}

class MappingRulesEntry {
    public String path
    public String verb

    public String toString() {
        return "path = $path , verb = $verb"
    }
}
