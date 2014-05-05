angular.module("cytomineUserArea")
//    .controller('DemoCtrl', function($scope, ngTableParams) {
//    var data = [{name: "Moroni", age: 50},
//        {name: "Tiancum", age: 43},
//        {name: "Jacob", age: 27},
//        {name: "Nephi", age: 29},
//        {name: "Enos", age: 34},
//        {name: "Tiancum", age: 43},
//        {name: "Jacob", age: 27},
//        {name: "Nephi", age: 29},
//        {name: "Enos", age: 34},
//        {name: "Tiancum", age: 43},
//        {name: "Jacob", age: 27},
//        {name: "Nephi", age: 29},
//        {name: "Enos", age: 34},
//        {name: "Tiancum", age: 43},
//        {name: "Jacob", age: 27},
//        {name: "Nephi", age: 29},
//        {name: "Enos", age: 34}];
//
//    $scope.tableParams = new ngTableParams({
//        page: 1,            // show first page
//        count: 10           // count per page
//    }, {
//        total: data.length, // length of data
//        getData: function($defer, params) {
//            $defer.resolve(data.slice((params.page() - 1) * params.count(), params.page() * params.count()));
//        }
//    });
//})
    .controller("userCtrl", function ($scope,$location,$routeParams,$filter,allUsers,formValidation,selectedUser,ngTableParams) {

        $scope.user = {error:{}};


        $scope.$on("$routeChangeSuccess", function () {
            if ($location.path().indexOf("/user-info/") == 0) {
                $scope.getAllUsers(function(data) {
                    $scope.user.users = data;
                    var id = $routeParams["id"];
                    selectedUser.setSelectedUser(allUsers.getUser(id,$scope.user.users));
                });

            }
            if ($location.path().indexOf("/user-editor/") == 0) {
                $scope.getAllUsers(function(data) {
                    $scope.user.users = data;
                    var id = $routeParams["id"];
                    selectedUser.setSelectedUser(allUsers.getUser(id,$scope.user.users));
                    var user = selectedUser.getSelectedUser();
                    $scope.currentEditedUser = user ? angular.copy(user) : {};
                });
            }

        });

        $scope.getAllUsers = function(callbackSuccess) {
            allUsers.getAllUsers(
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
            return selectedUser.getSelectedUser();
        };

        $scope.cancelEdit = function (form) {
            $scope.currentEditedUser = {};
            if(angular.isDefined(form)) {
                form.$dirty = false;
            }
            $scope.user.error.editOrAdd = null;
            $scope.showValidationUserForm = false;
            //$scope.displayMode = "list";
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

                allUsers.addUser(
                    newUser,
                function(data) {
                    $scope.user.error.editOrAdd = null;
                    $scope.addUserForm.$dirty = false;
                    //$scope.infoUser(data.user);
                    $location.url("/user-info/"+data.user.id);
                },function() {
                        $scope.user.error.editOrAdd = {status:status,message:data.errors};
                });
            } else {
                $scope.showValidationUserForm = true;
            }

        };

        $scope.updateUser = function(newUser,form) {
            $scope.editUserForm = form;
            if($scope.editUserForm.$valid) {

                allUsers.editUser(
                    newUser,
                    function(data) {
                        $scope.user.error.editOrAdd = null;
                        $scope.editUserForm.$dirty = false;
                        //$scope.displayMode = "list";
                        $location.url("/user-list");
                    },function() {
                        $scope.user.error.edit = {status:status,message:data.errors};
                    });
            }else {
                alert('edit error');
                $scope.showValidationUserForm = true;
            }

        };

        $scope.getError = function (error) {
            return formValidation.getError(error);
        };


        //TODO: move this to a directive
        $scope.currentPage = 0;
        $scope.pageSize = 10;
        $scope.numberOfPages=function(){
            if(angular.isDefined($scope.user.users)) {
                return Math.ceil($scope.user.users.length/$scope.pageSize);
            }
            return 1;
        };



    });