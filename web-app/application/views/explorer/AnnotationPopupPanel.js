/**
 * Created by IntelliJ IDEA.
 * User: stevben
 * Date: 12/06/11
 * Time: 12:33
 * To change this template use File | Settings | File Templates.
 */

var AnnotationPopupPanel = SideBarPanel.extend({
    tagName: "div",

    /**
     * ExplorerTabs constructor
     * @param options
     */
    initialize: function (options) {
        this.browseImageView = options.browseImageView;
        this.idAnnotation = options.idAnnotation;
    },
    /**
     * Grab the layout and call ask for render
     */
    render: function () {
        var self = this;
        require([
            "text!application/templates/explorer/PopupAnnotation.tpl.html"
        ], function (tpl) {
            self.doLayout(tpl);
        });
        return this;
    },
    /**
     * Render the html into the DOM element associated to the view
     * @param tpl
     */
    doLayout: function (tpl) {
        var self = this;

        new AnnotationModel({id: self.idAnnotation}).fetch({
            success: function (annotation, response) {
                self.createPopup(tpl,annotation)
                self.browseImageView.showAnnotationInReviewPanel(annotation);

                self.initTooglePanel("annotation-info-panel",".toggle-content-info");
                self.initTooglePanel("annotation-property-panel",".toggle-content-property");
                self.initTooglePanel("annotation-preview-panel",".toggle-content-preview");
                self.initTooglePanel("annotation-suggested-panel",".toggle-content-suggested");
                self.initTooglePanel("annotation-description-panel",".toggle-content-description");
             }
        });


    },
    initTooglePanel : function(panelName,toogleClass) {
        var self = this;
        var el = $('#annotationDetailPanel' + self.model.get('id'));
        var elContent = el.find("."+panelName);
        var sourceEvent = el.find(toogleClass);
        self.initToggle(el, elContent, sourceEvent, panelName);
    },
    createPopup : function(tpl,annotation) {
         var self = this;
        var user = window.app.models.projectUser.get(annotation.get("user"));
        if (user == undefined) {
            user = window.app.models.projectUserJob.get(annotation.get("user"));
        }
        annotation.set({"username": user.prettyName()});
       console.log("current annotation = " + annotation.id);
       self.browseImageView.currentAnnotation = annotation;

        var terms = [];
        //browse all term and compute the number of user who add this term

        if (annotation.get("reviewed")) {
            _.each(annotation.get("term"), function (idTerm) {
                var termName = window.app.status.currentTermsCollection.get(idTerm).get('name');
                var idOntology = window.app.status.currentProjectModel.get('ontology');
                var tpl = _.template("<a href='#ontology/<%=   idOntology %>/<%=   idTerm %>'><%=   termName %></a> ", {idOntology: idOntology, idTerm: idTerm, termName: termName});
                terms.push(tpl);

            });
        } else {
            _.each(annotation.get("userByTerm"), function (termuser) {
                var idTerm = termuser.term;
                var termName = window.app.status.currentTermsCollection.get(idTerm).get('name');
                var userCount = termuser.user.length;
                var idOntology = window.app.status.currentProjectModel.get('ontology');
                var tpl = _.template("<a href='#ontology/<%=   idOntology %>/<%=   idTerm %>'><%=   termName %></a> (<%=   userCount %>)", {idOntology: idOntology, idTerm: idTerm, termName: termName, userCount: userCount});
                terms.push(tpl);

            });
        }


        annotation.set({"terms": terms.join(", ")});

        if (annotation.get("nbComments") == undefined) {
            annotation.set({"nbComments": 0});
        }

        var content = _.template(tpl, annotation.toJSON());
        var elem = $("#" + self.browseImageView.divId).find("#annotationDetailPanel" + self.browseImageView.model.id);


        elem.html(content);
        elem.show();
        $("#annotationHide" + annotation.id).off('click');
        $("#annotationHide" + annotation.id).on('click', function () {

            self.controls.select.unselectAll();
            var feature = self.getFeature(annotation.id);
            if (feature == undefined || feature == null) {
                return false;
            }
            self.hideFeature(feature);
            var url = "";
            if (!self.reviewMode) {
                url = "tabs-image-" + window.app.status.currentProjectModel.get("id") + "-" + self.browseImageView.model.get("id") + "-";
            }
            else {
                url = "tabs-review-" + window.app.status.currentProjectModel.get("id") + "-" + self.browseImageView.model.get("id") + "-";
            }
            window.app.controllers.browse.navigate(url, false);
            return false;
        });
        if (window.app.status.currentProjectModel.get('privateLayer') || window.app.status.currentProjectModel.get('retrievalDisable')) {
            $("#loadSimilarAnnotation" + annotation.id).html("Retrieval not available");
        } else {
            self.showSimilarAnnotation(annotation);
        }

        //ICI
        //<li style="color:#FFFFFF;"><a href="#tabs-annotationsproperties-<%= idProject %>-<%= idAnnotation %>">Add a property</a></li>



    },

showSimilarAnnotation: function (model) {
     var self = this;
     console.log('showSimilarAnnotation');
     if (window.app.status.currentTermsCollection == undefined || (window.app.status.currentTermsCollection.length > 0 && window.app.status.currentTermsCollection.at(0).id == undefined)) {
         new TermCollection({idOntology: window.app.status.currentProjectModel.get('ontology')}).fetch({
             success: function (terms, response) {
                 window.app.status.currentTermsCollection = terms;
                 self.showSimilarAnnotationResult(model);
             }});
     } else {
         self.showSimilarAnnotationResult(model);
     }
 },
 showSimilarAnnotationResult: function (model) {
     var self = this;
     console.log('showSimilarAnnotationResult');
     new AnnotationRetrievalModel({annotation: model.id}).fetch({
         success: function (collection, response) {

             var termsList = collection.get('term');

             //var termsCollection = new TermCollection(termsList);

             var bestTerm1Object;
             var bestTerm2Object;

             var sum = 0;
             var i = 0;

             _.each(termsList, function (term) {
                 sum = sum + term.rate;
                 if (i == 0) {
                     bestTerm1Object = term;
                 }
                 if (i == 1) {
                     bestTerm2Object = term;
                 }
                 i++;
             });

             var bestTerm1;
             var bestTerm2;
             var bestTerm1Value = 0;
             var bestTerm2Value = 0;
             if (bestTerm1Object != undefined) {
                 bestTerm1 = bestTerm1Object.id;
                 bestTerm1Value = bestTerm1Object.rate;
                 if (bestTerm1Value == 0) {
                     bestTerm1 = undefined;
                 }
             }
             if (bestTerm2Object != undefined) {
                 bestTerm2 = bestTerm2Object.id;
                 bestTerm2Value = bestTerm2Object.rate;
                 if (bestTerm2Value == 0) {
                     bestTerm2 = undefined;
                 }
             }

             bestTerm1Value = (bestTerm1Value / sum) * 100;
             bestTerm2Value = (bestTerm2Value / sum) * 100;

             //Print suggested Term
             self.printSuggestedTerm(model, window.app.status.currentTermsCollection.get(bestTerm1), window.app.status.currentTermsCollection.get(bestTerm2), bestTerm1Value, bestTerm2Value, window.app.status.currentTermsCollection, collection.get('annotation'));
         }, error: function (bad, response) {
             $("#loadSimilarAnnotation" + model.id).replaceWith("Error: cannot reach retrieval");
         }});
 },
 printSuggestedTerm: function (annotation, bestTerm1, bestTerm2, bestTerm1Value, bestTerm2Value, terms, similarAnnotation) {
     var self = this;
     var suggestedTerm = "";
     var suggestedTerm2 = "";
     if (bestTerm1 != undefined) {
         suggestedTerm += "<span id=\"changeBySuggest" + bestTerm1.id + "\" style=\"display : inline\"><u>" + bestTerm1.get('name') + "</u> (" + Math.round(bestTerm1Value) + "%)<span>";
     }
     if (bestTerm2 != undefined) {
         suggestedTerm2 += " or " + "<span id=\"changeBySuggest" + bestTerm2.id + "\" style=\"display : inline\"><u>" + bestTerm2.get('name') + "</u> (" + Math.round(bestTerm2Value) + "%)<span>";
     }

     $("#suggTerm" + annotation.id).empty();
     $("#suggTerm" + annotation.id).append(suggestedTerm);
     $("#suggTerm" + annotation.id).append(suggestedTerm2);
     if (bestTerm1 != undefined) {
         self.createSuggestedTermLink(bestTerm1, annotation);
     }

     if (bestTerm2 != undefined) {
         self.createSuggestedTermLink(bestTerm2, annotation);
     }

     //$("#loadSimilarAnnotation" + annotation.id).replaceWith("<a href=\"#\" id=\"annotationSimilar" + annotation.id + "\"> Search similar annotations</a>");


     $("#loadSimilarAnnotation" + annotation.id).replaceWith('<a id="showRetrieval'+annotation.id+'" href="#myModalRetrieval" role="button" class="btn" data-toggle="modal">See similar annotations</a>');


     $("#showRetrieval" + annotation.id).click(function (event) {
         event.preventDefault();
         console.log("click");
         var bestTerms = [bestTerm1, bestTerm2];
         var bestTermsValue = [bestTerm1Value, bestTerm2Value];
         var panel = new AnnotationRetrievalView({
             model: new AnnotationRetrievalCollection(similarAnnotation),
             projectsPanel: self,
             container: self,
             el: "#annotationRetrievalInfo",
             baseAnnotation: annotation,
             terms: terms,
             bestTerms: bestTerms,
             bestTermsValue: bestTermsValue
         }).render();
         return true;

     });
 },

 createSuggestedTermLink: function (term, annotation) {
     var self = this;
     $("#changeBySuggest" + term.id).click(function () {
         new AnnotationTermModel({term: term.id, userannotation: annotation.id, clear: true
         }).save(null, {success: function (termModel, response) {
                 window.app.view.message("Correct Term", response.message, "");
                 //Refresh tree
                 self.ontologyPanel.ontologyTreeView.refresh(annotation.id);
             }, error: function (model, response) {
                 var json = $.parseJSON(response.responseText);
                 window.app.view.message("Correct term", "error:" + json.errors, "");
             }});
     });
 },





});
