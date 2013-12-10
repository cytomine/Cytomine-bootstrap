var HotKeys = {
    context : function() {
        var idImage = window.app.status.currentImage.idImage;
        var idProject = window.app.status.currentProject;
        return "div#tabs-"+window.app.status.currentImage.prefix+"-"+idProject+"-"+idImage+"-";
    },

    initHotKeys : function() {
        var self = this;

        $(document).bind('keydown.b',function (evt){
                    if(self.checkMode("review") || self.checkMode("image")) {
                        self.doClick(self.context(),".selectButton");
                    }
                }
        );
        $(document).bind('keydown.a',function (evt){
                    if(self.checkMode("review")) {
                        self.doClick(self.context(),".acceptButton");
                    }
                }
        );
        $(document).bind('keydown.r',function (evt){
                    if(self.checkMode("review")) {
                        self.doClick(self.context(),".rejectButton");
                    }
                }
        );
        $(document).bind('keydown.n',function (evt){
                    if(self.checkMode("review") || self.checkMode("image")) {
                        self.doClick(self.context(),".nextImage");
                    }
                }
        );
        $(document).bind('keydown.p',function (evt){
                    if(self.checkMode("review") || self.checkMode("image")) {
                        self.doClick(self.context(),".previousImage");
                    }
                }
        );

        $(document).bind('keydown.s',function (evt){
                    if(self.checkMode("review") || self.checkMode("image")) {
                        self.doClick(self.context(),".selectButton");
                    }
                }
        );
        $(document).bind('keydown.f',function (evt){
                    if(self.checkMode("review") || self.checkMode("image")) {
                        self.doClick(self.context(),".freehandButton");
                    }
                }
        );
        $(document).bind('keydown.w',function (evt){

                    if(self.checkMode("review") || self.checkMode("image")) {
                        self.doClick(self.context(),".magicWandButton");

                    }
                }
        );
        $(document).bind('keydown.e',function (evt){
                    if(self.checkMode("review") || self.checkMode("image")) {
                        self.doClick(self.context(),".editButton");
                    }
                }
        );
        $(document).bind('keydown.c',function (evt){
                    if(self.checkMode("review") || self.checkMode("image")) {
                        self.doClick(self.context(),".cutButton");
                    }
                }
        );
        $(document).bind('keydown.j',function (evt){
                    if(self.checkMode("review") || self.checkMode("image")) {
                        self.doClick(self.context(),".joinButton");
                    }
                }
        );
        $(document).bind('keydown.d',function (evt){
                    if(self.checkMode("review") || self.checkMode("image")) {
                        self.doClick(self.context(),".deleteButton");
                    }
                }
        );
        $(document).bind('keydown.t',function (evt){
                    if(self.checkMode("review")) {
                        self.doClick(self.context(),".printReviewLayerButton");
                    }
                }
        );

    },
    checkMode : function(mode) {

        console.log("prefix="+window.app.status.currentImage.prefix);
        return window.app.status.currentImage!= null && window.app.status.currentImage.prefix == mode;


    },
    doClick : function(context,button) {
        var elem = $(context).find(button);
        var disabled = elem.attr("disabled");
        if(disabled=="disabled") {
            //button is locked
        } else {
            elem.click();
        }
    }
};
