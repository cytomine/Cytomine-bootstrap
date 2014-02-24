/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 8/04/11
 * Time: 13:29
 * To change this template use File | Settings | File Templates.
 */
var JobModel = Backbone.Model.extend({
    url: function () {
        var base = 'api/job';
        var format = '.json';
        if (this.isNew()) {
            return base + format;
        }
        return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + format;
    },
    initialize: function (options) {
        this.project = options.project;
        this.software = options.software;
        this.light = options.light;
        this.max = options.max;
    },
    executeUrl : function() {
        return "/api/job/" + this.id + "/execute.json";
    },
    previewUrl : function() {
        return "/api/job/" + this.id + "/preview.json";
    },
    previewRoiUrl : function() {
        return "/api/job/" + this.id + "/preview_roi.json";
    },
    //to do : put theses methods into JOB MARSHALLER
    isNotLaunch: function () {
        return (this.get('status') == 0)
    },
    isInQueue: function () {
        return (this.get('status') == 1)
    },
    isRunning: function () {
        return (this.get('status') == 2)
    },
    isSuccess: function () {
        return (this.get('status') == 3)
    },
    isFailed: function () {
        return (this.get('status') == 4)
    },
    isIndeterminate: function () {
        return (this.get('status') == 5)
    },
    isWait: function () {
        return (this.get('status') == 6)
    },
    isPreviewed: function () {
        return (this.get('status') == 7)
    }
});

// define our collection
var JobCollection = PaginatedCollection.extend({
    model: JobModel,

    url: function () {
        var query_params = [];
        if (this.project) {
            query_params.push("project=" + this.project);
        }
        if (this.software) {
            query_params.push("software=" + this.software);
        }
        if (this.light) {
            query_params.push("light=" + this.light);
        }
        return "api/job.json?" + query_params.join("&");
    },
    initialize: function (options) {
        this.initPaginator(options);
        this.project = options.project;
        this.software = options.software;
        this.light = options.light;
    },
    comparator: function (job) {
        return -job.get("id");
    }
});


var JobTemplateModel = Backbone.Model.extend({
    url: function () {
        var base = 'api/jobtemplate';
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


// define our collection
var JobTemplateCollection = PaginatedCollection.extend({
    model: JobTemplateModel,
    url: function () {
        return "api/project/"+this.project + "/jobtemplate.json"
    },
    initialize: function (options) {
        this.initPaginator(options);
        this.project = options.project;
    },
    comparator: function (job) {
        return -job.get("id");
    }
});


var JobTemplateAnnotationModel = Backbone.Model.extend({
    url: function () {
        var base = 'api/jobtemplateannotation';
        var format = '.json';
        if (this.isNew()) {
            return base + format;
        }
        return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + format;
    }
});


// define our collection
var JobTemplateAnnotationCollection = PaginatedCollection.extend({
    model: JobTemplateModel,
    url: function () {
        var query = "";
        if(this.annotation) {
            query = query + "&annotation="+this.annotation;
        }
        if(this.jobtemplate) {
            query = query + "&jobtemplate="+this.jobtemplate;
        }
        return "api/jobtemplateannotation.json?"+query
    },
    initialize: function (options) {
        this.initPaginator(options);
        this.annotation = options.annotation;
        this.jobtemplate = options.jobtemplate;
    },
    comparator: function (job) {
        return -job.get("id");
    }
});


