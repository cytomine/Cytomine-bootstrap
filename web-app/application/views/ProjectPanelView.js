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
    },
    render: function() {
        this.printProjectInfo();
        return this;
    },
    refresh : function() {
        var self = this;
        new ProjectModel({id : self.model.id}).fetch({
            success : function (model, response) {
                console.log("refresh project panel");
                self.model = model;
                self.loadImages=true;
                self.printProjectInfo();
                self.projectsPanel.loadSearchProjectPanel();
            }});
    },
    printProjectInfo : function() {

        var self = this;

        var json = self.model.toJSON();

        //Get ontology name
        var idOntology = json.ontology;
        json.ontology = window.app.models.ontologies.get(idOntology).get('name');

        //Get users list
        new UserCollection({project:self.model.get('id')}).fetch({success : function (collection, response) {
            var users=new Array();
            collection.each(function(user) {
                users.push(user.get('username'));
            });
            json.users = users.join(", ");

            var proj = ich.projectviewtpl(json);

            if(self.addSlideDialog!=null)
                $("#projectlist"+json.id).replaceWith(proj);
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

        var self = this;

        if(self.addSlideDialog==null && $("#projectaddimagedialog"+this.model.id).length==0)
        {
            //Build dialog
            console.log("build dialog with project:"+this.model);
            self.addSlideDialog = new ProjectManageSlideDialog({model:this.model,projectPanel:this,el:self.el}).render();
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

        window.app.controllers.browse.closeAll();
        window.app.status.currentProject = idProject;

        console.log("New current project:"+window.app.status.currentProject);
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