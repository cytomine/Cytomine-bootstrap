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
            $("#annotationReviewListing").find(".component").find('input').prop('checked', true);
            self.reviewChecked();
        });

        $("#annotationReviewListing").find("#checkAll").click(function() {
            $("#annotationReviewListing").find(".component").find('input').prop('checked', true);

        });
        $("#annotationReviewListing").find("#unCheckAll").click(function() {
            $("#annotationReviewListing").find(".component").find('input').prop('checked', false);
        });


        $('.check:button').click(function(){
              var checked = !$(this).data('checked');
              $('input:checkbox').prop('checked', checked);
              $(this).val(checked ? 'uncheck all' : 'check all' )
              $(this).data('checked', checked);
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
        $(self.el).find("#AnnotationNotReviewed").find("div").remove();
        $(self.el).find("#AnnotationNotReviewed").append('<div class="alert alert-info"><i class="icon-refresh"/> Loading...</div>');
        self.model = new AnnotationCollection({project: self.project, images:[self.image], term: self.term, user: self.user,reviewed:false, notReviewedOnly:true});
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

        var indiceNext = self.currentPosition+self.max;
        if(indiceNext>self.model.size()-1) {
            $("#annotationReviewListing").find("#next1").attr("disabled","disabled");
            $("#annotationReviewListing").find("#next5").attr("disabled","disabled");
        } else {
            $("#annotationReviewListing").find("#next1").removeAttr("disabled");
            $("#annotationReviewListing").find("#next5").removeAttr("disabled");
        }

        var indicePrevious = self.currentPosition-1;

        if(indicePrevious<0) {
            $("#annotationReviewListing").find("#previous1").attr("disabled","disabled")
            $("#annotationReviewListing").find("#previous5").attr("disabled","disabled")
        } else {
            $("#annotationReviewListing").find("#previous1").removeAttr("disabled");
            $("#annotationReviewListing").find("#previous5").removeAttr("disabled");
        }

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

            //remove current
            $("#AnnotationNotReviewed").find('div[data-annotation='+id+']').remove();
            $("#AnnotationNotReviewed").find(".popover").remove();


        if(indiceNext<=self.model.size()-1) {
            //add the new last in list
            var next = self.model.at(indiceNext);
            self.addAnnotation(next,true);


        } else {
//            window.app.view.message("Annotation", "There is no other annotations!", "error");
        }

        self.currentPosition++;
        self.refreshPagaination();

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
//            window.app.view.message("Annotation", "There is no annotation before!", "error");
        }
    },



    reviewChecked : function() {
        var self = this;
        var allChecked = _.map($("#annotationReviewListing").find(".component").find('input:checked'),function(elem) {
            return $(elem).data("annotation");
        });
        $.each(allChecked, function(index, idAnnotation) {
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
            terms : window.app.status.currentTermsCollection,
            term: "all",
            reviewMode : true,
            size : 200
        }).render();

        $(thumb.el).draggable({
            scroll: true,
            //scrollSpeed : 00,
            revert: true,
            delay: 500,
            opacity: 0.35,
            cursorAt: { top: 85, left: 90},
            start: function (event, ui) {
                $("#AnnotationNotReviewed").find(".popover").remove();
            },
            stop: function (event, ui) {
                $("#annotationReviewListing").find(".component").find('input:checked').parent().parent().css({
                      top: 0,
                      left: 0
                 });
            },
            drag: function( event, ui ) {
                $("#annotationReviewListing").find(".component").find('input:checked').parent().parent().css({
                      top: ui.position.top,
                      left: ui.position.left
                 });
          }
        });




        $(thumb.el).append("<p class='terms text-center'></p>")
        var termNames = []
        _.each(annotation.get("term"), function (it) {
            var term = window.app.status.currentTermsCollection.get(it);
            termNames.push(_.template(self.container.termSpanTemplate,term.toJSON()))
        });

        $(thumb.el).find(".terms").append(termNames.join(", "));

        $(thumb.el).append('<div class="component text-center"></div>');
        $(thumb.el).css("max-width","200px");
        $(thumb.el).find(".component").append('<button  data-toggle="modal" data-target="#browseReviewModal" style="display:inline;" data-annotation = "'+annotation.id+'" class="btn btn-default openBrowseReview" style="min-width:100%;">Open</button>');
        $(thumb.el).find(".component").append('<button  style="display:inline;" data-annotation = "'+annotation.id+'" class="btn btn-default review" style="min-width:100%;">Accept</button>');
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




        console.log("registerReviewPopup");
//        require([
//            "text!application/templates/review/ReviewBrowse.tpl.html"
//        ],
//        function (tpl) {
            var modal = new CustomModal({
                idModal : "browseReviewModal",
                button : $(thumb.el).find("button.openBrowseReview"),
                header :"BrowseAnnotation",
                body :'<div id="browseAnnotationModal"></div>',
                width : Math.round($(window).width()*0.75),
                height : Math.round($(window).height()*0.75),
                callBack : function() {

                    new ImageInstanceModel({id: annotation.get('image')}).fetch({
                        success: function (model, response) {

                           var view = new BrowseImageView({
                               addToTab : false,
                               review: true,
                               initCallback: function () {
                                   console.log("initCallback");
                                   view.show({goToAnnotation : {value: annotation.id}})
                               },
                               el: $("#browseAnnotationModal")
                           });
                           view.model = model;
                           view.render();

                        }
                    });
                }
            });
            modal.addButtons("closeBrowseReview","Close",true,true);
//        });








//
//        $(thumb.el).find("button.openBrowseReview").click(function() {
////            self.container.registerReviewPopup($(this).data('annotation'));
//
//
//
//
//
//
//
//
//
//        });
    }
});