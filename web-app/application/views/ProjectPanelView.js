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
    renderAddImageButton : function() {
        var self = this;
        $(self.imageAddElem+self.project.get('id')).click(function () {
            //self.openImagesList(self.project.get('id'));
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

        var page = 0
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
                    if ((cpt > inf) && (cpt <= sup)) {
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
