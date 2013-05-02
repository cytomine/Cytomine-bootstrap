var AnnotationModel = Backbone.Model.extend({
    url: function () {
        var base = 'api/annotation';
        var format = '.json';
        if (this.isNew()) {
            return base + format;
        }
        var params = "";
        if (this.fill) {
            params = "?fill=true";
        }
        return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + format + params;
    },
    initialize: function (options) {
        this.id = options.id;
        this.fill = options.fill;
    }
});


var AnnotationCorrectionModel = Backbone.Model.extend({
    url: function () {
        var base = 'api/annotationcorrection';
        var format = '.json';
        if (this.isNew()) {
            return base + format;
        }
        return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + format;
    },
    initialize: function (options) {
        this.id = options.id;
    }
});

var AnnotationReviewedModel = Backbone.Model.extend({
    url: function () {
        if (this.fill) {
            return 'api/annotation/' + this.id + "/review/fill.json";
        }
        else {
            return 'api/annotation/' + this.id + "/review.json";
        }
    },
    initialize: function (options) {
        this.id = options.id;
        this.fill = options.fill;
    }
});

var AnnotationCopyModel = Backbone.Model.extend({
    isNew: function () {
        return true;
    },
    url: function () {

        return 'api/annotation/' + this.id + "/copy.json";
    },
    initialize: function (options) {
        this.id = options.id;
    }
});



var AnnotationCollection = PaginatedCollection.extend({
    model: AnnotationModel,
     initialize: function (options) {
         this.initPaginator(options);
         this.image = options.image;//one image
         this.user = options.user;
         this.images = options.images;//multiple image
         this.project = options.project;
         this.term = options.term;
         this.users = options.users;
         this.suggestTerm = options.suggestTerm;
         this.job = options.job;
         this.notReviewedOnly = options.notReviewedOnly;
         this.reviewed = options.reviewed
     },
    comparator: function (annotation) {
        return -annotation.get("id"); //id or created (chronology?)
    },
    url : function () {
             console.log("AnnotationPaginator.url");
            //construct queryString
            var queryString = "";

            if (this.notReviewedOnly) {
                queryString = queryString + "&notreviewed=" + this.notReviewedOnly;
            }

            //construct url
            if (this.user != undefined) {
                return "api/user/" + this.user + "/imageinstance/" + this.image + "/annotation.json?" + queryString;
            } else if (this.term && this.project) {
                console.log("a="+queryString);
                if (this.users) {
                    queryString += "&users=" + this.users.join('_');
                }
                console.log("b="+queryString);

                if (this.images) {
                    queryString += "&images=" + this.images.join('_');
                }
                console.log("c="+this.term);

                if(this.reviewed) {
                    queryString += "&reviewed="+this.reviewed
                }

                if (this.term < "0") { //annotations without terms (-1), annotations with multiple terms (-2)
                    console.log("term < 0 ");
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
                console.log("****************************");
                console.log(this.term >= "0");
                console.log(this.users == undefined);
                console.log(this.images == undefined);
                console.log("****************************");
                return "api/term/" + this.term + "/project/" + this.project + "/annotation.json?" + queryString;
            } else if (this.project != undefined) {

                console.log("a="+queryString);
                  if (this.users) {
                      queryString += "&users=" + this.users.join('_');
                  }
                  console.log("b="+queryString);

                  if (this.images) {
                      queryString += "&images=" + this.images.join('_');
                  }
                  console.log("c="+this.term);

                  if(this.reviewed) {
                      queryString += "&reviewed="+this.reviewed
                  }
                return "api/project/" + this.project + "/annotation.json?" + queryString;
            } else if (this.image != undefined && this.term != undefined) {
                return "api/term/" + this.term + "/imageinstance/" + this.image + "/userannotation.json";
            } else if (this.term != undefined) {
                return "api/term/" + this.term + "/annotation.json";
            } else if (this.image != undefined) {
                return "api/imageinstance/" + this.image + "/userannotation.json";
            } else {
                return "api/annotation.json";
            }
     }
});

// define our collection
var AnnotationReviewedCollection = PaginatedCollection.extend({
    model: AnnotationModel,
    fullSize: -1,
    url: function () {
        var offset = "?";
        if (this.offset != undefined) {
            offset = offset + "offset=" + this.offset;
        }

        console.log(this);

        console.log("*********************"+this.project);
        console.log("*********************"+this.user);

        if(this.project && this.user) {
            return "api/project/" + this.project + "/reviewedannotation.json?user=" + this.user;
        } else {
            return "api/imageinstance/" + this.image + "/reviewedannotation.json" + offset;
        }


    },
    initialize: function (options) {
        this.initPaginator(options);
        this.image = options.image;//one image
        this.user = options.user;//one image
        this.project = options.project;//one image
        this.map = options.map;
    },
    build: function () {
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
    url: function () {
        var task = ""
        if (this.task) {
            task = "&task=" + this.task;
        }


        return "api/imageinstance/" + this.id + "/annotation/review.json?users=" + this.layers.join(",") + task;
    },
    initialize: function (options) {
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
    url: function () {
        return "api/annotation/" + this.annotation + "/retrieval.json";
    },
    initialize: function (options) {
        this.annotation = options.annotation;
    }
});

var AnnotationRetrievalCollection = PaginatedCollection.extend({
    model: AnnotationModel,
    initialize: function (options) {
        this.initPaginator(options);
        this.annotation = options.annotation;
    },
    comparator: function (annotation) {
        return -annotation.get("similarity");
    }
});


var AnnotationCommentModel = Backbone.Model.extend({
    initialize: function (options) {
        this.annotation = options.annotation;
    },
    url: function () {
        var base = 'api/annotation/' + this.annotation + '/comment';
        var format = '.json';
        if (this.isNew()) {
            return base + format;
        }
        return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + format;
    }
});

var AnnotationCommentCollection = PaginatedCollection.extend({
    model: AnnotationCommentModel,
    initialize: function (options) {
        this.initPaginator(options);
        this.annotation = options.annotation;
    },
    url: function () {
        return'api/annotation/' + this.annotation + '/comment.json';
    }
});
