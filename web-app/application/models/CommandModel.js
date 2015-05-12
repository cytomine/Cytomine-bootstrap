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
 * Date: 9/05/11
 * Time: 16:16
 * To change this template use File | Settings | File Templates.
 */
/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 8/04/11
 * Time: 13:29
 * To change this template use File | Settings | File Templates.
 */
var CommandModel = Backbone.Model.extend({

    url: function () {
        var base = 'api/command';
        var format = '.json';
        if (this.isNew()) {
            return base + format;
        }
        return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + format;
    }
});

// define our collection
var CommandHistoryCollection = PaginatedCollection.extend({
    model: CommandModel,
    url: function () {
        var query = "?"
        if(this.user) {
            query = query+ "&user="+this.user
        }
        if(this.fullData) {
            query = query+ "&fullData="+this.fullData
        }
        if (this.project != undefined) {
            return "api/project/" + this.project + "/commandhistory.json"+query;
        } else {
            return "api/commandhistory.json"+query;
        }
    },
    initialize: function (options) {
        this.initPaginator(options);
        if (!options) {
            return;
        }
        if (options.project != undefined) {
            this.project = options.project;
        }
        if (options.user != undefined) {
            this.user = options.user;
        }
        if(options.fullData != undefined) {
            this.fullData = options.fullData;
        }
    }
});

CommandHistoryCollection.comparator = function (command) {
    return command.get("created");
};