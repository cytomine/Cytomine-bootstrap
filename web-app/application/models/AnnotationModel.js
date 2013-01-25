var AnnotationModel = Backbone.Model.extend({

    url:function () {
        var base = 'api/annotation';
        var format = '.json';
        if (this.isNew()) return base + format;
        var params = "";
        if(this.fill) params = "?fill=true";
        return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + format+params;
    },
    initialize:function (options) {
        this.id = options.id;
        this.fill = options.fill;
    }
});


var AnnotationCorrectionModel = Backbone.Model.extend({
    url:function () {
        var base = 'api/annotationcorrection';
        var format = '.json';
        if (this.isNew()) return base + format;
        return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + format;
    },
    initialize:function (options) {
        this.id = options.id;
    }
});

var AnnotationReviewedModel = Backbone.Model.extend({
    url:function () {
        if(this.fill)
            return 'api/annotation/' + this.id + "/review/fill.json";
        else return 'api/annotation/' + this.id + "/review.json";
    },
    initialize:function (options) {
        this.id = options.id;
        this.fill = options.fill;
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

// define our collection
var AnnotationCollection = Backbone.Collection.extend({
    model:AnnotationModel,
    fullSize:-1,
    url:function () {

        //construct queryString
        var queryString = "";
        if (this.offset) {
            queryString = queryString + "&offset=" + this.offset;
        }
        if (this.maxResult) {
            queryString = queryString + "&max=" + this.maxResult;
        }
        if (this.notReviewedOnly) {
            queryString = queryString + "&notreviewed=" + this.notReviewedOnly;
        }

        //construct url
        if (this.user != undefined) {
            return "api/user/" + this.user + "/imageinstance/" + this.image + "/annotation.json?" + queryString;
        } else if (this.term != undefined && this.project != undefined) {
            if (this.users) {
                queryString += "&users=" + this.users.join('_');
            }
            if (this.images) {
                queryString += "&images=" + this.images.join('_');
            }
            if (this.term < "0") { //annotations without terms (-1), annotations with multiple terms (-2)
                if (this.term == -1) {
                    queryString += "&noTerm=true";
                } else if (this.term == -2) {
                    queryString += "&multipleTerm=true";
                }
                return "api/project/" + this.project + "/annotation.json?" + queryString;
            }
            if (this.suggestTerm != undefined) { //ask annotation with suggest term diff than correct term
                return "api/term/" + this.term + "/project/" + this.project + "/annotation.json?suggestTerm=" + this.suggestTerm + "&job=" + this.job + queryString;
            }

            if (this.term >= "0" && this.users == undefined && this.images == undefined) return "api/term/" + this.term + "/project/" + this.project + "/annotation.json" + queryString;
            if (this.term >= "0" && this.users != undefined && this.images == undefined) {
                return "api/term/" + this.term + "/project/" + this.project + "/annotation.json?users=" + users + queryString;
            }
            if (this.term >= "0" && this.users == undefined && this.images != undefined) {
                return "api/term/" + this.term + "/project/" + this.project + "/annotation.json?images=" + images + queryString;
            }
            if (this.term >= "0" && this.users != undefined && this.images != undefined) {
                return "api/term/" + this.term + "/project/" + this.project + "/annotation.json?users=" + users + "&images=" + images + queryString;
            }
        } else if (this.project != undefined) {
            return "api/project/" + this.project + "/annotation.json";
        } else if (this.image != undefined && this.term != undefined) {
            return "api/term/" + this.term + "/imageinstance/" + this.image + "/userannotation.json";
        } else if (this.term != undefined) {
            return "api/term/" + this.term + "/annotation.json";
        } else if (this.image != undefined) {
            return "api/imageinstance/" + this.image + "/userannotation.json";
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
        this.notReviewedOnly = options.notReviewedOnly;
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
    },
     comparator : function (annotation) {
     return -annotation.get("id"); //id or created (chronology?)
     }
});


// define our collection
var AnnotationReviewedCollection = Backbone.Collection.extend({
    model:AnnotationModel,
    fullSize:-1,
    url:function () {
        var offset = "?";
        if (this.offset != undefined) {
            offset = offset + "offset=" + this.offset;
        }
        return "api/imageinstance/" + this.image + "/reviewedannotation.json" + offset;
    },
    initialize:function (options) {
        this.image = options.image;//one image
        this.map = options.map;
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
});


// define our collection
var AnnotationImageReviewedModel = Backbone.Model.extend({
    url:function () {
        var task = ""
        if(this.task) task = "&task="+this.task;


        return "api/imageinstance/" + this.id + "/annotation/review.json?users=" +this.layers.join(",")+task;
    },
    initialize:function (options) {
        this.id = options.image;//one image
        this.layers = options.layers;
        this.task = options.task;
    }
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
