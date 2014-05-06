angular.module("cytomineUserArea")
    .controller("userCtrl", function ($scope,$log,$location,$routeParams,$filter,userService,validationService,selectedUserService,ngTableParams) {

        $scope.user = {error:{}};
        $scope.selected;

        $scope.$on("$routeChangeSuccess", function () {
            if ($location.path().indexOf("/user-info/") == 0) {
                $scope.getAllUsers(function(data) {
                    $scope.user.users = data;
                    $scope.setSelectedUser($routeParams["id"]);
                });

            }
            if ($location.path().indexOf("/user-editor/") == 0) {
                $scope.getAllUsers(function(data) {
                    $scope.user.users = data;
                    var user
                    if(angular.isDefined($routeParams["id"])) {
                        user = $scope.setSelectedUser($routeParams["id"]);
                    }
                    $scope.currentEditedUser = user ? angular.copy(user) : {};
                });
            }

        });

        $scope.setSelectedUser = function(id) {
            var user = userService.getUser(id,$scope.user.users);
            selectedUserService.setSelectedUser(user);
            console.log("selectedUserChange.broadcast");
            $scope.$broadcast('selectedUserChange', [user.id]);
            $scope.selected = user;
            return user;
        }

        $scope.getAllUsers = function(callbackSuccess) {
            userService.getAllUsers(
                function(data) {
                    callbackSuccess(data);
                },
                function(data, status) {
                    $scope.user.error.retrieve = {status:status,message:data.errors};
                }
            );
        };

        $scope.loading = true;
        $scope.getAllUsers(
            function(data) {
                $scope.user.error.retrieve = null;
                $scope.user.users = data;

                $scope.tableParams = new ngTableParams({
                    page: 1,            // show first page
                    count: 10 ,          // count per page
                    sorting: {
                        username: 'asc'     // initial sorting
                    },
                    filter: {
                        username: ''       // initial filter
                    }
                }, {
                    total: $scope.user.users.length, // length of data
                    getData: function($defer, params) {
                        // use build-in angular filter
                        var newData = $scope.user.users;
                        // use build-in angular filter
                        newData = params.filter() ?$filter('filter')(newData, params.filter()) : newData;
                        newData = params.sorting() ? $filter('orderBy')(newData, params.orderBy()) : newData;
                        $scope.data = newData.slice((params.page() - 1) * params.count(), params.page() * params.count())
                        params.total(newData.length); // set total for recalc pagination
                        $defer.resolve($scope.data);
                        $scope.loading = false;
                    }
            });

        });

        $scope.getSelectedUser = function() {
            return selectedUserService.getSelectedUser();
        };

        $scope.cancelEdit = function (form) {
            $scope.currentEditedUser = {};
            if(angular.isDefined(form)) {
                form.$dirty = false;
            }
            $scope.user.error.editOrAdd = null;
            $scope.showValidationUserForm = false;
            $location.url("/user-list");
        };

        $scope.saveEdit = function (user, form) {
            if (angular.isDefined(user.id)) {
                $scope.updateUser(user,form);
            } else {
                $scope.createUser(user,form);
            }
        };

        $scope.createUser = function(newUser,form) {
            $scope.addUserForm = form;
            if($scope.addUserForm.$valid) {

                userService.addUser(
                    newUser,
                function(data) {
                    $scope.user.error.editOrAdd = null;
                    $scope.addUserForm.$dirty = false;
                    //$scope.infoUser(data.user);
                    $location.url("/user-info/"+data.user.id);
                },function(data, status) {
                        $scope.user.error.editOrAdd = {status:status,message:data.errors};
                });
            } else {
                $scope.showValidationUserForm = true;
            }

        };

        $scope.updateUser = function(newUser,form) {
            $scope.editUserForm = form;
            if($scope.editUserForm.$valid) {

                userService.editUser(
                    newUser,
                    function(data) {
                        $scope.user.error.editOrAdd = null;
                        $scope.editUserForm.$dirty = false;
                        //$scope.displayMode = "list";
                        $location.url("/user-info/"+data.user.id);
                    },function(data,status) {
                        $scope.user.error.editOrAdd = {status:status,message:data.errors};
                    });
            }else {
                alert('edit error');
                $scope.showValidationUserForm = true;
            }

        };

        $scope.resetPassword = function(idUser,password,passwordConfirm,form) {
            $log.info("idUser="+idUser +" password=" + password + " passwordConfirm="+passwordConfirm + " form="+form + " defin="+angular.isDefined(password));
            $scope.resetPasswordForm = form;

            if(!angular.isDefined(password) || password<5) {
                $scope.user.error.resetPassword = {status:status,message:"Password is too short!"};
            }else if(password!=passwordConfirm) {
                $scope.user.error.resetPassword = {status:status,message:"Confirm password is invalid!"};
            }else {
                userService.resetPassword(
                    idUser,
                    password,
                    function(data) {
                        $scope.user.error.resetPassword = null;
                        $scope.editUserForm.$dirty = false;
                        //$scope.displayMode = "list";
                        $location.url("/user-info/"+idUser);
                    },function(data,status) {
                        $scope.user.error.resetPassword = {status:status,message:data.errors};
                    });
            }
        };

        $scope.getError = function (error) {
            return validationService.getError(error);
        };
    });