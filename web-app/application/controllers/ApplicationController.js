var ApplicationController = Backbone.Router.extend({

    models: {},
    controllers: {},
    view: null,
    status: {},

    routes: {
        "": "initialRoute",
        "explorer": "explorer",
        "admin": "admin"
    },

    startup: function () {
        var self = this;

        self.dataTablesBootstrap();

        HotKeys.initHotKeys();

        self.view = new ApplicationView({
            el: $('#content')
        });

        //init collections
        self.models.images = new ImageCollection({project: undefined});
        self.models.imagesinstance = new ImageInstanceCollection({project: undefined});
        self.models.slides = new SlideCollection({project: undefined});
        self.models.terms = new TermCollection({project: undefined});
        self.models.ontologies = new OntologyCollection();
        self.models.ontologiesLigth = new OntologyCollection({light: true});
        self.models.disciplines = new DisciplineCollection();
        self.models.projects = new ProjectCollection({user: undefined});
        self.models.annotations = new AnnotationCollection({});

        //"hashtable" with custom collection (useful in software page)
        self.models.currentCollection = {};

        //fetch models
        var modelsToPreload = [];
        if (_.size(modelsToPreload) == 0) {
            self.modelFetched(0, 0);
        } else {
            var nbModelFetched = 0;
            _.each(modelsToPreload, function (model) {
                model.fetch({
                    success: function (model, response) {
                        self.modelFetched(++nbModelFetched, _.size(modelsToPreload));
                    }
                });
            });
        }
    },

    modelFetched: function (cpt, expected) {
        if (cpt == expected) {
            this.view.render(this.start);
        }
    },


    start: function () {
        window.app.controllers.image = new ImageController();
        window.app.controllers.project = new ProjectController();
        window.app.controllers.dashboard = new DashboardController();
        window.app.controllers.browse = new ExplorerController();
        window.app.controllers.ontology = new OntologyController();
        window.app.controllers.upload = new UploadController();
        window.app.controllers.command = new CommandController();
        window.app.controllers.annotation = new AnnotationController();
        window.app.controllers.activity = new ActivityController();
        window.app.controllers.account = new AccountController();
        window.app.controllers.phono = new PhonoController();
        //window.app.controllers.admin        = new AdminController();
        //Start the history
        window.app.view.initPreferences();
        window.app.view.initUserMenu();

        Backbone.history.start();
    },
    initialize: function () {
        var self = this;


        //init controllers
        self.controllers.auth = new AuthController();

        require(["text!application/templates/ServerDownDialog.tpl.html"], function (serverDownTpl) {
            var serverDown = function (status) {
                window.app.view.clearIntervals();
                $("#content").fadeOut('slow').empty();
                $(".navbar").remove();
                new ConfirmDialogView({
                    el: '#dialogs',
                    template: serverDownTpl,
                    dialogAttr: {
                        dialogID: "#server-down"
                    }
                }).render();
            }

            var successcallback = function (data) {
                console.log("Launch app!");
                self.status.version = data.get('version');
                self.status.serverURL = data.get('serverURL');
                if (data.get('authenticated')) {
                    new UserModel({id: data.get('user')}).fetch({
                        success: function (model, response) {
                            self.status.user = {
                                id: data.get('user'),
                                authenticated: data.get('authenticated'),
                                model: model,
                                filenameVisible : true
                            }
                            self.startup();
                        }
                    });

                } else {
                    self.controllers.auth.login();
                }
            }

            var pingURL = 'server/ping';
//            $.ajax({
//                url: pingURL,
//                type: 'GET',
//                contentType:'application/json',
//                data: "{test:hello}",
//                success : successcallback
//            });

            var project = window.app.status.currentProject
            if (project == undefined) {
                project = "null";
            }
            new PingModel({project: project}).save({}, {
                    success: function (model, response) {
                        console.log("Ping success first!");
                        successcallback(model)
                    },
                    error: function (model, response) {
                        console.log("Ping error!");
                    }
                }
            );


            self.status = new Status(pingURL, serverDown,
                function () { //TO DO: HANDLE WHEN USER IS DISCONNECTED BY SERVER
                }, 20000);

        });


    },

    explorer: function () {
        this.view.showComponent(this.view.components.explorer);
    },

    /*upload: function() {
     this.view.showComponent(this.view.components.upload);
     },*/

    admin: function () {
        this.view.showComponent(this.view.components.admin);
    },

    warehouse: function () {
        this.view.showComponent(this.view.components.warehouse);
    },

    initialRoute: function () {
        this.navigate("#project", true);
    },
    convertLongToDate: function (longDate) {
        var createdDate = new Date();
        createdDate.setTime(longDate);

        //date format
        var year = createdDate.getFullYear();
        var month = (createdDate.getMonth() + 1) < 10 ? "0" + (createdDate.getMonth() + 1) : (createdDate.getMonth() + 1);
        var day = (createdDate.getDate()) < 10 ? "0" + (createdDate.getDate()) : (createdDate.getDate());

        var hour = (createdDate.getHours()) < 10 ? "0" + (createdDate.getHours()) : (createdDate.getHours());
        var min = (createdDate.getMinutes()) < 10 ? "0" + (createdDate.getMinutes()) : (createdDate.getMinutes());

        return year + "-" + month + "-" + day + " " + hour + "h" + min;
    },
    convertLongToDateShort: function (longDate) {
        var createdDate = new Date();
        createdDate.setTime(longDate);

        //date format
        var year = createdDate.getFullYear();
        var month = (createdDate.getMonth() + 1) < 10 ? "0" + (createdDate.getMonth() + 1) : (createdDate.getMonth() + 1);
        var day = (createdDate.getDate()) < 10 ? "0" + (createdDate.getDate()) : (createdDate.getDate());
        return year + "-" + month + "-" + day;
    },
    minString: function (string, maxFirstCar, maxLastCar) {
        if (string.length <= (maxFirstCar + maxLastCar + 5)) {
            return  string;
        }
        var start = string.substr(0, maxFirstCar);
        var end = string.substr((string.length - maxLastCar), maxLastCar);

        return start + "[...]" + end;
    },
    replaceVariable: function (value) {
        var result = value;
        result = result.replace("$currentProjectCreationDate$", window.app.status.currentProjectModel.get('created'));
        result = result.replace("$currentProject$", window.app.status.currentProject);
        result = result.replace("$cytomineHost$", window.location.protocol + "//" + window.location.host);
        result = result.replace("$currentDate$", new Date().getTime());
        result = result.replace("$currentOntology$", window.app.status.currentProjectModel.get('ontology'));
        return result;
    },
    retrieveTerm: function (ontology) {
        var self = this;
        return new TermCollection(self.retrieveChildren(ontology.attributes));
    },
    retrieveChildren: function (parent) {
        var self = this;
        if (parent['children'].length == 0) {
            return [];
        }
        var children = [];
        _.each(parent['children'], function (elem) {
            children.push(elem);
            children = _.union(children, self.retrieveChildren(elem));
        });
        return children;
    },
    isCollectionUndefinedOrEmpty: function (collection) {
        console.log(collection);
        return (collection == undefined || (collection == 1 && collection.at(0).id == undefined))
    },
    getFromCache: function (key) {
        return  this.models.currentCollection[key];
    },
    addToCache: function (key, value) {
        this.models.currentCollection[key] = value;
    },
    clearCache: function () {
        this.models.currentCollection = {};
    },
    addOrReplaceEvent: function (element, eventType, fCallback) {
        if (!element || !element.data('events') || !element.data('events')[eventType] || !fCallback) {
            return false;
        }

        for (runner in element.data('events')[eventType]) {
            if (element.data('events')[eventType][runner].handler == fCallback) {
                return true;
            }

        }

        return false;
    },
    dataTablesBootstrap: function () {
        /* Default class modification */
        $.extend($.fn.dataTableExt.oStdClasses, {
            "sWrapper": "dataTables_wrapper form-inline"
        });

        /* API method to get paging information */
        $.fn.dataTableExt.oApi.fnPagingInfo = function (oSettings) {
            return {
                "iStart": oSettings._iDisplayStart,
                "iEnd": oSettings.fnDisplayEnd(),
                "iLength": oSettings._iDisplayLength,
                "iTotal": oSettings.fnRecordsTotal(),
                "iFilteredTotal": oSettings.fnRecordsDisplay(),
                "iPage": Math.ceil(oSettings._iDisplayStart / oSettings._iDisplayLength),
                "iTotalPages": Math.ceil(oSettings.fnRecordsDisplay() / oSettings._iDisplayLength)
            };
        }

        /* Bootstrap style pagination control */
        $.extend($.fn.dataTableExt.oPagination, {
            "bootstrap": {
                "fnInit": function (oSettings, nPaging, fnDraw) {
                    var oLang = oSettings.oLanguage.oPaginate;
                    var fnClickHandler = function (e) {
                        e.preventDefault();
                        if (oSettings.oApi._fnPageChange(oSettings, e.data.action)) {
                            fnDraw(oSettings);
                        }
                    };

                    $(nPaging).addClass('pagination').append(
                        '<ul>' +
                            '<li class="prev disabled"><a href="#">&larr; ' + oLang.sPrevious + '</a></li>' +
                            '<li class="next disabled"><a href="#">' + oLang.sNext + ' &rarr; </a></li>' +
                            '</ul>'
                    );
                    var els = $('a', nPaging);
                    $(els[0]).bind('click.DT', { action: "previous" }, fnClickHandler);
                    $(els[1]).bind('click.DT', { action: "next" }, fnClickHandler);
                },

                "fnUpdate": function (oSettings, fnDraw) {
                    var iListLength = 5;
                    var oPaging = oSettings.oInstance.fnPagingInfo();
                    var an = oSettings.aanFeatures.p;
                    var i, j, sClass, iStart, iEnd, iHalf = Math.floor(iListLength / 2);

                    if (oPaging.iTotalPages < iListLength) {
                        iStart = 1;
                        iEnd = oPaging.iTotalPages;
                    }
                    else if (oPaging.iPage <= iHalf) {
                        iStart = 1;
                        iEnd = iListLength;
                    } else if (oPaging.iPage >= (oPaging.iTotalPages - iHalf)) {
                        iStart = oPaging.iTotalPages - iListLength + 1;
                        iEnd = oPaging.iTotalPages;
                    } else {
                        iStart = oPaging.iPage - iHalf + 1;
                        iEnd = iStart + iListLength - 1;
                    }

                    for (i = 0, iLen = an.length; i < iLen; i++) {
                        // Remove the middle elements
                        $('li:gt(0)', an[i]).filter(':not(:last)').remove();

                        // Add the new list items and their event handlers
                        for (j = iStart; j <= iEnd; j++) {
                            sClass = (j == oPaging.iPage + 1) ? 'class="active"' : '';
                            $('<li ' + sClass + '><a href="#">' + j + '</a></li>')
                                .insertBefore($('li:last', an[i])[0])
                                .bind('click', function (e) {
                                    e.preventDefault();
                                    oSettings._iDisplayStart = (parseInt($('a', this).text(), 10) - 1) * oPaging.iLength;
                                    fnDraw(oSettings);
                                });
                        }

                        // Add / remove disabled classes from the static elements
                        if (oPaging.iPage === 0) {
                            $('li:first', an[i]).addClass('disabled');
                        } else {
                            $('li:first', an[i]).removeClass('disabled');
                        }

                        if (oPaging.iPage === oPaging.iTotalPages - 1 || oPaging.iTotalPages === 0) {
                            $('li:last', an[i]).addClass('disabled');
                        } else {
                            $('li:last', an[i]).removeClass('disabled');
                        }
                    }
                }
            }
        });
    }


});
