package be.cytomine.server.resolvers

import grails.converters.JSON


class SampleResolver extends Resolver {

    public Map<String, String> args

    public SampleResolver() {
        super()
    }

    public String getThumbUrl(String baseUrl, String imagePath, int width) {
        return "http://upload.wikimedia.org/wikipedia/commons/6/63/Wikipedia-logo.png"
    }

    public String getMetaDataURL(String baseUrl, String imagePath) {
        return baseUrl + "/getMetadatURL.json"
    }

     public String getPropertiesURL(String baseUrl, String imagePath) {
        return baseUrl + "/getPropertyURL.json"
    }

    public String getPreviewUrl(String baseUrl, String imagePath) {
        return "http://upload.wikimedia.org/wikipedia/commons/6/63/Wikipedia-logo.png"
    }

    public String getCropURL(String baseUrl, String imagePath, Integer topLeftX, Integer topLeftY, Integer width, Integer height, Integer baseImageWidth, Integer baseImageHeight, Integer desiredWidth, Integer desiredHeight) {
        return "http://upload.wikimedia.org/wikipedia/commons/6/63/Wikipedia-logo.png"
    }

    public String getCropURL(String baseUrl, String imagePath, int topLeftX, int topLeftY, int width, int height, int zoom, int baseImageWidth, int baseImageHeight, int desiredWidth, int desiredHeight) {
        return "http://upload.wikimedia.org/wikipedia/commons/6/63/Wikipedia-logo.png"
    }

    def  getZoomLevels(String baseUrl, String imagePath, int width, int height) {
        return [min : 0, max : 3, middle : 2]
    }
}
