angular.module("cytomineUserArea")
.constant("userUrl", "http://localhost:8090/api/user.json")
.constant("userEditUrl", "http://localhost:8090/api/user/{id}.json")
.factory("allUsers",function($http,userUrl,userEditUrl) {

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
        }


    };
});