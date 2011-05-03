var ProjectView = Backbone.View.extend({
    tagName : "div",
    projects : null,
    ontologies : null,
    users:null,
    searchProjectPanelElem : "#searchProjectPanel",
    allProjectsButtonElem: "#projectallbutton",
    searchProjectTextBoxElem : "#projectsearchtextbox",
    searchProjectButtonElem : "#projectsearchbutton",
    searchProjectCheckedOntologiesElem : 'input[type=checkbox][name=ontology]:checked',
    searchProjectOntolgiesListElem : "#ontologyChoiceList",
    projectListElem : "#projectlist",
    addProjectDialog : null,
    initialize: function(options) {
        this.container = options.container;
    },
    events: {
        "click .addProject": "showAddProjectPanel",
        "click .searchProjectCriteria": "searchProject",
        "click .showAllProject": "showAllProject"
    },
    //TODO: "utility" function (move in an other file...)
    //Return true if a contains obj otherwise return false
    contains: function(a, obj) {
        var i = a.length;
        while (i--) {
            if (a[i] == obj) {
                return true;
            }
        }
        return false;
    },
    render: function() {

        var self = this;

        $(this.el).html(ich.projectsviewtpl({}, true));

        //create search panel
        $(self.searchProjectPanelElem).panel({
            collapseSpeed:100
        });

        //configure "all projects" button
        $(self.allProjectsButtonElem).button({
            icons : {secondary: "ui-icon-refresh" }

        });

        //configure "search" button
        $(self.searchProjectButtonElem).button({
            icons : {secondary: "ui-icon-search" }

        });

        $("#projectaddbutton").button({
            icons : {secondary: "ui-icon-plus" }
        });




        //render ontologies choice
        new OntologyCollection({}).fetch({
            success : function (collection, response) {
                self.ontologies = collection;
                collection.each(function(ontology){
                    var choice = ich.ontologieschoicetpl({id:ontology.id,name:ontology.get("name")}, true);
                    $(self.searchProjectOntolgiesListElem).append(choice);
                });
            }});

        new ProjectCollection({user : self.userID}).fetch({
            success : function (collection, response) {
                var minNumberOfImage = Number.MAX_VALUE;
                var maxNumberOfImage = Number.MIN_VALUE;
                collection.each(function(project) {
                    var numberOfImage = project.get('numberOfImages');
                        console.log(numberOfImage + " VS " + maxNumberOfImage + " = " + (numberOfImage>maxNumberOfImage));
                        console.log(numberOfImage + " " + minNumberOfImage + " " + maxNumberOfImage + " " + (numberOfImage<minNumberOfImage) + " " +(numberOfImage>maxNumberOfImage));

                        if(numberOfImage<minNumberOfImage) minNumberOfImage =  numberOfImage;
                        if(numberOfImage>maxNumberOfImage) maxNumberOfImage =  numberOfImage;
                });
                $( "#numberofimage" ).slider({
                    range: true,
                    min: minNumberOfImage,
                    max: maxNumberOfImage,
                    values: [ minNumberOfImage, maxNumberOfImage ],
                    slide: function( event, ui ) {
                        $( "#amount" ).val( "" + ui.values[ 0 ] + " - " + ui.values[ 1 ] );
                        self.searchProject();
                    },
                    change: function( event, ui ) {
                        $( "#amount" ).val( "" + ui.values[ 0 ] + " - " + ui.values[ 1 ] );
                        self.searchProject();
                    }
                });
                $( "#amount" ).val( "" + $( "#numberofimage" ).slider( "values", 0 ) +
                        " - " + $( "#numberofimage" ).slider( "values", 1 ) );

                self.printProjects(collection);
            }});

        return this;
    },
    showAllProject:function() {
        var self = this;
        $(self.searchProjectTextBoxElem).val("");

        $.each($(self.searchProjectCheckedOntologiesElem), function(index, value) {
            $(self.searchProjectCheckedOntologiesElem).click();
        });
         var min = $( "#numberofimage" ).slider( "option", "min");
         var max = $( "#numberofimage" ).slider( "option", "max");

         $( "#numberofimage" ).slider( "values", [min,max] );
        //$( "#numberofimage" ).slider( "value", 1, 20 );

 /*$( "#numberofimage" ).slider({
                    range: true,
                    min: minNumberOfImage,
                    max: maxNumberOfImage,
                    values: [ minNumberOfImage, maxNumberOfImage ],
                    slide: function( event, ui ) {
                        $( "#amount" ).val( "" + ui.values[ 0 ] + " - " + ui.values[ 1 ] );
                        self.searchProject();
                    },
                    change: function( event, ui ) {
                        $( "#amount" ).val( "" + ui.values[ 0 ] + " - " + ui.values[ 1 ] );
                        self.searchProject();
                    }
                });  */
        self.searchProject();
        //self.filterProjects("");
    },
    searchProject : function() {
        console.log("searchProject");
        var self = this;
        var searchText = $(self.searchProjectTextBoxElem).val();

        //get ontologies list for ontology
        var searchOntologies = new Array();
        $.each($(self.searchProjectCheckedOntologiesElem), function(index, value) {
            var idOntology =  $(value).attr('id').replace("ontologies","");
            searchOntologies.push(idOntology);
        });

        var numberOfImages = new Array();
        console.log("0="+$( "#numberofimage" ).slider( "values", 0 ) + " 1="+ $( "#numberofimage" ).slider( "values", 1 ));
        numberOfImages.push($( "#numberofimage" ).slider( "values", 0 ));
        numberOfImages.push($( "#numberofimage" ).slider( "values", 1 ));

        self.filterProjects(searchText==""?undefined:searchText,searchOntologies.length==0?undefined:searchOntologies,numberOfImages);
    },
    showAddProjectPanel : function() {
        var self = this;
        if(self.addProjectDialog==null)
        {
            var dialog = ich.addprojectdialogtpl({});
            $(self.el).append(dialog);

            //render ontologies choice
            new OntologyCollection({}).fetch({
                success : function (collection, response) {
                    self.ontologies = collection;
                    $("#projectontology").empty();
                    collection.each(function(ontology){
                        var choice = ich.ontologieschoiceradiotpl({id:ontology.id,name:ontology.get("name")}, true);
                        $("#projectontology").append(choice);
                    });
                }});


            new UserCollection({}).fetch({
                success : function (collection, response) {
                    $("#projectuser").empty();
                    collection.each(function(user){
                        var choice = ich.userschoicetpl({id:user.id,username:user.get("username")}, true);
                        $("#projectuser").append(choice);
                    });
                }});


            //Build dialog
            self.addProjectDialog =    $("#addproject").dialog({
                width: 500,
                autoOpen : false,
                modal:true,
                buttons : {
                    "Save" : function() {
                        self.createProject();
                    },
                    "Cancel" : function() {
                        $("#addproject").dialog("close");
                    }
                }
            });


        }
        $("#errormessage").empty();
        $("#projecterrorlabel").hide();
        $("#addproject").dialog("open") ;
    },
    createProject : function() {
        var self = this;
        var name =  $("#project-name").val();
        var ontology = $('input[type=radio][name=ontologyradio]:checked').attr('value');
        var users = new Array();

        $('input[type=checkbox][name=usercheckbox]:checked').each(function(i,item){
            users.push($(item).attr("value"))
        });

        new ProjectModel({name : name, ontology : ontology}).save({name : name, ontology : ontology},{
            success: function (model, response) {
                console.log(response.message);

                new ProjectCollection({user : self.userID}).fetch({
                    success : function (collection, response) {
                        self.printProjects(collection);
                    }});
                $("#addproject").dialog("destroy") ;
                $("#addproject").dialog("close") ;

            },
            error: function (model, response) {
                /*console.log("response=");
                console.log(response);
                console.log("response.responseText=");
                console.log(response.responseText);
                console.log("response.responseText.project=");
                console.log(response.responseText.project); */
                var json = $.parseJSON(response.responseText);
                console.log("json.project="+json.errors);

                $("#projecterrorlabel").show();

                console.log($("#errormessage").append(json.errors));
            }
        }
                );


    },
    //show only project that have searchText value in their name and a ontology from searchOntologies.
    filterProjects : function(searchText,searchOntologies,searchNumberOfImages) {
        var self = this;

        var projects =  new ProjectCollection(self.projects.models);

        //each search function takes a search data and a collection and it return a collection without elem that
        //don't match with data search
        projects = self.filterByProjectsByName(searchText,projects);
        projects = self.filterProjectsByOntology(searchOntologies,projects);
        projects = self.filterProjectsByNumberOfImages(searchNumberOfImages,projects);


        //add here filter function


        self.projects.each(function(project) {
            //if project is in project result list, show it
            if(projects.get(project.id)!=null)
                $(self.projectListElem+project.id).show();
            else
                $(self.projectListElem+project.id).hide();
        });
    },
    filterByProjectsByName : function(searchText,projectOldList) {

        var projectNewList =  new ProjectCollection(projectOldList.models);

        projectOldList.each(function(project) {
            //if text is undefined: don't hide project
            //if project name contains search text, don't hide project
            if(searchText!=undefined && !project.get('name').toLowerCase().contains(searchText.toLowerCase()))
                projectNewList.remove(project);
        });

        return projectNewList;
    },

    filterProjectsByOntology : function(searchOntologies,projectOldList) {
        var self = this;
        var projectNewList =  new ProjectCollection(projectOldList.models);

        projectOldList.each(function(project) {

            if(searchOntologies!=undefined && !self.contains(searchOntologies,project.get('ontology')))
                projectNewList.remove(project);
        });
        return projectNewList;
    },
    filterProjectsByNumberOfImages : function(searchNumberOfImages,projectOldList) {
        var self = this;
        var projectNewList =  new ProjectCollection(projectOldList.models);

        projectOldList.each(function(project) {

            var numberOfImages = project.get('numberOfImages');
            console.log("numberOfImages of " + project.id);
            console.log("numberOfImages=" + numberOfImages + " " + searchNumberOfImages[0] + " " + searchNumberOfImages[1]);
            if(searchNumberOfImages[0]>numberOfImages || searchNumberOfImages[1]<numberOfImages)
                projectNewList.remove(project);
        });
        return projectNewList;
    },
    //print project from collection
    printProjects : function(collection) {
        var self = this;

        //clear de list
        $(self.projectListElem).empty();

        //array for autocompletion
        var projectNameArray = new Array();

        self.projects = collection;
        collection.each(function(project) {

            projectNameArray.push(project.get('name'));
            var json = project.toJSON();

            //create panel for a specific project
            var panel = new ProjectPanelView({
                model : project
            }).render();
            $(self.projectListElem).append(panel.el);

        });
        //autocomplete
        $(self.searchProjectTextBoxElem).autocomplete({
            minLength : 0, //with min=0, if user erase its text, it will show all project withouth name constraint
            source : projectNameArray,
            select : function (event,ui)
            {
                self.filterProjects(ui.item.label);

            },
            search : function(event)
            {

                console.log("TEXT:"+$(self.searchProjectTextBoxElem).val());
                self.searchProject();
            }
        });

    }
});
