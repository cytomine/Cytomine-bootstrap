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
    addProjectCheckedOntologiesRadioElem : 'input[type=radio][name=ontologyradio]:checked',
    addProjectCheckedUsersCheckboxElem : 'input[type=checkbox][name=usercheckbox]:checked',
    searchProjectOntolgiesListElem : "#ontologyChoiceList",
    projectListElem : "#projectlist",
    addProjectDialog : null,
    sliderNumberOfImagesElem : "#numberofimageSlider",
    labelNumberOfImagesElem : "#amountNumberOfImages",
    sliderNumberOfSlidesElem : "#numberofslideSlider",
    labelNumberOfSlidesElem : "#amountNumberOfSlides",
    sliderNumberOfAnnotationsElem : "#numberofannotationSlider",
    labelNumberOfAnnotationsElem : "#amountNumberOfAnnotations",
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
                //init slider to serach by slides number, images number...
                var minNumberOfImage = Number.MAX_VALUE;
                var maxNumberOfImage = 0;
                var minNumberOfSlide = Number.MAX_VALUE;
                var maxNumberOfSlide = 0;
                var minNumberOfAnnotation = Number.MAX_VALUE;
                var maxNumberOfAnnotation = 0;

                console.log("maxNumberOfAnnotation="+maxNumberOfAnnotation);

                //compute min/max value for slider
                collection.each(function(project) {
                    var numberOfImage = parseInt(project.get('numberOfImages'));
                    var numberOfSlide = parseInt(project.get('numberOfSlides'));
                    var numberOfAnnotation = parseInt(project.get('numberOfAnnotations'));

                    if(numberOfImage<minNumberOfImage) minNumberOfImage =  numberOfImage;
                    if(numberOfImage>maxNumberOfImage) maxNumberOfImage =  numberOfImage;
                    if(numberOfSlide<minNumberOfSlide) minNumberOfSlide =  numberOfSlide;
                    if(numberOfSlide>maxNumberOfSlide) maxNumberOfSlide =  numberOfSlide;
                    if(numberOfAnnotation<minNumberOfAnnotation) minNumberOfAnnotation =  numberOfAnnotation;
                    if(numberOfAnnotation>maxNumberOfAnnotation) maxNumberOfAnnotation =  numberOfAnnotation;

                });
                //create slider
                self.createSliderWithoutAmountPrint(self.sliderNumberOfImagesElem,self.labelNumberOfImagesElem,minNumberOfImage,maxNumberOfImage);
                self.createSliderWithoutAmountPrint(self.sliderNumberOfSlidesElem,self.labelNumberOfSlidesElem,minNumberOfSlide,maxNumberOfSlide);
                self.createSliderWithoutAmountPrint(self.sliderNumberOfAnnotationsElem,self.labelNumberOfAnnotationsElem,minNumberOfAnnotation,maxNumberOfAnnotation);

                //print all projects
                self.printProjects(collection);

            }});

        return this;
    },
    createSliderWithoutAmountPrint : function(sliderElem, labelElem,min,max) {
        var self = this;

        console.log("sliderElem="+sliderElem + " min="+min + " et max="+ max);
        $(sliderElem).slider({
            range: true,
            min : min,
            max : max,
            values: [ min, max ],
            /*slide: function( event, ui ) {
                $(labelElem).val( "" + ui.values[ 0 ] + " - " + ui.values[ 1 ] );
                self.searchProject();
            },*/
            change: function( event, ui ) {
                $(labelElem).val( "" + ui.values[ 0 ] + " - " + ui.values[ 1 ] );
                self.searchProject();
            }
        });
        $(labelElem).val( "" + $(sliderElem).slider( "values", 0 ) +" - " + $(sliderElem).slider( "values", 1 ) );
    },
    refresh : function() {
        console.log("refresh projects panel");

        this.render();
    },
    refreshSearchPanel : function() {

        //refresh item from search panel
        //ex: if a user add 1 slide to the project that have the hight number of slide, number of slides slider value must be change
        var self = this;
        console.log("refresh projects panel");
        new ProjectCollection({user : self.userID}).fetch({
            success : function (collection, response) {
                var minNumberOfImage = Number.MAX_VALUE;
                var maxNumberOfImage = Number.MIN_VALUE;
                var minNumberOfSlide = Number.MAX_VALUE;
                var maxNumberOfSlide = Number.MIN_VALUE;
                var minNumberOfAnnotation = Number.MAX_VALUE;
                var maxNumberOfAnnotation = Number.MIN_VALUE;

                collection.each(function(project) {
                    var numberOfImage = project.get('numberOfImages');
                    var numberOfSlide = project.get('numberOfSlides');
                    var numberOfAnnotation = project.get('numberOfAnnotations');

                    if(numberOfImage<minNumberOfImage) minNumberOfImage =  numberOfImage;
                    if(numberOfImage>maxNumberOfImage) maxNumberOfImage =  numberOfImage;
                    if(numberOfSlide<minNumberOfSlide) minNumberOfSlide =  numberOfSlide;
                    if(numberOfSlide>maxNumberOfSlide) maxNumberOfSlide =  numberOfSlide;
                    if(numberOfAnnotation<minNumberOfAnnotation) minNumberOfAnnotation =  numberOfAnnotation;
                    if(numberOfAnnotation>maxNumberOfAnnotation) maxNumberOfAnnotation =  numberOfAnnotation;
                });

                self.createSliderWithoutAmountPrint(self.sliderNumberOfImagesElem,self.labelNumberOfImagesElem,minNumberOfImage,maxNumberOfImage);
                self.createSliderWithoutAmountPrint(self.sliderNumberOfSlidesElem,self.labelNumberOfSlidesElem,minNumberOfSlide,maxNumberOfSlide);
                self.createSliderWithoutAmountPrint(self.sliderNumberOfAnnotationsElem,self.labelNumberOfAnnotationsElem,minNumberOfAnnotation,maxNumberOfAnnotation);
            }});
    },
    showAllProject:function() {
        var self = this;
        $(self.searchProjectTextBoxElem).val("");

        $(self.searchProjectCheckedOntologiesElem).attr("checked", false);

        //addProjectCheckedUsersCheckboxElem


        self.resetSlider(self.sliderNumberOfImagesElem);
        self.resetSlider(self.sliderNumberOfSlidesElem);
        self.resetSlider(self.sliderNumberOfAnnotationsElem);

        self.searchProject();
        //self.filterProjects("");
    },
    resetSlider : function(sliderElem) {
        //put the min slider cursor to min and the other to max
        var min = $(sliderElem).slider( "option", "min");
        var max = $(sliderElem).slider( "option", "max");
        $(sliderElem).slider( "values", [min,max] );
    },
    searchProject : function() {

        var self = this;
        var searchText = $(self.searchProjectTextBoxElem).val();

        //get ontologies list for ontology
        var searchOntologies = new Array();
        $.each($(self.searchProjectCheckedOntologiesElem), function(index, value) {
            var idOntology =  $(value).attr('id').replace("ontologies","");
            searchOntologies.push(idOntology);
        });

        var numberOfImages = new Array();
        numberOfImages.push($(self.sliderNumberOfImagesElem).slider( "values", 0 ));
        numberOfImages.push($(self.sliderNumberOfImagesElem).slider( "values", 1 ));

        var numberOfSlides = new Array();
        numberOfSlides.push($(self.sliderNumberOfSlidesElem).slider( "values", 0 ));
        numberOfSlides.push($(self.sliderNumberOfSlidesElem).slider( "values", 1 ));

        var numberOfAnnotations = new Array();
        numberOfAnnotations.push($(self.sliderNumberOfAnnotationsElem).slider( "values", 0 ));
        numberOfAnnotations.push($(self.sliderNumberOfAnnotationsElem).slider( "values", 1 ));
        console.log("filter project : " + numberOfImages);
        self.filterProjects(searchText==""?undefined:searchText,searchOntologies.length==0?undefined:searchOntologies,numberOfImages,numberOfSlides,numberOfAnnotations);
    },
    showAddProjectPanel : function() {
        var self = this;
        if(self.addProjectDialog==null)
        {
            //Build dialog
            console.log("build dialog");
            self.addProjectDialog = new AddProjectDialog({projectsPanel:self,el:self.el}).render();
        }
        self.addProjectDialog.open();
    },

    //show only project that have searchText value in their name and a ontology from searchOntologies.
    filterProjects : function(searchText,searchOntologies,searchNumberOfImages,searchNumberOfSlides,searchNumberOfAnnotations) {
        var self = this;
        self.projects = new ProjectCollection({user : self.userID}).fetch({
            success : function (collection, response) {
                var projects =  new ProjectCollection(collection.models);

                //each search function takes a search data and a collection and it return a collection without elem that
                //don't match with data search
                projects = self.filterByProjectsByName(searchText,projects);
                projects = self.filterProjectsByOntology(searchOntologies,projects);
                projects = self.filterProjectsByNumberOfImages(searchNumberOfImages,projects);
                projects = self.filterProjectsByNumberOfSlides(searchNumberOfSlides,projects);
                projects = self.filterProjectsByNumberOfAnnotations(searchNumberOfAnnotations,projects);
                //add here filter function


                collection.each(function(project) {
                    //if project is in project result list, show it
                    if(projects.get(project.id)!=null)
                        $(self.projectListElem+project.id).show();
                    else
                        $(self.projectListElem+project.id).hide();
                });
            }
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
            if(searchNumberOfImages[0]>numberOfImages || searchNumberOfImages[1]<numberOfImages)
                projectNewList.remove(project);
        });
        return projectNewList;
    },
    filterProjectsByNumberOfSlides : function(searchNumberOfSlides,projectOldList) {
        var self = this;
        var projectNewList =  new ProjectCollection(projectOldList.models);
        projectOldList.each(function(project) {
            var numberOfSlides = project.get('numberOfSlides');
            if(searchNumberOfSlides[0]>numberOfSlides || searchNumberOfSlides[1]<numberOfSlides)
                projectNewList.remove(project);
        });
        return projectNewList;
    },
    filterProjectsByNumberOfAnnotations : function(searchNumberOfAnnotations,projectOldList) {
        var self = this;
        var projectNewList =  new ProjectCollection(projectOldList.models);
        projectOldList.each(function(project) {
            var numberOfAnnotations = project.get('numberOfAnnotations');
            if(searchNumberOfAnnotations[0]>numberOfAnnotations || searchNumberOfAnnotations[1]<numberOfAnnotations)
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

            //create panel for a specific project
            var panel = new ProjectPanelView({
                model : project,
                projectsPanel : self
            }).render();

            $(self.projectListElem).append(panel.el);

        });
        //autocomplete
        $(self.searchProjectTextBoxElem).autocomplete({
            minLength : 0, //with min=0, if user erase its text, it will show all project withouth name constraint
            source : projectNameArray,
            select : function (event,ui)
            {
                $(self.searchProjectTextBoxElem).val(ui.item.label)
                self.searchProject();

            },
            search : function(event)
            {

                console.log("TEXT:"+$(self.searchProjectTextBoxElem).val());
                self.searchProject();
            }
        });

    }
});
