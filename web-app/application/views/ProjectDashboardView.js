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
    render: function() {
        var self = this;

        new ProjectModel({id : self.model.id}).fetch({
            success : function (model, response) {
                self.model = model;
                self.printProjectInfo();
            }});

        return this;
    },
    refresh : function() {
        /*console.log("refresh db");
         var self = this;
         $(this.projectElem+this.model.get('id')).empty();
         new ProjectModel({id : self.model.id}).fetch({
         success : function (model, response) {
         console.log("refresh project panel");

         self.model = model;
         self.printProjectInfo();
         }});
         */
        var self = this;
        self.fetchAnnotations();
        self.fetchCommands();
        self.fetchStats();

    },
    initTabs : function(){
        var self = this;
        new TermCollection({idOntology:self.model.get('ontology')}).fetch({success : function (collection, response) {
            console.log("TermCollection="+collection.length);
            collection.each(function(term) {

                var termelem = ich.termtitletabtpl({name:term.get("name"),id:term.get("id")});
                console.log(termelem);
                $("#ultabsannotation").append(termelem);
                var contenttermelem = ich.termdivtabtpl({name:term.get("name"),id:term.get("id")});
                console.log(contenttermelem);
                $("#listtabannotation").append(contenttermelem);

            });

            if(self.tabsAnnotation==null)
                self.tabsAnnotation = $("#tabsannotation").tabs();
            self.fetchAnnotations();
        }});
    },
    fetchAnnotations : function () {
        var self = this;

        console.log("TERMECOLLECTION");

        new TermCollection({idOntology:self.model.get('ontology')}).fetch({
            success : function (collection, response) {
                console.log("TermCollection="+collection.length);
                collection.each(function(term) {
                    $("#tabsterm-"+term.get("id")).empty();
                    new AnnotationCollection({term:term.get("id")}).fetch({success : function (collection, response) {
                        console.log("AnnotationCollection="+collection.length);
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







       /* if(self.tabsAnnotation==null) {
            //create tabs
            new TermCollection({idOntology:self.model.get('ontology')}).fetch({success : function (collection, response) {
                console.log("TermCollection="+collection.length);
                collection.each(function(term) {

                    var termelem = ich.termtitletabtpl({name:term.get("name"),id:term.get("id")});
                    console.log(termelem);
                    $("#ultabsannotation").append(termelem);
                    var contenttermelem = ich.termdivtabtpl({name:term.get("name"),id:term.get("id")});
                    console.log(contenttermelem);
                    $("#listtabannotation").append(contenttermelem);

                });
                console.log("ANNOTATIONCOLLECTION");


                if(self.tabsAnnotation==null)
                    self.tabsAnnotation = $("#tabsannotation").tabs();

            }});
        }
        else
        {

        }








        console.log("TERMECOLLECTION");
        new TermCollection({idOntology:self.model.get('ontology')}).fetch({success : function (collection, response) {
            console.log("TermCollection="+collection.length);
            collection.each(function(term) {

                var termelem = ich.termtitletabtpl({name:term.get("name"),id:term.get("id")});
                console.log(termelem);
                $("#ultabsannotation").append(termelem);
                var contenttermelem = ich.termdivtabtpl({name:term.get("name"),id:term.get("id")});
                console.log(contenttermelem);
                $("#listtabannotation").append(contenttermelem);

                console.log("ANNOTATIONCOLLECTION");
                new AnnotationCollection({term:term.get("id")}).fetch({success : function (collection, response) {
                    console.log("AnnotationCollection="+collection.length);
                    var view = new AnnotationView({
                        page : undefined,
                        model : collection,
                        el:$("#tabsterm-"+term.get("id")),
                        container : window.app.view.components.warehouse
                    }).render();

                }});

            });
            if(self.tabsAnnotation==null)
                self.tabsAnnotation = $("#tabsannotation").tabs();

        }});*/

    },
    fetchAnnotationsOld : function () {
        var self = this;
        var annotationCollection = new AnnotationCollection({project:self.model.get('id')});

        var annotationCallback = function(collection,response) {

            var view = new AnnotationView({
                page : undefined,
                model : collection,
                el:$("#projectAnnotationList"),
                container : window.app.view.components.warehouse
            }).render();
        }

        annotationCollection.fetch({
            success : function(model, response) {
                annotationCallback(model, response); //fonctionne mais très bourrin de tout refaire à chaque fois...
            }
        });

    },
    fetchCommands : function () {
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

    fetchImages : function() {
        var self = this;
        new ImageCollection({project:self.model.get('id')}).fetch({success : function (collection, response) {
            new ImageView({
                page : undefined,
                model : collection,
                el:$("#projectImageList"),
                container : window.app.view.components.warehouse
            }).render();
        }});
    },
    fetchStats : function () {
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

    printProjectInfo : function() {
        console.log("print.........");
        var self = this;
        var json = self.model.toJSON();
        var idOntology = json.ontology;

        //Get ontology name
        json.ontology = window.app.models.ontologies.get(idOntology).get('name');


        var dateCreated = new Date();
        dateCreated.setTime(json.created);
        json.created = dateCreated.toLocaleDateString() + " " + dateCreated.toLocaleTimeString();
        var dateUpdated = new Date();
        dateUpdated.setTime(json.updated);
        json.updated = dateUpdated.toLocaleDateString() + " " + dateUpdated.toLocaleTimeString();
        //json.created = new Date().setTime(json.created)

        //Get users list
        new UserCollection({project:self.model.get('id')}).fetch({success : function (collection, response) {



            var proj = ich.projectdashboardviewtpl(json);
            $(self.el).append(proj);
            window.app.controllers.browse.tabs.addDashboard(self);



            collection.each(function(user) {
                var userelem = ich.userlisttpl({name:user.get("username")});
                $("#userlist").append(userelem);
            });

            $(self.imageAddElem + self.model.id).button({
                icons : {secondary : "ui-icon-image"}
            });
            $(self.projectElem+self.model.get('id')).panel({
                collapsible:true,
                width:'300px'
            });

            $("#namedashboardinfo"+self.model.get('id')).panel({
                collapsible:false

            });

            $('#panelLeft_1').panel({
                collapseType:'slide-left',
                width:'300px'
            });

            $('#panelRight_1').panel({
                collapseType:'slide-right',
                collapsed:true,
                trueVerticalText:true,
                vHeight:'237px',
                width:'300px'
            });
            $('#panelCenter_1').panel({
                collapsible:false
            });
            $('#panelCenter_2').panel({
                collapsible:true
            });

            $('#panelCenter_3').panel({
                collapsible:true
            });

            self.initTabs();

            self.fetchImages();

            self.fetchCommands();

            self.fetchStats();



        }
        });
    }
});