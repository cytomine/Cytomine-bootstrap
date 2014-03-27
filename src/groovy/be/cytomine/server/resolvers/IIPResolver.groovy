package be.cytomine.server.resolvers

/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 10/05/11
 * Time: 11:26
 */
class IIPResolver extends Resolver{

    public LinkedList<String> args

    public IIPResolver() {
        super()

        this.args = new LinkedList<String>()
    }

    public String toURL(String base_url) {
        String url = base_url  + ARGS_PREFIX;
        int cpt = 0;
        for (String arg : args) {
            url += arg;
            cpt++;
            if (cpt != args.size()) url += ARGS_DELIMITER;
        }
        url = url.replace(" ", "%20")
        return url;
    }

    public String getThumbUrl(String baseUrl, String imagePath, int width) {
        args.clear()
        args.add("FIF" + ARGS_EQUAL + imagePath)
        args.add("SDS" + ARGS_EQUAL +  "0,90")
        args.add("CNT" + ARGS_EQUAL + "1.0")
        args.add("WID" + ARGS_EQUAL + width)
        args.add("QLT" + ARGS_EQUAL + "99")
        args.add("CVT" + ARGS_EQUAL + "jpeg")
        return toURL(baseUrl)

    }
    public String getPreviewUrl(String baseUrl, String imagePath) {
        args.clear()
        args.add("FIF" + ARGS_EQUAL + imagePath)
        args.add("SDS" + ARGS_EQUAL + "0,90")
        args.add("CNT" + ARGS_EQUAL + "1.0")
        args.add("CVT" + ARGS_EQUAL + "jpeg")
        args.add("QLT" + ARGS_EQUAL + "99")
        return toURL(baseUrl)
    }

    public String getPropertiesURL(String baseUrl, String imagePath) {
        args.clear()
        args.add("FIF" + ARGS_EQUAL +  imagePath)
        args.add("obj" + ARGS_EQUAL +  "Image-Properties")
        return toURL(baseUrl)
    }

    public String getMetaDataURL(String baseUrl, String imagePath) {
        args.clear()
        args.add("FIF" + ARGS_EQUAL +  imagePath)
        args.add("obj" + ARGS_EQUAL +  "IIP,1.0")
        args.add("obj" + ARGS_EQUAL +  "Max-size")
        args.add("obj" + ARGS_EQUAL +  "Tile-size")
        args.add("obj" + ARGS_EQUAL +  "Resolution-number")
        return toURL(baseUrl)
    }

    public String getCropURL(String baseUrl, String imagePath, def boundaries) {
        /*
            Y is the down inset value (positive) from 0 on the y axis at the max image resolution.
            X is the right inset value (positive) from 0 on the x axis at the max image resolution.
            H is the height of the image provided as response.
            W is the width of the image provided as response.
            X : 1/(34207/15000) = 0.4399859205
            Y : 1/(34092/15100) = 0.4414301166
            W : 1/(34092/400) = 0.01173295788
            H : 1/(34207/600) = 0.01754026954
        */
        int topLeftX = boundaries.topLeftX
        int topLeftY = boundaries.topLeftY
        int width = boundaries.width
        int height = boundaries.height
        int baseImageWidth = boundaries.baseImageWidth
        int baseImageHeight = boundaries.baseImageHeight
        def x = (topLeftX == 0) ? 0 : 1/(baseImageWidth / topLeftX)
        def y = ((baseImageHeight - topLeftY) == 0) ? 0 : 1/(baseImageHeight / (baseImageHeight - topLeftY))
        def w = (width == 0) ? 0 : 1/(baseImageWidth / width)
        def h = (height == 0) ? 0 : 1/(baseImageHeight / height)
        args.clear()
        args.add("FIF" + ARGS_EQUAL +  imagePath)
        args.add("RGN" + ARGS_EQUAL +  x + "," + y + "," + w + "," + h)
        if (boundaries.scale) {
            int scale = boundaries.scale
            if (height > boundaries.scale) {
                int hei = Math.round(baseImageHeight / Math.ceil(height / scale))
                args.add("HEI" + ARGS_EQUAL + hei)
            } else if (width > boundaries.scale) {
                int wid = Math.round(baseImageWidth / Math.ceil(width / scale))
                args.add("WID" + ARGS_EQUAL + wid)
            }
        }
        args.add("CVT" + ARGS_EQUAL + "jpeg")
        return toURL(baseUrl)
    }

    def getZoomLevels (String baseUrl, String imagePath, int width, int height) {
        double tmpWidth = width
        double tmpHeight = height
        def nbZoom = 0
        while (tmpWidth > 256 || tmpHeight > 256) {
            nbZoom++
            tmpWidth = tmpWidth / 2
            tmpHeight = tmpHeight / 2
        }
        return [min : 0, max : nbZoom, middle : (nbZoom / 2), overviewWidth : Math.round(tmpWidth), overviewHeight : Math.round(tmpHeight), width : width, height : height]
    }

    String tileURL(String baseUrl, String imagePath, params) { //String imagePath, int x, int y, int z, int channels, int timeframe, int layer) {

        /*int channels = params.int("channels")
        int timeframe = params.int("timeframe")
        int layer = params.int("layer")*/
        //long id = params.long("id")

        int tileGroup = params.int("TileGroup")
        int x = params.int("x")
        int y = params.int("y")
        int z = params.int("z")

        def zoomifyQuery = "$imagePath/TileGroup$tileGroup/$z-$x-$y" + ".jpg"
        args.clear()
        args.add("zoomify" + ARGS_EQUAL +  zoomifyQuery)
        return toURL(baseUrl)

    }
}
