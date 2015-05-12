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
    .constant("permissionUrl", "/api/acl.json")
    .constant("domainUrl", "/api/acl/domain.json")
    .constant("aclUrl","/api/domain/{domainClassName}/{domainIdent}/user/{idUser}.json?auth={auth}") //"api/domain/$domainClassName/$domainIdent/user/${user}.json?" + (auth? "auth=$auth" : "")

    .filter("removePackage", function () {
        return function (item) {
            var pathData = item.split('.');
            return pathData[pathData.length-1];
        };
    })
    .filter("filterByClass",function(domainPermissionService) {

        return function (data,filter) {
            var domainFiltered = [];
            angular.forEach(data,function(item) {
                if(domainPermissionService.getDomainName(item.className)==filter) {
                    domainFiltered.push(item);
                }
            });
            return domainFiltered;
        };

    })
    .controller("permissionCtrl", function ($scope,$location, $routeParams,$http, $resource,userService,permissionUrl,domainUrl,aclUrl,domainPermissionService,maskPermissionService) {

        //a lot of code should be in a permissionService
        //permisision pearams should be in URL

        //$scope.idUserForPermissionSelected=null;
        $scope.permission = {error:{},success:{}};
        $scope.mode="byUser";

        $scope.$on("$routeChangeSuccess", function () {
            $scope.permission.error = {};
            $scope.permission.success = {};
            if ($location.path().indexOf("/permission/user") == 0) {
                $scope.mode="byUser";
//                if($routeParams["id"]) {
                    userService.getAllUsers(
                        function(data) {
                            $scope.permission.users = data;
                            $scope.selectedUserForPermission = userService.getUser($location.search().user,$scope.permission.users);
                        }
                    );
//                }
            }
            if ($location.path().indexOf("/permission/domain") == 0) {
                $scope.mode="byDomain";

                userService.getAllUsers(
                    function(data) {
                        $scope.permission.users = data;
                        $scope.idAddPermissionDomainClass = $location.search().domainClassName
                        $scope.idAddPermissionDomainIdentByDomain = $location.search().domainIdent
                        //$scope.selectedUserForPermission = userService.getUser($location.search().user,$scope.permission.users);
                    }
                );

            }
        });

        $scope.changeMode = function(mode) {
            console.log("changeMode0 => "+$scope.mode + " vs " + mode);
            $scope.mode = mode;
            console.log("changeMode1 => "+$scope.mode);
        };

        $scope.getClassNameMode = function(mode) {
            if(mode==$scope.mode ) {
                return "btn-primary";
            } else {
                return "";
            }
        };

        $scope.domains = [];


        $scope.selectedUserForPermission = null;

        $scope.getAllUsers = function() {

            userService.getAllUsers(
                function(data) {
                    $scope.permission.users = data;
                }
            );
        };

        $scope.getAllUsers();




        $scope.getCurrentUser = function() {
            return $scope.selectedUserForPermission;

        };

        $scope.$watch("selectedUserForPermission", function() {

            if($scope.selectedUserForPermission!=null) {
                $location.search('user', $scope.selectedUserForPermission.id);
                $scope.getPermissionByUser($scope.selectedUserForPermission.id);
            }
        });

        $scope.$watch("idAddPermissionDomainIdentByDomain", function() {
            console.log($scope.idAddPermissionDomainIdentByDomain);
            if($scope.idAddPermissionDomainIdentByDomain!=null) {
                //$scope.$broadcast('selectedUserChange', [$scope.selectedUserForPermission.id]);
                $scope.getPermissionByDomain($scope.idAddPermissionDomainIdentByDomain);
                $location.search('domainIdent', $scope.idAddPermissionDomainIdentByDomain);
                $location.search('domainClassName', $scope.idAddPermissionDomainClass);
            }
        });

        $scope.permissionForSelectedUser = null;
        $scope.permissionForSelectedDomain = null;

        $scope.getPermissionByUser = function(idUser) {
            //TODO: we should use angular params object instead of adding params in url
            $http.get(permissionUrl+"?idUser="+idUser)
                .success(function (data) {
                    $scope.permissionForSelectedUser = data;
                })
                .error(function (data, status, headers, config) {
                });
        }

        $scope.getPermissionByDomain = function(idDomain) {
            //TODO: we should use angular params object instead of adding params in url
            $http.get(permissionUrl+"?idDomain="+idDomain)
                .success(function (data) {
                    $scope.permissionForSelectedDomain = data;
                })
                .error(function (data, status, headers, config) {
                });
        }
        //

        $scope.getDomains = function() {
            $http.get(domainUrl)
                .success(function (data) {
                    $scope.domains = data;
                })
                .error(function (data, status, headers, config) {
                });
        }

        $scope.getDomains();

        //?idUser=101

        $scope.getDomainLabel = function(item) {
            var label = "label-default";
            if(item==domainPermissionService.getDomainClassName("Project")) {
                label = "label-success";
            } else if(item==domainPermissionService.getDomainClassName("Ontology")) {
                label = "label-warning";
            } else if(item==domainPermissionService.getDomainClassName("Storage")) {
                label = "label-danger";
            }else if(item==domainPermissionService.getDomainClassName("Software")) {
                label = "label-info";
            }
            return label;
        };

        //addPermissionToUser(idAddPermissionDomainClass=project,ontology,...,idAddPermissionDomainIdent,idAddPermissionMask=ADMIN,READ,...)
        $scope.addPermissionToUser = function(shortDomainClassName,domainIdent,idUser,permission) {
            //String URL = Infos.CYTOMINEURL + "api/domain/$domainClassName/$domainIdent/user/${user}.json?" + (auth? "auth=$auth" : "")
            //http://localhost:8090/api/domain/{domainClassName}/{domainIdent}/user/{idUser}.json?auth={auth}
            var customACLUrl = aclUrl;
            customACLUrl = customACLUrl.replace("{domainClassName}",domainPermissionService.getDomainClassName(shortDomainClassName));
            customACLUrl = customACLUrl.replace("{domainIdent}",domainIdent);
            customACLUrl = customACLUrl.replace("{idUser}",idUser);
            customACLUrl = customACLUrl.replace("{auth}",maskPermissionService.getPermissionFromMask(permission));

            //TODO: we should use angular params object instead of adding params in url
            $http.post(customACLUrl,{})
                .success(function (data) {
                    $scope.permission.error.addForUser = null;
                    $scope.permission.error.addForDomain = null;
                    if($scope.selectedUserForPermission!=null) {
                        $scope.getPermissionByUser($scope.selectedUserForPermission.id);
                    }

                    $scope.getPermissionByDomain($scope.idAddPermissionDomainIdentByDomain);
                    $scope.permission.success.addForUser = true;
                    $scope.permission.success.addForDomain = true;
                })
                .error(function (data, status, headers, config) {
                    $scope.permission.success.addForUser = false;
                    $scope.permission.error.addForUser = {status:status,message:data.errors};
                    $scope.permission.success.addForDomain = false;
                    $scope.permission.error.addForDomain = {status:status,message:data.errors};
                });
        };

        //deletePermissionFromUser(permission.domainClassName,permission.domainIdent,selectedUserForPermission.id,permission.mask)
        $scope.deletePermissionFromUser = function(domainClassName,domainIdent,idUser,permission) {
            var customACLUrl = aclUrl;
            customACLUrl = customACLUrl.replace("{domainClassName}",domainPermissionService.getDomainClassName(domainClassName));
            customACLUrl = customACLUrl.replace("{domainIdent}",domainIdent);
            customACLUrl = customACLUrl.replace("{idUser}",idUser);
            customACLUrl = customACLUrl.replace("{auth}",maskPermissionService.getPermissionFromMask(permission));
            //TODO: we should use angular params object instead of adding params in url
            $http.delete(customACLUrl)
                .success(function (data) {
                    $scope.getPermissionByUser($scope.selectedUserForPermission.id);
                    $scope.getPermissionByDomain($scope.idAddPermissionDomainIdentByDomain);
                })
                .error(function (data, status, headers, config) {
                });
        };

        $scope.getUserFullname = function(permission) {
            var user = userService.getUser(permission.idUser,$scope.permission.users);
            return user.lastname.toUpperCase() + " " + user.firstname + " (" + user.username + ")";
        };


        $scope.domainsType = domainPermissionService.getAllDomainsType();

        $scope.permissions = maskPermissionService.getAllMasks();

    })
    .factory("domainPermissionService",function() {

        var domains = [
            {name:"Project",className:"be.cytomine.project.Project"},
            {name:"Ontology",className:"be.cytomine.ontology.Ontology"},
            {name:"Storage",className:"be.cytomine.image.server.Storage"},
            {name:"Software",className:"be.cytomine.software.Software"}
        ];

        return {
            getAllDomainsType : function() {
                return domains;
            },

            getDomainClassName : function(domain) {
                var domainClassName = null;
                angular.forEach(domains, function(entry) {
                    if(entry.name==domain || entry.className==domain) {
                        // || so that it works with Project and be.ctytomine...Project
                        domainClassName = entry.className;
                    }
                });
                return domainClassName;
            },
            getDomainName : function(className) {
                var domain = null;
                angular.forEach(domains, function(entry) {
                    if(entry.className==className || entry.name == className) {
                        domain = entry.name;
                    }
                });
                return domain;
            }


        };
    })
    .filter("formatMask", function (maskPermissionService){
        return function (item) {
            return maskPermissionService.getPermissionFromMask(item);
        };
    })
    .factory("maskPermissionService",function() {

        var masks = [
            {name:"READ",mask:1},
            {name:"WRITE",mask:2},
            {name:"DELETE",mask:8},
            {name:"ADMIN",mask:16}
        ];

        return {
            getAllMasks : function() {
                return masks;
            },

            getMaskFromPermission : function(permission) {
                var mask = null;
                angular.forEach(masks, function(entry) {
                    if(entry.name==permission) {
                        mask = entry.mask;
                    }
                });
                return mask;
            },
            getPermissionFromMask : function(mask) {
                console.log("getPermissionFromMask="+mask);
                var permission = null;
                angular.forEach(masks, function(entry) {
                    if(entry.mask==mask) {
                        permission = entry.name;
                    }
                });
                console.log("permission="+permission);
                return permission;
            }
        };
    })
;