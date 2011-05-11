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
    imageOpenElem : "#projectopenimages",
    imageAddElem : "#projectaddimages",
    projectChangeElem :"#radioprojectchange" ,
    projectChangeDialog : "div#projectchangedialog",
    loadImagesInAddPanel: true,
    projectsPanel : null,
    addSlideDialog : null,
    initialize: function(options) {
        this.container = options.container;
        this.projectsPanel = options.projectsPanel;
        _.bindAll(this, 'render');
    },
    events: {
        "click .addSlide": "showAddSlidesPanel",
        "click .seeSlide": "showSlidesPanel"
        //"click .changeProject": "changeProject"
        //"click .thumb" : "setCurrentProject"
    },
    /*setCurrentProject : function () {
     window.app.status.currentProject = this.model.id;

     },*/
    render: function() {
        this.printProjectInfo();
        return this;
    },
    refresh : function(model) {
        var self = this;
        //$(this.projectElem+this.model.get('id')).empty();
        new ProjectModel({id : self.model.id}).fetch({
            success : function (model, response) {
                console.log("refresh project panel");
                console.log(model.toJSON());
                self.model = model;
                self.loadImages=true;
                self.printProjectInfo();
                self.projectsPanel.refreshSearchPanel();

            }});

    },
    printProjectInfo : function() {
        var self = this;

        var json = self.model.toJSON();

        var idOntology = json.ontology;
        //TODO: make it faster: make a service to have all this information in one json
        //Get ontology name
        json.ontology = window.app.models.ontologies.get(idOntology).get('name');

        //Get users list
        new UserCollection({project:self.model.get('id')}).fetch({success : function (collection, response) {
            //json.users = collection.length;
            json.users = "  ";
            collection.each(function(user) {
                json.users = json.users  + user.get('username')+ ", ";
            });
            json.users = json.users.substring(0,json.users.length-2);

            var proj = ich.projectviewtpl(json);

            if(self.addSlideDialog!=null){
                $("#projectlist"+json.id).replaceWith(proj);
            }
            else
                $(self.el).append(proj);

            self.renderCurrentProjectButton();
            self.renderShowImageButton(json.numberOfImages);

            $(self.imageAddElem + self.model.id).button({
                icons : {secondary : "ui-icon-plus"}
            });
            $(self.projectElem+self.model.get('id')).panel({
                collapsible:false
            });
        }
        });
    },

    showAddSlidesPanel : function () {
        /*if(this.loadImagesInAddPanel) {
         var dialog = ich.projectaddimagedialog({id:this.model.get('id'),name:this.model.get('name')});
         $(this.el).append(dialog);
         this.loadImagesInAddPanel = false;
         }
         new AddImageProjectDialog({model:this.project,idProject:this.model.id,projectPanel:this}).render();*/


        var self = this;
        console.log($("#projectaddimagedialog"+this.model.id).length);
        if(self.addSlideDialog==null && $("#projectaddimagedialog"+this.model.id).length==0)
        {
            //Build dialog
            console.log("build dialog with project:"+this.model);
            self.addSlideDialog = new AddImageProjectDialog({model:this.model,projectPanel:this,el:self.el}).render();
        }
        self.addSlideDialog.open();

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
        var idProject = self.model.get('id');
        var cont = false;

        if(idProject==window.app.status.currentProject) return true;

        if(window.app.controllers.browse.tabs != null)
        {

            console.log("closeAll()");
            window.app.controllers.browse.closeAll();
            window.app.status.currentProject = idProject;
            console.log("window.app.status.currentProject"+window.app.status.currentProject);
            /*window.app.controllers.dashboard.dashboard();*/

            /* DISABLED
            //Some images are opene
            //Ask if close all or cancel
            var dialog = ich.projectchangedialog({id:idProject,name:self.model.get('name')});
            $(self.el).append(dialog);

            //Build dialog
            $(self.projectChangeDialog+idProject).dialog({
                buttons : {
                    "Close all" : function() {
                        //close all pictures and change current project id
                        console.log("closeAll()");
                        window.app.controllers.browse.closeAll();
                        window.app.status.currentProject = idProject;
                        console.log("window.app.status.currentProject"+window.app.status.currentProject);

                        $(self.projectChangeDialog+idProject).dialog("close");
                        return true;

                    },
                    "Cancel" : function() {
                        $(self.projectChangeDialog+idProject).dialog("close");
                    }
                }
            });

            //Open dialog
            $(self.projectChangeDialog+idProject).dialog("open");*/
        }
        else
        {
            //No image open
            window.app.controllers.browse.closeAll();
            window.app.status.currentProject = idProject;

            /*window.app.controllers.dashboard.dashboard();*/
        }
        console.log("New current project:"+window.app.status.currentProject);
        console.log("CONTINUE : " + cont);
        return true;//go to dashboard


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
            icons : {secondary : "ui-icon-image"}
        });
        if(isCurrentProject) $(self.projectChangeElem + self.model.id).click();
    },
    openImagesList: function(idProject) {
        /*
        var self = this;

        if(!this.loadImages) {
            //images are already loaded
            $(self.el).find('.galleria').toggle(); //toggle(1000) doesn't work with isotope?
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
                        var thumb = new ImageRowView({
                            model : image
                        }).render();
                        $(self.el).find('.galleria').append(thumb.el);
                    }
                    cpt++;
                });
                $(self.el).find('.galleria').galleria({
                    width: 650,
                    height: 500,
                    imageCrop : 'width',
                    imagePan : true,
                    showInfo : true,
                    _toggleInfo: false,
                    overlayOpacity : 0.50

                });

            },
            error: function(error){
                for (property in error) {
                    console.log(property + ":" + error[property]);
                }
            }
        });
        */

    }
});