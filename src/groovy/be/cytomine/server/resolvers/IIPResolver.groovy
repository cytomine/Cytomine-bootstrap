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

        this.args = new LinkedList<String, String>()
    }

    public String toURL(String base_url) {
        String url = base_url  + ARGS_PREFIX;
        int cpt = 0;
        for (String arg : args) {
            url += arg;
            cpt++;
            if (cpt != args.size()) url += ARGS_DELIMITER;
        }
        return url;
    }

    public String getThumbUrl(String baseUrl, String imagePath) {
        //args.put("zoomify", imagePath + "/TileGroup0/0-0-0.jpg")
        args.add("FIF" + ARGS_EQUAL + imagePath)
        args.add("SDS" + ARGS_EQUAL + "0,90")
        args.add("CNT" + ARGS_EQUAL + "1.0")
        args.add("CVT" + ARGS_EQUAL + "jpeg")
        /*args.add("WID" + ARGS_EQUAL + "233")*/
        args.add("QLT" + ARGS_EQUAL + "99")
        return toURL(baseUrl)

    }

    public String getPreviewUrl(String baseUrl, String imagePath) {
        return ""
    }

    public String getMetaDataURL(String baseUrl, String imagePath) {
        //http://localhost/fcgi-bin/iipsrv.fcgi?FIF=/home/maree/CYTOMINE/WholeSlides/Aperio/o.tif&obj=IIP,1.0&obj=Max-size&obj=Tile-size&obj=Resolution-number
        args.add("FIF" + ARGS_EQUAL +  imagePath)
        args.add("obj" + ARGS_EQUAL +  "IIP,1.0")
        args.add("obj" + ARGS_EQUAL +  "Max-size")
        args.add("obj" + ARGS_EQUAL +  "Tile-size")
        args.add("obj" + ARGS_EQUAL +  "Resolution-number")
        return toURL(baseUrl)
    }
    public String getCropURL(String baseUrl, String imagePath, int topLeftX, int topLeftY, int width, int height, int zoom) {
        return "";
    }
}
