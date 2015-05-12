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
 * Created by hoyoux on 03.11.14.
 */
var SearchEngineFilterModel = Backbone.Model.extend({

    url: function () {
        if (this.get("id") != undefined) {
            return "api/searchenginefilter/" + this.get("id") + ".json";
        } else {
            return "api/searchenginefilter.json";
        }
    },
    initialize: function (options) {
        /*if(options != undefined) {
            this.options = options;
            this.id = options.id;
        }*/
    },
    defaults : {
        user: null,
        name: '',
        filters: {
            words: [],
            attributes: [],
            domainTypes: [],
            projects: [],
            order: 'desc',
            sort: 'id',
            op: 'AND'
        }
    },
    validate: function (attrs) {
        if(attrs.filters.words == null ||attrs.filters.words.isEmpty) {
            return 'You cannot save without a searching description.';
        }
    },
    setInvalidCallback: function (callback) {
        this.on("invalid",function(model,error){
            callback(error);
        });
    }
});

// define our collection
var SearchEngineFilterCollection = PaginatedCollection.extend({
    model: SearchEngineFilterModel,
    fullSize : -1,
    url: function () {
        return "api/searchenginefilter.json";
    },
    initialize: function (options) {
        this.initPaginator(options);
        if (options != undefined) {
            this.user = options.user;
        }
    },
    comparator: function (filter) {
        return filter.get("name");
    }
});