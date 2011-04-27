
var ApplicationController = Backbone.Controller.extend({

    models : {},
    controllers : {},
    view : null,
    status : {},

    routes: {
        "explorer"  :   "explorer",
        "upload"    :   "upload",
        "admin"     :   "admin",
        "warehouse" :   "warehouse"
    },

    startup : function () {
        //init models

        this.models.images = new ImageCollection({project:undefined});
        this.models.users = new UserCollection({project:undefined});
        this.models.terms = new TermCollection();
        this.models.ontologies = new OntologyCollection();
        this.models.projects = new ProjectCollection({user:undefined});
        _.each(this.models, function(model){
            model.fetch();
        });

        //top component
        this.view = new ApplicationView({
            el: $('#app')
        }).render();

        this.status.currentProject =  25; //TMP

        this.warehouse(); //go to the warehouse when logged in

        Backbone.history.start();
    },

    initialize : function () {
        //init controllers
        this.controllers.project      = new ProjectController();
        this.controllers.image        = new ImageController();
        this.controllers.browse       = new BrowseController();
        this.controllers.term         = new TermController();
        this.controllers.ontology     = new OntologyController()
        this.controllers.auth         = new AuthController();
        this.controllers.command      = new CommandController();

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
        this.controllers.image.image(0);
        this.view.showComponent(this.view.components.warehouse);
    }



});