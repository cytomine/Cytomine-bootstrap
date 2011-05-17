var ProjectSearchPanel = Backbone.View.extend({
    ontologies : null,
    idUser : null,
    container : null,
    projectsPanel : null,
    allProjectsButtonElem: "#projectallbutton",
    addProjectButtonElem : "#projectaddbutton",
    searchProjectOntolgiesListElem : "#ontologyChoiceList",
    sliderNumberOfImagesElem : "#numberofimageSlider",
    labelNumberOfImagesElem : "#amountNumberOfImages",
    sliderNumberOfSlidesElem : "#numberofslideSlider",
    labelNumberOfSlidesElem : "#amountNumberOfSlides",
    sliderNumberOfAnnotationsElem : "#numberofannotationSlider",
    labelNumberOfAnnotationsElem : "#amountNumberOfAnnotations",
    searchProjectTextBoxElem : "#projectsearchtextbox",
    searchProjectButtonElem : "#projectsearchbutton",
    searchProjectCheckedOntologiesElem : 'input[type=checkbox][name=ontology]:checked',
    addProjectCheckedOntologiesRadioElem : 'input[type=radio][name=ontologyradio]:checked',
    addProjectCheckedUsersCheckboxElem : 'input[type=checkbox][name=usercheckbox]:checked',
    initialize: function(options) {
        this.ontologies = options.ontologies;
        this.idUser = options.idUser;
        this.container = options.container;
        this.projectsPanel = options.projectsPanel;
    },
    events: {
        "click .addProject": "showAddProjectPanel",
        "click .searchProjectCriteria": "searchProject",
        "click .showAllProject": "showAllProject"
    },
    render : function() {

        var self = this;
        var search = ich.projectviewsearchtpl({}, true);
        $(this.el).empty();
        $(this.el).append(search);



        self.loadPanelAndButton();

        self.loadSlider();

        self.loadAutocomplete();

    },
    /**
     * Load panel and all buttons/checkbox
     */
    loadPanelAndButton : function() {
        var self = this;
        //create search panel
        $(self.el).find("#searchProjectPanel").panel({
            collapseSpeed:100
        });

        //configure "all projects" button
        $(self.el).find(self.allProjectsButtonElem).button({
            icons : {secondary: "ui-icon-refresh" }

        });

        //configure "add projects" button
        $(self.el).find(self.addProjectButtonElem).button({
            icons : {secondary: "ui-icon-plus" }
        });

        //render ontologies choice
        self.ontologies = window.app.models.ontologies;
        self.ontologies.each(function(ontology) {
            var choice = ich.ontologieschoicetpl({id:ontology.id,name:ontology.get("name")}, true);
            $(self.searchProjectOntolgiesListElem).append(choice);
        });
    },
    /**
     * Load slider for images, annotations,.. number and compute the min/max value
     */
    loadSlider : function() {

        var self = this;
        //init slider to serach by slides number, images number...
        var minNumberOfImage = Number.MAX_VALUE;
        var maxNumberOfImage = 0;
        var minNumberOfSlide = Number.MAX_VALUE;
        var maxNumberOfSlide = 0;
        var minNumberOfAnnotation = Number.MAX_VALUE;
        var maxNumberOfAnnotation = 0;
        self.model.each(function(project) {

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
    },
    /**
     * Create autocomplete project name box in the textbox
     */
    loadAutocomplete : function() {
        var self = this;
        //array for autocompletion
        var projectNameArray = new Array();

        self.model.each(function(project) {
            projectNameArray.push(project.get('name'));
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
    },
    /**
     * Create a slider elem in slideElem with min/max value and a label with its amount in labelElem
     * @param sliderElem Html Element that will be a slider
     * @param labelElem Html Element that will print info
     * @param min  Minimum value for slider
     * @param max Maximum value for slider
     */
    createSliderWithoutAmountPrint : function(sliderElem, labelElem,min,max) {
        var self = this;

        console.log("sliderElem="+sliderElem + " min="+min + " et max="+ max);
        $(sliderElem).slider({
            range: true,
            min : min,
            max : max,
            values: [ min, max ],
            change: function( event, ui ) {
                $(labelElem).val( "" + ui.values[ 0 ] + " - " + ui.values[ 1 ] );
                self.searchProject();
            }
        });
        $(labelElem).val( "" + $(sliderElem).slider( "values", 0 ) +" - " + $(sliderElem).slider( "values", 1 ) );
    },
    /**
     * Refresh search panel with all projects info
     * @param Projects Projects list
     */
    refreshSearchPanel : function(Projects) {

        //refresh item from search panel
        //ex: if a user add 1 slide to the project that have the hight number of slide, number of slides slider value must be change
        var self = this;
        console.log("refresh projects panel");
        self.loadSlider();
        self.loadAutocomplete();
    },
    /**
     * Reset every item in the form by its "default" value (textbox empty, nothing check,..)
     */
    showAllProject:function() {
        var self = this;

        //reset every element
        $(self.searchProjectTextBoxElem).val("");
        $(self.searchProjectCheckedOntologiesElem).attr("checked", false);
        self.resetSlider(self.sliderNumberOfImagesElem);
        self.resetSlider(self.sliderNumberOfSlidesElem);
        self.resetSlider(self.sliderNumberOfAnnotationsElem);

        //start a search
        self.searchProject();
    },
    /**
     * Reset slider by putting its first cursor to min and the second one to max
     * @param sliderElem Element for slider
     */
    resetSlider : function(sliderElem) {
        //put the min slider cursor to min and the other to max
        var min = $(sliderElem).slider( "option", "min");
        var max = $(sliderElem).slider( "option", "max");
        $(sliderElem).slider( "values", [min,max] );
    },
    /**
     * Search project with all info from the form
     */
    searchProject : function() {

        var self = this;

        //get name
        var searchText = $(self.searchProjectTextBoxElem).val();

        //get ontologies
        var searchOntologies = new Array();
        $.each($(self.searchProjectCheckedOntologiesElem), function(index, value) {
            var idOntology =  $(value).attr('id').replace("ontologies","");
            searchOntologies.push(idOntology);
        });

        //get number of images [min,max]
        var numberOfImages = new Array();
        numberOfImages.push($(self.sliderNumberOfImagesElem).slider( "values", 0 ));
        numberOfImages.push($(self.sliderNumberOfImagesElem).slider( "values", 1 ));

        //get number of slides [min,max]
        var numberOfSlides = new Array();
        numberOfSlides.push($(self.sliderNumberOfSlidesElem).slider( "values", 0 ));
        numberOfSlides.push($(self.sliderNumberOfSlidesElem).slider( "values", 1 ));

        //get number of annotation [min,max]
        var numberOfAnnotations = new Array();
        numberOfAnnotations.push($(self.sliderNumberOfAnnotationsElem).slider( "values", 0 ));
        numberOfAnnotations.push($(self.sliderNumberOfAnnotationsElem).slider( "values", 1 ));
        console.log("filter project : " + numberOfImages);
        self.filterProjects(searchText==""?undefined:searchText,searchOntologies.length==0?undefined:searchOntologies,numberOfImages,numberOfSlides,numberOfAnnotations);
    },
    /**
     * Show dialog to add a project
     */
    showAddProjectPanel : function() {
        console.log("ProjectSearchPanel: showAddProjectPanel");
        var self = this;
        $('#addproject').remove();
        self.addProjectDialog = new AddProjectDialog({projectsPanel:self.projectsPanel,el:self.el}).render();
        self.addProjectDialog.open();
    },
    /**
     * Show only project that match with params
     * @param searchText Project Name
     * @param searchOntologies Ontologies
     * @param searchNumberOfImages Number of image array [min,max]
     * @param searchNumberOfSlides Number of slide array [min,max]
     * @param searchNumberOfAnnotations  Number of annotations array [min,max]
     */
    filterProjects : function(
            searchText,
            searchOntologies,
            searchNumberOfImages,
            searchNumberOfSlides,
            searchNumberOfAnnotations) {

        var self = this;
        var projects =  new ProjectCollection(self.model.models);

        //each search function takes a search data and a collection and it return a collection without elem that
        //don't match with data search
        projects = self.filterByProjectsByName(searchText,projects);
        projects = self.filterProjectsByOntology(searchOntologies,projects);
        projects = self.filterProjectsByNumberOfImages(searchNumberOfImages,projects);
        projects = self.filterProjectsByNumberOfSlides(searchNumberOfSlides,projects);
        projects = self.filterProjectsByNumberOfAnnotations(searchNumberOfAnnotations,projects);
        //add here filter function

        //show project from "projects" (and hide the other) in project view
        self.container.showProjects(projects);
    },
    filterProjectsOLD : function(
            searchText,
            searchOntologies,
            searchNumberOfImages,
            searchNumberOfSlides,
            searchNumberOfAnnotations) {



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

                //show project from "projects" (and hide the other) in project view
                self.container.showProjects(projects);
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

            var idOntology = project.get('ontology') +"";
            if(searchOntologies!=undefined && _.indexOf(searchOntologies,idOntology)==-1)
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
    }
});