var AnnotationModel = Backbone.Model.extend({
    /*initialize: function(spec) {
     if (!spec || !spec.name || !spec.username) {
     throw "InvalidConstructArgs";
     }
     },

     validate: function(attrs) {
     if (attrs.name) {
     if (!_.isString(attrs.name) || attrs.name.length === 0) {
     return "Name must be a string with a length";
     }
     }
     },*/

    url : function() {
        var base = 'api/annotation';
        var format = '.json';
        if (this.isNew()) return base + format;
        return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + format;
    }
});


var AnnotationModel = Backbone.Model.extend({

    url : function() {
        var base = 'api/annotation';
        var format = '.json';
        if (this.isNew()) return base + format;
        return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + format;
    }
});


var AnnotationCropModel = Backbone.Model.extend({

    url : null,
    initialize: function (options) {
        this.url = options.url;
    }
});

// define our collection
var AnnotationCollection = Backbone.Collection.extend({
    model: AnnotationModel,
    url: function() {
        if (this.user != undefined) {
            return "api/user/" + this.user + "/imageinstance/" + this.image + "/annotation.json";
        } else if (this.term != undefined && this.project !=undefined){

            if (this.term == "0" && this.users==undefined) return "api/project/" + this.project + "/annotation.json?noTerm=true";
            if (this.term == "0" && this.users!=undefined) return "api/project/" + this.project + "/annotation.json?noTerm=true&users="+this.users.join('_');
            if (this.term != "0" && this.users==undefined) return "api/term/" + this.term + "/project/" + this.project + "/annotation.json";
            if (this.term != "0" && this.users!=undefined) {
                console.log("good url");
                return "api/term/" + this.term + "/project/" + this.project + "/annotation.json?users="+this.users.join('_');
            }

            return "error";


        } else if (this.project != undefined) {
            return "api/project/" + this.project + "/annotation.json";
        }  else if (this.image != undefined && this.term != undefined){
            return "api/term/"+this.term+"/imageinstance/" + this.image + "/annotation.json";
        }  else if (this.term != undefined){
            return "api/term/" + this.term + "/annotation.json";
        } else  if(this.image != undefined) {
            return "api/imageinstance/" + this.image + "/annotation.json";
        } else  {
            return "api/annotation.json";
        }
    },
    initialize: function (options) {
        this.image = options.image;
        this.user = options.user;
        this.project = options.project;
        this.term = options.term;
        this.users = options.users;
    },
    comparator : function (annotation) {
        return -annotation.get("id"); //id or created (chronology?)
    }
});


AnnotationCollection.comparator = function(annotation) {
    return annotation.get("created");
};

// define our collection
var AnnotationRetrievalCollection = Backbone.Collection.extend({
    model: AnnotationModel,
    url: function() {
        return "api/annotation/" + this.annotation + "/retrieval.json";
    },
    initialize: function (options) {
        this.annotation = options.annotation;
    },
    comparator : function (annotation) {
        return -Number(annotation.get("similarity")); //id or created (chronology?)
    }
});