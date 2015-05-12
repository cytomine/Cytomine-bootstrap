package be.cytomine.image

/*
* Copyright (c) 2009-2015. Authors: see NOTICE file.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import be.cytomine.AnnotationDomain
import be.cytomine.Exception.ObjectNotFoundException
import ij.ImagePlus
import org.apache.http.HttpEntity
import org.apache.http.HttpHost
import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.protocol.BasicHttpContext

import javax.imageio.ImageIO
import java.awt.image.BufferedImage

/**
 *  TODOSTEVBEN:: doc + clean
 */
class ImageProcessingService {


    def grailsApplication

    static final int MIN_REQUESTED_CROP_SIZE = 8

    static transactional = false

    public def isInROI(ImagePlus ip, x, y) {
        return (x >= 0 && x < ip.getWidth() && y >= 0 && y < ip.getHeight())
    }


    /**
     * Get annotation crop from this image
     */
    String cropURL(AnnotationDomain annotation) {
        return annotation.toCropURL()
    }

    BufferedImage crop(AnnotationDomain annotation, params) {
        String cropURL = annotation.toCropURL(params)
        println cropURL
        return getImageFromURL(cropURL)
    }






    /**
     * Read a picture from url
     * @param url Picture url
     * @return Picture as an object
     */
//    public BufferedImage getImageFromURL(String url) {
//        BufferedImage bufferedImage = ImageIO.read(new URL(url))
//        return bufferedImage
//    }

    public BufferedImage getImageFromURL(String url) throws MalformedURLException, IOException, Exception {
        log.debug("readBufferedImageFromURL:"+url);
        URL URL = new URL(url);
        HttpHost targetHost = new HttpHost(URL.getHost(), URL.getPort());
        log.debug("targetHost:"+targetHost);
        DefaultHttpClient client = new DefaultHttpClient();
        log.debug("client:"+client);
        // Add AuthCache to the execution context
        BasicHttpContext localcontext = new BasicHttpContext();
        log.debug("localcontext:"+localcontext);
        BufferedImage img = null;
        HttpGet httpGet = new HttpGet(URL.toString());
        HttpResponse response = client.execute(targetHost, httpGet, localcontext);
        int code = response.getStatusLine().getStatusCode();
        System.out.println("url="+url + " is " + code + "(OK="+HttpURLConnection.HTTP_OK +",MOVED="+HttpURLConnection.HTTP_MOVED_TEMP+")");

        boolean isOK = (code == HttpURLConnection.HTTP_OK);
        boolean isFound = (code == HttpURLConnection.HTTP_MOVED_TEMP);
        boolean isErrorServer = (code == HttpURLConnection.HTTP_INTERNAL_ERROR);

        if(!isOK && !isFound & !isErrorServer) {
            throw new IOException(url + " cannot be read: "+code);
        }
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            img = ImageIO.read(entity.getContent());
        }
        return img;


    }

    /**
     * Get crop annotation URL
     * @param annotation Annotation
     * @param params Params
     * @return Crop Annotation URL
     */
    public def getCropAnnotationURL(AnnotationDomain annotation, def params) {
        if (annotation == null) {
            throw new ObjectNotFoundException("Annotation $params.annotation does not exist!")
        } else  {
            try {
                String cropURL = cropURL(annotation)
                if (cropURL == null) {
                    //no crop available, add lambda image
                    cropURL = grailsApplication.config.grails.serverURL + "/images/cytomine.jpg"
                }
                return cropURL
            } catch (Exception e) {
                log.error("GetCrop:" + e)
                return null
            }
        }
    }
}
