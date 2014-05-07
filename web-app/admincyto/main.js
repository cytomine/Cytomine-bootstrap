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
        $routeProvider.otherwise({
            templateUrl: "views/user/tableView.html"
        });
    });