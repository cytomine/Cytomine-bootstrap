var ImagePropertiesView = Backbone.View.extend({
   tagName : "div",

   initialize: function(options) {
   },
   doLayout: function(tpl) {

      this.dialog = new ConfirmDialogView({
         el:'#dialogs',
         template : _.template(tpl, this.model.toJSON()),
         dialogAttr : {
            dialogID : "#image-properties",
            autoOpen : false,
            buttons: {
               "Close": function() {
                  $(this).dialog("close");
               }
            },
            close :function (event) {
               $(this).dialog("destroy");
            }
         }
      }).render();
      return this;
   },
   render: function() {
      var self = this;
      require(["text!application/templates/image/ImageProperties.tpl.html"], function(tpl) {
         self.doLayout(tpl);
         self.printProperties();
      });
      return this;
   },
   printProperties : function() {
      var self = this;

      require(["text!application/templates/image/ImageProperty.tpl.html"], function(tpl) {
           new ImagePropertyCollection({image : self.model.get("baseImage")}).fetch({
            success : function (collection, response) {
               var target = $("#image-properties-content");
               $("#image-properties-content").empty();
               collection.each(function(model) {
                  var html = _.template(tpl, {key : model.get("key"), value : model.get("value")});
                  target.append(html);
               });
               $("#image-properties").dialog('open');
               //$("ul#image-properties-content>li").tsort();
            }
         });
      });

   }

});