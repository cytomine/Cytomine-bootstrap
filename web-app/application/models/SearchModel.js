/**
 * Created by hoyoux on 03.11.14.
 */
var SearchEngineFilterModel = Backbone.Model.extend({

    url: function () {
        if (this.get("user") != undefined) {
            if (this.get("id") != undefined) {
                return "api/user/" + this.get("user") + "/searchenginefilter/" + this.get("id") + ".json";
            } else {
                return "api/user/" + this.get("user") + "/searchenginefilter.json";
            }
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
        if (this.user != undefined) {
            return "api/user/" + this.user + "/searchenginefilter.json";
        } else {
            return "api/searchenginefilter.json";
        }
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