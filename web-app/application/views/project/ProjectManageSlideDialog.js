/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 28/04/11
 * Time: 10:14
 * To change this template use File | Settings | File Templates.
 */
var ProjectManageSlideDialog = Backbone.View.extend({
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
       projectPanel : null,
       addSlideDialog : null,
       page : 0, //start at the first page
       nb_slide_by_page : 10,
       imagesProject : null, //collection containing the images contained in the project
       /*events : {
        "click .next" : "nextPage",
        "click .previous" : "previousPage"
        },*/      //not work because of jquery dialog wich move content outside of this.el ?

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
             "text!application/templates/project/ProjectAddImageDialog.tpl.html"
          ],
              function(tpl) {
                 self.imagesProject = new ImageCollection({project:self.model.get('id')});
                 self.doLayout(tpl);
              });
       },
       /**
        * Render the html into the DOM element associated to the view
        * @param tpl
        */
       doLayout : function(tpl) {
          var self = this;
          console.log("Id project="+this.model.id);

          var dialog = _.template(tpl, {id:this.model.get('id'),name:this.model.get('name')});
          $(this.el).append(dialog);
          $(self.el).find("a.next").bind("click", self.nextPage);
          $(self.el).find("a.next").button();
          $(self.el).find("a.previous").bind("click", self.previousPage);
          $(self.el).find("a.previous").button();

          //Build dialog
          self.addSlideDialog = $(self.divDialog+this.model.get('id')).dialog({
                 create: function (event, ui) {
                    $(".ui-widget-header").hide();
                 },
                 modal : false,
                 autoOpen : false,
                 closeOnEscape: true,
                 beforeClose: function(event, ui) {
                    self.projectPanel.refresh();
                 },
                 close : function() {
                    $(this).dialog("destroy").remove();
                 },
                 buttons : {
                    "Close" : function() {
                       $(this).dialog("close");
                    }
                 },
                 width : ($(window).width()/100*90),
                 height: ($(window).height()/100*90) //bug with %age ?
              });
          this.renderImageList();
          this.open();
          return this;

       },
       /**
        * Open and ask to render image thumbs
        */
       open: function() {
          this.addSlideDialog.dialog("open") ;
       },
       renderImageList: function() {
          var self = this;

          var fetchCallback = function(cpt, expected) {
             if (cpt == expected)
                self.renderImageListLayout();
          };

          var modelsToPreload = [window.app.models.slides, window.app.models.images, self.imagesProject];
          var nbModelFetched = 0;
          _.each(modelsToPreload, function(model){
             model.fetch({
                    success :  function(model, response) {
                       fetchCallback(++nbModelFetched, _.size(modelsToPreload));
                    }
                 });
          });

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
             var item = _.template(tpl, {id:image.id,name:filename, slide: image.get('slide'), info : image.get('info')});

             $(domTarget).append(item);
             $(domTarget + " " + self.imageDivElem+image.id).append(thumb.el);  //get the div elem (img id) which have this project as parent
             $(thumb.el).css({"width":30}); //thumb must be smaller
             //if image is already in project, selected it
             if(projectImages.get(image.id)){
                //get the li elem (img id) which have this project as parent
                $(domTarget + " " + "#"+ self.liElem+image.id).addClass(self.selectedClass);
                $(domTarget + " " + "#"+self.liElem+image.id).find(":checkbox").attr(self.checkedAttr,self.checkedAttr);
             }
          });
       },
       selectAllImages : function (slideID) {
          $(".projectImageList" + slideID).find(".imageThumbChoice").each(function(){
             $(this).find("a.checkbox-select").click();
          });
       },
       unselectAllImages : function (slideID) {
          $(".projectImageList" + slideID).find(".imageThumbChoice").each(function(){
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
             el.append(item);
             el.find(".slideItem"+slide.get("id")).panel({collapsible:false});
             el.find("a[class=selectAll]").bind("click", function(){
                self.selectAllImages(slide.get("id"));
             });
             el.find("a[class=unselectAll]").bind("click", function(){
                self.unselectAllImages(slide.get("id"));
             });
             el.find("a[class=selectAll]").button();
             el.find("a[class=unselectAll]").button();
             var images = slide.get("images");
             var domTarget = ".projectImageList" + slide.get("id");
             _.each(images, function (imageID){
                self.renderImage(self.imagesProject, window.app.models.images.get(imageID), domTarget);
             });
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

          window.app.models.slides.each(function(slide){
             if ((cpt >= inf) && (cpt < sup)) {
                self.renderSlide(slide);
             }
             cpt++;
             if (cpt == sup) {
                self.initEvents();
             }
          });
       },
       /**
        * Init click events on Image thumbs
        */
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

                 //add slide to project
                 new ImageInstanceModel({}).save({project : self.model.get('id'), user : null, baseImage : idImage},{
                        success : function (image,response) {
                        },
                        error: function (model, response) {
                           console.log("ERROR:"+response);
                        }
                     });
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

                 //delete slide from project
                 new ImageInstanceModel({project : self.model.get('id'), user : null, baseImage : idImage}).destroy({
                        success : function (image,response) {
                        },
                        error: function (model, response) {
                           console.log("ERROR:"+response);
                        }
                     });
              });
       }
    });