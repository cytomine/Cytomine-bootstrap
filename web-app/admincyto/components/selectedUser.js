/**
 * Created by lrollus on 22/04/14.
 */

angular.module("selectedUser",[])
.constant("currentUser", "100036")
.factory("selectedUser",function(currentUser) {
    var selectedUser={};

    return {
        getSelectedUser : function() {
            return selectedUser;
        },
        getSelectedUserId : function() {
            return this.getSelectedUser()? this.getSelectedUser().id : undefined;
        },
        setSelectedUser : function(user) {
            selectedUser = user;
        }
    }
})
    .directive('userTable', function () {
        return {
            restrict:'E',
            templateUrl: "components/userTable.html",
            scope: {
                rolesFn: "&roles",
                userFn: "&user"
            },
            controller : function($scope) {
                $scope.getLabel = function(item) {
                    var label = "label-default";
                    if(item.authority=="ROLE_ADMIN") {
                        label = "label-danger";
                    } else if(item.authority=="ROLE_USER") {
                        label = "label-success";
                    } else if(item.authority=="ROLE_GUEST") {
                        label = "label-primary";
                    }
                    return label;
                };

                $scope.roleSorter = function(item) {
                    var index = 3;
                    if(item.authority=="ROLE_ADMIN") {
                        index = 0;
                    } else if(item.authority=="ROLE_USER") {
                        index = 1;
                    } else if(item.authority=="ROLE_GUEST") {
                        index = 2;
                    }
                    return index
                }
            }
        };
    })
    .directive('userJson', function () {
        return {
            restrict:'E',
            templateUrl: "components/userJson.html",
            scope: {
                userFn: "&user"
            }
        };
    })
;