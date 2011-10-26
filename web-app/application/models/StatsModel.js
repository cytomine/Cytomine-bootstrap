/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 7/04/11
 * Time: 10:53
 * To change this template use File | Settings | File Templates.
 */
var StatsModel = Backbone.Model.extend({

	url : function() {
        if (this.project != undefined) {
            return "api/project/" + this.project + "/stats/term.json";
        } else if (this.term != undefined) {
            return "api/term/" + this.term + "/project/stat.json";
        } else {
            return "api/stat.json";
        }
	},
    initialize: function (options) {
        this.project = options.project;
        this.term = options.term;
    }
});

// define our collection
var StatsTermCollection = Backbone.Collection.extend({
    model: StatsModel,

    url: function() {
        if (this.project != undefined) {
            return "api/project/" + this.project + "/stats/term.json";
        } else if (this.term != undefined) {
            return "api/term/" + this.term + "/project/stat.json";
        } else {
            return "api/stat.json";
        }
    },
    initialize: function (options) {
        this.project = options.project;
        this.term = options.term;
    }
});

// define our collection
var StatsUserCollection = Backbone.Collection.extend({
    model: StatsModel,

    url: function() {
        if (this.project != undefined) {
            return "api/project/" + this.project + "/stats/user.json";
        }
    },
    initialize: function (options) {
        this.project = options.project;
    }
});

var StatsUserAnnotationCollection = Backbone.Collection.extend({
    model: StatsModel,

    url: function() {
        if (this.project != undefined) {
            return "api/project/" + this.project + "/stats/userannotations.json";
        }
    },
    initialize: function (options) {
        this.project = options.project;
    }
});

var StatsTermSlideCollection = Backbone.Collection.extend({
    model: StatsModel,

    url: function() {
        if (this.project != undefined) {
            return "api/project/" + this.project + "/stats/termslide.json";
        }
    },
    initialize: function (options) {
        this.project = options.project;
    }
});

var StatsUserSlideCollection = Backbone.Collection.extend({
    model: StatsModel,

    url: function() {
        if (this.project != undefined) {
            return "api/project/" + this.project + "/stats/userslide.json";
        }
    },
    initialize: function (options) {
        this.project = options.project;
    }
});