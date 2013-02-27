var PaginatedCollection = Backbone.Paginator.requestPager.extend({
    fullSize : -1,
    getNumberOfPages : function() {
       return this.totalPages;
    },
    getMax : function() {
      return this.paginator_ui.perPage;
    },
    initPaginator: function (options) {
        if(options.max) {
            this.paginator_ui.perPage = options.max
        }
    },
    parse: function (response) {
        // Be sure to change this based on how your results
        // are structured (e.g d.results is Netflix specific)
        if(response.collection) {
            var collection = response.collection;
            //Normally this.totalPages would equal response.d.__count
            //but as this particular NetFlix request only returns a
            //total count of items for the search, we divide.

            console.log(response);

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
          'max': function() { return this.perPage!=Number.MAX_VALUE ? this.perPage : 0 },

          // how many results the request should skip ahead to
          // customize as needed. For the Netflix API, skipping ahead based on
          // page * number of results per page was necessary.
          'offset': function() { return this.currentPage * this.perPage }

          // field to sort by
//          'orderby': 'name'

          // what format would you like to request results in?
//          'format': 'json',

          // custom parameters
//          '$inlinecount': 'allpages'
        }
});
