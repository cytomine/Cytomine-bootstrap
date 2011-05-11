package be.cytomine.server.resolvers

import grails.converters.JSON

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
        args.add("zoomify" + ARGS_EQUAL + imagePath)
        def url = toURL(baseUrl)
        url += "/TileGroup0/0-0-0.jpg"
        //http://is5.cytomine.be:48/fcgi-bin/iipsrv.fcgi/fcgi-bin/iipsrv.fcgi?zoomify=/media/datalvm/anapath/upload/vms/OVA17cyto-2010-11-1513.09.42_clip.vms/TileGroup0/0-0-0.jpg
        return url

    }

    public String getPreviewUrl(String baseUrl, String imagePath) {
                //args.put("zoomify", imagePath + "/TileGroup0/0-0-0.jpg")
        args.add("FIF" + ARGS_EQUAL + imagePath)
        args.add("SDS" + ARGS_EQUAL + "0,90")
        args.add("CNT" + ARGS_EQUAL + "1.0")
        args.add("CVT" + ARGS_EQUAL + "jpeg")
        /*args.add("WID" + ARGS_EQUAL + "233")*/
        args.add("QLT" + ARGS_EQUAL + "99")
        return toURL(baseUrl)
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
        def dimensions = getWidthHeight(baseUrl, imagePath)
        print "dimensions : "  + dimensions
        /*#Y is the down inset value (positive) from 0 on the y axis at the max image resolution.
          #X is the right inset value (positive) from 0 on the x axis at the max image resolution.
          #H is the height of the image provided as response.
          #W is the width of the image provided as response.
          X : 1/(34207/15000) = 0.4399859205
          Y : 1/(34092/15100) = 0.4414301166
          W : 1/(34092/400) = 0.01173295788
          H : 1/(34207/600) = 0.01754026954*/
        def x = 1/(dimensions.width / topLeftX)
        def y = 1/(dimensions.height / (dimensions.height - topLeftY))
        def w = 1/(dimensions.width / width)
        def h = 1/(dimensions.height / height)
        args.add("FIF" + ARGS_EQUAL +  imagePath)
        args.add("RGN" + ARGS_EQUAL +  x + "," + y + "," + w + "," + h)
        args.add("CVT" + ARGS_EQUAL + "jpeg")
        def url = toURL(baseUrl)
        println url
        //RGN=0.4399859205,0.4414301166,0.01173295788,0.01754026954&CVT=JPEG
        return url
    }

    def getWidthHeight(baseUrl, imagePath) {
        def url = new URL(getMetaDataURL(baseUrl, imagePath))
        def dimensions = null
        url.eachLine { line ->
            println "line : " + line
            def args = line.split(":")
            println "args" + args[0] + " , " + args[1]
            if (args[0].equals("Max-size")) {
                println "...ok"
                def sizes = args[1].split(" ")
                dimensions = [width : Integer.parseInt(sizes[0]), height : Integer.parseInt(sizes[1])]
            }
        }
        return dimensions
    }

    def getZoomLevels (baseUrl, imagePath) {
        /*def metadata = JSON.parse(new URL(getMetaDataURL(baseUrl, imagePath)).text)
        int max = Integer.parseInt(metadata.levels)
        int min = 0
        int middle = ((max - min) / 2)
        return [min : 0, max : max, middle : middle]*/
        return [min : 0, max : 8, middle : 4]
    }
}
