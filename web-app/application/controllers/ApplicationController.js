
var ApplicationController = Backbone.Controller.extend({

    models : {},
    controllers : {},
    view : null,
    status : {},

    routes: {
        "logout"    :   "logout",
        "explorer"  :   "explorer",
        "admin"     :   "admin",
        "warehouse" :   "warehouse"
    },

    startup : function () {
        //init models
        this.models.images = new ImageCollection();
        this.models.users = new UserCollection();
        this.models.terms = new TermCollection();
        this.models.ontologies = new OntologyCollection();
        this.models.projects = new ProjectCollection();
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
            var dialog = new ConfirmDialogView({
                el:'#dialogs',
                //template : _.template($('#server-down-tpl').html()),
                template : ich.serverdowntpl({}, true),
                dialogAttr : {
                    dialogID : "#server-down",
                    buttons: {
                        "Ok :(": function() {
                            $(this).dialog("close");
                        }
                    },
                    close :function (event) {
                        $(this).remove();
                    }
                }
            });
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

    admin: function() {
        this.view.showComponent(this.view.components.admin);
    },

    warehouse : function () {
        this.controllers.image.image(0);
        this.view.showComponent(this.view.components.warehouse);
    }



});