var CustomModal = Backbone.View.extend({
   initialize: function (options) {
       this.buttons = [];
       this.idModal = options.idModal;
       this.button = options.button;
       this.header = options.header;
       this.body = options.body;
       this.width = options.width;
       this.height = options.height;
       this.callBack = options.callBack;
       this.registerModal();

   },
   addButtons : function(id,text,primary,close,callBack) {
       this.buttons.push({id:id,text:text,close: (close?'modal':''),primaryClass:(primary? 'btn-primary' :''),callBack:callBack});
   },
   registerModal : function() {
        var self = this;
       console.log("registerModal");
       console.log(self.button.length);
        //when click on button to open modal, build modal html, append to doc and open modal
        self.button.unbind();
        self.button.click(function () {
            console.log("click show modal");
            require([
                "text!application/templates/utils/CustomModal.tpl.html"
            ],
             function (tplModal) {
                 var modal = $("#modals");
                 modal.empty();
                 var htmlModal = _.template(tplModal,{id:self.idModal,header:self.header,body:self.body,width:self.width,height:self.height,halfWidth:(self.width/2), buttons:self.buttons});

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
    }
});

var DescriptionModal = {

    initDescriptionModal : function(container,idDescription,domainIdent, domainClassName, text,callback) {
        var width = Math.round($(window).width()*0.75);
        var height =  Math.round($(window).height()*0.75);
        console.log("initDescriptionModal");
        console.log(container.find("a.description").html());


         var modal = new CustomModal({
             idModal : "descriptionModal"+domainIdent,
             button : container.find("a.description"),
             header :"Description",
             body :'<div id="description'+domainIdent+'"><textarea style="width: '+(width-100)+'px;height: '+(height-100)+'px;" id="descriptionArea'+domainIdent+'" placeholder="Enter text ...">'+text+'</textarea></div>',
             width : width,
             height : height,
             callBack : function() {
                 $("#descriptionArea"+domainIdent).wysihtml5({});

                 $("#saveDescription"+idDescription).click(function(e) {

                         new DescriptionModel({id:idDescription,domainIdent: domainIdent, domainClassName: domainClassName}).save({
                             domainIdent: domainIdent,
                             domainClassName: domainClassName,
                             data :  $("#descriptionArea"+domainIdent).val()
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




