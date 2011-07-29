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
    images : null,
    imagesView : null, //image view
    imagesTabsView : null,
    imagesThumbOrTab : null, //0=thumb, 1=tab
    annotationsViews : [], //array of annotation view
    maxCommandsView : 10,
    selectedTermTab : 0,
    rendered : false,
    projectImages : null,
    initialize: function(options) {
        this.container = options.container;
        this.imagesThumbOrTab = options.imagesThumbOrTab;
        _.bindAll(this, 'render');
    },
    events: {
    },
    /**
     * Print all information for this project
     */
    render: function() {
        var self = this;
        require(["text!application/templates/dashboard/Dashboard.tpl.html"], function(tpl) {

            self.images = new Array();
            self.doLayout(tpl);
            self.rendered = true;
        });
        return this;
    },
    /**
     * Refresh all information for this project
     */
    refresh : function() {
        var self = this;
        if (!self.rendered) return;

        var projectModel = new ProjectModel({id : self.model.id});
        var projectCallback = function(model, response) {

            self.model = model;

            self.fetchProjectInfo();
            /*self.refreshImages();*/

            //refresh selected tab

            /*self.refreshAnnotations(self.selectedTermTab);*/

            //TODO: must be improve!
            new AnnotationCollection({project:self.model.id}).fetch({
                success : function (collection, response) {
                    self.fetchCommands(collection);
                }
            });


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
        /* Init dashboard */
        $("#projectcolmunChartPanel").panel({collapsible: true});
        $("#projectPieChartPanel").panel({collapsible: true});
        $("#projectInfoPanel").panel({collapsible: true});
        $("#projectLastCommandPanel").panel({collapsible: true});


        /* Init Annotations */
        require(["text!application/templates/dashboard/TermTab.tpl.html", "text!application/templates/dashboard/TermTabContent.tpl.html"], function(termTabTpl, termTabContentTpl) {
            new OntologyModel({id:self.model.get('ontology')}).fetch({
                success : function(model, response) {
                    $(self.el).find('.tree').dynatree({
                        checkbox: true,
                        selectMode: 2,
                        expand : true,
                        onExpand : function() {},
                        children: model.toJSON(),
                        onSelect: function(select, node) {
                            //if(!self.activeEvent) return;

                            if (node.isSelected()) {

                                self.refreshAnnotations(node.data.key);
                                $("#tabsterm-panel-"+self.model.id+"-"+node.data.key).show();
                            }
                            else {
                                $("#tabsterm-panel-"+self.model.id+"-"+node.data.key).hide();


                            }

                        },
                        onDblClick: function(node, event) {
                            //node.toggleSelect();
                        },

                        // The following options are only required, if we have more than one tree on one page:
                        initId: "treeData-annotations-"+self.model.get('ontology'),
                        cookieId: "dynatree-Cb-annotations-"+self.model.get('ontology'),
                        idPrefix: "dynatree-Cb-annotations-"+self.model.get('ontology')+"-"
                    });
                    //expand all nodes
                    $(self.el).find('.tree').dynatree("getRoot").visit(function(node){
                        node.expand(true);
                    });
                    $("#ontology-annotations-panel-"+self.model.id).panel();
                    $(self.el).find("#hideAllAnnotations").button();
                    $(self.el).find("#hideAllAnnotations").click(function(){
                        self.selectAnnotations(false);
                    });
                    $(self.el).find("#showAllAnnotations").button();
                    $(self.el).find("#showAllAnnotations").click(function(){
                        self.selectAnnotations(true);
                    });
                    $(self.el).find("#refreshAnnotations").button();
                    $(self.el).find("#refreshAnnotations").click(function(){
                        self.refreshSelectedTerms();
                    });


                }
            });
            new TermCollection({idOntology:self.model.get('ontology')}).fetch({
                success : function (collection, response) {
                    window.app.status.currentTermsCollection = collection;
                    collection.each(function(term) {
                        //add x term tab
                        $("#listtabannotation").prepend(_.template(termTabContentTpl, { project : self.model.id, id : term.get("id"), name : term.get("name")}));
                        $("#tabsterm-panel-"+self.model.id+"-"+term.get("id")).panel();
                        $("#tabsterm-panel-"+self.model.id+"-"+term.get("id")).hide();

                    });


                }});
        });
    },
    /**
     * Add the the tab with term info
     * @param id  term id
     * @param name term name
     */
    addTermToTab : function(termTabTpl, termTabContentTpl, data) {
        //$("#ultabsannotation").append(_.template(termTabTpl, data));
        $("#listtabannotation").append(_.template(termTabContentTpl, data));

    },
    selectAnnotations : function (selected) {
        var self = this;
        new TermCollection({idOntology:self.model.get('ontology')}).fetch({
            success : function (collection, response) {
                collection.each(function(term) {
                    $(self.el).find('.tree').dynatree("getTree").selectKey(term.get("id"), selected);
                });
            }
        });
    },
    refreshSelectedTerms : function () {
        var self = this;
        var tree = $(this.el).find('.tree').dynatree("getRoot");
        if (!_.isFunction(tree.visit)) return; //tree is not yet loaded
        tree.visit(function(node){
            if (!node.isSelected()) return;
            self.refreshAnnotations(node.data.key);
        });
    },
    /**
     * Refresh all annotation dor the given term
     * @param term annotation term to be refresh (all = 0)
     */
    refreshAnnotations : function(term) {
        this.printAnnotationThumb(term,"#tabsterm-"+this.model.id+"-"+term);
    },
    clearAnnotations : function (term) {
        var self = this;
        $("#tabsterm-"+self.model.id+"-"+term).empty();

    },
    /**
     * Print annotation for the given term
     * @param term term annotation term to be refresh (all = 0)
     * @param $elem  elem that will keep all annotations
     */
    printAnnotationThumb : function(term,$elem){
        var self = this;

        var idTerm = 0;
        if(term==0) {idTerm = undefined;}
        else idTerm = term

        new AnnotationCollection({project:self.model.id,term:idTerm}).fetch({
            success : function (collection, response) {

                if (self.annotationsViews[idTerm] != null) { //only refresh
                    self.annotationsViews[idTerm].refresh(collection);
                    return;
                }
                self.annotationsViews[idTerm] = new AnnotationView({
                    page : undefined,
                    model : collection,
                    el:$($elem),
                    container : window.app.view.components.warehouse
                }).render();

                //self.annotationsViews[term].refresh(collection);
                $("#listtabannotation > div").tsort();
            }
        });
    },
    /**
     * Get and Print ALL images (use for the first time)
     */
    fetchImages : function() {
        $('#imageThumbs'+this.model.id).button({
            text: false,
            icons: {
                primary: "ui-icon-image"

            }
        });
        $('#imageArray'+this.model.id).button({
            text: false,
            icons: {
                primary: "ui-icon-calculator"

            }
        });
        var self = this;
        //$("#tabs-images-listing-"+ self.model.get('id')).hide();
        new ImageInstanceCollection({project:self.model.get('id')}).fetch({
            success : function (collection, response) {

                self.projectImages = collection;
                console.log("self.projectImages="+self.projectImages.length);
                console.log("ImageInstanceCollection create image thumb view:"+$("#tabs-projectImageThumb"+self.model.get('id')).length);
                self.imagesView = new ImageView({
                    page : 0,
                    model : collection,
                    el:$("#tabs-projectImageThumb"+self.model.get('id')),
                    container : window.app.view.components.warehouse
                }).render();

                console.log("create image listing view:"+$("#tabs-projectImageListing"+self.model.get('id')).length);
                self.imagesTabsView = new ImageTabsView({
                    model : collection,
                    el:$("#tabs-projectImageListing"+self.model.get('id')),
                    container : window.app.view.components.warehouse,
                    idProject : self.model.id
                }).render();



                /*$("#tabs-images-listing-"+ self.model.get('id')).tabs();
                 self.selectTab(1);                                      */
                //$("#tabs-images-listing-"+ self.model.get('id')).show();
            }});

    },
    changeImagePage : function(page) {
      var self = this;
        console.log("self.projectImages="+self.projectImages.length);
        //$("#tabs-projectImageThumb"+self.model.get('id')).empty();
        self.imagesView = new ImageView({
            page : page,
            model : self.projectImages,
            el:$("#tabs-projectImageThumb"+self.model.get('id')),
            container : window.app.view.components.warehouse
        }).render();
    },

    /**
     * Get and Print only new images and remove delted images
     */
    refreshImages : function() {
        var self = this;
        if (self.imagesView == null) {
            self.fetchImages();
            return;
        }
        new ImageInstanceCollection({project:self.model.get('id')}).fetch({
            success : function (collection, response) {
                self.imagesView.refresh(collection);
            }});
    },
    refreshImagesTabs : function() {

        var self = this;
        if(self.imagesTabsView==null) return; //imageView is not yet build
        new ImageInstanceCollection({project:self.model.get('id')}).fetch({
            success : function (collection, response) {
                self.imagesTabsView.refresh(collection);
            }});
    },
    showImages : function() {
        $("#tabs-projectImageThumb"+this.model.id).show();
        $("#tabs-projectImageListing"+this.model.id).hide();
        $('#imageThumbs'+this.model.id).button( "disable" );
        $('#imageArray'+this.model.id).button( "enable");
    },
    showImagesArray : function() {
        $("#tabs-projectImageThumb"+this.model.id).hide();
        $("#tabs-projectImageListing"+this.model.id).show();
        $('#imageThumbs'+this.model.id).button( "enable" );
        $('#imageArray'+this.model.id).button( "disable");
    },
    drawPieChart : function (collection, response) {
        $("#projectPieChart").empty();
        // Create and populate the data table.
        var data = new google.visualization.DataTable();
        data.addColumn('string', 'Term');
        data.addColumn('number', 'Number of annotations');
        data.addRows(_.size(collection));
        var i = 0;
        var colors = [];
        collection.each(function(stat) {
            colors.push(stat.get('color'));
            data.setValue(i,0, stat.get('key'));
            data.setValue(i,1, stat.get('value'));
            i++;
        });

        // Create and draw the visualization.
        new google.visualization.PieChart(document.getElementById('projectPieChart')).
                draw(data, {width: 500, height: 350,title:"", colors : colors});
    },
    drawColumnChart : function (collection, response) {
        $("#projectColumnChart").empty();
        var dataToShow = false;
        // Create and populate the data table.
        var data = new google.visualization.DataTable();

        data.addRows(_.size(collection));

        data.addColumn('string', 'Number');
        data.addColumn('number', 0);
        var colors = [];
        var j = 0;
        collection.each(function(stat) {
            colors.push(stat.get('color'));
            if (stat.get('value') > 0) dataToShow = true;
            data.setValue(j, 0, stat.get("key"));
            data.setValue(j, 1, stat.get("value"));
            j++;
        });

        // Create and draw the visualization.
        new google.visualization.ColumnChart(document.getElementById("projectColumnChart")).
                draw(data,
            {title:"",
                width:500, height:350,
                vAxis: {title: "Number of annotations"},
                hAxis: {title: "Terms"}}
                );
        $("#projectColumnChart").show();

    },
    fetchStats : function () {
        var self = this;
        if (self.model.get('numberOfAnnotations') == 0) return;



        var statsCollection = new StatsCollection({project:self.model.get('id')});
        var statsCallback = function(collection, response) {
            //Check if there is something to display
            self.drawPieChart(collection, response);
            self.drawColumnChart(collection, response);
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
        //json.ontology = window.app.models.ontologies.get(idOntology).get('name');

        //Get created/updated date
        var dateCreated = new Date();
        dateCreated.setTime(json.created);
        json.created = dateCreated.toLocaleDateString() + " " + dateCreated.toLocaleTimeString();
        var dateUpdated = new Date();
        dateUpdated.setTime(json.updated);
        json.updated = dateUpdated.toLocaleDateString() + " " + dateUpdated.toLocaleTimeString();

        self.resetElem("#projectInfoName",json.name);
        self.resetElem("#projectInfoOntology",json.ontologyName);
        self.resetElem("#projectInfoNumberOfSlides",json.numberOfSlides);
        self.resetElem("#projectInfoNumberOfImages",json.numberOfImages);
        self.resetElem("#projectInfoNumberOfAnnotations",json.numberOfAnnotations);
        self.resetElem("#projectInfoCreated",json.created);
        self.resetElem("#projectInfoUpdated",json.updated);

        $("#projectInfoUserList").empty();

        require(["text!application/templates/dashboard/UserInfo.tpl.html"], function(tpl) {
            //Get users list
            var users = []
            _.each(self.model.get('users'), function (idUser) {
                users.push(window.app.models.users.get(idUser).prettyName());
            });
            $("#projectInfoUserList").html(users.join(", "));
        });



    },
    fetchCommands : function (annotations) {
        var self = this;
        require([
            "text!application/templates/dashboard/CommandAnnotation.tpl.html",
            "text!application/templates/dashboard/CommandAnnotationTerm.tpl.html",
            "text!application/templates/dashboard/CommandImageInstance.tpl.html"],
               function(commandAnnotationTpl, commandAnnotationTermTpl,commandImageInstanceTpl) {
                   var commandCollection = new CommandCollection({project:self.model.get('id'),max:self.maxCommandsView});

                   var commandCallback = function(collection, response) {

                       $("#lastactionitem").empty();
                       collection.each(function(command) {
                           var json = command.toJSON()




                           var dateCreated = new Date();
                           dateCreated.setTime(json.created);
                           var dateStr = dateCreated.toLocaleDateString() + " " + dateCreated.toLocaleTimeString();

                           var jsonCommand = $.parseJSON(json.command.data);
                           //jsonCommand.cropURL


                           var action = ""

                           if(json.command.CLASSNAME=="be.cytomine.command.annotation.AddAnnotationCommand")
                           {
                               var cropStyle = "block";
                               var cropURL = jsonCommand.cropURL;

                               if (annotations.get(jsonCommand.id) == undefined) {
                                   cropStyle = "none";
                                   cropURL = "";
                               }

                               var action = _.template(commandAnnotationTpl, {idProject : self.model.id, idAnnotation : jsonCommand.id, idImage : jsonCommand.image,imageFilename : jsonCommand.imageFilename, icon:"add.png",text:json.prefixAction+ " " + json.command.action,datestr:dateStr,cropURL:cropURL, cropStyle:cropStyle});
                               $("#lastactionitem").append(action);
                           }
                           if(json.command.CLASSNAME=="be.cytomine.command.annotation.EditAnnotationCommand")
                           {
                               var cropStyle = "";
                               var cropURL = jsonCommand.newAnnotation.cropURL;
                               if (annotations.get(jsonCommand.newAnnotation.id) == undefined) {
                                   cropStyle = "display : none;";
                                   cropURL = "";
                               }

                               var action = _.template(commandAnnotationTpl, {idProject : self.model.id, idAnnotation : jsonCommand.newAnnotation.id, idImage : jsonCommand.newAnnotation.image,imageFilename : jsonCommand.newAnnotation.imageFilename,icon:"delete.gif",text:json.prefixAction+ " " +json.command.action,datestr:dateStr,cropURL:cropURL, cropStyle:cropStyle});
                               $("#lastactionitem").append(action);
                           }
                           if(json.command.CLASSNAME=="be.cytomine.command.annotation.DeleteAnnotationCommand")
                           {
                               var cropStyle = "";
                               var cropURL = jsonCommand.cropURL;
                               if (annotations.get(jsonCommand.id) == undefined) {
                                   cropStyle = "display : none;";
                                   cropURL = "";
                               }
                               var action = _.template(commandAnnotationTpl, {idProject : self.model.id, idAnnotation : jsonCommand.id, idImage : jsonCommand.image,imageFilename : jsonCommand.imageFilename,icon:"delete.gif",text:json.prefixAction+ " " +json.command.action,datestr:dateStr,cropURL:cropURL, cropStyle:cropStyle});
                               $("#lastactionitem").append(action);
                           }


                           if(json.command.CLASSNAME=="be.cytomine.command.annotationterm.AddAnnotationTermCommand")
                           {

                               var action = _.template(commandAnnotationTermTpl, {icon:"ui-icon-plus",text:json.prefixAction+ " " +json.command.action,datestr:dateStr,image:""});
                               $("#lastactionitem").append(action);

                           }
                           if(json.command.CLASSNAME=="be.cytomine.command.annotationterm.EditAnnotationTermCommand")
                           {

                               var action = _.template(commandAnnotationTermTpl, {icon:"ui-icon-pencil",text:json.prefixAction+ " " +json.command.action,datestr:dateStr,image:""});
                               $("#lastactionitem").append(action);

                           }
                           if(json.command.CLASSNAME=="be.cytomine.command.annotationterm.DeleteAnnotationTermCommand")
                           {

                               var action = _.template(commandAnnotationTermTpl, {icon:"ui-icon-trash",text:json.prefixAction+ " " +json.command.action,datestr:dateStr,image:""});
                               $("#lastactionitem").append(action);

                           }


                           if(json.command.CLASSNAME=="be.cytomine.command.imageinstance.AddImageInstanceCommand")
                           {
                               var cropStyle = "block";
                               var cropURL = jsonCommand.thumb;




                               var action = _.template(commandImageInstanceTpl, {idProject : self.model.id, idImage : jsonCommand.id, imageFilename : jsonCommand.filename, icon:"add.png",text:json.prefixAction+ " " + json.command.action,datestr:dateStr,cropURL:cropURL, cropStyle:cropStyle});
                               $("#lastactionitem").append(action);
                           }


                           if(json.command.CLASSNAME=="be.cytomine.command.imageinstance.DeleteImageInstanceCommand")
                           {
                               var cropStyle = "block";
                               var cropURL = jsonCommand.thumb;

                               var action = _.template(commandImageInstanceTpl, {idProject : self.model.id, idImage : jsonCommand.id, imageFilename : jsonCommand.filename,icon:"delete.gif",text:json.prefixAction+ " " +json.command.action,datestr:dateStr,cropURL:cropURL, cropStyle:cropStyle});
                               $("#lastactionitem").append(action);
                           }




                       });
                   }

                   commandCollection.fetch({
                       success : function(model, response) {
                           commandCallback(model, response); //fonctionne mais très bourrin de tout refaire à chaque fois...
                       }
                   });
               });


    },
    resetElem : function(elem,txt) {

        $(this.el).find(elem).empty();
        $(this.el).find(elem).append(txt);
    },
    doLayout : function(tpl) {

        var self = this;
        var html = _.template(tpl, self.model.toJSON());
        $(self.el).append(html);
        window.app.controllers.browse.tabs.addDashboard(self);
        self.initTabs();
        self.showImages();
    },
    initWidgets : function() {
        return; //do nothing actually. We have to keep positions in memory if we do that

        $( ".widgets" ).sortable({
            connectWith: ".widgets"

        });

        $( ".widgets" ).disableSelection();
    }
});