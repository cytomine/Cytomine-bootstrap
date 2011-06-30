var ProjectAddImageThumbDialog = Backbone.View.extend({
    imageListing : null,
    imageThumb : null,
    slides : null,
    images : null,
    imagesProject : null,
    checklistChecked : ".checklist input:checked",
    checklistSelected : ".checklist .checkbox-select",
    checklistDeselected : ".checklist .checkbox-deselect",
    selectedClass : "selected",
    checkedAttr : "checked",
    liElem : "projectaddimageitemli",
    ulElem : "#projectaddimagedialoglist",
    allProjectUlElem : "ul[id^=projectaddimagedialoglist]",
    imageDivElem : "#projectaddimageitempict",


    page : 0, //start at the first page
    nb_slide_by_page : 20,
    nextPage : function() {
        var max_page = Math.round(_.size(window.app.models.slides) / this.nb_slide_by_page) - 1;
        this.page = Math.min(this.page+1, max_page);
        this.renderImageListLayout();
    },
    previousPage : function() {
        this.page = Math.max(this.page-1, 0);
        this.renderImageListLayout();
    },
    disablePrevious : function() {
    },
    enablePrevious : function() {
    },
    disableNext : function() {
    },
    enableNext : function() {
    },
    /**
     * ProjectManageSlideDialog constructor
     * @param options
     */
    initialize: function(options) {
        this.container = options.container;
        this.projectPanel = options.projectPanel;
        this.imagesProject = options.imagesProject;
        this.el = "#tabsProjectaddimagedialog"+this.model.id+"-1" ;

        this.slides = options.slides,
                this.images = options.images,
                _.bindAll(this, 'render');
        _.bindAll(this, 'nextPage');
        _.bindAll(this, 'previousPage');
    },
    /**
     * Grab the layout and call ask for render
     */
    render : function() {
        var self = this;
        require([
            "text!application/templates/project/ProjectAddImageThumbDialog.tpl.html"
        ],
               function(tpl) {
                   self.doLayout(tpl);
               });
        return this;
    },
    refresh : function() {
       this.renderImageList();
    },


    doLayout : function(tpl) {
        var self = this;
        var view = _.template(tpl, {id:this.model.get('id'),name:this.model.get('name')});
        $(self.el).append(view);

        //TODO: INIT searchPanel
        self.searchPanel = new ProjectAddImageSearchPanel({
            model : self.model,
            images : self.images,
            el:$("#tdsearchpanelthumb"+self.model.id),
            container : self,
            tab : 1
        }).render();

        $(self.el).find("a.next").bind("click", self.nextPage);
        $(self.el).find("a.next").button();
        $(self.el).find("a.previous").bind("click", self.previousPage);

        $(self.el).find("a.previous").button();

        console.log($(self.el).find("#tabsProjectaddimagedialog"+this.model.get('id')).length);
        // $(self.el).find("#tabsProjectaddimagedialog"+this.model.get('id')).tabs();
        self.renderImageList();
        return this;

    },

    searchImages : function() {
        var self = this;
        var images = self.searchPanel.search(self.images);

        //clear
        console.log("clear");

        //reload
        console.log("reload");
    },

    renderImageList: function() {
        var self = this;
        self.renderImageListLayout();

    },
    renderImage : function(projectImages, image, domTarget) {
        var self = this;
        require([
            "text!application/templates/project/ProjectAddImageChoice.tpl.html"
        ],   function(tpl) {
            var thumb = new ImageSelectView({
                model : image
            }).render();

            var filename = image.get("filename");
            if (filename.length > 15)
                filename = filename.substring(0,12) + "...";
            var item = _.template(tpl, {id:image.id,name:filename, namefull: image.get("filename"), slide: image.get('slide'), info : image.get('info')});

            $(domTarget).append(item);
            $(domTarget + " " + self.imageDivElem+image.id).append(thumb.el);  //get the div elem (img id) which have this project as parent
            $(thumb.el).css({"width":30}); //thumb must be smaller

            //size of the filename text
            $(domTarget).find("label  > b").css("font-size",10);

            //if image is already in project, selected it

            if(projectImages.get(image.id)){
                //get the li elem (img id) which have this project as parent
                $(domTarget + " " + "#"+ self.liElem+image.id).addClass(self.selectedClass);
                $(domTarget + " " + "#"+self.liElem+image.id).find(":checkbox").attr(self.checkedAttr,self.checkedAttr);
            }

        });
    },
    selectAllImages : function (slideID) {
        $(".projectImageList" + slideID).find("li.imageThumbChoice").each(function(){
            $(this).find("a.checkbox-select").click();
        });
    },
    unselectAllImages : function (slideID) {
        $(".projectImageList" + slideID).find("li.imageThumbChoice").each(function(){
            $(this).find("a.checkbox-deselect").click();
        });
    },

    renderSlide : function(slide) {
        var self = this;
        require([
            "text!application/templates/project/ProjectSlideDetail.tpl.html"
        ],   function(tpl) {
            var item = _.template(tpl, { id : slide.get("id"), name : slide.get("name")});
            var el = $(self.ulElem+self.model.get('id'));
            el.append("<td>"+item+"</td>");
            el.find(".slideItem"+slide.get("id")).panel({collapsible:false});
            el.find("a[class=selectAll]").bind("click", function(){
                self.selectAllImages(slide.get("id"));
            });
            el.find("a[class=unselectAll]").bind("click", function(){
                self.unselectAllImages(slide.get("id"));
            });
            el.find("a[class=selectAll]").button({text: false,
                 icons: {
                    secondary: "ui-icon-circle-plus"
                 }});
            el.find("a[class=unselectAll]").button({text: false,
                 icons: {
                    secondary: "ui-icon-circle-minus"
                 }});
            var images = slide.get("images");
            var domTarget = ".projectImageList" + slide.get("id");
            _.each(images, function (imageID){
                self.renderImage(self.imagesProject, window.app.models.images.get(imageID), domTarget);
            });
             $("#projectImageList"+slide.get("id")).carousel();
            $(domTarget).append('<div style="clear:both;"></div>');

        });

    },
    /**
     * Render Image thumbs into the dialog
     **/

    renderImageListLayout : function() {
        var self = this;
        var cpt = 0;
        var inf = Math.abs(self.page) * self.nb_slide_by_page;
        var sup = (Math.abs(self.page) + 1) * self.nb_slide_by_page;
        $(self.ulElem+self.model.get('id')).empty();

        var maxCol = 4
        var col = 0

        $(self.ulElem+self.model.get('id')).append("<table><tr width='25%'>");

        window.app.models.slides.each(function(slide){
            if ((cpt >= inf) && (cpt < sup)) {
                self.renderSlide(slide);
                col++;
                if(col==maxCol) {
                    col=0;
                    $(self.ulElem+self.model.get('id')).append("</tr><tr width='25%'>");
                }
            }
            $(self.ulElem+self.model.get('id')).append("</tr></table>");
            $(".carousel-wrap").css("height","200");
            cpt++;
            if (cpt == sup) {
                self.initEvents();
            }
        });
    },
    /**
     * Init click events on Image thumbs
     */

    addImageToProject: function(idImage,idProject ) {
        console.log("addImageToProject: idImage="+idImage+" idProject="+idProject);
        //add slide to project
        new ImageInstanceModel({}).save({project : idProject, user : null, baseImage :idImage},{
            success : function (image,response) {
                console.log(response);
                window.app.view.message("ImageInstance", response.message, "");
            },
            error: function (model, response) {
                console.log(response);
                var json = $.parseJSON(response.responseText);
                window.app.view.message("Image", json.errors[0], "");
            }
        });
    },

    deleteImageToProject: function(idImage,idProject ) {
        console.log("addImageToProject: idImage="+idImage+" idProject="+idProject);
        //add slide to project
        //delete slide from project
        new ImageInstanceModel({project : idProject, user : null, baseImage : idImage}).destroy({
            success : function (image,response) {
                console.log(response);
                window.app.view.message("ImageInstance", response.message, "");

            },
            error: function (model, response) {
                console.log(response);
                var json = $.parseJSON(response.responseText);
                window.app.view.message("Image", json.errors[0], "");
            }
        });
    },

    initEvents : function() {

        /* TO DO
         Recode this method : don't repeat yourself
         Use Backbone view EVENTs to bind CLICK
         */
        var self = this;

        /* see if anything is previously checked and reflect that in the view*/
        $(self.checklistChecked).parent().addClass(self.selectedClass);

        $(self.checklistSelected).click(
                                       function(event) {
                                           event.preventDefault();
                                           var slideID = $(this).parent().attr("class");

                                           $(this).parent().addClass(self.selectedClass);
                                           $(this).parent().find(":checkbox").attr(self.checkedAttr,self.checkedAttr);

                                           //Get the id of the selected image....
                                           //TODO: a better way to do that?
                                           var fullId = $(this).parent().attr("id");   //"projectaddimageitemliXXX"
                                           var idImage = fullId.substring(self.liElem.length,fullId.length);  //XXX
                                           self.addImageToProject(idImage,self.model.id);

                                       }
                );

        $(self.checklistDeselected).click(
                                         function(event) {
                                             event.preventDefault();
                                             $(this).parent().removeClass(self.selectedClass);
                                             $(this).parent().find(":checkbox").removeAttr(self.checkedAttr);

                                             //Get the id of the selected image....a better way to do that?
                                             var fullId = $(this).parent().attr("id");   //"projectaddimageitemliXXX"
                                             var idImage = fullId.substring(self.liElem.length,fullId.length);  //XXX
                                             self.deleteImageToProject(idImage,self.model.id);

                                         });
    }

});