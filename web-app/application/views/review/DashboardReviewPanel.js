var DashboardReviewPanel = Backbone.View.extend({
    width: null,
    software: null,
    project: null,
    parent: null,
    params: [],
    paramsViews: [],
    user : null,
    term: null,
    disableEvent: false,
    initialize: function (options) {
        this.el = "#tabs-review-" + this.model.id;
    },
    render: function (image,user,term) {
        var self = this;
        require([
            "text!application/templates/review/ReviewPanel.tpl.html"
        ],
            function (ReviewPanelTpl) {

                new ImageInstanceModel({id: image}).fetch({
                    success: function (model, response) {
                        $(self.el).empty();

                        var reviewer = null;
                        if(model.get('reviewUser')) {
                            reviewer = window.app.models.projectUser.get(model.get('reviewUser')).prettyName()
                        }

                        $(self.el).append(_.template(ReviewPanelTpl,{idImage: image,imageFilename:model.getVisibleName(window.app.status.currentProjectModel.get('blindMode')),projectName:self.model.get('name'),reviewer:reviewer}));

                        if(reviewer==null) {
                            $("a#startToReview"+image).click(function(evt){
                                 evt.preventDefault();

                                new ImageReviewModel({id: image}).save({}, {
                                    success: function (model, response) {
                                        window.app.view.message("Image", response.message, "success");
                                        window.location.reload();
                                    },
                                    error: function (model, response) {
                                        var json = $.parseJSON(response.responseText);
                                        window.app.view.message("Image", json.errors, "error");
                                     }});
                            });
                        }


                        self.initFilter(user,term);

                        self.initStatsReview(image,user);

                        self.initLastReview();

                        self.initAnnotationListing(image,user,term);

                        self.initTermListing();
                }});





            });
        window.app.view.currentReview = this;
        return this;
    },
    refresh: function(image,user,term) {
        var self = this;
        self.annotationListing.render(self.model.id,image,user,term)
        self.changeSelectedFilter(user,term);
        self.initStatsReview(image,user);
    },
    marskAsReviewed : function(id) {
        var self = this;
        new AnnotationReviewedModel({id: id}).save({}, {
            success: function (model, response) {
//
//                var lastReviewDiv = $("#lastReviewListing").find(".thumb-wrap").last();
//                console.log("lastReviewDiv="+lastReviewDiv);
//                if(lastReviewDiv) {
//                    var lastReviewPos = lastReviewDiv.position();
//                    console.log(lastReviewPos);
//                    $("#AnnotationNotReviewed").find('div[data-annotation='+id+']').animate({left:lastReviewPos.left, top:lastReviewPos.top}, 500);
//                }

                //remove from annotation listing
                self.annotationListing.remove(id);
                console.log(self.lastReviewListing);

                //refresh last review
                self.lastReviewListing.refresh();

                self.reviewStatsListing.refresh();

            },
            error: function (model, response) {
                var json = $.parseJSON(response.responseText);
                window.app.view.message("Annotation", json.errors, "error");
            }});
    },
    initLastReview : function() {
        var self = this;
        self.lastReviewListing = new ReviewLastReviewListing({
            user : window.app.status.user.id,
            project : self.model.id,
            container: self,
            el: $("#lastReviewListing")
        });
        self.lastReviewListing.render();
    },
    initStatsReview : function(image,user) {
        var self = this;
        self.reviewStatsListing = new ReviewStatsListing({
            project : self.model.id,
            image : image,
            user : user,
            container: self,
            el: $("#statsReviewListing")
        });
        self.reviewStatsListing.render();
    },
    initAnnotationListing: function(image,user,term) {
        console.log("initAnnotationListing");
        var self = this;
        self.image = image;
        self.user = user;
        self.term = term;
        self.annotationListing = new ReviewAnnotationListing({
            page: undefined,
            model: null,
            term: undefined,
            container: self,
            el: $("#annotationReviewListing")
        }).render(self.model.id,image,user,term);

        $("#annotationReviewListing").find(".thumb-wrap").draggable( {
              containment: self.el,
              stack: "#annotationReviewListing .thumb-wrap",
              cursor: 'move',
              revert: true
        });
        //drag/start/stop doesn't work :-/
        $("#annotationReviewListing").find(".thumb-wrap").on( "dragstart", function( event, ui ) {alert('345');} );
    },
    initTermListing: function() {
        console.log("initTermListing");
        var self = this;

        var collection = window.app.status.currentTermsCollection;

        self.termListing= new ReviewTermListing({
            model: collection,
            container: self,
            el: $("#termReviewChoice")
        }).render();

        $("#termReviewChoice").find("div").droppable( {
            accept: '#annotationReviewListing .thumb-wrap',
            hoverClass: 'termHovered',
            drop: self.handleNotReviewed
          } );
//        $("#termReviewChoice").find("div").droppable( {
//            accept: '#lastReviewedAnnotation .thumb-wrap',
//            hoverClass: 'termHovered',
//            drop: self.handleReviewed
//          } );


    },
    handleNotReviewed : function(event, ui) {
        var self = this;
        var idTerm = $(this).data("term");
        var idAnnotation = ui.draggable.data('annotation');
        console.log("IdTerm="+idTerm);
        console.log("idAnnotation="+idAnnotation);
       // window.app.view.currentReview.dropStyleAnnotation(event, ui);

       //first: map term with annotation remove other term!!!
         new AnnotationTermModel({term: idTerm, userannotation: idAnnotation, clearForAll: true}).save(null, {success: function (termModel, response) {
                 window.app.view.message("Correct Term", response.message, "");
                //second: review annotation
                window.app.view.currentReview.marskAsReviewed(idAnnotation);

             }, error: function (model, response) {
                 var json = $.parseJSON(response.responseText);
                 window.app.view.message("Correct term", "error:" + json.errors, "");
             }});

    },
    handleReviewed :function(event, ui) {
        var self = this;
        var idTerm = $(this).data("term");
        var idAnnotation = ui.draggable.data('annotation');
        console.log("IdTerm="+idTerm);
        console.log("idAnnotation="+idAnnotation);
         var idAnnotationParent = window.app.view.currentReview.lastReviewListing.model.get(idAnnotation).get('parentIdent');
        console.log(window.app.view.currentReview.lastReviewListing.model);
        console.log("idAnnotationParent="+idAnnotationParent);
        //first: reject annotation

            new AnnotationReviewedModel({id: idAnnotationParent}).destroy({
                success: function (model, response) {
                    console.log("1 ok");
                     //change terme
                    new AnnotationTermModel({term: idTerm, userannotation: idAnnotationParent, clearForAll: true}).save(null, {success: function (termModel, response) {
                        console.log("2 ok");
                            window.app.view.message("Correct Term", response.message, "");
                           //second: review annotation
                           window.app.view.currentReview.marskAsReviewed(idAnnotationParent);

                        }, error: function (model, response) {
                            var json = $.parseJSON(response.responseText);
                            window.app.view.message("Correct term", "error:" + json.errors, "");
                        }});

                },
                error: function (model, response) {
                    var json = $.parseJSON(response.responseText);
                    window.app.view.message("Annotation", json.errors, "error");
                }});







        //second: validate annotation

    },
    dropStyleAnnotation : function(event, ui ) {
         //ui.draggable.addClass( 'hove' );
         ui.draggable.draggable( 'disable' );

         //$(this).droppable( 'disable' );
         ui.draggable.position( { of: $(this), my: 'left top', at: 'left top' } );
         ui.draggable.draggable( 'option', 'revert', false );
    },
    initFilter : function(user, term) {
        var self = this;
        var userFilter = $("#filterReviewListing").find("#filterUser");
        userFilter.empty();
        userFilter.append('<option value="0">All</option>');

        window.app.models.projectUser.each(function(user) {
            userFilter.append('<option value="'+user.id+'">'+user.prettyName()+'</option>');
        });
        window.app.models.projectUserJob.each(function(user) {
            userFilter.append('<option value="'+user.id+'">'+user.prettyName()+'</option>');
        });

        var termFilter = $("#filterReviewListing").find("#filterTerm");
        termFilter.empty();
        termFilter.append('<option value="0">All</option>');
        termFilter.append('<option value="-1">No Term</option>');
        termFilter.append('<option value="-2">Multiple Term</option>');
        window.app.status.currentTermsCollection.each(function(term) {
            termFilter.append('<option value="'+term.id+'">'+term.get('name')+'</option>');
        });

        var changeFilter = function() {
            var userValue = userFilter.val();
            var termValue = termFilter.val();
            if(userValue==0) {
                userValue = null;
            }
            if(termValue==0) {
                termValue = null;
            } else if(termValue==-1) {
                termValue = "no";
             }else if(termValue==-2) {
                termValue = "multiple";
             }
            if(!self.disableEvent) {
               window.location = "#tabs-review-"+self.model.id+"-"+self.image+"-"+ userValue + "-" + termValue
            }
        }

        userFilter.change(changeFilter);
        termFilter.change(changeFilter);


        self.changeSelectedFilter(user,term)

    },
    changeSelectedFilter : function (user, term) {
        var self = this;
        var userFilter = $("#filterReviewListing").find("#filterUser");
        var termFilter = $("#filterReviewListing").find("#filterTerm");

        self.disableEvent = true;
        if(user) {
            userFilter.val(user);
        } else {
            userFilter.val(0);
        }

        if(term) {
            if(term=="no") {
                termFilter.val(-1);
            } else if(term=="multiple") {
                termFilter.val(-2);
            } else termFilter.val(term);
        } else {
            termFilter.val(0);
        }
        self.disableEvent = false;
    }

});