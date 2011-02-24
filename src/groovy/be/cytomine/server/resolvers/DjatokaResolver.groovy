package be.cytomine.server.resolvers

import grails.converters.JSON

/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 13/01/11
 * Time: 10:08
 */
class DjatokaResolver extends Resolver {

  public DjatokaResolver() {
    super()
  }

  /*
  public String getThumbUrl(String baseUrl, String imagePath) {
    def metadata = JSON.parse(new URL(getMetaDataURL(baseUrl, imagePath)).text)
    def height = Integer.parseInt(metadata.height)
    def width = Integer.parseInt(metadata.width)
    def levels = Integer.parseInt(metadata.levels)
    def scale =  Math.pow(2, levels-1)
    def thumbHeight = Math.round(height / scale)
    def thumbWidth  = Math.round(width / scale)

    println  "metadata" + height + " - " + width
    args.put("rft_id", imagePath)
    args.put("url_ver", "Z39.88-2004")
    args.put("svc_id", "info:lanl-repo/svc/getRegion")
    args.put("svc_val_fmt", "info:ofi/fmt:kev:mtx:jpeg2000")
    args.put("svc.format", "image/jpeg")
    //args.put("svc.scale", "192")
    args.put("svc.region", 0+ ","+0+","+thumbHeight+","+thumbWidth)
    args.put("svc.level",1)
    println "TOURL " + toURL(baseUrl)
    return toURL(baseUrl)
  }
   */
  public String getThumbUrl(String baseUrl, String imagePath) {
    args.put("rft_id", imagePath)
    args.put("url_ver", "Z39.88-2004")
    args.put("svc_id", "info:lanl-repo/svc/getRegion")
    args.put("svc_val_fmt", "info:ofi/fmt:kev:mtx:jpeg2000")
    args.put("svc.format", "image/jpeg")
    args.put("svc.scale", "192")
    return toURL(baseUrl)
  }

  public String getMetaDataURL(String baseUrl, String imagePath) {
    args.put("rft_id", imagePath)
    args.put("url_ver", "Z39.88-2004")
    args.put("svc_id", "info:lanl-repo/svc/getMetadata")
    args.put("svc_val_fmt", "info:ofi/fmt:kev:mtx:jpeg2000")
    args.put("svc.format", "image/jpeg")
    return toURL(baseUrl)
  }
  public String getCropURL(String baseUrl, String imagePath, int topLeftX, int topLeftY, int width, int height, int zoom) {
    args.put("rft_id", imagePath)
    args.put("url_ver", "Z39.88-2004")
    args.put("svc_id", "info:lanl-repo/svc/getRegion")
    args.put("svc_val_fmt", "info:ofi/fmt:kev:mtx:jpeg2000")
    args.put("svc.format", "image/jpeg")
    args.put("svc.region", topLeftY+ ","+topLeftX+","+height+","+width)
    args.put("svc.level",zoom)
    return toURL(baseUrl)
  }
}
