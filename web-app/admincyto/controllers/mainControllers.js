angular.module("cytomineUserArea")
    .constant("currentUserUrl", "http://localhost:8090/api/user/current.json")
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
    .controller("mainCtrl", function ($scope, $http, currentUserUrl) {
        console.log("mainCtrl");
        $scope.main = {error:{}};

        $scope.getCurrentUser = function() {
            $http.get(currentUserUrl)
                .success(function (data) {
                    console.log(data);
                    $scope.main.currentUserFullname = data.lastname + " " + data.firstname;

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