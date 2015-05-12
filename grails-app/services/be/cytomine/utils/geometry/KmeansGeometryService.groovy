package be.cytomine.utils.geometry

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

import be.cytomine.image.ImageInstance
import be.cytomine.security.SecUser
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.io.WKTReader
import groovy.sql.Sql

class KmeansGeometryService {

    def dataSource
    def annotationIndexService

    public static final int FULL = 3
    public static final int KMEANSFULL = 2
    public static final int KMEANSSOFT = 1

    public static final int ANNOTATIONSIZE1 = 0
    public static final int ANNOTATIONSIZE2 = 100
    public static final int ANNOTATIONSIZE3 = 2000
    public static final int ANNOTATIONSIZE4 = 10000
    public static final int ANNOTATIONSIZE5 = 100000


    public static final def rules = [
            100 : [((int)ANNOTATIONSIZE1): FULL, ((int)ANNOTATIONSIZE2): FULL, ((int)ANNOTATIONSIZE3): KMEANSFULL, ((int)ANNOTATIONSIZE4): KMEANSFULL, ((int)ANNOTATIONSIZE5): KMEANSFULL],
            75 : [((int)ANNOTATIONSIZE1): FULL, ((int)ANNOTATIONSIZE2): FULL, ((int)ANNOTATIONSIZE3): FULL, ((int)ANNOTATIONSIZE4): KMEANSFULL, ((int)ANNOTATIONSIZE5): KMEANSFULL],
            50 : [((int)ANNOTATIONSIZE1): FULL, ((int)ANNOTATIONSIZE2): FULL, ((int)ANNOTATIONSIZE3): FULL, ((int)ANNOTATIONSIZE4): FULL, ((int)ANNOTATIONSIZE5): KMEANSFULL],
            25 : [((int)ANNOTATIONSIZE1): FULL, ((int)ANNOTATIONSIZE2): FULL, ((int)ANNOTATIONSIZE3): FULL, ((int)ANNOTATIONSIZE4): FULL, ((int)ANNOTATIONSIZE5): FULL],
            0 : [((int)ANNOTATIONSIZE1): FULL, ((int)ANNOTATIONSIZE2): FULL, ((int)ANNOTATIONSIZE3): FULL, ((int)ANNOTATIONSIZE4): FULL, ((int)ANNOTATIONSIZE5): FULL],
    ]




    public def doKeamsFullRequest(String request) {
        String requestKmeans = "SELECT kmeans, count(*), st_astext(ST_ConvexHull(ST_Collect(location))) \n" +
                "FROM (\n" + request +"\n" +") AS ksub\n" +
                "GROUP BY kmeans\n" +
                "ORDER BY kmeans;"
        return selectAnnotationLightKmeans(requestKmeans)
    }

    public def doKeamsSoftRequest(String request) {
        String requestKmeans = "SELECT kmeans, count(*), st_astext(ST_Centroid(ST_Collect(location))) \n" +
                "FROM (\n" + request +"\n" +") AS ksub\n" +
                "GROUP BY kmeans\n" +
                "ORDER BY kmeans;"
        return selectAnnotationLightKmeans(requestKmeans)
    }

    private def selectAnnotationLightKmeans(String request) {
        def data = []

        double max = 1

        def sql = new Sql(dataSource)
        sql.eachRow(request) {

            long idK = it[0]
            long count = it[1]
            if(count>max) {
                max = count
            }
            String location = it[2]
            data << [id: idK, location: location, term:  [], count: count]
        }
        try {
            sql.close()
        }catch (Exception e) {}
        data.each {
            it.ratio = ((double)it.count/max)
        }

        data
    }

    public int mustBeReduce(Long image, Long user, String bbox) {
        mustBeReduce(ImageInstance.read(image),SecUser.read(user),new WKTReader().read(bbox))
    }


    public int mustBeReduce(ImageInstance image, SecUser user, Geometry bbox) {
        if (image.baseImage.width==null) {
            return  FULL
        }

        double imageWidth = image.baseImage.width
        double bboxWidth = bbox.getEnvelopeInternal().width

        double ratio = bboxWidth/imageWidth

//        log.info "imageWidth=$imageWidth"
//        log.info "bboxWidth=$bboxWidth"
//        log.info "ratio=$ratio"

        int ratio25 = ((int)((ratio/25d)*100))*25

        def ruleLine = rules.get(Math.min(ratio25,100))

        int numberOfAnnotation = annotationIndexService.count(image,user)

        def rule = getRuleForNumberOfAnnotations(numberOfAnnotation, ruleLine)

        return rule
    }

    public def getRuleForNumberOfAnnotations(def annotations, def ruleLine) {
        if (annotations >= ANNOTATIONSIZE5) return ruleLine.get(ANNOTATIONSIZE5)
        if (annotations >= ANNOTATIONSIZE4) return ruleLine.get(ANNOTATIONSIZE4)
        if (annotations >= ANNOTATIONSIZE3) return ruleLine.get(ANNOTATIONSIZE3)
        if (annotations >= ANNOTATIONSIZE2) return ruleLine.get(ANNOTATIONSIZE2)
        if (annotations >= ANNOTATIONSIZE1) return ruleLine.get(ANNOTATIONSIZE1)
    }

}
