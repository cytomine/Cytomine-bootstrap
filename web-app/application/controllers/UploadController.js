var UploadController = Backbone.Router.extend({
    initialized:false,
    routes:{
        "upload":"upload"
    },
    upload:function () {
        if (!this.initialized) {
            /* init upload */
            this.initForm();
            this.initialized = true;
        }
        window.app.view.showComponent(window.app.view.components.upload);
    },
    initForm:function () {
        new UploadFormView({
            el:$("#upload")
        }).render();
    }
});

