package be.cytomine.social

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

import be.cytomine.CytomineDomain
import be.cytomine.image.ImageInstance
import be.cytomine.project.Project
import be.cytomine.security.SecUser

/**
 *  User position on an image at a time
 *  Same as position but removed after 60 sec (light collection)
 *
 *  SEE UserPosition.groovy to understand why LastUserPosition and PersistentUserPosition doesn't extends UserPosition
 */
class LastUserPosition extends CytomineDomain {

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

        //SPECIFIC FOR LAST USER POSITION!!!!!
        compoundIndex created:1, indexAttributes:['expireAfterSeconds':60]
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
        com.vividsolutions.jts.geom.Polygon polygon = PersistentUserPosition.getPolygonFromMongo(domain?.location)
        returnArray.location = polygon.toString()
        returnArray.x = polygon.getCentroid().getX()
        returnArray.y = polygon.getCentroid().getY()
        returnArray
    }




}
