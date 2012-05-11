
var ApplicationController = Backbone.Router.extend({

    models : {},
    controllers : {},
    view : null,
    status : {},

    routes: {
        ""          :   "initialRoute",
        "explorer"  :   "explorer",
        //"upload"    :   "upload",
        "admin"     :   "admin"
    },

    startup : function () {
        var self = this;
        self.dataTablesBootstrap();
        self.view = new ApplicationView({
            el: $('#content')
        });
        var loadingView = new LoadingDialogView();
        //loadingView.render();
        //init collections
        self.models.images = new ImageCollection({project:undefined});
        self.models.imagesinstance = new ImageInstanceCollection({project:undefined});
        self.models.slides = new SlideCollection({project:undefined});
        self.models.users = new UserCollection({project:undefined});
        self.models.terms = new TermCollection({project:undefined});
        self.models.ontologies = new OntologyCollection();
        self.models.disciplines = new DisciplineCollection();
        self.models.projects = new ProjectCollection({user:undefined});
        self.models.annotations = new AnnotationCollection({});

        //fetch models
        var modelsToPreload = [self.models.users];
        if (_.size(modelsToPreload) == 0) {
            self.modelFetched(0, 0, loadingView);
        } else {
            loadingView.initProgressBar();
            var nbModelFetched = 0;
            _.each(modelsToPreload, function(model){
                model.fetch({
                    success :  function(model, response) {
                        self.modelFetched(++nbModelFetched, _.size(modelsToPreload), loadingView);
                    }
                });
            });
        }
    },

    modelFetched : function (cpt, expected, loadingView) {
        var step = 100 / expected;
        var value = cpt * step;
        //loadingView.progress(value);
        if (cpt == expected) {
            this.view.render(this.start);
        }
    },
    start : function () {
        window.app.controllers.image        = new ImageController();
        window.app.controllers.project      = new ProjectController();
        window.app.controllers.dashboard    = new DashboardController();
        window.app.controllers.browse       = new ExplorerController();
        window.app.controllers.ontology     = new OntologyController();
        window.app.controllers.upload       = new UploadController();
        window.app.controllers.command      = new CommandController();
        window.app.controllers.annotation   = new AnnotationController();
        window.app.controllers.activity     = new ActivityController();
        window.app.controllers.account      = new AccountController();
        //window.app.controllers.admin        = new AdminController();
        //Start the history
        window.app.view.initPreferences();
        window.app.view.initUserMenu();

        Backbone.history.start();
    },
    initialize : function () {
        var self = this;


        //init controllers
        self.controllers.auth         = new AuthController();

        require(["text!application/templates/ServerDownDialog.tpl.html"], function (serverDownTpl) {
            var serverDown = function(status) {
                window.app.view.clearIntervals();
                $("#content").fadeOut('slow').empty();
                $(".navbar").remove();
                new ConfirmDialogView({
                    el:'#dialogs',
                    template : serverDownTpl,
                    dialogAttr : {
                        dialogID : "#server-down"
                    }
                }).render();
            }

            var successcallback =  function (data) {
                self.status.version = data.version;
                self.status.serverURL = data.serverURL;
                if (data.authenticated) {
                    new UserModel({id : data.user}).fetch({
                        success : function (model, response) {
                            self.status.user = {
                                id : data.user,
                                authenticated : data.authenticated,
                                model : model
                            }
                            self.startup();
                        }
                    });

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


            self.status = new Status(pingURL, serverDown,
                    function () { //TO DO: HANDLE WHEN USER IS DISCONNECTED BY SERVER
                    }, 10000);

        });




    },

    explorer: function() {
        this.view.showComponent(this.view.components.explorer);
    },

    /*upload: function() {
     this.view.showComponent(this.view.components.upload);
     },*/

    admin: function() {
        this.view.showComponent(this.view.components.admin);
    },

    warehouse : function () {
        this.view.showComponent(this.view.components.warehouse);
    },

    initialRoute: function() {
        this.controllers.project.project();
    },
    convertLongToDate : function (longDate) {
          var createdDate = new Date();
          createdDate.setTime(longDate);

          //date format
          var year = createdDate.getFullYear();
          var month = (createdDate.getMonth()+1)  < 10 ? "0"+(createdDate.getMonth()+1) : (createdDate.getMonth()+1);
          var day =  (createdDate.getDate())  < 10 ? "0"+(createdDate.getDate()) : (createdDate.getDate());

          var hour =  (createdDate.getHours())  < 10 ? "0"+(createdDate.getHours()) : (createdDate.getHours());
          var min =  (createdDate.getMinutes())  < 10 ? "0"+(createdDate.getMinutes()) : (createdDate.getMinutes());

          var dateStr = year + "-" + month +"-" + day + " " + hour + "h" + min;
        return dateStr;
    },
    replaceVariable:function (value) {
        var self = this;
        var result =value;
        result=result.replace("$currentProjectCreationDate$",window.app.status.currentProjectModel.get('created'));
        result=result.replace("$currentProject$",window.app.status.currentProject);
        result=result.replace("$cytomineHost$",window.location.protocol + "//" + window.location.host);
        result=result.replace("$currentDate$",new Date().getTime());
        result=result.replace("$currentOntology$",window.app.status.currentProjectModel.get('ontology'));

        return result;
    },
    dataTablesBootstrap : function () {
        /* Default class modification */
        /* Default class modification */
        $.extend( $.fn.dataTableExt.oStdClasses, {
            "sWrapper": "dataTables_wrapper form-inline"
        } );

        /* API method to get paging information */
        $.fn.dataTableExt.oApi.fnPagingInfo = function ( oSettings )
        {
            return {
                "iStart":         oSettings._iDisplayStart,
                "iEnd":           oSettings.fnDisplayEnd(),
                "iLength":        oSettings._iDisplayLength,
                "iTotal":         oSettings.fnRecordsTotal(),
                "iFilteredTotal": oSettings.fnRecordsDisplay(),
                "iPage":          Math.ceil( oSettings._iDisplayStart / oSettings._iDisplayLength ),
                "iTotalPages":    Math.ceil( oSettings.fnRecordsDisplay() / oSettings._iDisplayLength )
            };
        }

        /* Bootstrap style pagination control */
        $.extend( $.fn.dataTableExt.oPagination, {
            "bootstrap": {
                "fnInit": function( oSettings, nPaging, fnDraw ) {
                    var oLang = oSettings.oLanguage.oPaginate;
                    var fnClickHandler = function ( e ) {
                        e.preventDefault();
                        if ( oSettings.oApi._fnPageChange(oSettings, e.data.action) ) {
                            fnDraw( oSettings );
                        }
                    };

                    $(nPaging).addClass('pagination').append(
                            '<ul>'+
                                    '<li class="prev disabled"><a href="#">&larr; '+oLang.sPrevious+'</a></li>'+
                                    '<li class="next disabled"><a href="#">'+oLang.sNext+' &rarr; </a></li>'+
                                    '</ul>'
                    );
                    var els = $('a', nPaging);
                    $(els[0]).bind( 'click.DT', { action: "previous" }, fnClickHandler );
                    $(els[1]).bind( 'click.DT', { action: "next" }, fnClickHandler );
                },

                "fnUpdate": function ( oSettings, fnDraw ) {
                    var iListLength = 5;
                    var oPaging = oSettings.oInstance.fnPagingInfo();
                    var an = oSettings.aanFeatures.p;
                    var i, j, sClass, iStart, iEnd, iHalf=Math.floor(iListLength/2);

                    if ( oPaging.iTotalPages < iListLength) {
                        iStart = 1;
                        iEnd = oPaging.iTotalPages;
                    }
                    else if ( oPaging.iPage <= iHalf ) {
                        iStart = 1;
                        iEnd = iListLength;
                    } else if ( oPaging.iPage >= (oPaging.iTotalPages-iHalf) ) {
                        iStart = oPaging.iTotalPages - iListLength + 1;
                        iEnd = oPaging.iTotalPages;
                    } else {
                        iStart = oPaging.iPage - iHalf + 1;
                        iEnd = iStart + iListLength - 1;
                    }

                    for ( i=0, iLen=an.length ; i<iLen ; i++ ) {
                        // Remove the middle elements
                        $('li:gt(0)', an[i]).filter(':not(:last)').remove();

                        // Add the new list items and their event handlers
                        for ( j=iStart ; j<=iEnd ; j++ ) {
                            sClass = (j==oPaging.iPage+1) ? 'class="active"' : '';
                            $('<li '+sClass+'><a href="#">'+j+'</a></li>')
                                    .insertBefore( $('li:last', an[i])[0] )
                                    .bind('click', function (e) {
                                        e.preventDefault();
                                        oSettings._iDisplayStart = (parseInt($('a', this).text(),10)-1) * oPaging.iLength;
                                        fnDraw( oSettings );
                                    } );
                        }

                        // Add / remove disabled classes from the static elements
                        if ( oPaging.iPage === 0 ) {
                            $('li:first', an[i]).addClass('disabled');
                        } else {
                            $('li:first', an[i]).removeClass('disabled');
                        }

                        if ( oPaging.iPage === oPaging.iTotalPages-1 || oPaging.iTotalPages === 0 ) {
                            $('li:last', an[i]).addClass('disabled');
                        } else {
                            $('li:last', an[i]).removeClass('disabled');
                        }
                    }
                }
            }
        } );
    }


});