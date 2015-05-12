package be.cytomine.ontology

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
import groovy.sql.Sql

class AnnotationIndexService {

    static transactional = true
    def modelService
    def dataSource

    def list(ImageInstance image) {
        String request = "SELECT user_id, image_id,count_annotation,count_reviewed_annotation  \n" +
                " FROM annotation_index \n" +
                " WHERE image_id = "+image.id
        def data = []
        def sql = new Sql(dataSource)

        sql.eachRow(request) {
            data << [user:it[0],image: it[1], countAnnotation: it[2],countReviewedAnnotation: it[3]]
        }

        try {
            sql.close()
        }catch (Exception e) {}
        return data

    }
    /**
     * Return the number of annotation created by this user for this image
     * If user is null, return the number of reviewed annotation for this image
     */
    def count(ImageInstance image, SecUser user) {
        String request
        if (user) {
            request = "SELECT count_annotation  \n" +
                    " FROM annotation_index \n" +
                    " WHERE image_id = "+image.id + " AND user_id = "+ user.id
        } else {
            request = "SELECT sum(count_reviewed_annotation)  \n" +
                    " FROM annotation_index \n" +
                    " WHERE image_id = "+image.id
        }

        long value = 0
        def sql = new Sql(dataSource)
        sql.eachRow(request) {
            def val = it[0]
            val? value = val : 0
        }
        try {
            sql.close()
        }catch (Exception e) {}
        return value
    }

}
