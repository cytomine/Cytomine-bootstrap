var HotKeys = {
    context : function() {
        var idImage = window.app.status.currentImage.idImage;
        var idProject = window.app.status.currentProject;
        return "div#tabs-"+window.app.status.currentImage.prefix+"-"+idProject+"-"+idImage+"-";
    },
    isBrowsingImage : function() {
        var hash = window.location.hash.substring(1);
        return hash.indexOf("tabs-review") != -1 && hash.indexOf("tabs-image") != 1;
    },
    initHotKeys : function() {
        var self = this;
        var target = $(document);
        target.bind('keydown.b',function (evt){
                if (!self.isBrowsingImage()) return;
                if(self.checkMode("review") || self.checkMode("image")) {
                    self.doClick(self.context(),".selectButton");
                }
            }
        );
        target.bind('keydown.a',function (evt){
                if (!self.isBrowsingImage()) return;
                if(self.checkMode("review")) {
                    self.doClick(self.context(),".acceptButton");
                }
            }
        );
        target.bind('keydown.r',function (evt){
                if (!self.isBrowsingImage()) return;
                if(self.checkMode("review")) {
                    self.doClick(self.context(),".rejectButton");
                }
            }
        );
        target.bind('keydown.n',function (evt){
                if (!self.isBrowsingImage()) return;
                if(self.checkMode("review") || self.checkMode("image")) {
                    self.doClick(self.context(),".nextImage");
                }
            }
        );
        target.bind('keydown.p',function (evt){
                if (!self.isBrowsingImage()) return;
                if(self.checkMode("review") || self.checkMode("image")) {
                    self.doClick(self.context(),".previousImage");
                }
            }
        );

        target.bind('keydown.s',function (evt){
                if (!self.isBrowsingImage()) return;
                if(self.checkMode("review") || self.checkMode("image")) {
                    self.doClick(self.context(),".selectButton");
                }
            }
        );
        target.bind('keydown.f',function (evt){
                if (!self.isBrowsingImage()) return;
                if(self.checkMode("review") || self.checkMode("image")) {
                    self.doClick(self.context(),".freehandButton");
                }
            }
        );
        target.bind('keydown.w',function (evt){
                if (!self.isBrowsingImage()) return;
                if(self.checkMode("review") || self.checkMode("image")) {
                    self.doClick(self.context(),".magicWandButton");

                }
            }
        );
        target.bind('keydown.e',function (evt){
                if (!self.isBrowsingImage()) return;
                if(self.checkMode("review") || self.checkMode("image")) {
                    self.doClick(self.context(),".editButton");
                }
            }
        );
        target.bind('keydown.c',function (evt){
                if (!self.isBrowsingImage()) return;
                if(self.checkMode("review") || self.checkMode("image")) {
                    self.doClick(self.context(),".cutButton");
                }
            }
        );
        target.bind('keydown.j',function (evt){
                if (!self.isBrowsingImage()) return;
                if(self.checkMode("review") || self.checkMode("image")) {
                    self.doClick(self.context(),".joinButton");
                }
            }
        );
        target.bind('keydown.d',function (evt){
                if (!self.isBrowsingImage()) return;
                if(self.checkMode("review") || self.checkMode("image")) {
                    self.doClick(self.context(),".deleteButton");
                }
            }
        );
        target.bind('keydown.t',function (evt){
                if (!self.isBrowsingImage()) return;
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
