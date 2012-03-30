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

    public Resolver() {
    }

    public static Resolver getResolver(String className) {
        /* Init resolvers, maybe we should load it dynamically with name but fails in grails */
        Map<String, Object> resolvers = new HashMap<String,Object>()
        resolvers.put("DjatokaResolver", new DjatokaResolver())
        resolvers.put("IPPResolver", new IIPResolver())
        return resolvers.get(className)

    }

    public abstract String getThumbUrl(String baseUrl, String imagePath, int width)
    public abstract String getMetaDataURL(String baseUrl, String imagePath)
    public abstract String getPropertiesURL(String baseUrl, String imagePath)
    public abstract String getPreviewUrl(String baseUrl, String imagePath)
    public abstract String getCropURL(String baseUrl, String imagePath, int topLeftX, int topLeftY, int width, int height, int baseImageWidth, int baseImageHeight)
    public abstract String getCropURL(String baseUrl, String imagePath,int topLeftX, int topLeftY, int width, int height, int zoom, int baseImageWidth, int baseImageHeight)
    public abstract Object getZoomLevels (baseUrl, imagePath, width, height)
}
