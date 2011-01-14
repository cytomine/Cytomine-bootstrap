package be.cytomine.server.resolvers

/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 13/01/11
 * Time: 10:07
 */
abstract class Resolver {

  protected final static String ARGS_PREFIX = "?"
  protected final static String ARGS_DELIMITER = "&"
  protected final static String ARGS_EQUAL = "="
  protected final static int    TILE_SIZE = 256

  public Map<String, String> args

  public Resolver() {
    this.args = new HashMap<String, String>()

  }

  public String toURL(String base_url) {
    String url = base_url  + ARGS_PREFIX;
    int cpt = 0;
    for (String key : args.keySet()) {
      url += key;
      url += ARGS_EQUAL;
      url += args.get(key);
      cpt++;
      if (cpt != args.size()) url += ARGS_DELIMITER;
    }
    return url;
  }

  public static Resolver getResolver(String className) {
     /* Init resolvers, maybe we should load it dynamically with name but fails in grails */
     Map<String, Object> resolvers = new HashMap<String,Object>()
     resolvers.put("DjatokaResolver", new DjatokaResolver())
     return resolvers.get(className)

  }

  public abstract String getThumbUrl(String baseUrl, String imagePath)
  public abstract String getMetaDataURL(String baseUrl, String imagePath)

}
