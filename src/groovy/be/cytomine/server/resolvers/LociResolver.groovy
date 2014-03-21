package be.cytomine.server.resolvers

/**
 * Created by stevben on 18/03/14.
 */
class LociResolver extends Resolver {

    public String getThumbUrl(String baseUrl, String imagePath, int width) {
       return "hello.jpg"
    }

    public String getMetaDataURL(String baseUrl, String imagePath) {
        return "hello.jpg"
    }

    public String getPropertiesURL(String baseUrl, String imagePath) {
        return "hello.jpg"
    }

    public String getPreviewUrl(String baseUrl, String imagePath) {
        return "hello.jpg"
    }

    public String getCropURL(String baseUrl, String imagePath, def boundaries) {
        return "hello.jpg"
    }

    public Object getZoomLevels  (String baseUrl, String imagePath, int width, int height) {
        return "hello.jpg"
    }

    public String tileURL(String baseUrl, String imagePath, def params) {
        return "hello.jpg"
    }

}
