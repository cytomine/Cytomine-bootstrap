/**
 * Created by hoyoux on 07.05.15.
 */
angular.module("cytomineUserArea")
    .constant("configUrl", "/api/config.json")
    .constant("configKeyUrl", "/api/config/{key}.json")
    .factory("configService",function($http,configUrl, configKeyUrl) {

        var configs=[];

        return {

            getAllConfigs : function(callbackSuccess, callbackError) {
                if(configs.length==0) {
                    this.refreshAllConfigs(callbackSuccess,callbackError);
                } else {
                    callbackSuccess(configs);
                }
            },

            refreshAllConfigs : function(callbackSuccess, callbackError) {
                $http.get(configUrl)
                    .success(function (data) {
                        configs = data;
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

            addConfig : function(config,callbackSuccess,callbackError) {
                $http.post(configUrl,config)
            },

            updateConfig : function(key, config) {
                var self = this;
                $http.put(configKeyUrl.replace("{key}", key), config)
            }

        };
    });