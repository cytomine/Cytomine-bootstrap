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

    .controller("roleCtrl", function ($scope,$location, $http,roleService,selectedUserService) {

        $scope.role = {error:{}};


        $scope.getAllRoles = function(callbackSuccess) {
            roleService.getAllRoles(
                function(data) {
                    callbackSuccess(data);
                },
                function(data, status) {
                    $scope.role.error.retrieve = {status:status,message:data.errors};
                }
            );
        };

        $scope.getAllRoles(
            function(data) {
                $scope.role.error.retrieve = null;
                $scope.role.allRoles = data;

         });

        $scope.getUserRole = function(idUser) {
            roleService.getUserRole(
                idUser,
                function (data) {
                    $scope.role.error.retrieve = null;
                    $scope.role.userRoles = data;
                    $scope.roles = $scope.role.userRoles;
                    $scope.idSelectRoleValue = roleService.getRoleId(roleService.highestUserAuthority($scope.role.userRoles));
                },
                function (data, status) {
                    $scope.role.error.retrieve = {status:status,message:data.errors};
                }
            )
        };

        if(angular.isDefined($scope.selected)) {
            $scope.getUserRole($scope.selected.id);
        }


        $scope.getRoles = function () {
            return $scope.roles;
        };

        $scope.$on('selectedUserChange', function(event, mass) {
            console.log("selectedUserChange="+mass[0]);
            $scope.getUserRole(mass[0]);
        });

        $scope.allRoles = function () {
            console.log(roleService.allRoles());
            return roleService.allRoles();
        };

        $scope.test = function() {
            return "test";
        }


        $scope.editUserRole = function(idRole,form) {
            $scope.editRoleForm = form;
            $scope.idSelectRoleValue = idRole;
            console.log("editUserRole: " + $scope.idSelectRoleValue);

            roleService.editUserRole(
                idRole,
                selectedUserService.getSelectedUserId(),
                function(data) {
                    $scope.role.error.edit = null;
                    $scope.editRoleForm.$dirty = false;
                    $scope.getUserRole(selectedUserService.getSelectedUserId());
                    //$scope.$emit('printUserInfo', []);
                    $location.url("/user-info/"+selectedUserService.getSelectedUserId());
                },
                function(data, status) {
                    $scope.role.error.edit = {status:status,message:data.errors};
                });
        };

        $scope.roleSorter = function(item) {
            var index = 4;
            if(item.authority=="ROLE_SUPER_ADMIN") {
                index = 0;
            } else if(item.authority=="ROLE_ADMIN") {
                index = 1;
            } else if(item.authority=="ROLE_USER") {
                index = 2;
            } else if(item.authority=="ROLE_GUEST") {
                index = 3;
            }
            return index
        }
    });