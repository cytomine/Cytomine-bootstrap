angular.module("cytomineUserArea")
    .constant("currentUser", "101")
    .constant("userUrl", "http://localhost:8090/api/user.json")
    .constant("userEditUrl", "http://localhost:8090/api/user/{id}.json")
    .filter("longToDate", function () {
        return function (value) {
            if (angular.isNumber(value)) {
                var date = new Date();
                date.setTime(value);
                return date;
            } else {
                return value;
            }
        };
    })
    .controller("userCtrl", function ($scope,$location, $http, $sanitize,userUrl,userEditUrl,selectedUser,currentUser,allUsers) {

        $scope.displayMode = "list";

        $scope.user = {error:{}};

//        $scope.idSelect = selectedUser.getSelectedUserId();
        $scope.selected = null;

        $scope.lastRefresh = null;


        $scope.getAllUsers = function() {

            var success = function(data) {
                $scope.user.error.retrieve = null;
                console.log(data);
                $scope.user.users = data;
                $scope.lastRefresh = new Date();
            };

            var error = function(data, status) {
                $scope.user.error.retrieve = {status:status,message:data.errors};
            };

            allUsers.getAllUsers(
                success,error
            );
        };



//        $scope.setSelectedUser = function(idUser) {
//            $scope.idSelected = idUser;
//            $scope.selected = this.getUser(idUser);
//            $scope.selectedCopy = angular.copy($scope.selected);
//            selectedUser.setSelectedUser($scope.selected);
//            $scope.$broadcast('selectedUserChange', [$scope.idSelected]);
//        }
//        $scope.setSelectedUser = function(idUser) {
//            $scope.idSelected = idUser;
//            $scope.selected = this.getUser(idUser);
//            $scope.selectedCopy = angular.copy($scope.selected);
//            selectedUser.setSelectedUser($scope.selected);
//            $scope.$broadcast('selectedUserChange', [$scope.idSelected]);
//        }

        $scope.getUser = function(id){
            var goodUser = null;
            angular.forEach($scope.user.users, function(user) {
                if(user.id==id) {
                    goodUser = user;
                }
            });
            return goodUser;
        };

        $scope.getAllUsers();


//        $scope.editMode = function() {
//
//        };
        $scope.$watch("selected", function() {
            if($scope.selected!=null) {
                $scope.$broadcast('selectedUserChange', [$scope.selected.id]);
            }
        });

        $scope.selected = null;

        $scope.infoUser = function(user) {
            $scope.displayMode = "info";
            $scope.selected = user;
        };

        $scope.getCurrentUser = function() {
            return $scope.selected;
        };

        $scope.editOrCreateUser = function (user) {
            $scope.currentUser = user ? angular.copy(user) : {};
            $scope.selected = user;
            $scope.displayMode = "edit";
        };

        $scope.cancelEdit = function (form) {
            $scope.currentUser = {};
            if(angular.isDefined(form)) {
                form.$dirty = false;
            }
            $scope.user.error.editOrAdd = null;
            $scope.showValidationUserForm = false;
            $scope.displayMode = "list";
        };

        $scope.saveEdit = function (user, form) {
            console.log("saveEdit="+user.id);
            if (angular.isDefined(user.id)) {
                $scope.updateUser(user,form);
            } else {
                $scope.createUser(user,form);
            }
        }

        $scope.createUser = function(newUser,form) {
            $scope.addUserForm = form;
            if($scope.addUserForm.$valid) {
                $http.post(userUrl, newUser)
                    .success(function (data) {
                        $scope.user.error.editOrAdd = null;
                        $scope.addUserForm.$dirty = false;

                       // $scope.user.users.push(data.user);
                        allUsers.addUser(data.user);

                        $scope.infoUser(data.user);
                    })
                    .error(function (data, status, headers, config) {
                        $scope.user.error.editOrAdd = {status:status,message:data.errors};
                    })
            } else {
                alert('create error');
                $scope.showValidationUserForm = true;
            }

        };

        $scope.updateUser = function(editedUser,form) {
            $scope.editUserForm = form;
            if($scope.editUserForm.$valid) {
                $http.put(userEditUrl.replace("{id}", editedUser.id), editedUser)
                    .success(function (data) {
                        $scope.user.error.editOrAdd = null;
                        $scope.editUserForm.$dirty = false;
//                        $scope.refreshUser(editedUser.id);
//
//                        $scope.setMode('Table');
                        $scope.user.users[$scope.user.users.indexOf($scope.getUser(data.user.id))] = data.user;
                        $scope.displayMode = "list";

                    })
                    .error(function (data, status, headers, config) {
                        $scope.user.error.edit = {status:status,message:data.errors};
                    })
            }else {
                alert('edit error');
                $scope.showValidationUserForm = true;
            }
        };

        $scope.$on('printUserInfo', function(event, mass) {
            $scope.infoUser($scope.getCurrentUser());
        });


        $scope.currentPage = 0;
        $scope.pageSize = 2;
        $scope.numberOfPages=function(){
            if(angular.isDefined($scope.user.users)) {
                return Math.ceil($scope.user.users.length/$scope.pageSize);
            }
            return 1;
        };





//        $scope.setSelectedUser = function(idUser) {
//            $scope.idSelected = idUser;
//            $scope.selected = this.getUser(idUser);
//            $scope.selectedCopy = angular.copy($scope.selected);
//            selectedUser.setSelectedUser($scope.selected);
//            $scope.$broadcast('selectedUserChange', [$scope.idSelected]);
//        }
//
//        $scope.setMode = function(mode) {
//            $scope.mode = mode;
//        };
//
//        $scope.getCurrentUser = function() {
//            return selectedUser.getSelectedUser();
//        };
//
//        $scope.$on('refreshUser', function(event, mass) {
//            $scope.refreshCurrentUser();
//        });
//
//        $scope.refreshCurrentUser = function() {
//            $scope.refreshUser(selectedUser.getSelectedUserId());
//        }
//
//        $scope.refreshUser = function(idUser) {
//            $http.get(userEditUrl.replace("{id}", idUser))
//                .success(function (data) {
//                    $scope.user.error.retrieve = null;
//                    $scope.replaceCurrentUser(data);
//                })
//                .error(function (data, status, headers, config) {
//                    $scope.user.error.retrieve = {status:status,message:data.errors};
//                })
//        };
//
//        $scope.replaceCurrentUser = function(newUser) {
//            for(var i=0;i<$scope.user.users.length;i++) {
//                if($scope.user.users[i].id==newUser.id) {
//                    $scope.user.users[i] = angular.copy(newUser);
//                    this.setSelectedUser($scope.user.users[i].id);
//                    break;
//                }
//            }
//
//        };
//
//        $scope.getFullname = function(user) {
//            return user.lastname + " " + user.firstname
//        };
//

//        $scope.$watch("idSelected", function() {
//            $scope.setSelectedUser($scope.idSelected);
//        });
//
//        $scope.sanitizeFormData = function(newObj) {
//            for (var key in newObj) {
//                if (newObj.hasOwnProperty(key)) {
//                    var obj = newObj[key];
//                    for (var prop in obj) {
//                        if (obj.hasOwnProperty(prop)) {
//                            obj[prop] = $sanitize(obj[prop]);
//                        }
//                    }
//                }
//            }
//        }
//


        $scope.getError = function (error) {
            if (angular.isDefined(error)) {
                if (error.required) {
                    return "Please enter a value";
                } else if (error.email) {
                    return "Please enter a valid email address";
                }
            }
        }
//
    });