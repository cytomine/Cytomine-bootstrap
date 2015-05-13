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
 * Date: 7/04/11
 * Time: 10:53
 * To change this template use File | Settings | File Templates.
 */
var SlideModel = Backbone.Model.extend({

    url: function () {
        var base = 'api/sample';
        var format = '.json';
        if (this.isNew()) {
            return base + format;
        }
        return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + format;
    }
});

var ProjectSlideModel = Backbone.Model.extend({
    url: function () {
        if (this.slide == null) {
            return 'api/project/' + this.project + '/sample.json';
        }
        else {
            return 'api/project/' + this.project + '/sample/' + this.slide + '.json';
        }
    },
    initialize: function (options) {
        this.project = options.project;
        this.slide = options.slide;
    }
});

var ProjectSlideCollection = PaginatedCollection.extend({
    model: SlideModel,
    url: function () {
        return 'api/project/' + this.idProject + '/sample.json';
    },
    initialize: function (options) {
        this.initPaginator(options);
        this.idProject = options.idProject;

    }
});


// define our collection
var SlideCollection = PaginatedCollection.extend({
    model: SlideModel,
    CLASS_NAME: "be.cytomine.image.Sample",
    url: function () {
        if (this.page == null) {
            return 'api/sample.json';
        }
        else {
            return 'api/sample.json?page=' + this.page + '&limit=';
        }

        //Request URL:http://localhost:8080/cytomine-web/api/currentuser/image.json?_search=false&nd=1310463777413&rows=10&page=1&sidx=filename&sord=asc
    },
    initialize: function (options) {
        this.initPaginator(options);
        this.page = options.page;
        this.limit = options.limit;
        this.sidx = options.sidx;
        this.sord = options.sord;
    }
});
