
var ApplicationController = Backbone.Controller.extend({

    models : {},
    controllers : {},
    view : null,
    status : {},

    routes: {
        ""          :   "initialRoute",
        "explorer"  :   "explorer",
        "upload"    :   "upload",
        "admin"     :   "admin",
        "warehouse" :   "warehouse"
    },

    startup : function () {
        var dialog = new ConfirmDialogView({
            el:'#dialogs',
            //template : _.template($('#login-dialog-tpl').html()),
            template : ich.loadingdialogtpl({}, true),
            dialogAttr : {
                dialogID : "#loading-dialog",
                width : 475,
                height : 375,
                buttons: {

                },
                close :function (event) {

                }
            }
        }).render();
        //init models
        var nbModelFetched = 0;
        var self = this;
        $("#progress").show();
        $("#login-progressbar" ).progressbar({
            value: 0
        });

        this.models.images = new ImageCollection({project:undefined});
        this.models.users = new UserCollection({project:undefined});
        this.models.terms = new TermCollection();
        this.models.ontologies = new OntologyCollection();
        this.models.projects = new ProjectCollection({user:undefined});
        _.each(this.models, function(model){
            model.fetch({
                success :  function(model, response) {
                    self.modelFetched(++nbModelFetched, _.size(self.models));
                }
            });
        });

    },

    modelFetched : function (cpt, expected) {
        var step = 100 / expected;
        var value = cpt * step;
        $("#login-progressbar" ).progressbar({
            value: value
        });
        if (cpt == expected) {
            $("#loading-dialog").dialog("close");
            this.view.render();
            this.controllers.image        = new ImageController();
            this.controllers.project      = new ProjectController();
            this.controllers.dashboard    = new DashboardController();
            this.controllers.browse       = new BrowseController();
            this.controllers.term         = new TermController();
            this.controllers.ontology     = new OntologyController()
            this.controllers.command      = new CommandController();
            Backbone.history.start();
        }
    },

    initialize : function () {

        this.view = new ApplicationView({
            el: $('#app')
        });

        //init controllers
        this.controllers.auth         = new AuthController();


        var self = this;
        var serverDown = function(status) {
            $("#app").fadeOut('slow');
            var dialog = new ConfirmDialogView({
                el:'#dialogs',
                template : ich.serverdowntpl({}, true),
                dialogAttr : {
                    dialogID : "#server-down"
                }
            }).render();
        }

        var successcallback =  function (data) {
            self.status.user = {
                id : data.user,
                authenticated : data.authenticated
            }
            if (data.authenticated) {
                self.startup();
            } else {
                self.controllers.auth.login();
            }
        }

        var pingURL = 'server/ping';
        $.ajax({
            url: pingURL,
            type: 'GET',
            success : successcallback
        });

        this.status = new Status(pingURL, serverDown,
                function () { //TO DO: HANDLE WHEN USER IS DISCONNECTED BY SERVER
                }, 10000);
    },

    explorer: function() {
        this.view.showComponent(this.view.components.explorer);
    },

    upload: function() {
        this.view.showComponent(this.view.components.upload);
    },

    admin: function() {
        this.view.showComponent(this.view.components.admin);
    },

    warehouse : function () {
        this.view.showComponent(this.view.components.warehouse);
    },

    initialRoute: function() {
        this.controllers.project.project();

    }



});