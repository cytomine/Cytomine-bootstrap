/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 26/04/11
 * Time: 14:38
 * To change this template use File | Settings | File Templates.
 */
var ProjectPanelView = Backbone.View.extend({
    tagName : "div",
    loadImages : true, //load images from server or simply show/hide images
    imageOpened : false, //image are shown or not
    project : null,
    projectElem : "#projectlist",  //div with project info
    imagesListElem :  "#imagelistproject", //div with image list
    imageOpenElem : "#projectopenimages",
    imageAddElem : "#projectaddimages",
    projectChangeElem :"#radioprojectchange" ,
    projectChangeDialog : "div#projectchangedialog",
    loadImagesInAddPanel: true,
    initialize: function(options) {
        this.container = options.container;
        _.bindAll(this, 'render');
    },
    render: function() {
        var self = this;
        self.project = this.model;
        console.log(self.project.toJSON());
        var json = self.project.toJSON();

        //Get ontology name
        new OntologyModel({id:json.ontology}).fetch({success : function (ontology,response) {
            json.ontology = ontology.get('name');

            new ImageCollection({project:self.project.get('id')}).fetch({success : function (collection, response) {
                json.images = collection.length;

                new AnnotationCollection({project:self.project.get('id'), user: undefined, image : undefined}).fetch({success : function (collection, response) {
                    json.annotations = collection.length;

                    new UserCollection({project:self.project.get('id')}).fetch({success : function (collection, response) {
                        //json.users = collection.length;
                        json.users = "  ";
                        collection.each(function(user) {
                            json.users = json.users  + user.get('username')+ ", ";
                        });
                        json.users = json.users.substring(0,json.users.length-2);

                        var proj = ich.projectviewtpl(json);
                        $(self.el).append(proj);

                        self.renderCurrentProjectButton();
                        self.renderShowImageButton(json.images);
                        self.renderAddImageButton();




                        //self.renderAddImagePanel();

                    }
                    });

                }
                });

            }
            });
        }
        });
        return this;
    },
    buildAddImagedialog : function() {
        var self = this;
        /* see if anything is previously checked and reflect that in the view*/
        $(".checklist input:checked").parent().addClass("selected");

        $(".checklist .checkbox-select").click(
                                              function(event) {
                                                  console.log("click a");
                                                  event.preventDefault();
                                                  $(this).parent().addClass("selected");
                                                  $(this).parent().find(":checkbox").attr("checked","checked");
                                              }
                );

        $(".checklist .checkbox-deselect").click(
                                                function(event) {
                                                    console.log("click b");
                                                    event.preventDefault();
                                                    $(this).parent().removeClass("selected");
                                                    $(this).parent().find(":checkbox").removeAttr("checked");
                                                });



        //Build dialog

        $("div#projectaddimagedialog"+self.project.get('id')).dialog({
            buttons : {
                "Close all" : function() {
                    $("div#projectaddimagedialog"+self.project.get('id')).dialog("close");

                },
                "Cancel" : function() {
                    console.log("close");
                    $("div#projectaddimagedialog"+self.project.get('id')).dialog("close");
                }
            },
            width : 800
        });
        //if($("div#projectaddimagedialog"+self.project.get('id')).dialog("isOpen")) $("div#projectaddimagedialog"+self.project.get('id')).dialog("close");
        $("div#projectaddimagedialog"+self.project.get('id')).dialog("open");
    },
    renderAddImagePanel : function() {

        var self = this;

        console.log("loadImagesInAddPanel="+self.loadImagesInAddPanel);

        //don't add dialog in document again, just reload it
        if(self.loadImagesInAddPanel)
        {
            var dialog = ich.projectaddimagedialog({id:self.project.get('id')});
            $(self.el).append(dialog);
            self.loadImagesInAddPanel = false;

        }
        $("ul#projectaddimagedialoglist"+self.project.get('id')).empty(); //clear the list

        //Get all images from server
        new ImageCollection({project:undefined}).fetch({
            success: function(collection,response){
                var page = 0;
                var cpt = 0;
                var nb_thumb_by_page = 50;
                var inf = Math.abs(page) * nb_thumb_by_page;
                var sup = (Math.abs(page) + 1) * nb_thumb_by_page;
                console.log("Model size=" + collection.length);


                //Get images from project server
                new ImageCollection({project:self.project.get('id')}).fetch({
                    success: function(projectImages,response){

                        console.log("collection size=" + collection.length);
                        console.log("projectImages size=" + projectImages.length);

                        collection.each(function(image) {
                            if ((cpt > inf) && (cpt <= sup)) {
                                var thumb = new ImageThumbView({
                                    model : image
                                }).render();
                                console.log("image="+image.get('filename'));
                                var item = ich.projectaddimageitem({id:image.get('id'),name:image.get('filename')});


                                $(thumb.el).css({"width":30});
                                $("ul#projectaddimagedialoglist"+self.project.get('id')).append(item);
                                $("#projectaddimageitempict"+image.get('id')).append(thumb.el);

                                if(projectImages.get(image.get('id')))
                                {
                                    $("#projectaddimageitemli"+image.get('id')).addClass("selected");
                                    $("#projectaddimageitemli"+image.get('id')).find(":checkbox").attr("checked","checked");
                                }

                                //console.log($("#projectaddimageitempict"+image.get('id')).html());
                                //console.log(item.html());

                                //$(self.imagesListElem + self.project.get('id')).append(thumb.el);
                            }
                            cpt++;
                        });
                        // console.log($("ul#projectaddimagedialoglist"+self.project.get('id') + " img").css({"width":75}));
                        $("ul#projectaddimagedialoglist"+self.project.get('id') + " img").addClass("thumbProject");
                        //$("ul#projectaddimagedialoglist"+self.project.get('id') + " div").removeClass("thumb").addClass("thumbProject");
                        // $("ul#projectaddimagedialoglist"+self.project.get('id') + " div").addClass("thumbProject");

                        //$(self.imagesListElem + idProject).append("<br>");

                        /*$("ul#projectaddimagedialoglist"+self.project.get('id')).imagesLoaded( function(){
                         $("ul#projectaddimagedialoglist"+self.project.get('id')).isotope({
                         itemSelector: '.thumb-wrap'
                         });

                         });*/
                        self.buildAddImagedialog();
                    },
                    error: function(error){
                        for (property in error) {
                            console.log(property + ":" + error[property]);
                        }
                    }
                });






            },
            error: function(error){
                for (property in error) {
                    console.log(property + ":" + error[property]);
                }
            }
        });


















    },
    renderAddImageButton : function() {
        var self = this;
        $(self.imageAddElem+self.project.get('id')).click(function () {
            self.renderAddImagePanel();
        });

        $(self.imageAddElem + self.project.id).button({
            icons : {secondary : "ui-icon-image"}
        });
    },
    renderShowImageButton : function(imageNumber) {
        var self = this;
        $(self.imageOpenElem+self.project.get('id')).click(function () {
            self.openImagesList(self.project.get('id'));

            //change the icon
            self.imageOpened=!self.imageOpened;
            $(self.imageOpenElem + self.project.id).button({icons : {secondary :self.imageOpened? "ui-icon-carat-1-n":"ui-icon-carat-1-s"}});
        });

        var disabledButton= true;
        if(imageNumber>0) disabledButton= false;

        $(self.imageOpenElem + self.project.id).button({
            icons : {secondary : "ui-icon-carat-1-s"},
            disabled: disabledButton
        });
    },
    renderCurrentProjectButton : function() {
        var self = this;

        var isCurrentProject = window.app.status.currentProject==self.project.id
        //change button style for current project
        $(self.projectChangeElem + self.project.id).button({
            icons : {secondary : "ui-icon-star"}
        });
        if(isCurrentProject) $(self.projectChangeElem + self.project.id).click();

        $(self.projectChangeElem+self.project.get('id')).click(function () {
            var idNewProject = self.project.get('id');
            var idOldProject = window.app.status.currentProject;

            console.log("change for project:"+idNewProject);
            console.log("current project:"+idOldProject);

            if(idNewProject==idOldProject) return;

            if(window.app.controllers.browse.imagesOpen.length>0)
            {
                //Some images are opene
                //Ask if close all or cancel
                var dialog = ich.projectchangedialog({id:idNewProject});
                $(self.el).append(dialog);

                //Build dialog
                $(self.projectChangeDialog+idNewProject).dialog({
                    buttons : {
                        "Close all" : function() {
                            //close all pictures and change current project id
                            window.app.controllers.browse.tabs.closeAll();
                            window.app.status.currentProject = idNewProject;
                            $(self.projectChangeDialog+idNewProject).dialog("close");

                        },
                        "Cancel" : function() {
                            $(self.projectChangeDialog+idNewProject).dialog("close");
                        }
                    }
                });

                //Open dialog
                $(self.projectChangeDialog+idNewProject).dialog("open");
            }
            else
            {
                //No image open
                window.app.status.currentProject = idNewProject;
            }

            console.log("New current project:"+window.app.status.currentProject);

        });
    },
    openImagesList: function(idProject) {

        console.log("click open image:"+idProject);

        var self = this;

        if(!this.loadImages) {
            //images are already loaded
            console.log("CLOSE");
            $(self.imagesListElem+idProject).toggle(); //toggle(1000) doesn't work with isotope?
            return;
        }
        console.log("OPEN");

        this.loadImages = false;//don't load again images

        var page = 0;
        var tpl = ich.imageprojectviewtpl({page : (Math.abs(page)+1), id : idProject}, true);

        $(self.projectElem+idProject).append(tpl);

        new ImageCollection({project:idProject}).fetch({
            success: function(collection,response){
                var cpt = 0;
                var nb_thumb_by_page = 21;
                var inf = Math.abs(page) * nb_thumb_by_page;
                var sup = (Math.abs(page) + 1) * nb_thumb_by_page;
                console.log("Model size=" + collection.length);

                collection.each(function(image) {
                    if ((cpt >= inf) && (cpt < sup)) {
                        var thumb = new ImageThumbView({
                            model : image
                        }).render();
                        $(self.imagesListElem + idProject).append(thumb.el);
                    }
                    cpt++;
                });
                $(self.imagesListElem + idProject).append("<br>");

                $(self.imagesListElem+idProject).imagesLoaded( function(){
                    $(self.imagesListElem+idProject).isotope({
                        itemSelector: '.thumb-wrap'
                    });

                });
            },
            error: function(error){
                for (property in error) {
                    console.log(property + ":" + error[property]);
                }
            }
        });

    }
});
