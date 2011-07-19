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
                thumb : "<img src='"+image.get('thumb')+"' width=30/>",
                filename: image.get('filename'),
                type : image.get('mime'),
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
        $("#"+self.listproject).jqGrid({
            datatype: "local",
            width: 900,
            height:500,
            colNames:['id','base','thumb','filename','type','annotations','added','see'],
            colModel:[
                {name:'id',index:'id', width:50, sorttype:"int"},
                {name:'base',index:'base', width:50, sorttype:"int"},
                {name:'thumb',index:'thumb', width:50},
                {name:'filename',index:'filename', width:220},
                {name:'type',index:'type', width:50},
                {name:'annotations',index:'annotations', width:75},
                {name:'added',index:'added', width:90,sorttype:"date",formatter:self.dateFormatter},
                {name:'See',index:'See', width:200,sortable:false}

            ],
            onSelectRow: function(id){

                var checked = $("#"+self.listproject).find("#" + id).find(".cbox").attr('checked');
                if(checked) $("#"+self.listproject).find("#" + id).find("td").css("background-color", "CD661D");
                else $("#"+self.listproject).find("#" + id).find("td").css("background-color", "a0dc4f");
            },
            loadComplete: function() {

            },
            //rowNum:10,
            pager: '#'+self.pageproject,
            sortname: 'id',
            viewrecords: true,
            sortorder: "asc",
            caption:"Images from project"
        });
        $("#"+self.listproject).jqGrid('navGrid','#'+self.listproject,{edit:false,add:false,del:false});
        $("#"+self.listproject).jqGrid('hideCol',"id");
        $("#"+self.listproject).jqGrid('hideCol',"base");
    },
    dateFormatter : function (cellvalue, options, rowObject)
    {
        console.log("dateFormatter="+cellvalue);
        // do something here
        var createdDate = new Date();
        createdDate.setTime(cellvalue);

        return createdDate.getFullYear() + "-" + createdDate.getMonth() + "-" + createdDate.getDate()
    }
});
