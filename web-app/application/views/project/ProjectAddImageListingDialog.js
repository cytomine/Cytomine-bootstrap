var ProjectAddImageListingDialog = Backbone.View.extend({
    imagesProject : null, //collection containing the images contained in the project
    searchPanel : null,
    initialize: function(options) {
        var self = this;
        this.container = options.container;
        this.projectPanel = options.projectPanel;
        this.imagesProject = options.imagesProject;
        this.abstractImageProject = new Array();
        this.el = "#tabsProjectaddimagedialog"+self.model.id+"-2" ;
        this.listmanageproject = "listmanageproject"+this.model.id;
        this.pagemanageproject = "pagemanageproject"+this.model.id;
        this.listmanageall = "listmanageall"+this.model.id;
        this.pagemanageall = "pagemanageall"+this.model.id;
        this.addImageButton = "addimageprojectbutton"+this.model.id;
        this.delImageButton = "delimageprojectbutton"+this.model.id;
        this.tab = 2;
        this.timeoutHnd = null

    },
    render : function() {
        var self = this;
        //self.fillAbstractImageProjectCollection(self.imagesProject);
        require([
            "text!application/templates/project/ProjectAddImageListingDialog.tpl.html"
        ],
               function(tpl) {
                   self.doLayout(tpl);
               });
        return this;
    },
    refresh : function() {
        this.refreshImageList();
    },

    doLayout : function(tpl) {
        var self = this;

        var json = self.model.toJSON();
        json.tab = 2;
        var dialog = _.template(tpl, json);
        $(self.el).append(dialog);

        /*
        self.searchPanel = new ProjectAddImageSearchPanel({
            model : self.model,
            images : self.images,
            el:$("#tdsearchpanel"+self.model.id),
            container : self,
            tab : 2
        }).render(); */

        //print listing
        this.renderImageList();

        return this;
    },
    renderImageList: function() {
        var self = this;
        self.renderImageListing();
    },
    renderImageListing : function() {
        var self = this;

        $("#"+self.addImageButton).button({
            icons : {primary: "ui-icon-circle-arrow-w" } ,
            text: false
        });
        $("#"+self.delImageButton).button({
            icons : {primary: "ui-icon-circle-arrow-e"} ,
            text: false
        });

        $("#infoProjectPanel"+self.model.id).panel({
            collapseSpeed:100
        });

        $('#'+self.addImageButton).click(function() {
            self.addImageProjectFromTable();
        });

        $('#'+self.delImageButton).click(function() {
            self.deleteImageProjectFromTable();
        });

        //search panel

        console.log($("#searchImagetPanelup"+self.model.id+"-"+self.tab).html());

        $("#searchImagetPanelup"+self.model.id+"-"+self.tab).panel({
            collapseSpeed:100
        });
         $("#filenamesearchtextboxup"+self.model.id+"-"+self.tab).val("");


        $("#imagesallbutton"+self.model.id+"-"+self.tab).button({
            text: true
        });

            $("#filenamesearchtextboxup"+self.model.id+"-"+self.tab).keyup(function () {
                 self.doSearch();
            }).keyup();

        $("#imagesallbutton"+self.model.id+"-"+self.tab).click(function() {
            $("#filenamesearchtextboxup"+self.model.id+"-"+self.tab).val("");
            $("#datestartsearchup"+self.model.id+"-"+self.tab ).val("");
            $("#dateendsearchup"+self.model.id+"-"+self.tab).val("");
            self.doSearch();
        });

        $( "#datestartsearchup"+self.model.id+"-"+self.tab ).datepicker({
            onSelect: function(dateText, inst) { self.doSearch(); }
        });
        $( "#dateendsearchup"+self.model.id+"-"+self.tab ).datepicker({
            onSelect: function(dateText, inst) { self.doSearch(); }
        });

        self.renderImageListProject();
        self.renderImageListAll();

    },
    doSearch : function(){
        var self = this;
        console.log("doSearch: "+$.data(this, 'timer'));

        if($.data(this, 'timer')!=null) {
            console.log("clearTimeout:"+$.data(this, 'timer'));
           clearTimeout($.data(this, 'timer'));
        }
        console.log("setTimeout");
          var wait = setTimeout(function(){
                 self.searchImages()}
              ,500);
          $(this).data('timer', wait);
        //setTimeout("alert('foo');",5000);
        //this.searchImages();

    },

    /**
     * Look for search panel info and print result on grid
     */
    searchImages : function() {
        var self = this;
        console.log("searchImages");
        //var images = self.searchPanel.search(self.images);
        var searchText = $("#filenamesearchtextboxup"+self.model.id+"-"+self.tab).val();
        var dateStart =  $("#datestartsearchup"+self.model.id+"-"+self.tab).datepicker("getDate");
        var dateEnd =  $("#dateendsearchup"+self.model.id+"-"+self.tab).datepicker("getDate");

        var dateTimestampStart="";
        if(dateStart!=null) dateTimestampStart=dateStart.getTime();
        var dateTimestampEnd="";
        if(dateEnd!=null) dateTimestampEnd=dateEnd.getTime();

        console.log("searchText="+searchText);
        $("#"+self.listmanageall).jqGrid('setGridParam',{url:"api/currentuser/image.json?filename="+searchText+"&createdstart="+dateTimestampStart+"&createdstop="+dateTimestampEnd,page:1}).trigger("reloadGrid");


    },
    refreshImageList : function() {
        var self = this;
        console.log("refreshImageListProject");
        //clear grid
        $("#"+self.listmanageproject).jqGrid("clearGridData", true);
        //get imagesproject on server
        self.imagesProject.fetch({
            success : function (collection, response) {

                //create abstractImagepProject collection
                self.fillAbstractImageProjectCollection(collection);

                //print data from project image table
                self.loadDataImageListProject(collection);

                //print data from all image table
                //self.searchImages();
                console.log("reloadgrid");
                $("#"+self.listmanageall).trigger("reloadGrid");
            }
        });
    },
    /**
     * Fill collection of abstract image id from image project
     * @param images Project Images
     */
    fillAbstractImageProjectCollection : function(images) {
        var self = this;
        console.log(self.abstractImageProject);
        self.abstractImageProject = [];
        self.imagesProject.each(function(image) {
            self.abstractImageProject.push(image.get('baseImage'));
        });
    },
    loadDataImageListProject : function(collection) {
        console.log("loadDataImageListProject");
        var self = this;
        var data = new Array();
        var i = 0;

        collection.each(function(image) {
            //console.log(image.get('created'));
            var createdDate = new Date();
            createdDate.setTime(image.get('created'));
            //console.log("id="+image.id);
            data[i] = {
                id: image.id,
                base : image.get('baseImage'),
                thumb : "<img src='"+image.get('thumb')+"' width=30/>",
                filename: image.get('filename'),
                type : image.get('mime'),
                annotations : image.get('numberOfAnnotations'),
                added : createdDate.getFullYear() + "-" + createdDate.getMonth() + "-" + createdDate.getDate(),
                See : ''
            };
            i++;
        });
        console.log("addRow");
        for(var j=0;j<data.length;j++) {
            $("#"+self.listmanageproject).jqGrid('addRowData',data[j].id,data[j]);
        }
        $("#"+self.listmanageproject).jqGrid('sortGrid','filename',false);
    },
    renderImageListProject : function() {
        var self = this;

        $("#"+self.listmanageproject).jqGrid({
            datatype: "local",
            autowidth: true,
            height:500,
            colNames:['id','base','thumb','filename','type','annotations','added'],
            colModel:[
                {name:'id',index:'id', width:50, sorttype:"int"},
                {name:'base',index:'base', width:50, sorttype:"int"},
                {name:'thumb',index:'thumb', width:50},
                {name:'filename',index:'filename', width:220},
                {name:'type',index:'type', width:50},
                {name:'annotations',index:'annotations', width:50},
                {name:'added',index:'added', width:90,sorttype:"date"}
            ],
            onSelectRow: function(id){

                var checked = $("#"+self.listmanageproject).find("#" + id).find(".cbox").attr('checked');
                if(checked) $("#"+self.listmanageproject).find("#" + id).find("td").css("background-color", "CD661D");
                else $("#"+self.listmanageproject).find("#" + id).find("td").css("background-color", "a0dc4f");
            },
            loadComplete: function() {
                //change color of already selected image
                self.imagesProject.each(function(image) {
                    //console.log("image project="+image.id);
                    $("#"+self.listmanageproject).find("#" + image.id).find("td").css("background-color", "a0dc4f");
                });
            },
            //rowNum:10,
            pager: '#'+self.pagemanageproject,
            sortname: 'id',
            viewrecords: true,
            sortorder: "asc",
            caption:"Images in " + self.model.get('name'),
            multiselect: true
        });
        $("#"+self.listmanageproject).jqGrid('navGrid','#'+self.listmanageproject,{edit:false,add:false,del:false});
        $("#"+self.listmanageproject).jqGrid('hideCol',"id");
        $("#"+self.listmanageproject).jqGrid('hideCol',"base");
    },
    /**
     * Check if abstract image id is in project
     * @param id  Abstract Image id
     */
    isAbstractImageInProject : function(id) {
        if(_.indexOf(this.abstractImageProject, id)!=-1) return true;
        else return false;
    },
    renderImageListAll : function() {
        var self = this;
        var lastsel;
        var thumbColName = 'thumb';
        $("#"+self.listmanageall).jqGrid({
            datatype: "json",
            url : 'api/currentuser/image.json',
            autowidth: true,
            height:500,
            colNames:['id',thumbColName,'filename','mime','created'],
            colModel:[
                {name:'id',index:'id', width:30},
                {name:thumbColName,index:thumbColName, width:50},
                {name:'filename',index:'filename', width:220},
                {name:'mime',index:'mime', width:45},
                {name:'created',index:'created', width:100,sorttype:"date", formatter:self.dateFormatter}
            ],
            onSelectRow: function(id){
                if(self.isAbstractImageInProject(id)) {
                    //if image in project, row cannot be checked
                    $("#"+self.listmanageall).find("#" + id).find(".cbox").removeAttr('checked')
                }
            },
            loadComplete: function(data) {

                //load thumb
                $("#"+self.listmanageall).find("tr").each(function(index) {
                    if(index!=0) {
                        //0 is not a valid row

                        //replace the text of the thumb by a <img element with its src value
                        var thumbplace = $(this).find('[aria-describedby$="_'+thumbColName+'"]');
                        $(thumbplace).html('<img src="'+$(thumbplace).text()+'" width=30/>');
                    }
                });

                //aria-describedby="listmanageall3069_thumb">http://is1.cytomine.be:888/fcgi-bin/iipsrv.fcgi?FIF=/home/stevben/Slides/CERVIX/09-087214.mrxs&amp;SDS=0,90&amp;CNT=1.0&amp;WID=200&amp;SQL=99&amp;CVT=jpeg</td>

                //change color of already selected image
                self.imagesProject.each(function(image) {
                    // console.log("image project="+image.id);
                    $("#"+self.listmanageall).find("#" + image.get('baseImage')).find("td").css("background-color", "a0dc4f");
                    $("#"+self.listmanageall).find("#" + image.get('baseImage')).find(".cbox").attr('disabled', true)
                    $("#"+self.listmanageall).find("#" + image.get('baseImage')).find(".cbox").css("visible", false);
                });
            },
            //rowNum:10,
            pager: '#'+self.pagemanageall,
            sortname: 'id',
            viewrecords: true,
            sortorder: "asc",
            caption:"Available images",
            multiselect: true,
            rowNum:10,
            rowList:[10,20,30],
            jsonReader: {
                repeatitems : false,
                id: "0"
            }
        });
        $("#"+self.listmanageall).jqGrid('navGrid','#'+self.pagemanageall,{edit:false,add:false,del:false});
        $("#"+self.listmanageall).jqGrid('sortGrid','filename',false);
        $("#"+self.listmanageall).jqGrid('hideCol',"id");
        //self.loadDataImageListAll(window.app.models.images);
    },
    dateFormatter : function (cellvalue, options, rowObject)
    {
       // do something here
                var createdDate = new Date();
                createdDate.setTime(cellvalue);

       return createdDate.getFullYear() + "-" + createdDate.getMonth() + "-" + createdDate.getDate()
    },

    addImageProjectFromTable : function() {
        console.log("addImageProjectFromTable");
        var self = this;
        var idSelectedArray = $("#"+self.listmanageall).jqGrid('getGridParam','selarrrow');
        if (idSelectedArray.length == 0) return;
        var counter = 0;
        _.each(idSelectedArray, function(idImage){
            new ImageInstanceModel({}).save({project : self.model.id, user : null, baseImage :idImage},{
                success : function (image,response) {
                    console.log(response);
                    window.app.view.message("Image", response.message, "");
                    self.addImageProjectCallback(idSelectedArray.length, ++counter)
                },
                error: function (model, response) {
                    console.log(response);
                    var json = $.parseJSON(response.responseText);
                    window.app.view.message("Image", json.errors, "");
                    self.addImageProjectCallback(idSelectedArray.length, ++counter)
                }
            });
        });
    },
    addImageProjectCallback : function(total, counter) {
        if (counter < total) return;
        var self = this;
        self.refreshImageList();
    },

    deleteImageProjectFromTable : function() {
        console.log("deleteImageProjectFromTable");
        var self = this;
        var idSelectedArray = $("#"+self.listmanageproject).jqGrid('getGridParam','selarrrow');
        if (idSelectedArray.length == 0) return;
        var counter = 0;
        _.each(idSelectedArray, function(idImage){
            var idAsbtractImage = self.imagesProject.get(idImage).get('baseImage');
            new ImageInstanceModel({project : self.model.id, user : null, baseImage :idAsbtractImage}).destroy({
                success : function (image,response) {
                    console.log(response);
                    window.app.view.message("Image", response.message, "");
                    self.deleteImageProjectCallback(idSelectedArray.length, ++counter)
                },
                error: function (model, response) {
                    console.log(response);
                    var json = $.parseJSON(response.responseText);
                    window.app.view.message("Image", json.errors, "");
                    self.deleteImageProjectCallback(idSelectedArray.length, ++counter)
                }
            });
        });
    },
    deleteImageProjectCallback : function(total, counter) {
        if (counter < total) return;
        var self = this;
        self.refreshImageList();
    }
});