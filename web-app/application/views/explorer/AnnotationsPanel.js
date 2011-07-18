/**
 * Created by IntelliJ IDEA.
 * User: stevben
 * Date: 21/06/11
 * Time: 22:19
 * To change this template use File | Settings | File Templates.
 */

var AnnotationsPanel = Backbone.View.extend({
       tagName : "div",

       /**
        * ExplorerTabs constructor
        * @param options
        */
       initialize: function(options) {
          this.refreshAnnotationsTabsFunc = [];
       },
       /**
        * Grab the layout and call ask for render
        */
       render : function() {
          var self = this;
          require([
             "text!application/templates/explorer/AnnotationsPanel.tpl.html"
          ], function(tpl) {
             self.doLayout(tpl);
          });

          return this;
       },
       createTabs : function(idOntology) {
          var self = this;
          require(["text!application/templates/explorer/TermTab.tpl.html", "text!application/templates/explorer/TermTabContent.tpl.html"], function(termTabTpl, termTabContentTpl) {

             new TermCollection({idOntology:idOntology}).fetch({
                    success : function (collection, response) {

                       //add "All annotation from all term" tab
                       self.addTermToTab(termTabTpl, termTabContentTpl, { id : "all", image : self.model.id, name : "All"});
                       self.refreshAnnotationsTabsFunc.push({
                              index : 0,
                              idTerm : "all",
                              refresh : function() {self.refreshAnnotations(undefined,$("#tabsterm-"+self.model.id+"-all"))}
                           });
                       var i = 1;
                       collection.each(function(term) {
                          //add x term tab
                          self.addTermToTab(termTabTpl, termTabContentTpl, { id : term.get("id"), image : self.model.id,name : term.get("name")});
                          self.refreshAnnotationsTabsFunc.push({
                                 index : i,
                                 idTerm : term.get("id"),
                                 refresh : function() {self.refreshAnnotations(term.get("id"),$("#tabsterm-"+self.model.id+"-"+term.get("id")))}
                              });
                          i++;
                       });
                       $("#annotationsPanel"+self.model.id).find(".tabsAnnotation").tabs({
                              add : function (event, ui) {

                              },
                              select: function(event, ui) {
                                 var obj = _.detect(self.refreshAnnotationsTabsFunc, function (object){
                                    return (object.index == ui.index);
                                 });
                                 obj.refresh.call();
                              }
                           });
                       $("#annotationsPanel"+self.model.id).find( ".tabs-bottom .ui-tabs-nav, .tabs-bottom .ui-tabs-nav > *" )
                           .removeClass( "ui-corner-all ui-corner-top" )
                           .addClass( "ui-corner-bottom" );
                       self.initAnnotations();
                    }});


          });
       },
       refreshAnnotationTabs : function (idTerm) {
          var self = this;
          if (idTerm != undefined) {
             var obj = _.detect(self.refreshAnnotationsTabsFunc, function (object){
                return (object.idTerm == idTerm);
             });
             obj.refresh.call();
          } else { //refresh the "all tabs"
             self.refreshAnnotationsTabsFunc[0].refresh.call();
          }
       },
        refreshAnnotations : function(idTerm, el) {
          new AnnotationCollection({image:this.model.id,term:idTerm}).fetch({
                 success : function (collection, response) {
                    el.empty();
                    var view = new AnnotationView({
                           page : undefined,
                           model : collection,
                           el:el
                        }).render();
                 }
              });
       },
       /**
        * Add the the tab with term info
        * @param id  term id
        * @param name term name
        */
       addTermToTab : function(termTabTpl, termTabContentTpl, data) {
          $("#annotationsPanel"+this.model.id).find(".ultabsannotation").append(_.template(termTabTpl, data));
          $("#annotationsPanel"+this.model.id).find(".listtabannotation").append(_.template(termTabContentTpl, data));
       },
       initAnnotations : function () {
          var self = this;


          //init panel for all annotation (with or without term
          new AnnotationCollection({image:self.model.id}).fetch({
                 success : function (collection, response) {

                    self.refreshAnnotations(undefined, $("#tabsterm-"+self.model.id+"-all"));
                    /*self.annotationsViews[0] = view;*/

                 }
              });
       },
       /**
        * Render the html into the DOM element associated to the view
        * @param tpl
        */
       doLayout: function(tpl) {
          var self = this;
          var el = $('#annotationsPanel' + self.model.get('id'));
          el.html(_.template(tpl, {id : self.model.get('id')}));
          new ProjectModel({id : window.app.status.currentProject}).fetch({
                 success : function (model, response) {
                    self.createTabs(model.get("ontology"));
                 }
              });
          el.css("padding", "1px");
          el.css("margin", "0px");
          el.css("width", "20px");
          el.css("height", "20px");
          el.css("bottom", "0px");
          el.find("div.panel_button").click(function(){
             el.find("div.panel_button").toggle();
             el.css("bottom", "0px");
             el.animate({
                    height: "250px"
                 }, "fast").animate({
                    width: "100%"
                 });
             setTimeout(function(){el.find("div.panel_content").fadeIn();}, 1000);
             return false;
          });

          el.find("div#refresh_annotations_button").click(function(){
             var tabSelected = el.find(".tabsAnnotation").tabs('option', 'selected');
             var obj = _.detect(self.refreshAnnotationsTabsFunc, function (object){
                return (object.index == tabSelected);
             });
             obj.refresh.call();
          });
          el.find("div#hide_button").click(function(){
             el.animate({
                    height: "20px"
                 }, "fast")
                 .animate({
                    width : "20px"
                 }, "fast");

             setTimeout(function(){el.find("div.panel_content").hide();el.css("bottom", "0px");}, 1000);

             return false;

          });
          el.find("div.previous_button").click(function(){
             alert("prev" + self.currentAnnotation);
          });
          el.find("div.next_button").click(function(){
             alert("next"+ self.currentAnnotation);
          });
          return this;
       }
    });
