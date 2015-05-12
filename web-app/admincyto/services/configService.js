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

/**
 * Created by hoyoux on 07.05.15.
 */
angular.module("cytomineUserArea")
    .constant("configUrl", "/api/config.json")
    .constant("configKeyUrl", "/api/config/key/{key}.json")
    .constant("configIdUrl", "/api/config/{id}.json")
    .factory("configService",function($http,configUrl, configKeyUrl, configIdUrl) {

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
                    .success(function (data) {
                        configs.push(data.config);
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

            updateConfig : function(key, config,callbackSuccess,callbackError) {
                var self = this;
                $http.put(configKeyUrl.replace("{key}", key), config)
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
            },
            deleteConfig : function(config,callbackSuccess,callbackError) {
                var self = this;
                $http.delete(configIdUrl.replace("{id}", config.id), config)
                    .success(function (data) {
                        var index = configs.indexOf(config)
                        if(index > -1) {
                            configs.splice(index,1)
                        }
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