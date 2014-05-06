angular.module("cytomineUserArea")
    .constant("permissionUrl", "http://localhost:8090/api/acl.json")
    .constant("domainUrl", "http://localhost:8090/api/acl/domain.json")
    .constant("aclUrl","http://localhost:8090/api/domain/{domainClassName}/{domainIdent}/user/{idUser}.json?auth={auth}") //"api/domain/$domainClassName/$domainIdent/user/${user}.json?" + (auth? "auth=$auth" : "")

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
    .controller("permissionCtrl", function ($scope,$location, $http, $resource,userService,permissionUrl,domainUrl,aclUrl,domainPermissionService,maskPermissionService) {

        $scope.idUserForPermissionSelected=null;
        $scope.permission = {error:{}};
        $scope.mode="byUser";

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
                    if($scope.selectedUserForPermission==null) {
                        angular.forEach($scope.permission.users,function(user) {
                            if(user.id==101) {
                                $scope.selectedUserForPermission = user;
                            }
                        });
                    }
                }
            );
        };

        $scope.getAllUsers();




        $scope.getCurrentUser = function() {
            return $scope.selectedUserForPermission;

        };

        $scope.$watch("selectedUserForPermission", function() {
            if($scope.selectedUserForPermission!=null) {
                //$scope.$broadcast('selectedUserChange', [$scope.selectedUserForPermission.id]);
                $scope.getPermissionByUser($scope.selectedUserForPermission.id);
            }
        });

        $scope.$watch("idAddPermissionDomainIdentByDomain", function() {
            console.log($scope.idAddPermissionDomainIdentByDomain);
            if($scope.idAddPermissionDomainIdentByDomain!=null) {
                //$scope.$broadcast('selectedUserChange', [$scope.selectedUserForPermission.id]);
                $scope.getPermissionByDomain($scope.idAddPermissionDomainIdentByDomain);
            }
        });

        $scope.permissionForSelectedUser = null;
        $scope.permissionForSelectedDomain = null;

        $scope.getPermissionByUser = function(idUser) {
            $http.get(permissionUrl+"?idUser="+idUser)
                .success(function (data) {
                    $scope.permissionForSelectedUser = data;
                })
                .error(function (data, status, headers, config) {
                });
        }

        $scope.getPermissionByDomain = function(idDomain) {
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


            $http.post(customACLUrl,{})
                .success(function (data) {
                    $scope.getPermissionByUser($scope.selectedUserForPermission.id);
                    $scope.getPermissionByDomain($scope.idAddPermissionDomainIdentByDomain);
                })
                .error(function (data, status, headers, config) {
                });
        };

        //deletePermissionFromUser(permission.domainClassName,permission.domainIdent,selectedUserForPermission.id,permission.mask)
        $scope.deletePermissionFromUser = function(domainClassName,domainIdent,idUser,permission) {
            var customACLUrl = aclUrl;
            customACLUrl = customACLUrl.replace("{domainClassName}",domainPermissionService.getDomainClassName(domainClassName));
            customACLUrl = customACLUrl.replace("{domainIdent}",domainIdent);
            customACLUrl = customACLUrl.replace("{idUser}",idUser);
            customACLUrl = customACLUrl.replace("{auth}",maskPermissionService.getPermissionFromMask(permission));

            $http.delete(customACLUrl)
                .success(function (data) {
                    $scope.getPermissionByUser($scope.selectedUserForPermission.id);
                    $scope.getPermissionByDomain($scope.idAddPermissionDomainIdentByDomain);
                })
                .error(function (data, status, headers, config) {
                });
        };

        $scope.getUserFullname = function(idUser) {
            var user = userService.getUser(idUser,$scope.permission.users);
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
                var permission = null;
                angular.forEach(masks, function(entry) {
                    if(entry.mask==mask) {
                        permission = entry.name;
                    }
                });
                return permission;
            }
        };
    })
;