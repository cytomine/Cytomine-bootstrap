var Component = Backbone.View.extend({
   tagName: "div",
   views: {},
   /* Component constructor */
   initialize: function (options) {
      this.divId = options.divId;
      this.el = options.el;
      this.template = options.template;
      this.buttonAttr = options.buttonAttr;
      if (options.activate != undefined) {
         this.activate = options.activate;
      }
      if (options.deactivate != undefined) {
         this.deactivate = options.deactivate;
      }
      if (options.show != undefined) {
         this.show = options.show;
      }
   },
   /**
    *  Render the component into it's DOM element and add it to the menu
    */
   render: function () {
      var self = this;
      $(this.el).append(this.template);

      //Init menu
      $("#project-button").tooltip({
         placement : "bottom"
      });
       $("#ontology-button").tooltip({
         placement : "bottom"
      });
      $("#upload-button").tooltip({
         placement : "bottom"
      });
      $("#explorer-button").tooltip({
         placement : "bottom"
      });
       $("#activity-button").tooltip({
         placement : "bottom"
      });
      return this;
   },
   /**
    * Add a button to the menu which activates the components when clicked

   addToMenu: function () {
      var self = this;
      require(["text!application/templates/MenuButton.tpl.html"], function(tpl) {
         var button = _.template(tpl,{
            id: self.buttonAttr.elButton,
            route: self.buttonAttr.route,
            text: self.buttonAttr.buttonText,
            datacontent : self.buttonAttr.dataContent,
            datatitle : self.buttonAttr.dataTitle
         }, true);
         $(self.buttonAttr.buttonWrapper).append(button);
         //$("#" + self.buttonAttr.elButton).button({
         // icons: {
         // primary: self.buttonAttr.icon
         // }
         // });
         if (self.buttonAttr.click) {
            $("#" + self.buttonAttr.elButton).click(self.buttonAttr.click);
         }
         $("#" + self.buttonAttr.elButton).popover({
            placement : "below"
         });

      });
   }, */
   /**
    * Show the DOM element and disable the button associated to the component
    **/
   activate: function () {
      $("#" + this.divId).show();
      $("#" + this.buttonAttr.elButton).parent().addClass("active");
   },

   /**
    * Hide the DOM element and enable the button associated
    */
   deactivate: function () {
      $("#" + this.divId).hide();
      $("#" + this.buttonAttr.elButton).parent().removeClass("active");
   },

   /**
    * Show a subpage of the component
    * - view : the DOM element which contains the content of the page to activate
    * - scope : the DOM element name which contains pages
    * - name : the name of the page to activate
    */
   show: function (view, scope, name) {
      $(scope).find(".title.active").each(function () {
         $(this).removeClass("active");
      });
      $(scope).find("a[name=" + name + "]").addClass("active");
      for (var i in this.views) {
         var v = this.views[i];
         if (v != view) {
            $(v.el).hide();
         }
      }

      $(view.el).show();
   }
});