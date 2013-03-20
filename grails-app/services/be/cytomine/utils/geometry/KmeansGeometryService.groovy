package be.cytomine.utils.geometry

import be.cytomine.AnnotationDomain
import be.cytomine.image.ImageInstance
import be.cytomine.ontology.*
import be.cytomine.security.SecUser
import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.geom.GeometryFactory
import groovy.sql.Sql

class KmeansGeometryService {

    def dataSource

    public def doKeamsRequest(String request) {
        String requestKmeans = "SELECT kmeans, count(*), st_astext(ST_MinimumBoundingCircle(ST_Collect(location))) \n" +
                "FROM (\n" + request +"\n" +") AS ksub\n" +
                "GROUP BY kmeans\n" +
                "ORDER BY kmeans;"
        return selectAnnotationLightKmeans(requestKmeans)
    }

    private def selectAnnotationLightKmeans(String request) {
        def data = []
        println request
        new Sql(dataSource).eachRow(request) {

            long idK = it[0]
            long count = it[1]
            String location = it[2]
            data << [id: idK, location: location, term:  [], count: count]
        }
        data
    }

    public boolean mustBeReduce() {
        return false
    }

}
