var ProjectDashboardAnnotations = Backbone.View.extend({
   tabsAnnotation : null,
   annotationsViews : [], //array of annotation view
   doLayout : function (termTabTpl, termTabContentTpl) {
      var self = this;
      new OntologyModel({id:self.model.get('ontology')}).fetch({
         success : function(model, response) {
            $(self.el).find("input.undefinedAnnotationsCheckbox").click(function(){
               if ($(this).attr("checked") == "checked") {
                  self.updateContentVisibility();
                  self.refreshAnnotations(0);
                  $("#tabsterm-panel-"+self.model.id+"-0").show();
               } else {
                  self.updateContentVisibility();
                  $("#tabsterm-panel-"+self.model.id+"-0").hide();
               }
            });
            $(self.el).find('.tree').dynatree({
               checkbox: true,
               selectMode: 2,
               expand : true,
               onExpand : function() {},
               children: model.toJSON(),
               onSelect: function(select, node) {
                  //if(!self.activeEvent) return;
                  if (node.isSelected()) {
                     self.updateContentVisibility();
                     self.refreshAnnotations(node.data.key);
                     $("#tabsterm-panel-"+self.model.id+"-"+node.data.key).show();
                  }
                  else {
                     self.updateContentVisibility();
                     $("#tabsterm-panel-"+self.model.id+"-"+node.data.key).hide();
                  }
               },
               onDblClick: function(node, event) {
                  //node.toggleSelect();
               },

               // The following options are only required, if we have more than one tree on one page:
               initId: "treeData-annotations-"+self.model.get('ontology'),
               cookieId: "dynatree-Cb-annotations-"+self.model.get('ontology'),
               idPrefix: "dynatree-Cb-annotations-"+self.model.get('ontology')+"-"
            });
            //expand all nodes
            $(self.el).find('.tree').dynatree("getRoot").visit(function(node){
               node.expand(true);
            });
            $("#ontology-annotations-panel-"+self.model.id).panel();

            $(self.el).find("#hideAllAnnotations").click(function(){
               self.selectAnnotations(false);
            });

            $(self.el).find("#showAllAnnotations").click(function(){
               self.selectAnnotations(true);
            });

            $(self.el).find("#refreshAnnotations").click(function(){
               self.refreshSelectedTerms();
            });
         }
      });
      new TermCollection({idOntology:self.model.get('ontology')}).fetch({
         success : function (collection, response) {
            window.app.status.currentTermsCollection = collection;
            $("#listtabannotation").prepend(_.template(termTabContentTpl, { project : self.model.id, id : 0, name : "Undefined"}));
            $("#tabsterm-panel-"+self.model.id+"-0").panel();
            $("#tabsterm-panel-"+self.model.id+"-0").hide();
            collection.each(function(term) {
               //add x term tab
               $("#listtabannotation").prepend(_.template(termTabContentTpl, { project : self.model.id, id : term.get("id"), name : term.get("name")}));
               $("#tabsterm-panel-"+self.model.id+"-"+term.get("id")).panel();
               $("#tabsterm-panel-"+self.model.id+"-"+term.get("id")).hide();
            });
         }});
   },
   render : function() {
      var self = this;
      require(["text!application/templates/dashboard/TermTab.tpl.html", "text!application/templates/dashboard/TermTabContent.tpl.html"], function(termTabTpl, termTabContentTpl) {
         self.doLayout(termTabTpl, termTabContentTpl);
      });
   },
   /**
    * Add the the tab with term info
    * @param id  term id
    * @param name term name
    */
   addTermToTab : function(termTabTpl, termTabContentTpl, data) {
      //$("#ultabsannotation").append(_.template(termTabTpl, data));
      $("#listtabannotation").append(_.template(termTabContentTpl, data));

   },
   selectAnnotations : function (selected) {
      var self = this;
      new TermCollection({idOntology:self.model.get('ontology')}).fetch({
         success : function (collection, response) {
            collection.each(function(term) {
               $(self.el).find('.tree').dynatree("getTree").selectKey(term.get("id"), selected);
            });
         }
      });
   },
   updateContentVisibility : function () {
      var tree = $(this.el).find('.tree').dynatree("getRoot");
      if (!_.isFunction(tree.visit)) return; //tree is not yet loaded
      var nbTermSelected = 0;
      tree.visit(function(node){
         if (!node.isSelected()) return;
         nbTermSelected++;
      });
      nbTermSelected += ($(this.el).find("input.undefinedAnnotationsCheckbox").attr("checked") == "checked") ? 1 : 0;
      if (nbTermSelected > 0){
         $("#listtabannotation").show();
      } else {
         $("#listtabannotation").hide();
      }
   },
   refreshSelectedTerms : function () {
      var self = this;
      var tree = $(this.el).find('.tree').dynatree("getRoot");
      if (!_.isFunction(tree.visit)) return; //tree is not yet loaded
      tree.visit(function(node){
         if (!node.isSelected()) return;
         self.refreshAnnotations(node.data.key);
      });
      if ($(this.el).find("input.undefinedAnnotationsCheckbox").attr("checked") == "checked") {
         self.refreshAnnotations(0);
      }
      self.updateContentVisibility();
   },
   /**
    * Refresh all annotation dor the given term
    * @param term annotation term to be refresh (all = 0)
    */
   refreshAnnotations : function(term) {
      this.printAnnotationThumb(term,"#tabsterm-"+this.model.id+"-"+term);
   },
   clearAnnotations : function (term) {
      var self = this;
      $("#tabsterm-"+self.model.id+"-"+term).empty();
   },
   /**
    * Print annotation for the given term
    * @param term term annotation term to be refresh (all = 0)
    * @param $elem  elem that will keep all annotations
    */
   printAnnotationThumb : function(idTerm,$elem){
      var self = this;

      /*var idTerm = 0;
      if(term==0) {idTerm = undefined;}
      else idTerm = term*/

      new AnnotationCollection({project:self.model.id,term:idTerm}).fetch({
         success : function (collection, response) {

            if (self.annotationsViews[idTerm] != null) { //only refresh
               self.annotationsViews[idTerm].refresh(collection);
               return;
            }
            self.annotationsViews[idTerm] = new AnnotationView({
               page : undefined,
               model : collection,
               el:$($elem),
               container : window.app.view.components.warehouse
            }).render();

            //self.annotationsViews[term].refresh(collection);
            $("#listtabannotation > div").tsort();
         }
      });
   }
});