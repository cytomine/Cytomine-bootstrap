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

var SuggestedAnnotationTermModel = Backbone.Model.extend({

    url: function () {
        return 'api/annotation/term/suggest.json';
    }
});

var SuggestedAnnotationTermCollection = PaginatedCollection.extend({
    model: SuggestedAnnotationTermModel,
    url: function () {
        return "api/project/" + this.project + "/annotation/term/suggest.json?max=" + this.max;
    },
    initialize: function (options) {
        this.initPaginator(options);
        this.project = options.project;
        this.max = options.max;
    },
    comparator: function (annotation) {
        return -Number(annotation.get("rate")); //id or created (chronology?)
    }
});

var SuggestedTermCollection = PaginatedCollection.extend({
    model: SuggestedAnnotationTermModel,
    url: function () {
        return "api/project/" + this.project + "/term/suggest.json";
    },
    initialize: function (options) {
        this.initPaginator(options);
        this.project = options.project;
    },
    comparator: function (annotation) {
        return -Number(annotation.get("rate")); //id or created (chronology?)
    }
});