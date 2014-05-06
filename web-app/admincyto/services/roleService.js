angular.module("cytomineUserArea")
    .constant("roleUrl", "/api/role.json")
    .constant("userRoleUrl", "/api/user/{id}/role.json")
    .constant("userRoleUrlLong", "/api/user/{idUser}/role/{idRole}.json")
    .constant("userRoleDefineUrl", "/api/user/{idUser}/role/{idRole}/define.json")
.factory("roleService",function($http, roleUrl,userRoleUrl,userRoleDefineUrl) {

    var roles=[];

    return {


        allRoles : function() {
            return roles;
        },

        getAllRoles : function(callbackSuccess, callbackError) {
            if(roles.length==0) {
                this.refreshAllRoles(callbackSuccess,callbackError);
            } else {
                callbackSuccess(roles);
            }
        },

        refreshAllRoles : function(callbackSuccess, callbackError) {
            $http.get(roleUrl)
                .success(function (data) {

                    roles = data;
                    if(callbackSuccess) {
                        callbackSuccess(data);
                    }
                })
                .error(function (data, status, headers, config) {
                    if(callbackError) {
                        callbackError(data,status);
                    }
                })
        },

        getUserRole : function(idUser,callbackSuccess,callbackError) {
            $http.get(userRoleUrl.replace("{id}", idUser))
                .success(function (data) {
                    if(callbackSuccess) {
                        callbackSuccess(data);
                    }
                })
                .error(function (data, status, headers, config) {
                    callbackError(data,status)
                });
        },

        editUserRole : function(idRole, idUser,callbackSuccess,callbackError) {
            $http.put(userRoleDefineUrl.replace("{idUser}",idUser).replace("{idRole}",idRole), "")
                .success(function (data) {
                    if(callbackSuccess) {
                        callbackSuccess(data);
                    }
                })
                .error(function (data, status, headers, config) {
                    callbackError(data,status)
                })
        },


        getRoleId : function(authority) {
            var id = -1;
            angular.forEach(roles,function(userRole) {
                if(userRole.authority==authority) {
                    id = userRole.id;
                }
            });
            return id;
        },

        isUserHightestAuthority : function(userRoles, authority) {
            return $scope.highestUserAuthority(userRoles)==authority;
        },

        highestUserAuthority : function(userRoles) {
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

    };
});