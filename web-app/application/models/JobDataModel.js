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
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 8/04/11
 * Time: 13:29
 * To change this template use File | Settings | File Templates.
 */
var JobDataModel = Backbone.Model.extend({
    url: function () {
        var base = 'api/jobdata';
        var format = '.json';
        if (this.isNew()) {
            return base + format;
        }
        return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + format;
    }
});




// define our collection
var JobDataCollection = PaginatedCollection.extend({
    model: JobDataModel,

    url: function () {
        if (this.task == null || this.task == undefined) {
            return "api/job/" + this.job + "/jobdata.json";
        }
        else {
            return "api/job/" + this.job + "/jobdata.json?task=" + this.task;
        }
    },
    initialize: function (options) {
        this.initPaginator(options);
        this.job = options.job;
        this.task = options.task;
    }
});


var JobDataStatsModel = Backbone.Model.extend({
    url: function () {
        console.log("task=" + this.task);
        if (this.task == null || this.task == undefined) {
            return "api/job/" + this.id + "/alldata.json";
        }
        else {
            return "api/job/" + this.id + "/alldata.json?task=" + this.task;
        }
    },
    initialize: function (options) {
        this.id = options.id;
        this.task = options.task;
    }
});



var JobDataClearModel = Backbone.Model.extend({
    url: function () {
        console.log("task=" + this.task);
        if (this.task == null || this.task == undefined) {
            return "api/project/" + this.id + "/job/purge.json";
        }
        else {
            return "api/project/" + this.id + "/job/purge.json?task=" + this.task;
        }
    },
    initialize: function (options) {
        this.id = options.id;
        this.task = options.task;
    }
});