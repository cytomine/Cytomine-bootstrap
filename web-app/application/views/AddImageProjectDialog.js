/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 28/04/11
 * Time: 10:14
 * To change this template use File | Settings | File Templates.
 */
var AddImageProjectDialog = Backbone.View.extend({
    idProject : null,
    checklistChecked : ".checklist input:checked",
    checklistSelected : ".checklist .checkbox-select",
    checklistDeselected : ".checklist .checkbox-deselect",
    selectedClass : "selected",
    checkedAttr : "checked",
    liElem : "projectaddimageitemli",
    ulElem : "#projectaddimagedialoglist",
    allProjectUlElem : "ul[id^=projectaddimagedialoglist]",
    imageDivElem : "#projectaddimageitempict",
    divDialog : "div#projectaddimagedialog",
    initialize: function(options) {
        this.container = options.container;
        this.idProject = options.idProject
        _.bindAll(this, 'render');
    },
    render : function() {
        var self = this;
        console.log("Id project="+self.idProject);

        //Build dialog
        $(self.divDialog+self.idProject).dialog({
            autoOpen : false,
            buttons : {
                "Close" : function() {
                    console.log("close");
                    $(self.divDialog+self.idProject).dialog("close");
                }
            },
            width : "85%"
        });
        if(!$(self.divDialog+self.idProject).dialog("isOpen"))
        {
            self.renderImageList();
            $(self.divDialog+self.idProject).dialog("open");
        }


    },
    refresh : function() {
        this.renderImageList();
    },
    renderImageList : function() {
        var self = this;

        //Dialog is maybe already in document (but closed)...clear the all list elem
        $(self.ulElem+self.idProject).empty();

        //Get all images from server
        //TODO: filter by user right
        new ImageCollection({project:undefined}).fetch({
            success: function(collection,response){
                //TODO: multi-page print
                var page = 0;
                var cpt = 0;
                var nb_thumb_by_page = 50;
                var inf = Math.abs(page) * nb_thumb_by_page;
                var sup = (Math.abs(page) + 1) * nb_thumb_by_page;

                //Get images from project server
                new ImageCollection({project:self.idProject}).fetch({
                    success: function(projectImages,response){

                        console.log("collection size=" + collection.length);
                        console.log("projectImages size=" + projectImages.length);

                        collection.each(function(image) {
                            if ((cpt > inf) && (cpt <= sup)) {
                                var thumb = new ImageThumbView({
                                    model : image
                                }).render();

                                var item = ich.projectaddimageitem({id:image.id,name:image.get('filename')});

                                $(thumb.el).css({"width":30}); //thumb must be smaller

                                $(self.ulElem+self.idProject).append(item);
                                $(self.ulElem+self.idProject + " " + self.imageDivElem+image.id).append(thumb.el);  //get the div elem (img id) which have this project as parent

                                //if image is already in project, selected it
                                if(projectImages.get(image.id))
                                {
                                    //get the li elem (img id) which have this project as parent
                                    $(self.ulElem+self.idProject + " " +  "#"+ self.liElem+image.id).addClass(self.selectedClass);
                                    $(self.ulElem+self.idProject + +" " + "#"+self.liElem+image.id).find(":checkbox").attr(self.checkedAttr,self.checkedAttr);
                                }
                            }
                            cpt++;
                        });
                        $(self.ulElem+self.idProject + " img").addClass("thumbProject");

                        //build dialog and event
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
    buildAddImagedialog : function() {
        var self = this;

        /* see if anything is previously checked and reflect that in the view*/
        $(self.checklistChecked).parent().addClass(self.selectedClass);

        $(self.checklistSelected).click(
                                       function(event) {
                                           event.preventDefault();
                                           $(this).parent().addClass(self.selectedClass);
                                           $(this).parent().find(":checkbox").attr(self.checkedAttr,self.checkedAttr);

                                           //Get the id of the selected image....
                                           //TODO: a better way to do that?
                                           var fullId = $(this).parent().attr("id");   //"projectaddimageitemliXXX"
                                           var idImage = fullId.substring(self.liElem.length,fullId.length);  //XXX

                                           //add slide to project
                                           new ImageModel({id:idImage}).fetch({success : function (image,response) {
                                               var slide = image.get('slide');
                                               console.log("Image id = " + idImage + " Slide id = " + slide + " Project id = " + self.idProject);
                                               new ProjectSlideModel({project : self.idProject, slide : slide}).save({project : self.idProject, slide : slide});
                                           }
                                           });
                                           self.refresh()
                                       }
                );

        $(self.checklistDeselected).click(
                                         function(event) {
                                             console.log("click b");
                                             event.preventDefault();
                                             $(this).parent().removeClass(self.selectedClass);
                                             $(this).parent().find(":checkbox").removeAttr(self.checkedAttr);

                                             //Get the id of the selected image....a better way to do that?
                                             var fullId = $(this).parent().attr("id");   //"projectaddimageitemliXXX"
                                             var idImage = fullId.substring(self.liElem.length,fullId.length);  //XXX

                                             //delete slide from project
                                             new ImageModel({id:idImage}).fetch({success : function (image,response) {
                                                 var slide = image.get('slide');
                                                 console.log("Image id = " + idImage + " Slide id = " + slide + " Project id = " + self.idProject);
                                                 new ProjectSlideModel({project : self.idProject, slide : slide}).destroy({project : self.idProject, slide : slide});
                                             }
                                             });
                                             self.refresh();
                                         });



    }
});