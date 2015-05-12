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
            var isSuperAdmin = false;
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
                if(userRole.authority=="ROLE_SUPER_ADMIN") {
                    isSuperAdmin = true;
                }
            });
            if(isSuperAdmin) {
                return "ROLE_SUPER_ADMIN";
            }
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