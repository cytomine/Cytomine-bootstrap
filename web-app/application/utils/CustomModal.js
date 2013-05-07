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
   addButtons : function(id,text,primary, callBack) {
       this.buttons.push({id:id,text:text,primaryClass:(primary? 'btn-primary' :''),callBack:callBack});
   },
   registerModal : function() {
        var self = this;
        //when click on button to open modal, build modal html, append to doc and open modal
        self.button.unbind();
        self.button.click(function () {
            require([
                "text!application/templates/utils/CustomModal.tpl.html"
            ],
             function (tplModal) {
                 $("#modals").empty();
                 var htmlModal = _.template(tplModal,{id:self.idModal,header:self.header,body:self.body,width:self.width,height:self.height,halfWidth:(self.width/2), buttons:self.buttons});
                 $("#modals").append(htmlModal);
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