/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 26/04/11
 * Time: 14:38
 * To change this template use File | Settings | File Templates.
 */
var ProjectDashboardView = Backbone.View.extend({
    tagName : "div",
    projectElem : "#projectdashboardinfo",  //div with project info
    tabsAnnotation : null,
    initialize: function(options) {
        this.container = options.container;
        _.bindAll(this, 'render');
    },
    events: {
    },
    /**
     * Print all information for this project
     */
    render: function() {
        this.printProjectInfo();
        return this;
    },
    /**
     * Refresh all information for this project
     */
    refresh : function() {
        var self = this;

        var projectModel = new ProjectModel({id : self.model.id});
        var projectCallback = function(model, response) {
            console.log(model);
            self.model = model;

            self.fetchProjectInfo();
            self.fetchAnnotations();
            self.fetchCommands();
            self.fetchStats();

        }

        projectModel.fetch({
            success : function(model, response) {
                projectCallback(model, response); //fonctionne mais très bourrin de tout refaire à chaque fois...
            }
        });

    },
    /**
     * Init annotation tabs
     */
    initTabs : function(){
        var self = this;

        var idOntology = self.model.get('ontology');

        new TermCollection({idOntology:idOntology}).fetch({
            success : function (collection, response) {

                //add "All annotation from all term" tab
                self.addTermToTab("all","All");

                collection.each(function(term) {
                    //add x term tab
                    self.addTermToTab(term.get("id"),term.get("name"));
                });

                if(self.tabsAnnotation==null)
                    self.tabsAnnotation = $("#tabsannotation").tabs();
                self.fetchAnnotations();
            }});
    },
    /**
     * Add the the tab with term info
     * @param id  term id
     * @param name term name
     */
    addTermToTab : function(id, name) {
        var termelem = ich.termtitletabtpl({name:name,id:id});
        $("#ultabsannotation").append(termelem);
        var contenttermelem = ich.termdivtabtpl({name:name,id:id});
        $("#listtabannotation").append(contenttermelem);
    },
    /**
     * Load annotations on annotation tabs
     * -'All' tab: all annotation for this project
     * -'X' tab: all annotation for this project and the term X
     */
    fetchAnnotations : function () {
        console.log("ProjectDashboardView: fetchAnnotations");

        var self = this;

        //init panel for all annotation (with or without term
        new AnnotationCollection({project:self.model.id}).fetch({
            success : function (collection, response) {
                $("#tabsterm-all").empty();

                var view = new AnnotationView({
                    page : undefined,
                    model : collection,
                    el:$("#tabsterm-all"),
                    container : window.app.view.components.warehouse
                }).render();


            }
        });

        //init specific panel for each term
        new TermCollection({idOntology:self.model.get('ontology')}).fetch({
            success : function (collection, response) {
                //init specific panel
                collection.each(function(term) {
                    $("#tabsterm-"+term.get("id")).empty();
                    new AnnotationCollection({term:term.get("id"),project:self.model.id}).fetch({
                        success : function (collection, response) {
                            var view = new AnnotationView({
                                page : undefined,
                                model : collection,
                                el:$("#tabsterm-"+term.get("id")),
                                container : window.app.view.components.warehouse
                            }).render();

                        }});

                });
            }
        });

    },

    fetchImages : function() {
        console.log("ProjectDashboardView: fetchImages");
        var self = this;
        new ImageCollection({project:self.model.get('id')}).fetch({
            success : function (collection, response) {
                new ImageView({
                    page : undefined,
                    model : collection,
                    el:$("#projectImageList"),
                    container : window.app.view.components.warehouse
                }).render();
            }});
    },
    fetchStats : function () {
        console.log("ProjectDashboardView: fetchStats");
        var self = this;
        var statsCollection = new StatsCollection({project:self.model.get('id')});
        var statsCallback = function(collection, response) {

            console.log(collection);
            $("#plotterms").empty();

            var empty = true;
            var arrayData = new Array();
            var arrayColor = new Array();

            collection.each(function(stat) {
                var subArray = new Array(stat.get('key'),stat.get('value'));
                arrayData.push(subArray);
                arrayColor.push(stat.get('color'));
                empty = empty && stat.get('value')=="0";
            });
            console.log("empty="+empty);

            //if empty, add "nothing" to the legend with 100%
            if(empty) {
                arrayData.push(new Array("Nothing",100));
                arrayColor.push("d5d5d5");
            }


            $.jqplot('plotterms', [arrayData], {
                seriesColors: arrayColor,
                height: 450,
                width: 450,
                grid: {
                    drawBorder: false,
                    drawGridlines: false,
                    background: '#ffffff',
                    shadow:false
                },
                axesDefaults: {

                },
                seriesDefaults:{
                    renderer:$.jqplot.PieRenderer,
                    rendererOptions: {
                        showDataLabels: true
                    }
                },
                legend: {
                    show: true,
                    location: 'e'
                }

            });



        }

        statsCollection.fetch({
            success : function(model, response) {
                statsCallback(model, response); //fonctionne mais très bourrin de tout refaire à chaque fois...
            }
        });

    },

    fetchProjectInfo : function () {
        var self = this;
        var json = self.model.toJSON();
        var idOntology = json.ontology;

        //Get ontology name
        json.ontology = window.app.models.ontologies.get(idOntology).get('name');

        //Get created/updated date
        var dateCreated = new Date();
        dateCreated.setTime(json.created);
        json.created = dateCreated.toLocaleDateString() + " " + dateCreated.toLocaleTimeString();
        var dateUpdated = new Date();
        dateUpdated.setTime(json.updated);
        json.updated = dateUpdated.toLocaleDateString() + " " + dateUpdated.toLocaleTimeString();

        self.resetElem("#projectInfoName",json.name);
        self.resetElem("#projectInfoOntology",json.ontology);
        self.resetElem("#projectInfoNumberOfSlides",json.numberOfSlides);
        self.resetElem("#projectInfoNumberOfImages",json.numberOfImages);
        self.resetElem("#projectInfoNumberOfAnnotations",json.numberOfAnnotations);
        self.resetElem("#projectInfoCreated",json.created);
        self.resetElem("#projectInfoUpdated",json.updated);

        $("#projectInfoUserList").empty();

        //Get users list
        new UserCollection({project:self.model.get('id')}).fetch({
            success : function (collection, response) {
                collection.each(function(user) {
                    var userelem = ich.userlisttpl({name:user.get("username")});
                    $("#projectInfoUserList").append(userelem);
                });

            }
        });
    },
    fetchCommands : function () {
        console.log("ProjectDashboardView: fetchCommands");
        var self = this;
        var commandCollection = new CommandCollection({project:self.model.get('id'),max:10});

        var commandCallback = function(collection, response) {

            $("#lastactionitem").empty();
            collection.each(function(command) {

                var dateCreated = new Date();
                dateCreated.setTime(command.get('created'));
                var dateStr = dateCreated.toLocaleDateString() + " " + dateCreated.toLocaleTimeString()

                var json = $.parseJSON(command.get("data"));
                var action = ""

                var errorImage = "http://www.bmxforever.net/website/wp-content/uploads/2010/03/Error.jpg";

                if(command.get("class")=="be.cytomine.command.annotation.AddAnnotationCommand")
                {
                    var action = ich.annotationcommandlisttpl({icon:"ui-icon-plus",text:command.get("action"),datestr:dateStr,image:json.cropURL});
                    $("#lastactionitem").append(action);
                    $(action).find("img").hide();
                    $.ajax({
                        url: json.cropURL,
                        success: function(data){$(action).find("img").show();},
                        error: function(XMLHttpRequest, textStatus, errorThrown){ }
                    });
                }
                if(command.get("class")=="be.cytomine.command.annotation.EditAnnotationCommand")
                {
                    var action = ich.annotationcommandlisttpl({icon:"ui-icon-pencil",text:command.get("action"),datestr:dateStr,image:json.cropURL});
                    $("#lastactionitem").append(action);
                    $(action).find("img").hide();
                    $.ajax({
                        url: json.cropURL,
                        success: function(data){$(action).find("img").show();},
                        error: function(XMLHttpRequest, textStatus, errorThrown){}
                    });
                }
                if(command.get("class")=="be.cytomine.command.annotation.DeleteAnnotationCommand")
                {
                    var action = ich.annotationcommandlisttpl({icon:"ui-icon-trash",text:command.get("action"),datestr:dateStr,image:json.cropURL});
                    $("#lastactionitem").append(action);
                    $(action).find("img").hide();
                    $.ajax({
                        url: json.cropURL,
                        success: function(data){$(action).find("img").show();},
                        error: function(XMLHttpRequest, textStatus, errorThrown){}
                    });
                }


                if(command.get("class")=="be.cytomine.command.annotationterm.AddAnnotationTermCommand")
                {

                    var action = ich.annotationtermcommandlisttpl({icon:"ui-icon-plus",text:command.get("action"),datestr:dateStr,image:""});
                    $("#lastactionitem").append(action);

                }
                if(command.get("class")=="be.cytomine.command.annotationterm.EditAnnotationTermCommand")
                {

                    var action = ich.annotationtermcommandlisttpl({icon:"ui-icon-pencil",text:command.get("action"),datestr:dateStr,image:""});
                    $("#lastactionitem").append(action);

                }
                if(command.get("class")=="be.cytomine.command.annotationterm.DeleteAnnotationTermCommand")
                {

                    var action = ich.annotationtermcommandlisttpl({icon:"ui-icon-trash",text:command.get("action"),datestr:dateStr,image:""});
                    $("#lastactionitem").append(action);

                }
            });
        }

        commandCollection.fetch({
            success : function(model, response) {
                commandCallback(model, response); //fonctionne mais très bourrin de tout refaire à chaque fois...
            }
        });

    },
    resetElem : function(elem,txt) {
        console.log("find:"+$(this.el).find(elem).length);
        $(this.el).find(elem).empty();
        $(this.el).find(elem).append(txt);
    },
    printProjectInfo : function(model) {
        console.log("ProjectDashboardView: printProjectInfo");

        var self = this;
        var json = self.model.toJSON();
        var proj = ich.projectdashboardviewtpl(json);
        $(self.el).append(proj);
        window.app.controllers.browse.tabs.addDashboard(self);

        //Get users list
        new UserCollection({project:self.model.get('id')}).fetch({
            success : function (collection, response) {

                collection.each(function(user) {
                    var userelem = ich.userlisttpl({name:user.get("username")});
                    $("#projectInfoUserList").append(userelem);
                });

            }
        });
        $(proj).find("#nameDashboardInfo"+self.model.get('id')).panel({
            collapsible:false

        });

        $(proj).find('#projectInfoPanel').panel({
            collapseType:'slide-left',
            width:'300px'
        });

        $(proj).find('#projectLastCommandPanel').panel({
            collapseType:'slide-right',
            collapsed:true,
            trueVerticalText:true,
            vHeight:'237px',
            width:'300px'
        });

        $(proj).find('#projectStatsPanel').panel({
            collapsible:false
        });
        $(proj).find('#projectImagesPanel').panel({
            collapsible:true
        });

        $(proj).find('#projectAnnotationsPanel').panel({
            collapsible:true
        });



        self.initTabs();

        self.fetchImages();

        self.fetchCommands();

        self.fetchStats();

    }
});