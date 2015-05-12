package be.cytomine.utils.database

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

import groovy.sql.Sql

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 6/03/14
 * Time: 16:05
 * Service used to refresh counter (project annotation counter,...)
 */
class CounterService {

    def sessionFactory
    def dataSource
    def grailsApplication
    static transactional = true

    /**
     * Create domain index
     */
    def refreshCounter() {

        try {

            log.info "refreshCounter start"
            /*
             * Refresh counter for each images
             * UPDATE image_instance ii SET
             * count_image_annotations = (SELECT count(*) FROM user_annotation WHERE image_id = ii.id AND deleted IS NULL),
             * count_image_job_annotations = (SELECT count(*) FROM algo_annotation WHERE image_id = ii.id AND deleted IS NULL),
             * count_image_reviewed_annotations = (SELECT count(*) FROM reviewed_annotation WHERE image_id = ii.id AND deleted IS NULL);
             *
             */
            def sql = new Sql(dataSource)
             sql.executeUpdate("UPDATE image_instance ii\n" +
                    "SET\n" +
                    "  count_image_annotations = (SELECT count(*) FROM user_annotation WHERE image_id = ii.id AND deleted IS NULL),\n" +
                    "  count_image_job_annotations = (SELECT count(*) FROM algo_annotation WHERE image_id = ii.id AND deleted IS NULL),\n" +
                    "  count_image_reviewed_annotations = (SELECT count(*) FROM reviewed_annotation WHERE image_id = ii.id AND deleted IS NULL)")

            try {
                sql.close()
            }catch (Exception e) {}
            /*
            * Refresh counter for each images
            * UPDATE project p
            *  SET
            *    count_annotations = (SELECT sum(count_image_annotations) FROM image_instance WHERE project_id = p.id AND deleted IS NULL),
            *    count_job_annotations = (SELECT sum(count_image_job_annotations) FROM image_instance WHERE project_id = p.id AND deleted IS NULL),
            *    count_reviewed_annotations = (SELECT sum(count_image_reviewed_annotations) FROM image_instance WHERE project_id = p.id AND deleted IS NULL),
            *    count_images = (SELECT count(*) FROM image_instance WHERE project_id = p.id AND deleted IS NULL)
            * WHERE p.id IN (SELECT DISTINCT project_id FROM image_instance WHERE deleted IS NULL);
            *
            */
             sql = new Sql(dataSource)
             sql.executeUpdate("UPDATE project p\n" +
                    "  SET \n" +
                    "    count_annotations = (SELECT sum(count_image_annotations) FROM image_instance WHERE project_id = p.id AND deleted IS NULL),\n" +
                    "    count_job_annotations = (SELECT sum(count_image_job_annotations) FROM image_instance WHERE project_id = p.id AND deleted IS NULL),\n" +
                    "    count_reviewed_annotations = (SELECT sum(count_image_reviewed_annotations) FROM image_instance WHERE project_id = p.id AND deleted IS NULL),\n" +
                    "    count_images = (SELECT count(*) FROM image_instance WHERE project_id = p.id AND deleted IS NULL)\n" +
                    "WHERE p.id IN (SELECT DISTINCT project_id FROM image_instance WHERE deleted IS NULL)")
            try {
                sql.close()
            }catch (Exception e) {}



            /*
           * Refresh counter for each images
           * UPDATE project p
           * SET
           * count_annotations = 0,
           * count_job_annotations = 0,
           * count_reviewed_annotations = 0,
           * count_images = 0
           * WHERE p.id NOT IN (SELECT DISTINCT project_id FROM image_instance WHERE deleted IS NULL);
           *
           */
           sql = new Sql(dataSource)
            sql.executeUpdate("UPDATE project p\n" +
                    "SET\n" +
                    "  count_annotations = 0,\n" +
                    "  count_job_annotations = 0,\n" +
                    "  count_reviewed_annotations = 0,\n" +
                    "  count_images = 0\n" +
                    "WHERE p.id NOT IN (SELECT DISTINCT project_id FROM image_instance WHERE deleted IS NULL)")
            try {
                sql.close()
            }catch (Exception e) {}

        } catch (org.postgresql.util.PSQLException e) {
            log.info e
        }
        log.info "refreshCounter end"
    }
}
