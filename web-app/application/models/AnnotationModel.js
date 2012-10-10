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

    url:function () {
        var base = 'api/annotation';
        var format = '.json';
        if (this.isNew()) return base + format;
        return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + format;
    }
});


var AnnotationModel = Backbone.Model.extend({

    url:function () {
        var base = 'api/annotation';
        var format = '.json';
        if (this.isNew()) return base + format;
        return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + format;
    }
});

var AnnotationCopyModel = Backbone.Model.extend({
    isNew:function () {
        return true;
    },
    url:function () {

        return 'api/annotation/' + this.id + "/copy.json";
    },
    initialize:function (options) {
        this.id = options.id;
    }
});


var AnnotationCropModel = Backbone.Model.extend({

    url:null,
    initialize:function (options) {
        this.url = options.url;
    }
});

// define our collection
var AnnotationCollection = Backbone.Collection.extend({
    model:AnnotationModel,
    fullSize:-1,
    url:function () {

        var offset = "";
        if (this.offset != undefined) {
            offset = offset + "&offset=" + this.offset;
        }
        if (this.maxResult != undefined) {
            offset = offset + "&max=" + this.maxResult;
        }

        if (this.user != undefined) {
            return "api/user/" + this.user + "/imageinstance/" + this.image + "/annotation.json" + offset;
        } else if (this.term != undefined && this.project != undefined) {
            var users = undefined;
            if (this.users != undefined) {
                users = this.users.join('_');
            }
            var images = undefined;
            if (this.images != undefined) {
                images = this.images.join('_');
            }
            if (this.term < "0") { //annotations without terms (-1), annotations with multiple terms (-2)
                var critera = "";
                if (this.term == -1) {
                    critera = "noTerm=true";
                } else if (this.term == -2) {
                    critera = "multipleTerm=true";
                }
                var url = "api/project/" + this.project + "/annotation.json?" + critera + offset;
                if (users) {
                    url += "&users=" + users;
                }
                if (images) {
                    url += "&images=" + images;
                }
                return url;
            }
            if (this.suggestTerm != undefined) { //ask annotation with suggest term diff than correct term
                return "api/term/" + this.term + "/project/" + this.project + "/annotation.json?suggestTerm=" + this.suggestTerm + "&job=" + this.job + offset;
            }

            if (this.term >= "0" && this.users == undefined && this.images == undefined) return "api/term/" + this.term + "/project/" + this.project + "/annotation.json" + offset;
            if (this.term >= "0" && this.users != undefined && this.images == undefined) {
                return "api/term/" + this.term + "/project/" + this.project + "/annotation.json?users=" + users + offset;
            }
            if (this.term >= "0" && this.users == undefined && this.images != undefined) {
                return "api/term/" + this.term + "/project/" + this.project + "/annotation.json?images=" + images + offset;
            }
            if (this.term >= "0" && this.users != undefined && this.images != undefined) {
                return "api/term/" + this.term + "/project/" + this.project + "/annotation.json?users=" + users + "&images=" + images + offset;
            }

            return "error";


        } else if (this.project != undefined) {
            return "api/project/" + this.project + "/annotation.json";
        } else if (this.image != undefined && this.term != undefined) {
            return "api/term/" + this.term + "/imageinstance/" + this.image + "/annotation.json";
        } else if (this.term != undefined) {
            return "api/term/" + this.term + "/annotation.json";
        } else if (this.image != undefined) {
            return "api/imageinstance/" + this.image + "/annotation.json";
        } else {
            return "api/annotation.json";
        }
    },
    initialize:function (options) {
        this.image = options.image;//one image
        this.user = options.user;
        this.images = options.images;//multiple image
        this.project = options.project;
        this.term = options.term;
        this.users = options.users;
        this.suggestTerm = options.suggestTerm;
        this.job = options.job;
    },
    build:function () {
        var self = this;
        var model = self.at(0);
        var coll = model.get("collection");
        this.remove(self.models);
        this.fullSize = model.get("size");
        _.each(coll, function (item) {
            self.add(item);
        });
    }


    /*,
     comparator : function (annotation) {
     return -annotation.get("id"); //id or created (chronology?)
     }*/
});


AnnotationCollection.comparator = function (annotation) {
    return annotation.get("created");
};

// define our collection
var AnnotationRetrievalModel = Backbone.Model.extend({
    url:function () {
        return "api/annotation/" + this.annotation + "/retrieval.json";
    },
    initialize:function (options) {
        this.annotation = options.annotation;
    }
});

var AnnotationRetrievalCollection = Backbone.Collection.extend({
    model:AnnotationModel,
    initialize:function (options) {
        this.annotation = options.annotation;
    },
    comparator:function (annotation) {
        return -annotation.get("similarity");
    }
});


var AnnotationCommentModel = Backbone.Model.extend({
    initialize:function (options) {
        this.annotation = options.annotation;
    },
    url:function () {
        var base = 'api/annotation/' + this.annotation + '/comment';
        var format = '.json';
        if (this.isNew()) return base + format;
        return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + format;
    }
});

var AnnotationCommentCollection = Backbone.Collection.extend({
    model:AnnotationCommentModel,
    initialize:function (options) {
        this.annotation = options.annotation;
    },
    url:function () {
        return'api/annotation/' + this.annotation + '/comment.json';
    }
});
