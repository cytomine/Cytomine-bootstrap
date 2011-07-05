var ProjectAddImageSearchPanel = Backbone.View.extend({
    images : null,
    tab : null,
    initialize: function(options) {
        var self = this;
        this.container = options.container;
        this.images = options.images;
        this.tab = options.tab;

    },
    render : function() {
        var self = this;
        require([
            "text!application/templates/project/ProjectAddImageSearchDialog.tpl.html"
        ],
               function(tpl) {
                   self.doLayout(tpl);
               });
        return this;
    },
    doLayout : function(tpl) {
        var self = this;
        console.log("Id project="+this.model.id);

        var json = self.model.toJSON();

        //Get ontology name
        var idOntology = json.ontology;
        json.ontologyId = idOntology;
        json.tab = self.tab;
        var dialog = _.template(tpl, json);
        $(self.el).append(dialog);

        console.log($(self.el).find("#tabsProjectaddimagedialog"+this.model.get('id')).length);
        // $(self.el).find("#tabsProjectaddimagedialog"+this.model.get('id')).tabs();

        self.renderImageListing();

        var imagesNameArray = new Array();

        //TODO: improve perf and fill it when browse in an other place?
        /*self.images.each(function(image) {
         imagesNameArray.push(image.get('filename'));
         }); */


        //autocomplete
        $("#filenamesearchtextboxup"+self.model.id+"-"+self.tab).autocomplete({
            minLength : 0, //with min=0, if user erase its text, it will show all project withouth name constraint
            source : imagesNameArray,
            select : function (event,ui)
            {
                $("#filenamesearchtextboxup"+self.model.id+"-"+self.tab).val(ui.item.label)
                self.container.searchImages();

            },
            search : function(event)
            {

                console.log("TEXT:"+$("#filenamesearchtextboxup"+self.model.id+"-"+self.tab).val());
                self.container.searchImages();
            }
        });

        $("#datestartsearchup"+self.model.id+"-"+self.tab).change(function() { self.container.searchImages(); });
        $("#dateendsearchup"+self.model.id+"-"+self.tab).change(function() { self.container.searchImages(); });

        return this;

    },
    search : function(images) {
        var self = this;
        var searchText = $("#filenamesearchtextboxup"+self.model.id+"-"+self.tab).val();
        var dateStart =  $("#datestartsearchup"+self.model.id+"-"+self.tab).datepicker("getDate");
        var dateEnd =  $("#dateendsearchup"+self.model.id+"-"+self.tab).datepicker("getDate");



        //console.log("dateStart="+dateStart);
        return self.filterImages(searchText==""?undefined:searchText,dateStart,dateEnd);
    },
    filterImages : function(searchText,dateStart,dateEnd) {

        var self = this;
        var images =  new ImageCollection(self.images.models);

        //each search function takes a search data and a collection and it return a collection without elem that
        //don't match with data search
        console.log("start images:"+images.length);
        images = self.filterByImagesByName(searchText,images);
        console.log("filter by name '" + searchText + "' images:"+images.length);
        images = self.filterByDateStart(dateStart,images);
        console.log("filter by date start images:"+images.length);
        images = self.filterByDateEnd(dateEnd,images);
        console.log("filter by date end images:"+images.length);
        //add here filter function
        return images;


    },
    filterByImagesByName : function(searchText,imagesOldList) {

        var imagesNewList =  new ImageCollection(imagesOldList.models);

        imagesOldList.each(function(image) {
            if(searchText!=undefined && !image.get('filename').toLowerCase().contains(searchText.toLowerCase()))
                imagesNewList.remove(image);
        });

        return imagesNewList;
    },

    filterByDateStart : function(dateStart,imagesOldList) {

        var imagesNewList =  new ImageCollection(imagesOldList.models);

        imagesOldList.each(function(image) {
            var dateAdded = new Date()
            dateAdded.setTime(image.get('created'));
            //console.log("dateAdded="+dateAdded + " dateStart=" + dateStart);
            if(dateStart!=undefined && dateAdded<dateStart) {
                imagesNewList.remove(image);
            }
        });

        return imagesNewList;

    },

    filterByDateEnd : function(dateEnd,imagesOldList) {
        var imagesNewList =  new ImageCollection(imagesOldList.models);

        imagesOldList.each(function(image) {
            var dateAdded = new Date()
            dateAdded.setTime(image.get('created'));
            //console.log("dateAdded="+dateAdded + " dateEnd=" + dateEnd);
            if(dateEnd!=undefined && dateAdded>dateEnd) {
                imagesNewList.remove(image);
            }
        });

        return imagesNewList;
    },

    renderImageListing : function() {
        var self = this;
        console.log("renderImageListing");


        $("#searchImagetPanelup"+self.model.id+"-"+self.tab).panel({
            collapseSpeed:100
        });



        $("#imagesallbutton"+self.model.id+"-"+self.tab).button({
            text: true
        });


        $("#imagesallbutton"+self.model.id+"-"+self.tab).click(function() {
            $("#filenamesearchtextboxup"+self.model.id+"-"+self.tab).val("");
            $("#datestartsearchup"+self.model.id+"-"+self.tab ).val("");
            $("#dateendsearchup"+self.model.id+"-"+self.tab).val("");
            self.container.searchImages();
        });

        $( "#datestartsearchup"+self.model.id+"-"+self.tab ).datepicker({
            onSelect: function(dateText, inst) { self.container.searchImages(); }
        });
        $( "#dateendsearchup"+self.model.id+"-"+self.tab ).datepicker({
            onSelect: function(dateText, inst) { self.container.searchImages(); }
        });


    }
});