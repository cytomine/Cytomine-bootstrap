angular.module("cytomineUserArea")
.constant("userUrl", "http://localhost:8090/api/user.json")
.factory("allUsers",function($http,userUrl) {

    var users=[];

    return {
        allUsers : function() {
            return users;
        },

        addUser : function(user) {
            users.push(user);
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

        getUser : function(idUser) {
            var goodUser = null;
            angular.forEach(users,function(user) {
               if(user.id == idUser) {
                   goodUser = user;
               }
            });
            return goodUser;
        }
    };
});