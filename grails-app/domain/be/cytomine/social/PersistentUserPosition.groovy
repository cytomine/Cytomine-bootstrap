package be.cytomine.social

import be.cytomine.CytomineDomain
import be.cytomine.image.ImageInstance
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.geom.LinearRing
import com.vividsolutions.jts.geom.Polygon

/**
 *  User position on an image at a time
 *  Same as position but store for a long time
 *
 *  SEE UserPosition.groovy to understand why LastUserPosition and PersistentUserPosition doesn't extends UserPosition
 */
class PersistentUserPosition extends CytomineDomain {

    static mapWith = "mongo"

    static transients = ['id','updated','deleted','class']

    static belongsTo = [user : SecUser, image : ImageInstance, project: Project]

    SecUser user
    ImageInstance image
    Project project

    String imageName

    /**
     * User screen area
     */
    List location

    /**
     * User zoom on image
     */
    int zoom

    static constraints = {
        project nullable: true
    }

    static mapping = {
        version false
        stateless true //don't store data in memory after read&co. These data don't need to be update.
        image index:true
        compoundIndex user:1, image:1, created:-1
        compoundIndex location:"2d", indexAttributes:[min:Integer.MIN_VALUE, max:Integer.MAX_VALUE], image:1
    }

    /**
     * Define fields available for JSON response
     * @param domain Domain source for json value
     * @return Map with fields (keys) and their values
     */
    static def getDataFromDomain(def domain) {
        def returnArray = CytomineDomain.getDataFromDomain(domain)
        returnArray.created = domain?.created
        returnArray.user = domain?.user?.id
        returnArray.image = domain?.image?.id
        returnArray.project = domain?.project?.id
        returnArray.zoom = domain?.zoom
        Polygon polygon = getPolygonFromMongo(domain?.location)
        returnArray.location = polygon.toString()
        returnArray.x = polygon.getCentroid().getX()
        returnArray.y = polygon.getCentroid().getY()
        returnArray
    }

    static Polygon getPolygonFromMongo(def locationList) {
        GeometryFactory fact = new GeometryFactory();
        locationList.add(locationList[0])
        Coordinate[] coordinates = locationList.collect{new Coordinate(it.get(0),it.get(1))}
        LinearRing linear = new GeometryFactory().createLinearRing(coordinates);
        Polygon poly = new Polygon(linear, null, fact);
        poly
    }


}
