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
    String url = base_url + ARGS_PREFIX;
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

  public abstract String getFileKey()


}
