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

var PaginatedCollection = Backbone.Paginator.requestPager.extend({
    fullSize : -1,
    maxVar : Number.MAX_VALUE,
    getNumberOfPages : function() {
       return this.totalPages;
    },
    getMax : function() {
      return this.paginator_ui.perPage;
    },
    initPaginator: function (options) {
        this.paginator_ui.perPage = 0;
        if(!options)  return;
        if(options.max) {
            this.paginator_ui.perPage = options.max;
            this.maxVar = options.max;
            options.max=undefined;
        } else {
            this.paginator_ui.perPage = 0;
        }
    },
    parse: function (response) {
        var self = this;
        // Be sure to change this based on how your results
        // are structured (e.g d.results is Netflix specific)
        if(response.collection!=undefined) {
            var collection = response.collection;
            //Normally this.totalPages would equal response.d.__count
            //but as this particular NetFlix request only returns a
            //total count of items for the search, we divide.

            this.totalPages = response.totalPages;
            this.fullSize = response.size;
            return collection;
        }
        return response;
    },

//    url : function() {
//
//    },
    paginator_core: {
        // the type of the request (GET by default)
        type: 'GET',

        // the type of reply (jsonp by default)
        dataType: 'json',

        url: function() {
            return this.url();
            //alert("Implement url in collection!");
        }
    },
    paginator_ui: {
       // the lowest page index your API allows to be accessed
       firstPage: 0,

       // which page should the paginator start from
       // (also, the actual page the paginator is on)
       currentPage: 0,
//
//       // how many items per page should be shown
       perPage: Number.MAX_VALUE
     },
    server_api: {
          // the query field in the request
//          '$filter': '',

          // number of items to return per request/page
          'max': function() {
              return this.maxVar!=Number.MAX_VALUE ? this.maxVar : 0
          },

          // how many results the request should skip ahead to
          // customize as needed. For the Netflix API, skipping ahead based on
          // page * number of results per page was necessary.
          'offset': function() { return this.currentPage * this.maxVar }

          // field to sort by
          //'orderby': 'name'

          // what format would you like to request results in?
//          'format': 'json',

          // custom parameters
//          '$inlinecount': 'allpages'
        }
});
