/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 13/04/11
 * Time: 14:15
 * To change this template use File | Settings | File Templates.
 */
var OntologyTreeView = Backbone.View.extend({
   tagName : "div",

   //template : _.template($('#project-view-tpl').html()),
   initialize: function(options) {
      this.tree = null;
      this.activeEvent = true;
      this.browseImageView = options.browseImageView;
      this.idAnnotation  = null;
   },
   showColors : function() {
      $(this.el).find('.tree').dynatree("getRoot").visit(function(node){

         if (node.children != null) return; //title is ok

         var title = node.data.title
         var color = node.data.color
         var htmlNode = "{{title}} <span style='background-color:{{color}}'>&nbsp;&nbsp;</span> ";
         if(!node.data.isFolder) htmlNode = htmlNode + "(<span id='usercount"+node.data.key+"'>0</span>)";
         var nodeTpl = _.template(htmlNode, {title : title, color : color});


         node.setTitle(nodeTpl);
      });
   },
   render : function () {
      var self = this;
      require(["text!application/templates/explorer/OntologyTreeWrapper.tpl.html"], function(tpl) {
         self.doLayout(tpl);
      });
      return this;
   },
   doLayout: function(tpl) {
      $(this.el).html(_.template(tpl,{}));
      this.tree = $(this.el).find('.tree');
      var self = this;

      $(this.el).find('.tree').dynatree({
         checkbox: true,
         selectMode: 2,
         expand : true,
         onExpand : function() {},
         children: this.model.toJSON(),
         onSelect: function(select, node) {

            if(!self.activeEvent) return;
            if (self.idAnnotation == null) return; // nothing to do

            if (node.isSelected()) {
               self.linkTerm(node.data.key);
            } else if (!node.isSelected()) {
               self.unlinkTerm(node.data.key);
            }
         },
//         onClick: function(node, event) {
//            node.toggleSelect();
//         },
         onRender: function(node, nodeSpan) {
            $(nodeSpan).find("span.dynatree-icon").css({"background-image":"url(css/custom-theme/images/ui-icons_ffffff_256x240.png)"});
         },

         // The following options are only required, if we have more than one tree on one page:
         initId: "treeData"+this.model.id,
         cookieId: "dynatree-Cb"+this.model.id,
         idPrefix: "dynatree-Cb"+this.model.id+"-"
      });

      self.showColors();


      //expand all nodes
      $(this.el).find('.tree').dynatree("getRoot").visit(function(node){
         node.expand(true);
      });
      return this;
   },
   clear : function() {
       var self = this;
      this.activeEvent = false;
       $(this.el).find('.otherUsersTerms').empty();
      $(this.el).find('.tree').dynatree("getRoot").visit(function(node){
         node.select(false);
          $("#usercount"+node.data.key).text("0")
      });
      this.activeEvent = true;
   },
   clearAnnotation : function() {
      this.idAnnotation = null;
   },
   check : function(idTerm) {
      var self = this;
      self.activeEvent = false;
      (this.el).find('.tree').dynatree("getRoot").visit(function(node){
         if (node.data.key == idTerm) {
             node.select(true);
         }
      });
      self.activeEvent = true;
   },
   uncheck : function(idTerm) {
      var self = this;
      self.activeEvent = false;
      (this.el).find('.tree').dynatree("getRoot").visit(function(node){
         if (node.data.key == idTerm) {
             node.select(false);
         }
      });
      self.activeEvent = true;
   },
   refresh: function(idAnnotation) {
      var self = this;
      console.log("refresh term for annotation " + idAnnotation);

      this.idAnnotation = idAnnotation;
      var refreshTree = function(model , response) {
         self.clear();
         self.activeEvent = false;
         //check term for this user
         model.each(function(term) {
             var idTerm = term.get('term');
             var users = term.get('user');
             _.each(users,function(user) {
                 if(user==window.app.status.user.id)
                     self.check(idTerm);
              });
               $("#usercount"+idTerm).text(users.length);
         });
         self.activeEvent = true;
      }

      new AnnotationTermCollection({idAnnotation:idAnnotation}).fetch({success:refreshTree});
   },
   getTermsChecked : function() {
      var terms = [];
      (this.el).find('.tree').dynatree("getRoot").visit(function(node){
         if (node.isSelected()) terms.push(node.data.key);
      });
      return terms;
   },
   linkTerm : function(idTerm) {
      var self = this;
      new AnnotationTermModel({annotation : this.idAnnotation, term : idTerm}).save({annotation : this.idAnnotation, term : idTerm},
          {
             success: function (model, response) {
                window.app.view.message("Annotation Term", response.message, "success");
                self.browseImageView.reloadAnnotation(self.idAnnotation);
                self.browseImageView.refreshAnnotationTabs(idTerm);
             },
             error: function (model, response) {
                var json = $.parseJSON(response.responseText);
                window.app.view.message("Annotation-Term", json.errors, "error");
             }
          }
      );
   },
   unlinkTerm : function(idTerm) {
      var self = this;
      new AnnotationTermModel({annotation : this.idAnnotation, term : idTerm}).destroy(
          {
             success: function (model, response) {
                window.app.view.message("Annotation Term", response.message, "success");
                self.browseImageView.reloadAnnotation(self.idAnnotation);
                self.browseImageView.refreshAnnotationTabs(idTerm);
             },
             error: function (model, response) {
                var json = $.parseJSON(response.responseText);
                window.app.view.message("Annotation-Term", json.errors, "error");
             }
          }

      );
   }

});
