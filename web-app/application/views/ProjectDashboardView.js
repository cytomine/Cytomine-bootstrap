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
        self.printProjectInfo();

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
            console.log(proj);
            if(self.addSlideDialog!=null){
                console.log("addSlideDialog!=null");
                $("#projectdashboardinfo"+json.id).replaceWith(proj);
            }
            else
                $(self.el).append(proj);


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
                width:'200px'
            });
            $('#panelCenter_1').panel({
                collapsible:true
            });
            $('#panelCenter_2').panel({
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


            var commandCollection = new CommandCollection({project:self.model.get('id'),max:10});
            var commandCallback = function(collection, response) {
                $("#lastactionitem").empty();
                collection.each(function(command) {
                    console.log(command);

                    var dateCreated = new Date();
                    dateCreated.setTime(command.get('created'));
                    var dateStr = dateCreated.toLocaleDateString() + " " + dateCreated.toLocaleTimeString()

                    var action = ""
                    if(command.get("type")=="ADD") {
                        action = ich.addlisttpl({text:command.get("action"),datestr:dateStr});
                    }
                    else if(command.get("type")=="EDIT") {
                        action = ich.editlisttpl({text:command.get("action"),datestr:dateStr});
                    }
                    else if(command.get("type")=="DELETE") {
                        action = ich.deletelisttpl({text:command.get("action"),datestr:dateStr});
                    }
                    $("#lastactionitem").append(action);
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
            setInterval(function(){
                fetchCommands();
            }, 5000);




            var statsCollection = new StatsCollection({project:self.model.get('id')});
            var statsCallback = function(collection, response) {
                //console.log("**************************************");
                console.log(collection);
                $("#plotterms").empty();

                var array = new Array();
                collection.each(function(stat) {
                    console.log("###" + stat.cid);

                    var subArray = new Array(stat.get('key'),stat.get('value'));
                    array.push(subArray);
                    //console.log("###" + stat.get('key'));
                });

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
            /*setInterval(function(){
                fetchStats();
            }, 5000);*/



        }
        });
    }
});