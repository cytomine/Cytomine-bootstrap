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

/**
 * Created with IntelliJ IDEA.
 * User: pierre
 * Date: 24/04/13
 * Time: 14:35
 * To change this template use File | Settings | File Templates.
 */

class SearchOperator {
    public static final String OR = "OR"
    public static final String AND = "AND"

    public static String[] getPossibleValues() {
        [AND, OR]
    }
}

class SearchFilter {
    public static final String ALL = "ALL"
    public static final String PROJECT = "PROJECT"
    public static final String ANNOTATION = "ANNOTATION"
    public static final String IMAGE = "IMAGE"
    public static final String ABSTRACTIMAGE = "ABSTRACTIMAGE"

    public static String[] getPossibleValues() {
        [ALL, PROJECT,ANNOTATION,IMAGE,ABSTRACTIMAGE]
    }
}