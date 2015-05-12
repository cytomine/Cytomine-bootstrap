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

import be.cytomine.ontology.UserAnnotation

/**
 * Created by lrollus on 7/22/14.
 */
class UserAnnotationSearch extends AnnotationSearch {

    public String getClassName() {
        return UserAnnotation.class.name
    }

    public String getTable() {
        return "user_annotation"
    }


    public String getTermTable() {
        return "annotation_term"
    }

    public String getLinkTerm() {
        return "AND annotation.id = at.user_annotation_id\n" +
                "AND term.id = at.term_id"
    }


}
