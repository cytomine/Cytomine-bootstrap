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

var RelationTermModel = Backbone.Model.extend({
    url: function () {
        if (this.term != undefined) {
            return  'api/relation/term/' + this.term + '.json';
        }
        else if (this.relation != undefined && this.term1 != undefined && this.term2 != undefined) {
            return 'api/relation/' + this.relation + '/term1/' + this.term1 + "/term2/" + this.term2 + ".json";
        }
        else if (this.relation == undefined && this.term1 == undefined && this.term2 == undefined) {
            return 'api/relation/parent/term.json';
        }
        else if (this.term1 == undefined && this.term2 == undefined) {
            return 'api/relation/' + this.relation + '/term.json';
        }
        else {
            return 'api/relation/parent/term1/' + this.term1 + "/term2/" + this.term2 + ".json";
        }
    },
    initialize: function (options) {
        this.relation = options.relation;
        this.term = options.term;
        this.term1 = options.term1;
        this.term2 = options.term2;
    }
});

var RelationTermCollection = PaginatedCollection.extend({
    model: RelationTermModel,
    url: function () {
        if (this.term != undefined) {
            return  'api/relation/term/' + this.term + '.json';
        }
        else {
            return 'api/relation/' + this.relation + '/term.json';
        }
    },
    initialize: function (options) {
        this.initPaginator(options);
        this.relation = options.relation;
        this.term = options.term;
    }
});