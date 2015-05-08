/**
 * Created by hoyoux on 07.05.15.
 */
angular.module("cytomineUserArea")
    .controller("configCtrl", function ($scope,$location, $routeParams,$http, $resource,configService) {

        $scope.configs = {error:{},success:{}};

        $scope.$on("$routeChangeSuccess", function () {
            $scope.configs.error = {};
            $scope.configs.success = {};
            $scope.getAllConfigs();
        });

        $scope.getAllConfigs = function() {

            configService.getAllConfigs(
                function(data) {
                    $scope.configs.data = data
                    for(var i=0;i<data.length;i++) {
                        $scope.configs[data[i].key] = data[i].value;
                    }
                }
            );
        };

        $scope.getAllConfigs();

        $scope.getConfig = function(key) {
            var configs = $scope.configs.data;
            for(var i=0;i<configs.length;i++) {
                if(configs[i].key == key){
                    return configs[i]
                }
            }
            return null;
        };

        $scope.saveWelcome = function() {
            var config = $scope.getConfig("WELCOME")
            if(config != null) {
                config.value = $scope.configs.WELCOME
                configService.updateConfig("WELCOME", config);
            } else {
                config = {key: "WELCOME", value: $scope.configs["WELCOME"]}
                configService.addConfig(config);
            }
        }
    });