var ProjectAddImageListingDialog = Backbone.View.extend({
    imagesProject : null, //collection containing the images contained in the project
    slides : null,
    images : null,
    initialize: function(options) {
        var self = this;
        this.container = options.container;
        this.projectPanel = options.projectPanel;
        this.imagesProject = options.imagesProject;
        this.slides = options.slides,
                this.images = options.images,
                this.el = "#tabsProjectaddimagedialog"+self.model.id+"-2" ;

        this.listmanageproject = "listmanageproject"+this.model.id;
        this.pagemanageproject = "pagemanageproject"+this.model.id;
        this.listmanageall = "listmanageall"+this.model.id;
        this.pagemanageall = "pagemanageall"+this.model.id;
        this.addImageButton = "addimageprojectbutton"+this.model.id;
        this.delImageButton = "delimageprojectbutton"+this.model.id;

    },
    render : function() {
        var self = this;
        require([
            "text!application/templates/project/ProjectAddImageListingDialog.tpl.html"
        ],
               function(tpl) {
                   self.doLayout(tpl);
               });
    },
    //append to tabsProjectaddimagedialog{{id}}-2
    doLayout : function(tpl) {
        var self = this;
        console.log("Id project="+this.model.id);

        var dialog = _.template(tpl, {id:this.model.get('id'),name:this.model.get('name')});
        $(self.el).append(dialog);

        console.log($(self.el).find("#tabsProjectaddimagedialog"+this.model.get('id')).length);
        // $(self.el).find("#tabsProjectaddimagedialog"+this.model.get('id')).tabs();

        this.renderImageList();

        var imagesNameArray = new Array();

        //TODO: improve perf and fill it when browse in an other place?
        self.images.each(function(image) {
            imagesNameArray.push(image.get('filename'));
        });


        //autocomplete
        $("#filenamesearchtextboxup"+self.model.id).autocomplete({
            minLength : 0, //with min=0, if user erase its text, it will show all project withouth name constraint
            source : imagesNameArray,
            select : function (event,ui)
            {
                $("#filenamesearchtextboxup"+self.model.id).val(ui.item.label)
                self.searchImages();

            },
            search : function(event)
            {

                console.log("TEXT:"+$("#filenamesearchtextboxup"+self.model.id).val());
                self.searchImages();
            }
        });

        return this;

    },



    searchImages : function() {
        var self = this;
        var searchText = $("#filenamesearchtextboxup"+self.model.id).val();
        self.filterImages(searchText==""?undefined:searchText);
    },

    filterImages : function(searchText) {

        var self = this;
        var images =  new ImageCollection(self.images.models);

        //each search function takes a search data and a collection and it return a collection without elem that
        //don't match with data search
        images = self.filterByImagesByName(searchText,images);

        //add here filter function


        $("#"+self.listmanageall).jqGrid("clearGridData", true);
        self.loadDataImageListAll(images);

    },











    filterByImagesByName : function(searchText,imagesOldList) {

        var imagesNewList =  new ImageCollection(imagesOldList.models);

        imagesOldList.each(function(image) {
            if(searchText!=undefined && !image.get('filename').toLowerCase().contains(searchText.toLowerCase()))
                imagesNewList.remove(image);
        });

        return imagesNewList;
    },

    filterByFormat : function() {

    },

    filterByDateStart : function() {

    },

    filterByDateEnd : function() {

    },





    renderImageList: function() {
        var self = this;
        self.renderImageListing();
        /*var fetchCallback = function(cpt, expected) {
         if (cpt == expected) {
         self.renderImageListing();
         }
         };

         var modelsToPreload = [window.app.models.slides, window.app.models.images, self.imagesProject];
         var nbModelFetched = 0;
         _.each(modelsToPreload, function(model){
         model.fetch({
         success :  function(model, response) {
         fetchCallback(++nbModelFetched, _.size(modelsToPreload));
         }
         });
         });*/

    },
    renderImageListing : function() {
        var self = this;
        console.log("renderImageListing");

        $("#"+self.addImageButton).button({
            icons : {primary: "ui-icon-circle-arrow-w" } ,
            text: false
        });
        $("#"+self.delImageButton).button({
            icons : {primary: "ui-icon-circle-arrow-e"} ,
            text: false
        });

        $("#searchImagetPanelup"+self.model.id).panel({
            collapseSpeed:100
        });

        $("#imagesallbutton"+self.model.id).button({
            text: true
        });

        $( "#datestartsearchup"+self.model.id ).datepicker();
        $( "#dateendsearchup"+self.model.id ).datepicker();

        $('#'+self.addImageButton).click(function() {
            self.addImageProjectFromTable();
        });

        $('#'+self.delImageButton).click(function() {
            self.deleteImageProjectFromTable();
        });

        self.renderImageListProject();
        self.renderImageListAll();

    },




    refreshImageList : function() {
        var self = this;
        console.log("refreshImageListProject");
        console.log("clear");
        $("#"+self.listmanageall).jqGrid("clearGridData", true);
        $("#"+self.listmanageproject).jqGrid("clearGridData", true);
        self.imagesProject.fetch({
            success : function (collection, response) {
                self.loadDataImageListProject(collection);

                //window.app.models.images.fetch({
                //  success : function (collection, response) {
                self.loadDataImageListAll(window.app.models.images);
                //}});
            }
        });
    },
    loadDataImageListProject : function(collection) {
        //add image data
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
                filename: image.get('filename'),
                type : image.get('mime'),
                added : createdDate.getFullYear() + "-" + createdDate.getMonth() + "-" + createdDate.getDate(),
                See : ''
            };
            i++;
        });
        for(var j=0;j<data.length;j++) {
            console.log(data[j]);
            $("#"+self.listmanageproject).jqGrid('addRowData',data[j].id,data[j]);
        }
        $("#"+self.listmanageproject).jqGrid('sortGrid','filename',false);

    },
    renderImageListProject : function() {
        var self = this;
        var lastsel;
        console.log("JQGRID:"+$("#"+self.listmanageproject).length);
        $("#"+self.listmanageproject).jqGrid({
            datatype: "local",
            width: "450",
            height : "300",
            colNames:['id','filename','type','added'],
            colModel:[
                {name:'id',index:'id', width:50, sorttype:"int"},
                {name:'filename',index:'filename', width:250},
                {name:'type',index:'type', width:50},
                {name:'added',index:'added', width:90,sorttype:"date"}
            ],
            onSelectRow: function(id){

                var checked = $("#"+self.listmanageproject).find("#" + id).find(".cbox").attr('checked');
                if(checked) $("#"+self.listmanageproject).find("#" + id).find("td").css("background-color", "CD661D");
                else $("#"+self.listmanageproject).find("#" + id).find("td").css("background-color", "a0dc4f");


                if(id && id!==lastsel){
                    //alert("Click on "+id);

                    lastsel=id;
                }
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
            caption:"Image in " + self.model.get('name'),
            multiselect: true
        });
        $("#"+self.listmanageproject).jqGrid('navGrid','#'+self.listmanageproject,{edit:false,add:false,del:false});

        self.loadDataImageListProject(self.imagesProject);
    },
    renderImageListAll : function() {
        var self = this;
        var lastsel;
        $("#"+self.listmanageall).jqGrid({
            datatype: "local",
            width: "700",
            height : "300",
            colNames:['id','filename','type','added'],
            colModel:[
                {name:'id',index:'id', width:30},
                {name:'filename',index:'filename', width:300},
                {name:'type',index:'type', width:40},
                {name:'added',index:'added', width:70,sorttype:"date"}
            ],
            onSelectRow: function(id){
                if(self.imagesProject.get(id)!=null) {
                    //if image in project, row cannot be checked
                    $("#"+self.listmanageall).find("#" + id).find(".cbox").removeAttr('checked')
                }
            },
            loadComplete: function() {
                //change color of already selected image


                self.imagesProject.each(function(image) {
                    // console.log("image project="+image.id);
                    $("#"+self.listmanageall).find("#" + image.id).find("td").css("background-color", "a0dc4f");
                    //$("#"+listmanage).find("#" + image.id).find(".cbox").attr('checked', 'checked')
                    $("#"+self.listmanageall).find("#" + image.id).find(".cbox").attr('disabled', true)
                    $("#"+self.listmanageall).find("#" + image.id).find(".cbox").css("visible", false);
                    /* alert(" 1:"+$("#"+self.listmanageall).find("#" + image.id).find(".cbox").length +
                     " 2:" + image.id + " " +$("#"+self.listmanageall).find("#" + image.id).length +
                     " 3:"+$("#"+self.listmanageall).length); */


                });
            },
            //rowNum:10,
            pager: '#'+self.pagemanageall,
            sortname: 'id',
            viewrecords: true,
            sortorder: "asc",
            caption:"Other images",
            multiselect: true
        });
        $("#"+self.listmanageall).jqGrid('navGrid','#'+self.pagemanageall,{edit:false,add:false,del:false});
        $("#"+self.listmanageall).jqGrid('sortGrid','filename',false);
        self.loadDataImageListAll(window.app.models.images);
    },

    loadDataImageListAll : function(collection) {
        //add image data
        console.log("loadDataImageListAll");
        var self = this;
        var data = new Array();
        var i = 0;

        collection.each(function(image) {

            if(true) { //if(self.imagesProject.get(image.id)==null) => Image not in project

                var createdDate = new Date();
                createdDate.setTime(image.get('created'));

                data[i] = {
                    id: image.id,
                    filename: image.get('filename'),
                    type : image.get('mime'),
                    added : createdDate.getFullYear() + "-" + createdDate.getMonth() + "-" + createdDate.getDate()
                };
                i++;
            }
        });
        for(var j=0;j<data.length;j++) {
            console.log("add row");
            $("#"+self.listmanageall).jqGrid('addRowData',data[j].id,data[j]);
        }
        $("#"+self.listmanageall).jqGrid('sortGrid','filename',false);
        $("#"+self.listmanageall).jqGrid('sortGrid','filename',false);

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
            new ImageInstanceModel({project : self.model.id, user : null, baseImage :idImage}).destroy({
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