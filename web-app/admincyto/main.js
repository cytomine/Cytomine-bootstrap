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
 * Created by lrollus on 06/05/14.
 */
angular.module("cytomineUserArea", ["ngRoute","ngSanitize","ngResource","ngTable"])
    .config(function($routeProvider, $locationProvider) {

        $routeProvider.when("/user", {
            templateUrl: "views/user/tableView.html"
        });
        $routeProvider.when("/user-table", {
            templateUrl: "views/user/tableView.html"
        });
        $routeProvider.when("/user-editor/:id?", {
            templateUrl: "views/user/editorView.html"
        });
        $routeProvider.when("/user-info/:id", {
            templateUrl: "views/user/infoView.html"
        });
        $routeProvider.when("/group/:id?", {
            templateUrl: "views/group.html"
        });
        $routeProvider.when("/permission", {
            redirectTo : "/permission/user"
        });
        $routeProvider.when("/permission/user", {
            templateUrl: "views/permission.html"
        });
        $routeProvider.when("/permission/domain", {
            templateUrl: "views/permission.html"
        });
        $routeProvider.when("/configuration", {
            templateUrl: "views/config.html"
        });
        $routeProvider.otherwise({
            templateUrl: "views/user/tableView.html"
        });
    });