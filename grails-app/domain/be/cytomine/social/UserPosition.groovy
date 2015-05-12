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

/**
 *  User position on an image at a time
 *  2 sub class:
 *  -persistentUserPosition: usefull store the position for a long time (analyze,...)
 *  -lastuserposition: usefull to get the last position (remove if > 1 min)
 */
class UserPosition {//extends CytomineDomain {

    //All field from LastUserPosition and PersistentUserPosition should be here!!!

    //but mongodb plugin doesn't support tablePerHierarchy config.
    //so data from lastuserp (no persistent, remove after x sec) and persistuserp (persistent) go into the same collection

    static def copyProperties(PersistentUserPosition source, LastUserPosition target) {
        target.user = source.user
        target.image = source.image
        target.project = source.project
        target.location = source.location
        target.zoom = source.zoom
        target.created = source.created
        target.imageName = source.imageName
    }
}
