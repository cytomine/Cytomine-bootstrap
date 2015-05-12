/*
 * Copyright (c) 2009-2015. Authors: see NOTICE file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

var SearchView = Backbone.View.extend({
    projects : null,
    suggestProject : null,
    currentFilter:null,
    render: function () {
        console.log("render");
        var self = this;

        if(window.app.models.projects.length>1) { //don't know why an empty window.app.models.projects has 1 item
            self.doLayout();
        } else {
            window.app.models.projects.fetch({
                success: function (collection1) {
                    window.app.models.projects = collection1;
                    self.doLayout();
                }});
        }
        return this;
    },
    doLayout: function () {
        console.log("search doLayout");
        var self = this;
        self.buildSearchCriteria();
        self.buildDomainCriteria();
        self.buildTypeCriteria();
        self.buildProjectCriteria();
        self.buildFiltersBox();


    },
    buildSearchCriteria : function() {
        var self = this;
        $(self.el).find(".search-button").click(function() {
            self.doRequestFirstStep();
        });
    },
    buildDomainCriteria : function() {
        var self = this;
        $(self.el).find("#domainTypeToShow").change(function() {
            self.doRequestFirstStep();
        });
        var domain = $("#domainTypeToShow input[checked]");
        domain.removeAttr( 'checked' );
        domain.prop('checked', true);
    },
    buildTypeCriteria : function() {
        var self = this;
        $(self.el).find("#attributeTosearch").change(function(it) {
            self.doRequestFirstStep();
        });
        var attribute = $("#attributeTosearch input[checked]");
        attribute.removeAttr( 'checked' );
        attribute.prop('checked', true);
    },
    buildProjectCriteria : function() {
        var self = this;
        self.suggestProject = $(self.el).find('#magicsuggestProjectSearch').magicSuggest({
            data: _.map(
                window.app.models.projects.models,
                function(item) {return {id:item.id,name:item.get('name')}})
        });
        $(self.suggestProject).on('selectionchange', function(e,m){

            for(var i =0;i<this.getSelection().length;i++){
                if(!$.isNumeric(this.getSelection()[i].id)) {
                    console.log("C'est ici que Xerxès s'exerça, que les vieux assyriens s'assirent, que des Parthes partirent, que des Thébains tombèrent, que des Athéniens s'atteignirent,que des Mèdes médirent, que des Perses se percèrent, que des Thraces tracèrent et que des Satrapes s'attrapèrent…");
                    var suggestedName = _.map(
                        this.getData(),
                        function(item) {return item.name.toLowerCase()});

                    var index = jQuery.inArray(this.getSelection()[i].name.toLowerCase(), suggestedName);
                    if(index >=0) {
                        console.log("found");
                        this.getSelection()[i].id = this.getData()[index].id;
                        this.getSelection()[i].name = this.getData()[index].name;
                    }
                    else {
                        console.log("not found");
                        this.removeFromSelection([this.getSelection()[i]]);
                    }
                }
            }
            self.projects = this.getValue();
            self.doRequestFirstStep();
        });
    },
    getDomainCriteria : function() {
        var val = $("#domainTypeToShow input[name=optionsDomain]:checked").val();
        if(val=="all"){
            return null;
        }
        else{
            return val;
        }
    },
    getTypeCriteria : function() {
        var val = $('#attributeTosearch input[name=optionsAtt]:checked').val();
        if(val=="all"){
            return null;
        }
        else {
            return val;
        }
    },
    doRequestFirstStep : function() {
        var self = this;
        var resultArea = $(self.el).find(".search-result");
        var words = self.extractWordsFromQueryStr($(self.el).find(".search-area").val());
        if(words.length==0){
            return;
        }
        resultArea.empty();
        resultArea.append('<img class="img-responsive center-block" src="images/loadingbig.gif"/></div>');

        var criteria = {"expr":words.join(",")};

        var domainCriteria = self.getDomainCriteria();
        if(domainCriteria!=null) {
            criteria.domain = domainCriteria;
        }
        var typeCriteria = self.getTypeCriteria();
        if(typeCriteria!=null) {
            criteria.types = typeCriteria;
        }
        var projectsCriteria = self.projects;
        if(projectsCriteria!=null) {
            criteria.projects = projectsCriteria;
        }
        criteria.toQueryString = function() {
            var properties = $.extend(true, {}, this);
            properties.toQueryString = undefined;
            return _.map(properties, function(num, key){ return key + "=" + num;}).join("&");
        };
        $.get("/api/search-engine.json?"+criteria.toQueryString(),function (data) {

            var results = data.collection;
            var ids = _.pluck(results, 'id');
            if(ids.length>0) {
                new SearchResultView({
                    listIds: ids,
                    criteria: criteria,
                    words : words,
                    el : resultArea
                }).render();
            } else {
                resultArea.empty();
                resultArea.append('<div class="alert alert-warning" role="alert">No result found!</div>');
            }
        }).fail(function (data) {
                resultArea.empty();
                resultArea.append('<div class="alert alert-danger" role="alert">'+data.responseJSON.errors+'</div>');
        });
    },
    refresh : function() {

    },
    extractWordsFromQueryStr : function(queryString) {
        var words = [];
        var currentWord = "";
        var insideQuote = false;

        _.each(queryString,function(chara) {

            if(chara==" " && !insideQuote) {
                words.push(currentWord);
                currentWord = "";
            }  else if(chara==String.fromCharCode(34)) { //ascii 34 = quote
                insideQuote = !insideQuote;
            } else {
                currentWord = currentWord +chara;
            }
        });
        words.push(currentWord);
        return _.reject(words,function(word) {return word==""});
    },
    buildFiltersBox : function() {
        var self = this;
        $(self.el).find(".save-filter-button").click(function() {
            self.saveCurrentFilter();
        });
        $(self.el).find(".delete-filter-button").click(function() {
            self.deleteSelectedFilter();
        });

        self.updateFiltersBox();
    },
    saveCurrentFilter : function() {
        var self = this;
        $(".alertFilter").hide(); //in case of there will an error previously

        console.log("save Filter");

        var currentFilter = new SearchEngineFilterModel();


        var domains = $('input[name=optionsDomain]:checked', '#domainTypeToShow').val();
        domains = (domains=="all") ? null : domains;

        var attributes = $('input[name=optionsAtt]:checked', '#attributeTosearch').val();
        attributes = (attributes=="all") ? null : attributes;

        var words = this.extractWordsFromQueryStr($(this.el).find(".search-area").val());
        words = (words.length==0) ? null : words;

        var projects = (this.projects == null) ? [] : this.projects;

        currentFilter.set("name", $('.newFilterName')[0].value);
        currentFilter.set("user", window.app.status.user.id);

        var filters = {
            words: words,
            attributes: [attributes],
            domainTypes: [domains],
            projects: projects,
            order: 'desc',
            sort: 'id',
            op: 'AND'
        };
        currentFilter.set("filters", filters);
        var invCallback = function(error) {
            //$(".alertFilter").text(error);
            //$(".alertFilter").show();
            window.app.view.message("Search Filters", error, "error");
        };
        currentFilter.setInvalidCallback(invCallback); // Invalidation doesn't throw the error in save method. Yipie !

        currentFilter.save({},{
            success: function (model, response) {
                model.set('id', response.callback.searchenginefilterID);
                self.updateFiltersBox(model);
            },
            error: function (model, response) {
                var json = JSON.parse(response.responseText);
                invCallback(json.errors);
            }
        });
    },
    deleteSelectedFilter : function() {
        var self = this;
        console.log("delete Filter");

        var toDel = $('#filterlist option:checked');
        console.log("id to delete ", toDel.val());

        var filterToDel = new SearchEngineFilterModel({id: toDel.val(), user: window.app.status.user.id});
         filterToDel.fetch({
             success: function () {
                 filterToDel.destroy({
                     success: function () {
                         self.updateFiltersBox();
                     }
                 });
             }
         });
    },
    updateFiltersBox : function(filterSelected) {
        var self = this;
        console.log("update Filter");

        var dropListFilters = $(self.el).find(".dropdown");
        dropListFilters.empty();


        var filters = new SearchEngineFilterCollection({user: window.app.status.user.id});
        filters.fetch({
            success: function (collection) {
                collection.unshift(new SearchEngineFilterModel());

                var onSelectedCallback = function(name, id) {
                    //Get complete informations
                    var filter = new SearchEngineFilterModel({id: id, user: window.app.status.user.id});
                    filter.fetch({
                        success: function (model) {
                            self.updateSearchParams(JSON.parse(model.toJSON().filters));
                        }
                    })
                };

                var filtersView  = new SearchEngineFiltersView({
                    collection : collection,
                    selectCallback : onSelectedCallback
                });
                dropListFilters.append(filtersView.render().el);


                if(filterSelected != null && filterSelected != undefined) {
                    $('#filterlist option[value='+filterSelected.get('id')+']').prop('selected', true);
                    //console.log($('#filterlist option'));
                }
            }
        });

    },
    updateSearchParams : function(filter) {
        var self = this;

        if(filter.domainTypes.length > 1 || filter.attributes.length > 1){
            alert("Not in the scope");
        }
        // Get values
        var domainType = filter.domainTypes[0];
        if(domainType == null || domainType == undefined)  {
            domainType = 'all';
        }
        var attributeType = filter.attributes[0];
        if(attributeType == null || attributeType == undefined)  {
            attributeType = 'all';
        }

        // test if all projects still exists ?
        var projectsIds = filter.projects;

        var words = filter.words;

        // Set domain
        var oldDomain = $("#domainTypeToShow input[name=optionsDomain]:checked");

        var domain = $('#domainTypeToShow input[value='+domainType+']');
        domain.prop('checked', true);

        // we also need to update the parent label.
        domain.parent().toggleClass("active");
        oldDomain.parent().toggleClass("active");


        // Set attribute
        var oldAttribute = $('#attributeTosearch input[name=optionsAtt]:checked');

        var attribute = $('#attributeTosearch input[value='+attributeType+']');
        attribute.prop('checked', true);

        // we also need to update the parent label.
        attribute.parent().toggleClass("active");
        oldAttribute.parent().toggleClass("active");


        // Set projects
        self.suggestProject.clear();
        var existingIds = jQuery.map( window.app.models.projects.models, function( a ) {
            return a.id;
        });
        var projectsList = [];
        for(var i=0; i<projectsIds.length; i++){
            // if the project are known by the context. BTW, we avoid deleted projects
            var index = jQuery.inArray(projectsIds[i], existingIds);
            if(index >=0){
                projectsList.push({id:projectsIds[index],name: window.app.models.projects.models[index].attributes.name});
            }
        }
        self.suggestProject.setSelection(projectsList);


        // Set words
        $(".search-area").val(words.join(" "));
    }
});