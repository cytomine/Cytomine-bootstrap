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
 * Created by lrollus on 05/05/14.
 */
angular.module("cytomineUserArea")
    .filter('camelcase', function() {
        return function(input) {
            return input.charAt(0).toUpperCase() + input.substr(1).toLowerCase();
        }
    })
    .filter("longToDate", function () {
        return function (value) {
            if(value==null || value==undefined) {
                return undefined;
            }
            var date = new Date();
            date.setTime(value);
            return date;
        };
    })

    .filter("dateToFuzzyTime", function () {
        return function (date) {
            if(angular.isDefined(date)) {
                /*
                 * JavaScript Pretty Date
                 * Copyright (c) 2011 John Resig (ejohn.org)
                 * Licensed under the MIT and GPL licenses.
                 * 20140506: updated by lrollus
                 */

                // Takes an ISO time and returns a string representing how
                // long ago the date represents.
                function prettyDate(date){
                    diff = (((new Date()).getTime() - date.getTime()) / 1000),
                        day_diff = Math.floor(diff / 86400);

                    if ( isNaN(day_diff) || day_diff < 0) {
                        return;
                    }

//                    if(day_diff>100) {
//
//                    }


                    var result = day_diff == 0 && (
                        diff < 60 && "just now" ||
                        diff < 120 && "1 minute ago" ||
                        diff < 3600 && Math.floor( diff / 60 ) + " minutes ago" ||
                        diff < 7200 && "1 hour ago" ||
                        diff < 86400 && Math.floor( diff / 3600 ) + " hours ago") ||
                        day_diff == 1 && "Yesterday" ||
                        day_diff < 7 && day_diff + " days ago" ||
                        day_diff < 31 && Math.ceil( day_diff / 7 ) + " weeks ago";

                    if(!result) {
                        var printFullDate = function (longDate) {
                            var createdDate = new Date();
                            createdDate.setTime(longDate);

                            //date format
                            var year = createdDate.getFullYear();
                            var month = (createdDate.getMonth() + 1) < 10 ? "0" + (createdDate.getMonth() + 1) : (createdDate.getMonth() + 1);
                            var day = (createdDate.getDate()) < 10 ? "0" + (createdDate.getDate()) : (createdDate.getDate());
                            return year + "-" + month + "-" + day;
                        }
                        result = printFullDate(date);
                    }
                    return result;
                }
                return prettyDate(date)
            } else {
                return date;
            }


        };
    })


;







