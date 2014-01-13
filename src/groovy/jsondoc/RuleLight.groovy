package jsondoc

/**
 * Created by lrollus on 1/10/14.
 */
class RulesLight {

    Map<String,RuleLight> rules = new TreeMap<String,RuleLight>()

    public void addRule(String controllerName, String actioName, String path, String verb) {
        String key = (controllerName+"."+actioName).toUpperCase()
        key = key.replace("CONTROLLER","")

        String shortPath = path
        if(shortPath.startsWith("/api")) {
            shortPath = shortPath.substring(4)
        }
        shortPath = shortPath.replace("{format}","json")

        rules.put(key,new RuleLight(path:shortPath,verb:verb))
    }

    public RuleLight getRule(String controllerName, String actioName) {
        String key = (controllerName+"."+actioName).toUpperCase()
        key = key.replace("CONTROLLER","")
        println key
        rules.get(key)
    }

}

class RuleLight {
    public String path
    public String verb

    public String toString() {
        return "path = $path , verb = $verb"
    }
}
