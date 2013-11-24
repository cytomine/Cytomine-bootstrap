var CustomModal = Backbone.View.extend({
    initialize: function (options) {
        this.buttons = [];
        this.idModal = options.idModal;
        this.button = options.button;
        this.header = options.header;
        this.body = options.body;
        this.wide = options.wide || false;
        this.callBack = options.callBack;
        this.registerModal();

    },
    addButtons : function(id,text,primary,close,callBack) {
        this.buttons.push({id:id,text:text,close: (close?'modal':''),primaryClass:(primary? 'btn-primary' :''),callBack:callBack});
    },
    registerModal : function() {
        var self = this;

        //when click on button to open modal, build modal html, append to doc and open modal
        self.button.unbind();
        self.button.click(function (evt) {

            require([
                "text!application/templates/utils/CustomModal.tpl.html"
            ],
                function (tplModal) {

                    var modal = $("#modals");
                    modal.empty();

                    var htmlModal = _.template(tplModal,{
                        id : self.idModal,
                        header : self.header,
                        body : self.body,
                        wide : (self.wide ? "modal-wide" : ""),
                        buttons : self.buttons
                    });
                    modal.append(htmlModal);

                    _.each(self.buttons,function(b) {
                        $("#"+b.id).click(function() {
                            if(b.callBack) {
                                b.callBack();
                            }
                            return true;
                        });
                    });

                    if(self.callBack) {
                        self.callBack();
                    }

                });
            return true;
        });
    },
    close : function () {
        $('#'+this.idModal).modal('hide').remove();
        $('body').removeClass('modal-open');
        $('.modal-backdrop').remove();
    }
});


var DescriptionModal = {

    initDescriptionModal : function(container,idDescription,domainIdent, domainClassName, text,callback) {
        var width = Math.round($(window).width()*0.58);
        var height =  Math.round($(window).height()*0.58);

        //add host url for images
        text = text.split('/api/attachedfile').join(window.location.protocol + "//" + window.location.host+'/api/attachedfile');

         var modal = new CustomModal({
             idModal : "descriptionModal"+domainIdent,
             button : container.find("a.description"),
             header :"Description",
             body :'<div id="description'+domainIdent+'"><textarea style="width: '+(width-100)+'px;height: '+(height-100)+'px;" id="descriptionArea'+domainIdent+'" placeholder="Enter text ...">'+text+'</textarea></div>',
             wide : true,
             callBack : function() {
                 $("#descriptionArea"+domainIdent).wysihtml5({});

                 $("#saveDescription"+idDescription).click(function(e) {
                     // remove the host url for images
                        text = $("#descriptionArea"+domainIdent).val().split(window.location.protocol + "//" + window.location.host+'/api/attachedfile').join('/api/attachedfile');
                         new DescriptionModel({id:idDescription,domainIdent: domainIdent, domainClassName: domainClassName}).save({
                             domainIdent: domainIdent,
                             domainClassName: domainClassName,
                             data :  text
                         }, {success: function (termModel, response) {
                             callback();
                          }, error: function (model, response) {
                              var json = $.parseJSON(response.responseText);
                              window.app.view.message("Correct term", "error:" + json.errors, "");
                          }});

                 });

             }
         });
         modal.addButtons("saveDescription"+idDescription,"Save",true,true);
         modal.addButtons("closeDescription"+idDescription,"Close",false,true);

    },
    initDescriptionView : function(domainIdent, domainClassName, container, maxPreviewCharNumber, callbackGet,callbackUpdate) {
         var self = this;
        new DescriptionModel({domainIdent: domainIdent, domainClassName: domainClassName}).fetch(
                {success: function (description, response) {
                    container.empty();
                    var text = description.get('data');
                    var textButton = "Edit";
                    text = text.split('\\"').join('"');
                    if(text.replace(/<[^>]*>/g, "").length>maxPreviewCharNumber) {
                        text = text.substr(0,maxPreviewCharNumber)+"...";
                        textButton = "See full text and edit"
                    }
                    container.append(text);
                    container.append(' <a href="#descriptionModal'+domainIdent+'" role="button" class="description" data-toggle="modal">'+textButton+'</a>');
                    callbackGet();

                    self.initDescriptionModal(container,description.id,domainIdent,domainClassName,description.get('data'),callbackUpdate);
                }, error: function (model, response) {
                    container.empty();
                    container.append(' <a href="#descriptionModal'+domainIdent+'" role="button" class="description" data-toggle="modal">Add description</a>');
                    self.initDescriptionModal(container,null,domainIdent,domainClassName,"",callbackUpdate);

                }});

    }
}



var ImportAnnotationModal = {

    initImportAnnotationModal : function(container,idImage,callback) {
        var width = Math.round($(window).width()*0.75);
        var height =  Math.round($(window).height()*0.75);


         var modal = new CustomModal({

             idModal : "importAnnotationModal"+idImage,
             button : container.find("a.importannotation"),
             header :"Import Annotation Layer",
             body :'<div id="importLayer'+idImage+'">Import Annotation allow you to get annotations (terms, descriptions, properties) from an image from another project. ' +
                     '<br/>The image must be the same image, in an other project. You can only import annotation from layer (user) with at least 1 annotation and layer that are in the current project.<br/><br/>  <div id="layersSelection'+idImage+'"><div class="alert alert-info"><i class="icon-refresh"/> Loading...</div></div></div>',
             width : width,
             height : height,
             callBack : function() {

                 $.get("/api/imageinstance/"+idImage+"/sameimagedata", function(data) {
                     $("#layersSelection"+idImage).empty();
                     if(data.collection.length==0) {
                         $("#layersSelection"+idImage).append("This image has no other layers in other projects.");
                     } else {
                         $("#layersSelection"+idImage).append('<input type="checkbox" id="giveMeAnnotations"> Copy all annotations on my layer (if not checked, annotation will stay on the same layers) </input><br/><br/><br/> ');
                         _.each (data.collection, function (item){
                             var layer = item.image + "_" + item.user
                             var templ = '<input type="checkbox" class="layerSelection" id="'+layer+'"> Import annotation from project ' + item.projectName + ' -> ' + item.lastname + " " + item.firstname + ' (' + item.username + ') <br/>';
                              $("#layersSelection"+idImage).append(templ);
                         });
                     }
                }).fail(function(json) {
                     window.app.view.message("Import data", json.responseJSON.errors, "error",5000);
                    $("#closeImportLayer"+idImage).click();
                });

                 $("#importLayersButton"+idImage).click(function(e) {

                     $("#closeImportLayer"+idImage).hide();
                     $("#importLayersButton"+idImage).hide();
                     var layers = []
                     _.each($("#importLayer"+idImage).find("input.layerSelection"), function(item) {
                        if($(item).is(':checked')) {
                            layers.push(item.id)
                        }
                     });
                     var giveMe = $("#giveMeAnnotations").is(':checked');
                     $("#layersSelection"+idImage).empty();
                     new TaskModel({project: window.app.status.currentProject}).save({}, {
                             success: function (task, response) {
                                 console.log(response.task);
                                 $("#layersSelection"+idImage).append('<div id="task-' + response.task.id + '"></div>');
                                 var timer = window.app.view.printTaskEvolution(response.task,  $("#layersSelection"+idImage).find("#task-" + response.task.id), 2000);


                                 $.post("/api/imageinstance/"+idImage+"/copyimagedata?task="+response.task.id+"&layers="+layers.join(",") + "&giveMe="+giveMe, function(data) {
                                     clearInterval(timer);
                                     $("#closeImportLayer"+idImage).show();
                                     $("#closeImportLayer"+idImage).click();
                                }).fail(function(json) {
                                      clearInterval(timer);
                                     window.app.view.message("Import data", json.errors, "error");
                                 });
                             },
                             error: function (model, response) {
                                 var json = $.parseJSON(response.responseText);
                                 window.app.view.message("Task", json.errors, "error");
                             }
                         }
                     );
                 });

             }
         });
         modal.addButtons("importLayersButton"+idImage,"Import these layers",true,false);
         modal.addButtons("closeImportLayer"+idImage,"Close",false,true);

    }
}


