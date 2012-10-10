var AccountController = Backbone.Router.extend({
    initialized:false,
    routes:{
        "account":"account"
    },
    account:function () {
        if (!this.initialized) {
            /* init upload */
            this.render();
            this.initialized = true;
        }
        window.app.view.showComponent(window.app.view.components.account);
    },
    render:function () {

        new AccountDetails({
            el:"#account",
            model:window.app.status.user.model
        }).render();
    }
});

