var BrowserSupport = {
    CHARTS: "Charts cannot be display!",
    isTooOld : function() {
        return (bowser.msie && bowser.version <= 8)
    },

    addMessage : function(element, message) {
        require([
            "text!application/templates/utils/BrowserTooOld.tpl.html"
        ],
            function (tpl) {
                element.empty();
                element.append(_.template(tpl,{message:message}));
         });
    }

};






