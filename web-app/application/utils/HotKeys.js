var HotKeys = {
    initHotKeys : function() {
        console.log("initHotKeys");
        var self = this;

        $(document).bind('keydown.a',function (evt){
                    console.log("press a");
                    if(self.checkMode("review")) {
                        self.doClick("div"+window.location.hash.toString(),".acceptButton");
                    }
                }
        );
        $(document).bind('keydown.r',function (evt){
                    if(self.checkMode("review")) {
                        self.doClick("div"+window.location.hash.toString(),".rejectButton");
                    }
                }
        );
        $(document).bind('keydown.n',function (evt){
                    if(self.checkMode("review") || self.checkMode("image")) {
                        self.doClick("div"+window.location.hash.toString(),".nextImage");
                    }
                }
        );
        $(document).bind('keydown.p',function (evt){
                    if(self.checkMode("review") || self.checkMode("image")) {
                        self.doClick("div"+window.location.hash.toString(),".previousImage");
                    }
                }
        );

        $(document).bind('keydown.s',function (evt){
                    if(self.checkMode("review") || self.checkMode("image")) {
                        self.doClick("div"+window.location.hash.toString(),".selectButton");
                    }
                }
        );
        $(document).bind('keydown.f',function (evt){
                    if(self.checkMode("review") || self.checkMode("image")) {
                        self.doClick("div"+window.location.hash.toString(),".freehandButton");
                    }
                }
        );
        $(document).bind('keydown.w',function (evt){
                    if(self.checkMode("review") || self.checkMode("image")) {
                        self.doClick("div"+window.location.hash.toString(),".magicWandButton");
                    }
                }
        );
        $(document).bind('keydown.e',function (evt){
                    if(self.checkMode("review") || self.checkMode("image")) {
                        self.doClick("div"+window.location.hash.toString(),".editButton");
                    }
                }
        );
        $(document).bind('keydown.c',function (evt){
                    if(self.checkMode("review") || self.checkMode("image")) {
                        self.doClick("div"+window.location.hash.toString(),".cutButton");
                    }
                }
        );
        $(document).bind('keydown.j',function (evt){
                    if(self.checkMode("review") || self.checkMode("image")) {
                        self.doClick("div"+window.location.hash.toString(),".joinButton");
                    }
                }
        );
        $(document).bind('keydown.d',function (evt){
                    if(self.checkMode("review") || self.checkMode("image")) {
                        self.doClick("div"+window.location.hash.toString(),".deleteButton");
                    }
                }
        );
        $(document).bind('keydown.t',function (evt){
                    if(self.checkMode("review")) {
                        self.doClick("div"+window.location.hash.toString(),".printReviewLayerButton");
                    }
                }
        );

    },
    checkMode : function(mode) {
        var loc = window.location.hash.toString();
        if(loc.indexOf("#tabs-review")!=-1) {
            return "review"==mode;
        }
        if(loc.indexOf("#tabs-image")!=-1) {
            return "image"==mode;
        }

        return "error";

    },
    doClick : function(context,button) {
        var elem = $(context).find(button);
        var disabled = elem.attr("disabled");
        console.log("***************");
        console.log(context);
        console.log(button);
        console.log($(context).length);
        console.log(elem.length);
        console.log(disabled);

        if(disabled=="disabled") {
            //button is locked
        } else {
            elem.click();
        }
    }
};
