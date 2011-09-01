
var CrudGridView = Backbone.View.extend({

   initialize : function(options) {
      this.title = options.title;
      this.colNames = options.colNames;
      this.colModel = options.colModel;
   },

   render : function() {
      var self = this;
      require(["text!application/templates/utils/CrudGridView.tpl.html"],
          function(tpl) {
             self.doLayout(tpl);
          });
   },

   doLayout : function(tpl){
      var html = _.template(tpl, {title : this.title});
      $(this.el).html(html);
      this.renderTable();
      this.initToolbar();
   },

   initToolbar : function() {
      var self = this;
      //Edit button
      var editButton = $(this.el).find(".crud-toolbar").find("input[name=edit]");
      editButton.button();
      editButton.click(function(){
         var grid = $(self.el).find(".grid");
         var gr = grid.jqGrid('getGridParam','selrow');
         if( gr != null ) grid.jqGrid('editGridRow',gr,{height:280,reloadAfterSubmit:false});
         else alert("Please Select Row");
      });
   },

   renderTable : function () {
      var self = this;
      var grid = $(self.el).find(".grid");
      grid.jqGrid({
         datatype: "json",
         url : "api/user.json",
         width: 900,
         height:500,
         colNames:this.colNames,
         colModel:this.colModel,
         onSelectRow: function(id){
         },
         loadComplete: function(data) {
         },
         intializeForm : function (el) {
            alert("intializeForm");
            $(el).html("form");
         },
         sortname: 'id',
         viewrecords: true,
         sortorder: "asc",
         caption:"",
         modal: true,
         editurl : "api/user.json",
         jsonReader: {
            repeatitems : false,
            id: "0"
         }

      });
      /*grid.jqGrid('navGrid','#',{edit:false,add:false,del:false},{},{},{},{multipleSearch:true});
       grid.jqGrid('sortGrid','firstname',false);
       grid.jqGrid('hideCol',"id");*/

     /* this.model.fetch({
         success : function (collection, response) {
            collection.each(function (model) {
               grid.jqGrid('addRowData',model.id,model.toJSON());
            });
         }
      });*/
   }

});

