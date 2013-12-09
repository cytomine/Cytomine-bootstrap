/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 8/04/11
 * Time: 13:29
 * To change this template use File | Settings | File Templates.
 */
var ProjectModel = Backbone.Model.extend({

    url: function () {
        var base = 'api/project';
        var format = '.json';
        if (this.task != null && this.task != undefined) {
            format = format+"?task="+this.task
        }
        if (this.isNew()) {
            return base + format;
        }
        return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + format;
    },
    initialize: function (options) {
        this.id = options.id;
        this.task = options.task;
    },
    isReadOnly : function(admins) {
        var isAdmin = admins.get(window.app.status.user.id) != undefined;
        var isRO = this.get('isReadOnly');
        return !isAdmin && isRO;
    }
});


var ProjectUserModel = Backbone.Model.extend({
    url: function () {
        if (this.user == undefined) {
            return "api/project/" + this.project + "/user.json";
        } else {
            return "api/project/" + this.project + "/user/" + this.user + ".json";
        }
    },
    initialize: function (options) {
        this.project = options.project;
        this.user = options.user;
    }
});


var OntologyProjectModel = PaginatedCollection.extend({
    model: ProjectModel,
    url: function () {
        return "api/ontology/" + this.ontology + "/project.json";
    },
    initialize: function (options) {
        this.initPaginator(options);
        this.ontology = options.ontology;
    }
});


// define our collection
var ProjectCollection = PaginatedCollection.extend({
    model: ProjectModel,
    fullSize : -1,
    url: function () {
        if (this.user != undefined) {
            return "api/user/" + this.user + "/project.json";
        } else if (this.ontology != undefined) {
            return "api/ontology/" + this.ontology + "/project.json";
        } else {
            return "api/project.json";
        }
    },
    initialize: function (options) {
        this.initPaginator(options);
        if (options != undefined) {
            this.user = options.user;
            this.ontology = options.ontology;
        }
    },
    comparator: function (project) {
        return project.get("name");
    }
});
