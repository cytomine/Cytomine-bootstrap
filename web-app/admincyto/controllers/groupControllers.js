angular.module("cytomineUserArea")
    .constant("groupUrl", "http://localhost:8090/api/group/")
    .config(function ($httpProvider) {
        $httpProvider.interceptors.push(function ($log) {
            return {
                response: function (response) {
                    if(angular.isDefined(response) && angular.isDefined(response.data) && angular.isDefined(response.data.collection)) {
                        response.data = response.data.collection;
                        return response;
                    } else {
                        return response;
                    }

                }
            }
        });
    })
    .controller("groupCtrl", function ($scope,$location, $http, $resource,groupUrl,userService, groupService, validationService) {

        //a lot of method here should be move to group service

        $scope.displayMode = "list";

        $scope.currentGroup = null;

        $scope.usersFromGroup = null;

        $scope.group = {error:{}};

        $scope.groupsResource = $resource(groupUrl + ":id" + ".json", { id: "@id" },{ create: { method: "POST" }, save: { method: "PUT"}});

        $scope.listGroups = function () {
            $scope.group.groups = $scope.groupsResource.query();
            $scope.group.groups.$promise.then(function (data) {
                $scope.setSelectedGroup($scope.group.groups[0].id);
            });
        };

        $scope.createGroup = function (group,form) {
            if(form.$valid) {
                new $scope.groupsResource(group).$create(function (response) {
                    $scope.group.error.add = null;
                    form.$dirty = false;
                    $scope.idSelected = response.group.id;
                    //$scope.selected = $scope.getGroup(response.group.id);
                    $scope.newGroup = null;
                    $scope.group.groups.push(new $scope.groupsResource(response.group));
                },function(response) {
                    $scope.group.error.add = {status:response.status,message:response.data.errors};
                })
            } else {
                $scope.showValidationAddGroupForm = true;
            }
        };

        $scope.updateGroup = function (group,form) {
            if(form.$valid) {
                new $scope.groupsResource(group).$save(function (response) {
                    $scope.group.error.add = null;
                    form.$dirty = false;
                    $scope.idSelected = response.group.id;
                    $scope.newGroup = null;
                    $scope.group.groups[$scope.group.groups.indexOf($scope.getGroup(response.group.id))] = response.group;
                },function(response) {
                    $scope.group.error.edit = {status:response.status,message:response.data.errors};
                })
            } else {
                $scope.showValidationEditGroupForm = true;
            }
        };

        $scope.deleteGroup = function (group) {
            group.$delete().then(function () {
                $scope.group.groups.splice($scope.group.groups.indexOf(group), 1);
                $scope.idSelected = null
            });
        };

        $scope.getUserFromGroup = function(idGroup) {
            $http.get("http://localhost:8090/api/group/{id}/user.json".replace("{id}",idGroup))
                .success(function (data) {
                    $scope.group.usersFromGroup = data;
                })
        }

        $scope.addUserToGroup = function(group,idUser) {
            console.log("add " + idUser + " from " + group.name);
            $http.post("http://localhost:8090/api/user/{id}/group.json".replace("{id}",idUser), {user:idUser,group:group.id})
                .success(function (data) {
                    $scope.getUserFromGroup(group.id);
                })
                .error(function (data, status, headers, config) {
                })
        }


        $scope.deleteUserFromGroup = function(group,user) {
            console.log("delete " + user.username + " from " + group.name);
            $http.delete("http://localhost:8090/api/user/{idUser}/group/{idGroup}.json".replace("{idUser}",user.id).replace("{idGroup}",group.id))
                .success(function (data) {
                    $scope.getUserFromGroup(group.id);
                })
                .error(function (data, status, headers, config) {
                })
        }

        $scope.selectOnlyUserNotInGroup = function(item) {
            var takeIt = true;
            if(angular.isDefined($scope.group.usersFromGroup)) {
                for(var i=0;i<$scope.group.usersFromGroup.length;i++) {
                    if($scope.group.usersFromGroup[i].id==item.id) {
                        takeIt = false;
                        break;
                    }
                }
            }
            return takeIt;
        };

        $scope.getError = function (error) {
            validationService.getError(error);
        };

        $scope.$watch("idSelected", function() {
            $scope.setSelectedGroup($scope.idSelected);
        });

        $scope.setSelectedGroup = function(idSelected) {
            $scope.group.error = {};
            $scope.idSelected = idSelected;
            $scope.selected = $scope.getGroup(idSelected);
            $scope.getUserFromGroup(idSelected);
            $scope.selectedCopy = angular.copy($scope.selected);
        };

        $scope.getCurrentGroup = function() {
            return $scope.getGroup($scope.idSelected);
        };

        $scope.getGroup = function(id){
            var goodGroup = null;
            angular.forEach($scope.group.groups, function(group) {
                if(group.id==id) {
                    goodGroup = group; //bad perf!
                }
            });
            return goodGroup;
        };


        $scope.listGroups();
        userService.getAllUsers(
            function(data) {
                $scope.group.users = data;
        });



//        $scope.displayMode = "list";
//        $scope.currentGroup = null;
//        $scope.lastRefresh = null;
//
//        $scope.group = {error:{}};
//
//        $scope.groupsResource = $resource(groupUrl + ":id" + ".json", { id: "@id" },{ create: { method: "POST" }, save: { method: "PUT"}});
//
//        $scope.listGroups = function () {
//            $scope.group.groups = $scope.groupsResource.query();
//            $scope.group.groups.$promise.then(function (data) {
//                $scope.lastRefresh = new Date();
//                $scope.setSelectedGroup($scope.group.groups[0].id);
//            });
//        };
//
//        $scope.createGroup = function (group,form) {
//            if(form.$valid) {
//                new $scope.groupsResource(group).$create(function (response) {
//                    $scope.group.error.add = null;
//                    form.$dirty = false;
//                    $scope.idSelected = response.group.id;
//                    //$scope.selected = $scope.getGroup(response.group.id);
//                    $scope.newGroup = null;
//                    $scope.group.groups.push(new $scope.groupsResource(response.group));
//                },function(response) {
//                    $scope.group.error.add = {status:response.status,message:response.data.errors};
//                })
//            } else {
//                $scope.showValidationAddGroupForm = true;
//            }
//        };
//
//        $scope.updateGroup = function (group,form) {
//            console.log(form);
//            if(form.$valid) {
//                new $scope.groupsResource(group).$save(function (response) {
//                    $scope.group.error.add = null;
//                    form.$dirty = false;
//                    $scope.idSelected = response.group.id;
//                    $scope.newGroup = null;
//                    $scope.group.groups[$scope.group.groups.indexOf($scope.getGroup(response.group.id))] = response.group;
//                },function(response) {
//                    $scope.group.error.edit = {status:response.status,message:response.data.errors};
//                })
//            } else {
//                $scope.showValidationEditGroupForm = true;
//            }
//        }
//
//        $scope.deleteGroup = function (group) {
//            console.log(group);
//            group.$delete().then(function () {
//                $scope.group.groups.splice($scope.group.groups.indexOf(group), 1);
//                $scope.idSelected = null
//            });
//        }
//
//
//        $scope.getAllUsers = function() {
//            userService.getAllUsers(
//                function(data) {
//                    $scope.group.users = data;
//                }
//            );
//        };
//
//
//        $scope.getUserFromGroup = function(idGroup) {
//            $http.get("http://localhost:8090/api/group/{id}/user.json".replace("{id}",idGroup))
//                .success(function (data) {
//                    $scope.group.usersFromGroup = data;
//                })
//        }
//
//        $scope.addUserToGroup = function(group,idUser) {
//            console.log("add " + idUser + " from " + group.name);
//            $http.post("http://localhost:8090/api/user/{id}/group.json".replace("{id}",idUser), {user:idUser,group:group.id})
//                .success(function (data) {
//                    //$scope.group.usersFromGroup.push(data.usergroup.user);
//                    $scope.getUserFromGroup(group.id);
//                })
//                .error(function (data, status, headers, config) {
//                })
//        }
//
//
//        $scope.deleteUserFromGroup = function(group,user) {
//            console.log("delete " + user.username + " from " + group.name);
//            $http.delete("http://localhost:8090/api/user/{idUser}/group/{idGroup}.json".replace("{idUser}",user.id).replace("{idGroup}",group.id))
//                .success(function (data) {
//                    //$scope.group.usersFromGroup.push(data.usergroup.user);
//                    $scope.getUserFromGroup(group.id);
//                })
//                .error(function (data, status, headers, config) {
//                })
//        }
//
//        $scope.selectOnlyUserNotInGroup = function(item) {
//            var takeIt = true;
//            if(angular.isDefined($scope.group.usersFromGroup)) {
//                for(var i=0;i<$scope.group.usersFromGroup.length;i++) {
//                    if($scope.group.usersFromGroup[i].id==item.id) {
//                        takeIt = false;
//                        break;
//                    }
//                }
//            }
//            return takeIt;
//        }
//
//
//
//        $scope.usersFromGroup = null;
//
//        $scope.listGroups();
//        $scope.getAllUsers();
//
//
//
//
//
//
//
//        $scope.getError = function (error) {
//            if (angular.isDefined(error)) {
//                if (error.required) {
//                    return "Please enter a value";
//                } else if (error.email) {
//                    return "Please enter a valid email address";
//                }
//            }
//        }
//
//        $scope.$watch("idSelected", function() {
//            $scope.setSelectedGroup($scope.idSelected);
//        });
//
//        $scope.setSelectedGroup = function(idSelected) {
//            $scope.group.error = {};
//            $scope.idSelected = idSelected;
//            $scope.selected = this.getGroup(idSelected);
//            $scope.getUserFromGroup(idSelected);
//            $scope.selectedCopy = angular.copy($scope.selected);
//        }
//
//        $scope.getCurrentGroup = function() {
//            var currentGroup = null;
//            angular.forEach($scope.group.groups,function(group) {
//                if(group.id==$scope.idSelected) {
//                    currentGroup = group;
//                }
//            });
//            return currentGroup;
//        }
//
//        $scope.getGroup = function(id){
//            var goodGroup = null;
//            angular.forEach($scope.group.groups, function(group) {
//                if(group.id==id) {
//                    goodGroup = group;
//                }
//            });
//            return goodGroup;
//        };

    });