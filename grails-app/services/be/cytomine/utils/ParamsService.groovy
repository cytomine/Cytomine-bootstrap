package be.cytomine.utils

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

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.sql.AnnotationListing
import groovy.sql.Sql

/**
 * @author lrollus
 *
 * This service simplify request parameters extraction in controller
 * E.g. thanks to "/api/annotation.json?users=1,5 => it will retrieve user object with 1 and 5
 */
class ParamsService {

    def imageInstanceService
    def termService
    def secUserService
    def dataSource


        /**
         * Retrieve all user id from paramsUsers request string (format users=x,y,z or x_y_z)
         * Just get user from project
         */
        public List<Long> getParamsUserList(String paramsUsers, Project project) {
           if(paramsUsers != null && !paramsUsers.equals("null")) {
               if (!paramsUsers.equals(""))
                   return secUserService.getAllowedUserIdList(project).intersect(paramsUsers.split(paramsUsers.contains("_")?"_":",").collect{ Long.parseLong(it)})
               else return []
           } else {
               secUserService.getAllowedUserIdList(project)
           }
        }

        /**
         * Retrieve all user and userjob id from paramsUsers request string (format users=x,y,z or x_y_z)
         * Just get user and user job  from project
         */
        public List<Long> getParamsSecUserList(String paramsUsers, Project project) {
           if(paramsUsers != null && !paramsUsers.equals("null")) {
               if (!paramsUsers.equals(""))
                   return getUserIdList(paramsUsers.split(paramsUsers.contains("_")?"_":",").collect{ Long.parseLong(it)})
               else return []
           } else {
               secUserService.getAllowedUserIdList(project)
           }
        }

        /**
         * Retrieve all images id from paramsImages request string (format images=x,y,z or x_y_z)
         * Just get images from project
         */
        public List<Long> getParamsImageInstanceList(String paramsImages, Project project) {
           if(paramsImages != null && !paramsImages.equals("null")) {
               if (!paramsImages.equals(""))
                        return imageInstanceService.getAllImageId(project).intersect(paramsImages.split(paramsImages.contains("_")?"_":",").collect{ Long.parseLong(it)})
               else return []
           } else {
               imageInstanceService.getAllImageId(project)
           }
        }

        /**
         * Retrieve all images id from paramsImages request string (format images=x,y,z or x_y_z)
         * Just get images from project
         */
        public List<Long> getParamsTermList(String paramsTerms, Project project) {
           if(paramsTerms != null && !paramsTerms.equals("null")) {
               if (!paramsTerms.equals(""))
                        return termService.getAllTermId(project).intersect(paramsTerms.split(paramsTerms.contains("_")?"_":",").collect{ Long.parseLong(it)})
               else return []
           } else {
               termService.getAllTermId(project)
           }
        }

        /**
         * Retrieve all user and userjob object from paramsUsers request string (format users=x,y,z or x_y_z)
         * Just get user and user job  from project
         */
        public List<SecUser> getParamsSecUserDomainList(String paramsUsers, Project project) {
            List<SecUser> userList = []
            if (paramsUsers != null && paramsUsers != "null" && paramsUsers != "") {
                userList = secUserService.list(project, paramsUsers.split("_").collect{ Long.parseLong(it)})
            }
            return userList
        }

        private List<Long> getUserIdList(List<Long> users) {
            String request = "SELECT DISTINCT sec_user.id \n" +
                    " FROM sec_user \n" +
                    " WHERE id IN ("+users.join(",")+")"
            def data = []
            def sql = new Sql(dataSource)
            sql.eachRow(request) {
                data << it[0]
            }
            try {
                sql.close()
            }catch (Exception e) {}
            return data
        }


        public List<String> getPropertyGroupToShow(params) {
            def propertiesToPrint = []

            //map group properties and the url params name
            def assoc = [showBasic:'basic',showMeta:'meta',showWKT:'wkt',showGIS:'gis',showTerm:'term',showImage:'image',showAlgo:'algo', showUser: 'user']

            //show if ask
            assoc.each { show, group ->
                if(params.getBoolean(show)) {
                    propertiesToPrint << group
                }
            }

            //if no specific show asked show default prop
            if(params.getBoolean('showDefault') || propertiesToPrint.isEmpty()) {
                AnnotationListing.availableColumnDefault.each {
                    propertiesToPrint << it
                }
                propertiesToPrint.unique()
            }

            //hide if asked
            assoc.each { show, group ->
                if(params.getBoolean(show.replace('show','hide'))) {
                    propertiesToPrint = propertiesToPrint - group
                }
            }

            if(propertiesToPrint.isEmpty()) {
                throw new ObjectNotFoundException("You must ask at least one properties group for request.")
            }

            propertiesToPrint
        }

    }

