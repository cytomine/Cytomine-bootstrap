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

var ImageFilterModel = Backbone.Model.extend({
    url: function () {
        var base = 'api/imagefilter';
        var format = '.json';
        if (this.isNew()) {
            return base + format;
        }
        return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + format;
    }
});

var ImageFilterCollection = PaginatedCollection.extend({
    model: ImageFilterModel,
    url: function () {
        return 'api/imagefilter.json';
    },
    initialize: function (options) {
        this.initPaginator(options);
    }
});

var ProjectImageFilterModel = Backbone.Model.extend({
    url: function () {
        var base = 'api/imagefilterproject';
        var format = '.json';
        if (this.isNew()) {
            return base + format;
        }
        return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + format;
    },
    initialize: function (options) {
        this.project = options.project;
    }
});

var ProjectImageFilterCollection = PaginatedCollection.extend({
    model: ImageFilterModel,
    url: function () {
        return "api/project/" + this.project + "/imagefilterproject.json";
    },
    initialize: function (options) {
        this.initPaginator(options);
        this.project = options.project;
    }
});