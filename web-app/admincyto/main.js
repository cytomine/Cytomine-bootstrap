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
        $routeProvider.when("/group", {
            templateUrl: "views/group.html"
        });
        $routeProvider.when("/permission", {
            templateUrl: "views/permission.html"
        });
        $routeProvider.otherwise({
            templateUrl: "views/user/tableView.html"
        });
    });