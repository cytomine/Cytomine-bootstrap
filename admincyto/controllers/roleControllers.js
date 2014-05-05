angular.module("cytomineUserArea")
    .constant("roleUrl", "http://localhost:8090/api/role.json")
    .constant("userRoleUrl", "http://localhost:8090/api/user/{id}/role.json")
    .constant("userRoleUrlLong", "http://localhost:8090/api/user/{idUser}/role/{idRole}.json")
    .constant("userRoleDefineUrl", "http://localhost:8090/api/user/{idUser}/role/{idRole}/define.json")
    .controller("roleCtrl", function ($scope,$location, $http, roleUrl,userRoleUrl,userRoleDefineUrl,selectedUser) {

        $scope.role = {error:{}};

        $scope.selectedUserId = null;

        $scope.getAllRoles = function() {
            $http.get(roleUrl)
                .success(function (data) {
                    $scope.role.error.retrieve = null;
                    $scope.role.allRoles = data;
                })
                .error(function (data, status, headers, config) {
                    $scope.role.role.retrieve = {status:status,message:data.errors};
                });
        };

        $scope.getAllRoles();


        $scope.getUserRole = function(idUser) {
            $http.get(userRoleUrl.replace("{id}", idUser))
                .success(function (data) {
                    $scope.role.error.retrieve = null;
                    $scope.role.userRoles = data;
                    $scope.roles = $scope.role.userRoles;
                    $scope.idSelectRoleValue = $scope.getRoleId($scope.highestUserAuthority($scope.role.userRoles));
                })
                .error(function (data, status, headers, config) {
                    $scope.role.error.retrieve = {status:status,message:data.errors};
                });
        };


        $scope.getRoles = function () {
            return $scope.roles;
        };

        $scope.$on('selectedUserChange', function(event, mass) {
            $scope.selectedUserId = mass[0];
            $scope.getUserRole($scope.selectedUserId);
        });

        $scope.allRoles = function () {
            return $scope.role.allRoles;
        };

        $scope.editUserRole = function(idRole,form) {
            $scope.editRoleForm = form;
            $scope.idSelectRoleValue = idRole;
            console.log("editUserRole: " + $scope.idSelectRoleValue);
            $http.put(userRoleDefineUrl.replace("{idUser}",$scope.selectedUserId).replace("{idRole}",$scope.idSelectRoleValue), "")
                .success(function (data) {
                    $scope.role.error.edit = null;
                    $scope.editRoleForm.$dirty = false;
                    $scope.getUserRole($scope.selectedUserId);
                    $scope.$emit('printUserInfo', []);
                })
                .error(function (data, status, headers, config) {
                    $scope.role.error.edit = {status:status,message:data.errors};
                })
        };

        $scope.getRoleId = function(authority) {
            var id = -1;
            angular.forEach($scope.role.allRoles,function(userRole) {
                if(userRole.authority==authority) {
                    id = userRole.id;
                }
            });
            return id;
        }

        $scope.isUserHightestAuthority = function(userRoles, authority) {
            return $scope.highestUserAuthority(userRoles)==authority;
        }

        $scope.highestUserAuthority = function(userRoles) {
            var isGuest = false;
            var isUser = false;
            var isAdmin = false;
            angular.forEach(userRoles,function(userRole) {
                if(userRole.authority=="ROLE_GUEST") {
                    isGuest = true;
                }
                if(userRole.authority=="ROLE_USER") {
                    isUser = true;
                }
                if(userRole.authority=="ROLE_ADMIN") {
                    isAdmin = true;
                }
            });
            if(isAdmin) {
                return "ROLE_ADMIN";
            }
            if(isUser) {
                return "ROLE_USER";
            }
            if(isGuest) {
                return "ROLE_GUEST";
            }
            return "Unknow";
        }

//
//

//
//
//        $scope.getUserRole(selectedUser.getSelectedUserId());
//

//

//

//

//
//

//
//
//        //TODO: merge with code from directive
//        $scope.getLabel = function(item) {
//            var label = "label-default";
//            if(item.authority=="ROLE_ADMIN") {
//                label = "label-danger";
//            } else if(item.authority=="ROLE_USER") {
//                label = "label-success";
//            } else if(item.authority=="ROLE_GUEST") {
//                label = "label-primary";
//            }
//            return label;
//        };
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


    });