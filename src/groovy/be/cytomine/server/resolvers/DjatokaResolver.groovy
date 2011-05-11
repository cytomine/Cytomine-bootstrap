package be.cytomine.server.resolvers

import grails.converters.JSON

/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 13/01/11
 * Time: 10:08
 */
class DjatokaResolver extends Resolver {

    public Map<String, String> args

    public DjatokaResolver() {
        super()
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

    public String getPreviewUrl(String baseUrl, String imagePath) {
        args.put("rft_id", imagePath)
        args.put("url_ver", "Z39.88-2004")
        args.put("svc_id", "info:lanl-repo/svc/getRegion")
        args.put("svc_val_fmt", "info:ofi/fmt:kev:mtx:jpeg2000")
        args.put("svc.format", "image/jpeg")
        args.put("svc.scale", "900")
        return toURL(baseUrl)
    }

    public String getCropURL(String baseUrl, String imagePath, int topLeftX, int topLeftY, int width, int height) {
        def maxZoom = getZoomLevels(baseUrl, imagePath).max
        def widthTarget = 500
        def tmpWidth = width
        while (tmpWidth > widthTarget) {
            maxZoom--
            tmpWidth = tmpWidth / 2
        }
        getCropURL(baseUrl, imagePath, topLeftX, topLeftY, width, height, maxZoom)
    }

    public String getCropURL(String baseUrl, String imagePath, int topLeftX, int topLeftY, int width, int height, int zoom) {
        int deltaZoom = Math.pow(2, (getZoomLevels(baseUrl, imagePath).max - zoom))
        def metadata = JSON.parse(new URL(getMetaDataURL(baseUrl, imagePath)).text)
        println "crop url metadata" + metadata.height
        def dw = (int) width / deltaZoom
        def dh = (int) height / deltaZoom
        def x = topLeftX
        def y = Integer.parseInt(metadata.height) - topLeftY //Y is inverted between Djatoka & OpenLayers
        args.put("rft_id", imagePath)
        args.put("url_ver", "Z39.88-2004")
        args.put("svc_id", "info:lanl-repo/svc/getRegion")
        args.put("svc_val_fmt", "info:ofi/fmt:kev:mtx:jpeg2000")
        args.put("svc.format", "image/jpeg")
        args.put("svc.region", y+ ","+x+","+dh+","+dw)
        args.put("svc.level",zoom)
        return toURL(baseUrl)
    }

    def getZoomLevels (baseUrl, imagePath) {
        def metadata = JSON.parse(new URL(getMetaDataURL(baseUrl, imagePath)).text)
        int max = Integer.parseInt(metadata.levels)
        int min = 0
        int middle = ((max - min) / 2)
        return [min : 0, max : max, middle : middle]
    }
}
