package be.cytomine.search.engine

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

import be.cytomine.security.SecUser

/**
 * Created by lrollus on 7/22/14.
 */
abstract class EngineSearch {

    List<Long> idProject
    SecUser currentUser
    String operator = "OR"
    boolean extractValue
    List<Long> restrictedIds = null

    public abstract String createRequestOnAttributes(List<String> words)

    public abstract String createRequestOnProperty(List<String> words)

    public abstract String createRequestOnDescription(List<String> words)

    public String formatCriteriaToWhere(List<String> words, String column) {
        List<String> req = []
        words.each {
            req << "$column ilike '%$it%' "
        }
        return "(" + req.join(" $operator ") + ")"
//        return "true"
    }

    public String getDate(String table) {
        return "CASE WHEN ${table}.updated IS NULL THEN ${table}.created ELSE ${table}.updated END as date "
    }

    public String getMatchingValue(String column) {
        if (!extractValue) {
            return ""
        }

        String type = "domain"
        if (column.contains("description")) {
            type = "description"
        } else if (column.contains("property")) {
            type = "property"
        }
        return ", $column as value, '$type' as type"
    }

    public String getRestrictedIdForm(String column) {
        return (restrictedIds == null ? "" : "AND $column IN (${restrictedIds.join(",")})")
    }

    public String getName(String column) {
        if (!extractValue) {
            return ""
        }
        return ", $column as name"
    }
}
