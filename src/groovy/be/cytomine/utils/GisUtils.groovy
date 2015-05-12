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
 * User: lrollus
 * Date: 17/10/12
 * GIGA-ULg
 * Utility class to deals with file
 */
class GisUtils {

    public static String PIXEL = 'pixels'
    public static String PIXELS2 = 'pixels²'
    public static String MM = 'mm'
    public static String MICRON2 = 'micron²'


    public static Integer PIXELv = 0
    public static Integer PIXELS2v = 1
    public static Integer MMv = 2
    public static Integer MICRON2v = 3


    public static Map<String,String> unit = [0:PIXEL,1:PIXELS2,2:MM,3:MICRON2]

    public static String retrieveUnit(def value) {
        return unit.get(value)
    }

}
