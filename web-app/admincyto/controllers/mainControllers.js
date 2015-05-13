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

angular.module("cytomineUserArea")
    .constant("currentUserUrl", "/api/user/current.json")
    .config(function ($logProvider) {
        $logProvider.debugEnabled(true);
    })
//    .config(function ($httpProvider) {
//        $httpProvider.interceptors.push(function ($log) {
//            return {
//                response: function (response) {
//                    $log.debug(response);
//                    return response;
//                }
//            }
//        });
//    })
//    .factory('$exceptionHandler', function () {
//        return function (exception, cause) {
//            alert(exception.message +" " + cause);
//        };
//    })
    .controller("mainCtrl", function ($scope, $http, $location,currentUserUrl) {
        console.log("mainCtrl");
        $scope.main = {error:{}};

        $scope.isCurrentUserAdmin = false;
        $scope.isLoading = true;

        $scope.getCurrentUser = function() {
            $http.get(currentUserUrl)
                .success(function (data) {
                    console.log(data);
                    $scope.main.currentUserFullname = data.lastname + " " + data.firstname;
                    $scope.isCurrentUserAdmin = data.admin;
                    $scope.isLoading = false;

                })
                .error(function (data, status, headers, config) {
                    $scope.main.error.retrieve = {status:status,message:data.errors};
                })
        };
        $scope.getCurrentUser();


        $scope.throwEx = function() {
            throw  { message: 'error occurred ;-)!' }
        }
    });