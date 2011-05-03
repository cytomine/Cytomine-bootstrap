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
    events: {
        "click .addSlide": "showAddSlidesPanel",
        "click .seeSlide": "showSlidesPanel",
        "click .changeProject": "changeProject"
    },
    render: function() {
        var self = this;
        var json = self.model.toJSON();

        //TODO: make it faster: make a service to have all this information in one json
        //Get ontology name
        new OntologyModel({id:json.ontology}).fetch({success : function (ontology,response) {
            json.ontology = ontology.get('name');

            //Get image number
            new ImageCollection({project:self.model.get('id')}).fetch({success : function (collection, response) {
                json.images = collection.length;

                //Get annotation number
                new AnnotationCollection({project:self.model.get('id'), user: undefined, image : undefined}).fetch({success : function (collection, response) {
                    json.annotations = collection.length;

                    //Get users list
                    new UserCollection({project:self.model.get('id')}).fetch({success : function (collection, response) {
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

                        $(self.imageAddElem + self.model.id).button({
                            icons : {secondary : "ui-icon-image"}
                        });
                        $(self.projectElem+self.model.get('id')).panel({
                            collapsible:false
                        });
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
    showAddSlidesPanel : function () {
        if(this.loadImagesInAddPanel) {
            var dialog = ich.projectaddimagedialog({id:this.model.get('id'),name:this.model.get('name')});
            $(this.el).append(dialog);
            this.loadImagesInAddPanel = false;
        }
        new AddImageProjectDialog({model:this.project,idProject:this.model.id}).render();
    },
    showSlidesPanel : function () {
        var self = this;
            self.openImagesList(self.model.get('id'));

            //change the icon
            self.imageOpened=!self.imageOpened;
            $(self.imageOpenElem + self.model.id).button({icons : {secondary :self.imageOpened? "ui-icon-carat-1-n":"ui-icon-carat-1-s"}});
    },
    changeProject : function () {
        var self = this;
        var idNewProject = self.model.get('id');
                    var idOldProject = window.app.status.currentProject;

                    console.log("change for project:"+idNewProject);
                    console.log("current project:"+idOldProject);

                    if(idNewProject==idOldProject) return;

                    if(window.app.controllers.browse.imagesOpen.length>0)
                    {
                        //Some images are opene
                        //Ask if close all or cancel
                        var dialog = ich.projectchangedialog({id:idNewProject,name:self.model.get('name')});
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

    },
    renderShowImageButton : function(imageNumber) {

        var self = this;

        var disabledButton= true;
        if(imageNumber>0) disabledButton= false;

        $(self.imageOpenElem + self.model.id).button({
            icons : {secondary : "ui-icon-carat-1-s"},
            disabled: disabledButton
        });
    },
    renderCurrentProjectButton : function() {
        var self = this;

        var isCurrentProject = window.app.status.currentProject==self.model.id
        //change button style for current project
        $(self.projectChangeElem + self.model.id).button({
            icons : {secondary : "ui-icon-star"}
        });
        if(isCurrentProject) $(self.projectChangeElem + self.model.id).click();
    },
    openImagesList: function(idProject) {

        var self = this;

        if(!this.loadImages) {
            //images are already loaded
            $(self.imagesListElem+idProject).toggle(); //toggle(1000) doesn't work with isotope?
            return;
        }


        this.loadImages = false;//don't load again images

        var page = 0;

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
                        $(self.el).find('.scroll-content').append(thumb.el);
                    }
                    cpt++;
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
