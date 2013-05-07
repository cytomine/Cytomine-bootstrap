var ReviewAnnotationListing = Backbone.View.extend({
    tagName: "div",
    pagination_window: 3,
    nbAnnotation: -1,
    max : 5,
    currentPosition : 0,
    initialize: function (options) {
        this.container = options.container;
        this.page = options.page;
        this.term = options.term;
        if (this.page == undefined) {
            this.page = 0;
        }
    },
    render : function(project,image,user,term) {
        var self = this;
        console.log("render: project="+project + " image=" +image+ " user="+user + " term="+term);
        self.project = project;
        self.image = image;
        self.user = user;
        self.term = term;
        if(self.term=="no") {
            self.term = -1;
        } else if(self.term=="multiple") {
            self.term = -2;
        }

        $("#annotationReviewListing").find("#next1").click(function() {
            self.next();
        });
        $("#annotationReviewListing").find("#next5").click(function() {
            for(var i = 0;i<5;i++) {
                self.next();
            }
        });

        $("#annotationReviewListing").find("#previous1").click(function() {
            self.previous();
        });
        $("#annotationReviewListing").find("#previous5").click(function() {
            for(var i = 0;i<5;i++) {
                self.previous();
            }
        });



        $("#annotationReviewListing").find("#reviewAll").click(function() {
            self.reviewVisible();
        });

        $("#annotationReviewListing").find("#checkAll").click(function() {
            $("#annotationReviewListing").find(".component").find('input').attr("checked",true);
        });



        self.refresh();



        return this;
    },
    refresh: function () {
        var self = this;
        console.log("REFRESH ANNOTATION ||||||||||||||||||||||||||||||||||||||||||||||||");
        console.log(self.project) ;
        console.log([self.image]) ;
        console.log(self.term) ;
        console.log((self.user? [self.user] : null)) ;
        self.model = new AnnotationCollection({project: self.project, images:[self.image], term: self.term, users: (self.user? [self.user] : null),reviewed:false, notReviewedOnly:true});
        self.model.goTo(this.page,{
            success: function (collection, response) {
                $(self.el).find("#AnnotationNotReviewed").find("div").remove();
                self.model = collection;
                self.nbAnnotation = collection.fullSize;
                self.appendThumbs(self.page);
                self.refreshPagaination();
        }});
    },
    refreshPagaination : function() {
        var self = this;
        $(self.el).find("span#pagin").empty();
        $(self.el).find("span#pagin").append("&nbsp;" + (self.currentPosition+1) + " / " + self.model.length +"&nbsp;");
    },

    next : function() {
        var firstDiv = $("#AnnotationNotReviewed").find('div').first();
        this.hide(firstDiv.data('annotation'));
        this.refreshPagaination();
    },
    remove : function(id) {
        var self = this;
        self.hide(id);
        self.model.remove(self.model.get(id));
        self.currentPosition--;
        self.refreshPagaination();
    },
    hide : function(id) {

        var self = this;

        var indiceNext = self.currentPosition+self.max;

        if(indiceNext<=self.model.size()-1) {
            //remove current
            $("#AnnotationNotReviewed").find('div[data-annotation='+id+']').remove();
            $("#AnnotationNotReviewed").find(".popover").remove();

            //add the new last in list

            var next = self.model.at(indiceNext);
            self.addAnnotation(next,true);


             self.currentPosition++;
            self.refreshPagaination();
        } else {
            window.app.view.message("Annotation", "There is no other annotations!", "error");
        }
    },
    previous : function(id) {
        var self = this;

        var indicePrevious = self.currentPosition-1;

        if(indicePrevious>=0) {
            //remove last
            if($("#AnnotationNotReviewed").find('div.thumb-wrap').length==5) {
                $("#AnnotationNotReviewed").find('div.thumb-wrap').last().remove();
                $("#AnnotationNotReviewed").find(".popover").remove();
            }

            //add the new last in list
            var previous = self.model.at(indicePrevious);
            self.addAnnotation(previous, false);

             self.currentPosition--;
            self.refreshPagaination();
        } else {
            window.app.view.message("Annotation", "There is no annotation before!", "error");
        }
    },



    reviewVisible : function() {
        var self = this;
        var visibleAnnotation = $("#AnnotationNotReviewed").find(".thumb-wrap");
        $.each(visibleAnnotation, function(index, value) {
          var idAnnotation = $(value).data('annotation');
            self.container.marskAsReviewed(idAnnotation,null,false);
        });
    },
    appendThumbs: function (page) {
        var self = this;

        var models = self.model.models;

        if(models.length==0) {
            $(self.el).find("#AnnotationNotReviewed").append("<div class='alert alert-block'>No data to display: There is no annotation for this user and this term.</div> ");
        }

        for(var i=0;(i<self.max && i<models.length);i++) {
            self.addAnnotation(models[i],true);
        }
    },


    addAnnotation : function(annotation, after) {
        var self = this;
        var thumb = new AnnotationThumbView({
            model: annotation,
            className: "thumb-wrap",
            term: "all",
            reviewMode : true,
            size : 200
        }).render();

        $(thumb.el).append("<p class='terms text-center'></p>")
        var termNames = []
        _.each(annotation.get("term"), function (it) {
            var term = window.app.status.currentTermsCollection.get(it);
            var termName = term.get('name');
            termNames.push('<span class="label label-warning" style="background-color:'+term.get('color')+';">'+termName+'</span>')
        });

        $(thumb.el).find(".terms").append(termNames.join(", "));

        $(thumb.el).append('<div class="component text-center"></div>');

        $(thumb.el).find(".component").append('<button  style="display:inline;" data-annotation = "'+annotation.id+'" class="btn review" style="min-width:100%;">Accept</button>');
        $(thumb.el).find(".component").append('<input data-annotation="'+annotation.id+'" style="display:inline;" type="checkbox" value="takeMe">');

        if(after) {
            $(self.el).find("#AnnotationNotReviewed").append(thumb.el);
        } else {
            $(self.el).find("#AnnotationNotReviewed").prepend(thumb.el);
        }

        $(self.el).attr("data-reviewed", 'false');


        $(thumb.el).find("button.review").click(function() {
            self.container.marskAsReviewed($(this).data('annotation'),null,false);
        });

    }
});