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
.constant("userUrl", "/api/user.json")
.constant("userEditUrl", "/api/user/{id}.json")
.constant("resetPasswordUrl","/api/user/{idUser}/password.json?password={password}")
.constant("ldapUrl","/api/ldap/{username}/user.json")
.factory("userService",function($http,userUrl,userEditUrl,resetPasswordUrl,ldapUrl) {

    var users=[];

    return {

        allUsers : function() {
            return users;
        },

        getAllUsers : function(callbackSuccess, callbackError) {
            if(users.length==0) {
                this.refreshAllUsers(callbackSuccess,callbackError);
            } else {
                callbackSuccess(users);
            }
        },

        refreshAllUsers : function(callbackSuccess, callbackError) {
            $http.get(userUrl)
                .success(function (data) {
                    users = data;
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

        getUser : function(idUser,users) {
            var allusers = angular.isDefined(users)? users : this.users;
            var goodUser = null;

            angular.forEach(allusers,function(user) {
               if(user.id == idUser) {

                   goodUser = user;
               }
            });
//            if(!goodUser) {
//                angular.forEach(this.users,function(user) {
//                    if(user.id == idUser) {
//                        goodUser = user;
//                    }
//                });
//            }
            return goodUser;
        },

        checkLdap : function(user) {
            $http.get(ldapUrl.replace("{username}", user.username))
                .success(function (data) {
                    user.ldap = data.result
                })
                .error(function (data, status, headers, config) {
                    console.log("error in ldap checking");
                    user.ldap = false
                })
        },

        addUser : function(user,callbackSuccess,callbackError) {
            $http.post(userUrl,user)
                .success(function (data) {
                    users.push(data.user);
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

        editUser : function(editedUser,callbackSuccess,callbackError) {
            var self = this;
            //TODO: we should use angular params object instead of adding params in url
            $http.put(userEditUrl.replace("{id}", editedUser.id),editedUser)
                .success(function (data) {
                    users[users.indexOf(self.getUser(data.user.id,users))] = data.user;
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


        resetPassword : function(idUser,password,callbackSuccess,callbackError) {
            var self = this;
            //TODO: we should use angular params object instead of adding params in url
            $http.put(resetPasswordUrl.replace("{idUser}", idUser).replace("{password}", password),"")
                .success(function (data) {
                    if(callbackSuccess) {
                        callbackSuccess(data);
                    }
                })
                .error(function (data, status, headers, config) {
                    if(callbackError) {
                        callbackError(data,status);
                    }
                })
        }



    };
});