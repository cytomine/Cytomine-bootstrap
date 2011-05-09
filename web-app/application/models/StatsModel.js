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
            return "api/project/" + this.project + "/term/stat.json";
        } else {
            return "api/stat.json";
        }
	},
    initialize: function (options) {
        this.project = options.project;
    }
});

// define our collection
var StatsCollection = Backbone.Collection.extend({
    model: StatsModel,

    url: function() {
        if (this.project != undefined) {
            return "api/project/" + this.project + "/term/stat.json";
        } else {
            return "api/stat.json";
        }
    },
    initialize: function (options) {
        this.project = options.project;
    }
});