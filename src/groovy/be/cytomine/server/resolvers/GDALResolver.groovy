package be.cytomine.server.resolvers

/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 10/03/11
 * Time: 15:08
 */
class GDALResolver extends Resolver {

  public GDALResolver() {
    super()
  }

  public String getThumbUrl(String baseUrl, String imagePath) {
    //imagePath : ImageNEO13_CNS_5.10_5_4_01
    //baseURL : http://localhost/~stevben/
    def url = baseUrl + "/" + imagePath + "/1/0/0.png"
    println "getThumbUrl : " + url
    return url
  }

  public String getMetaDataURL(String baseUrl, String imagePath) {
    def url = baseUrl + "/" + imagePath + "/tilemapresource.xml"
    println "getMetaDataURL : " + url
    return toURL(baseUrl)
  }

    public String getPreviewUrl(String baseUrl, String imagePath) {
        return getThumbUrl(baeUrl, imagePath)

    }

  public String getCropURL(String baseUrl, String imagePath, int topLeftX, int topLeftY, int width, int height, int zoom) {
    return "http://suttonplace.mlblogs.com/photos/uncategorized/working_overtime.jpg"
  }

}
