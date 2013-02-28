/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 8/04/11
 * Time: 13:29
 * To change this template use File | Settings | File Templates.
 */
var JobDataModel = Backbone.Model.extend({
    url: function () {
        var base = 'api/jobdata';
        var format = '.json';
        if (this.isNew()) {
            return base + format;
        }
        return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + format;
    }
});

// define our collection
var JobDataCollection = PaginatedCollection.extend({
    model: JobDataModel,

    url: function () {
        if (this.task == null || this.task == undefined) {
            return "api/job/" + this.job + "/jobdata.json";
        }
        else {
            return "api/job/" + this.job + "/jobdata.json?task=" + this.task;
        }
    },
    initialize: function (options) {
        this.initPaginator(options);
        this.job = options.job;
        this.task = options.task;
    }
});


var JobDataStatsModel = Backbone.Model.extend({
    url: function () {
        console.log("task=" + this.task);
        if (this.task == null || this.task == undefined) {
            return "api/job/" + this.id + "/alldata.json";
        }
        else {
            return "api/job/" + this.id + "/alldata.json?task=" + this.task;
        }
    },
    initialize: function (options) {
        this.id = options.id;
        this.task = options.task;
    }
});
