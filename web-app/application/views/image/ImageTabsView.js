var ImageTabsView = Backbone.View.extend({
   tagName : "div",
   images : null, //array of images that are printed
   idProject : null,
   imagesProject : null, //collection containing the images contained in the project
   searchPanel : null,
   initialize: function(options) {
      var self = this;
      this.idProject = options.idProject;
      this.container = options.container;
      this.imagesProject = options.model;
      this.listproject = "listimage"+this.idProject;
      this.pageproject = "pagerimage"+this.idProject;
      this.tab = 2;
      this.timeoutHnd = null
   },
   render: function() {
      var self = this;
      console.log("ImageTabsView.render");


      $(self.el).append('<table id=\"listimage'+self.idProject+'\" align=\"center\"></table><div id=\"pagerimage'+this.idProject+'\"></div>');
      self.renderImageListProject();
      self.loadDataImageListProject(self.model);
   },
   refresh : function() {
      console.log("ImageTabsView.refresh");
      this.refreshImageList();
   },
   refreshImageList : function() {
      console.log("ImageTabsView.refreshImageList");
      var self = this;

      //clear grid
      $("#"+self.listproject).jqGrid("clearGridData", true);
      //get imagesproject on server
      self.imagesProject.fetch({
         success : function (collection, response) {

            //print data from project image table
            self.loadDataImageListProject(collection);
         }
      });
   },
   loadDataImageListProject : function(collection) {
      console.log("ImageTabsView.loadDataImageListProject");
      var self = this;
      var data = new Array();
      var i = 0;

      collection.each(function(image) {
         //
         var url = '<a href=\"#tabs-image-'+self.idProject+'-' + image.id + '-\">Click here to see the image</a>'
         data[i] = {
            id: image.id,
            base : image.get('baseImage'),
            thumb : image.get('thumb'),
            filename: image.get('filename'),
            slide : image.get('slide'),
            type : image.get('mime'),
            width : image.get('width'),
            height : image.get('height'),
            magnification : image.get('magnification'),
            resolution : image.get('resolution'),
            annotations : image.get('numberOfAnnotations'),
            added : image.get('created'),
            See : url
         };
         i++;
      });

      for(var j=0;j<data.length;j++) {
         $("#"+self.listproject).jqGrid('addRowData',data[j].id,data[j]);
      }
      $("#"+self.listproject).jqGrid('sortGrid','filename',false);
   },
   renderImageListProject : function() {
      var self = this;
      console.log("ImageTabsView.renderImageListProject:"+"#"+self.listproject +":"+$("#"+self.listproject).length);
      var thumbColName = 'thumb';
      var container = $("#tabs-images-"+self.idProject);
      var gridWidth = container.width() ; //90% of the windows. JQGrid does not like % as width

      var gridHeight = $(window).height() * 0.7; //90% of the windows. JQGrid does not like % as width
      $("#"+self.listproject).jqGrid({
         datatype: "local",
         width: gridWidth,
         height:gridHeight,
         colNames:['id','base','thumb','filename','type','slide','width', 'height', 'magnification', 'resolution','annotations','added','see'],
         colModel:[
            {name:'id',index:'id', width:1, sorttype:"int"},
            {name:'base',index:'base', width:1, sorttype:"int"},
            {name:thumbColName,index:thumbColName, width:50},
            {name:'filename',index:'filename', width:220},
            {name:'type',index:'type', width:50},
            {name:'slide',index:'slide', width:50},
            {name:'width',index:'width', width:50},
            {name:'height',index:'height', width:50},
            {name:'magnification',index:'magnification', width:50},
            {name:'resolution',index:'resolution', width:50},
            {name:'annotations',index:'annotations', width:75},
            {name:'added',index:'added', width:90,sorttype:"date",formatter:self.dateFormatter},
            {name:'See',index:'See', width:400,sortable:false}

         ],
         onSelectRow: function(id){

            var checked = $("#"+self.listproject).find("#" + id).find(".cbox").attr('checked');
            if(checked) $("#"+self.listproject).find("#" + id).find("td").css("background-color", "CD661D");
            else $("#"+self.listproject).find("#" + id).find("td").css("background-color", "a0dc4f");
         },
         loadComplete: function() {
            $("#"+self.listproject).find("tr").each(function(index) {
               if(index!=0) {
                  //0 is not a valid row

                  //replace the text of the thumb by a <img element with its src value
                  var thumbplace = $(this).find('[aria-describedby$="_'+thumbColName+'"]');
                  $(thumbplace).html('<img src="'+$(thumbplace).text()+'" width=30/>');
               }
            });
         },
         //rowNum:10,
         pager: '#'+self.pageproject,
         sortname: 'id',
         viewrecords: true,
         sortorder: "asc",
         rowNum:30,
         rowList:[10,30,50,100],
         caption:"Images from project"
      });
      $("#"+self.listproject).jqGrid('navGrid','#'+self.listproject,{edit:false,add:false,del:false});
      $("#"+self.listproject).jqGrid('hideCol',"id");
      $("#"+self.listproject).jqGrid('hideCol',"base");

   },
   dateFormatter : function (cellvalue, options, rowObject)
   {
      // do something here
      var createdDate = new Date();
      createdDate.setTime(cellvalue);

      return createdDate.getFullYear() + "-" + createdDate.getMonth() + "-" + createdDate.getDate()
   }
});
