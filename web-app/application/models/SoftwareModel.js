var SoftwareModel = Backbone.Model.extend({
    url : function() {
        var base = 'api/software';
        var format = '.json';
        if (this.isNew()) return base + format;
        return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + format;
    }
});

// define our collection
var SoftwareCollection = Backbone.Collection.extend({
    model: SoftwareModel,
    CLASS_NAME: "be.cytomine.processing.Software",
    url : function() {
        if (this.project != null)
            return 'api/project/'+ this.project +'/software.json';
        else
            return 'api/software.json';
    },
    initialize: function (options) {
        if (!options) return;
        this.project = options.project;
    },
    comparator : function(software) {
        return software.get("name");
    }
});

var SoftwareProjectModel = Backbone.Model.extend({
    url : function() {
        var base = 'api/softwareproject';
        var format = '.json';
        if (this.isNew()) return base + format;
        return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + format;
    }
});

var SoftwareProjectCollection = Backbone.Collection.extend({
    model: SoftwareProjectModel,
    url : function() {
        if (this.project != null)
            return 'api/project/'+ this.project +'/softwareproject.json';
        else
            return 'api/softwareproject.json';
    },
    initialize: function (options) {
        if (!options) return;
        this.project = options.project;
    }
});