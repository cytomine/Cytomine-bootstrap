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
    refresh : function(model) {

        var self = this;
        $(this.projectElem+this.model.get('id')).empty();
        new ProjectModel({id : self.model.id}).fetch({
            success : function (model, response) {
                console.log("refresh project panel");
                console.log(model.toJSON());
                self.model = model;
                self.printProjectInfo();
            }});

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

            if(self.addSlideDialog!=null){
                console.log("addSlideDialog!=null");
                $("#projectdashboardinfo"+json.id).replaceWith(proj);
            }
            else {
                $(self.el).append(proj);
                window.app.controllers.browse.tabs.addDashboard(self);
            }


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

            new ImageCollection({project:self.model.get('id')}).fetch({success : function (collection, response) {
                var view = new ImageView({
                    page : undefined,
                    model : collection,
                    el:$("#projectImageList"),
                    container : window.app.view.components.warehouse
                }).render();

            }});

            var annotationCollection = new AnnotationCollection({project:self.model.get('id')});

            var annotationCallback = function(collection,response) {
                console.log("**** ANNOTATION SIZE:"+collection.length);
                //$("#projectAnnotationList").empty();
                var view = new AnnotationView({
                    page : undefined,
                    model : collection,
                    el:$("#projectAnnotationList"),
                    container : window.app.view.components.warehouse
                }).render();
            }
            var fetchAnnotations = function() {
                annotationCollection.fetch({
                    success : function(model, response) {
                        annotationCallback(model, response); //fonctionne mais très bourrin de tout refaire à chaque fois...
                    }
                });
            }

            fetchAnnotations();


            var commandCollection = new CommandCollection({project:self.model.get('id'),max:10});

            var commandCallback = function(collection, response) {

                collection.each(function(command) {

                });


                $("#lastactionitem").empty();
                collection.each(function(command) {
                    console.log(command);

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
                        $.ajax({
                            async : false,
                            url: json.cropURL,
                            success: function(data){},
                            error: function(XMLHttpRequest, textStatus, errorThrown){ $(action).find("img").hide();}
                        });
                    }
                    if(command.get("class")=="be.cytomine.command.annotation.EditAnnotationCommand")
                    {
                        var action = ich.annotationcommandlisttpl({icon:"ui-icon-pencil",text:command.get("action"),datestr:dateStr,image:json.cropURL});
                        $("#lastactionitem").append(action);
                        $.ajax({
                            async : false,
                            url: json.cropURL,
                            success: function(data){},
                            error: function(XMLHttpRequest, textStatus, errorThrown){ $(action).find("img").hide();}
                        });
                    }
                    if(command.get("class")=="be.cytomine.command.annotation.DeleteAnnotationCommand")
                    {
                        var action = ich.annotationcommandlisttpl({icon:"ui-icon-trash",text:command.get("action"),datestr:dateStr,image:json.cropURL});
                        $("#lastactionitem").append(action);
                        $.ajax({
                            async : false,
                            url: json.cropURL,
                            success: function(data){},
                            error: function(XMLHttpRequest, textStatus, errorThrown){ $(action).find("img").hide();}
                        });
                    }


                    if(command.get("class")=="be.cytomine.command.annotationterm.AddAnnotationTermCommand")
                    {
                        console.log(json);
                        var action = ich.annotationtermcommandlisttpl({icon:"ui-icon-plus",text:command.get("action"),datestr:dateStr,image:""});
                        $("#lastactionitem").append(action);

                    }
                    if(command.get("class")=="be.cytomine.command.annotationterm.EditAnnotationTermCommand")
                    {
                        console.log(json);
                        var action = ich.annotationtermcommandlisttpl({icon:"ui-icon-pencil",text:command.get("action"),datestr:dateStr,image:""});
                        $("#lastactionitem").append(action);

                    }
                    if(command.get("class")=="be.cytomine.command.annotationterm.DeleteAnnotationTermCommand")
                    {
                        console.log(json);
                        var action = ich.annotationtermcommandlisttpl({icon:"ui-icon-trash",text:command.get("action"),datestr:dateStr,image:""});
                        $("#lastactionitem").append(action);

                    }
                });
            }
            var fetchCommands = function() {
                commandCollection.fetch({
                    success : function(model, response) {
                        commandCallback(model, response); //fonctionne mais très bourrin de tout refaire à chaque fois...
                    }
                });
            }

            fetchCommands();



            var statsCollection = new StatsCollection({project:self.model.get('id')});
            var statsCallback = function(collection, response) {
                //console.log("**************************************");
                console.log(collection);
                $("#plotterms").empty();

                var empty = true;
                var array = new Array();
                collection.each(function(stat) {
                    console.log("###" + stat.cid);

                    var subArray = new Array(stat.get('key'),stat.get('value'));
                    array.push(subArray);
                    //console.log("###" + stat.get('key'));
                    empty = empty && stat.get('value')=="0";
                });
                console.log("empty="+empty);

                if(empty) {
                    array.push(new Array("Nothing",100))
                }


                $.jqplot('plotterms', [array], {
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
            var fetchStats = function() {
                statsCollection.fetch({
                    success : function(model, response) {
                        statsCallback(model, response); //fonctionne mais très bourrin de tout refaire à chaque fois...
                    }
                });
            }

            fetchStats();

        }
        });
    }
});