var SearchView = Backbone.View.extend({
    initialize: function (options) {

    },
    render: function () {
        console.log("render");
        var self = this;

        if(window.app.models.projects.length>1) { //don't know why an empty window.app.models.projects has 1 item
            self.doLayout();
        } else {
            window.app.models.projects.fetch({
                success: function (collection1, response) {
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
        self.buildProjectCriteria();


    },
    buildSearchCriteria : function() {
        var self = this;
        $(self.el).find(".search-button").click(function() {
            self.doRequestFirstStep();
        });
    },
    buildDomainCriteria : function() {
        var self = this;
        $(self.el).find("#domainTypeToShow").change(function(it) {
            self.doRequestFirstStep();
        });
    },
    buildProjectCriteria : function() {
        $('#magicsuggestProjectSearch').magicSuggest({
            data: _.map(
                window.app.models.projects.models,
                function(item) {return {id:item.id,name:item.get('name')}})
        });
    },
    getDomainCriteria : function() {
        var val = $('input[name=options]:checked', '#domainTypeToShow').val();
        if(val=="all") return null;
        else return val;
    },
    doRequestFirstStep : function() {
        var self = this;
        var resultArea = $(self.el).find(".search-result");
        var words = self.extractWordsFromQueryStr($(self.el).find(".search-area").val());
        resultArea.empty();
        resultArea.append('<img class="img-responsive center-block" src="images/loadingbig.gif"/></div>');

        var criteria = {"expr":words.join(",")};

        var domainCriteria = self.getDomainCriteria();
        if(domainCriteria!=null) {
            criteria.domain = domainCriteria;
        }

        criteria.toQueryString = function() {
            var properties = $.extend(true, {}, this);
            properties.toQueryString = undefined;
            return _.map(properties, function(num, key){ return key + "=" + num;}).join("&")
        }
        $.get("/api/search-engine.json?"+criteria.toQueryString(),function (data) {

            var results = data.collection;
            var ids = _.pluck(results, 'id');
            var thumb = new SearchResultView({
                listIds: ids,
                criteria: criteria,
                words : words,
                el : resultArea
            }).render();

        }).fail(function (json) {
            console.log("failed! " + json)
        });
    },
    refresh : function(project) {

    },
    extractWordsFromQueryStr : function(queryString) {
        var words = [];
        var currentWord = "";
        var insideQuote = false;

        _.each(queryString,function(char) {

            if(char==" " && !insideQuote) {
                words.push(currentWord);
                currentWord = "";
            } else if(char=="\"") {
                insideQuote = !insideQuote;
            } else {
                currentWord = currentWord +char;
            }
        });
        words.push(currentWord);
        console.log("words=");
        console.log(words);
        console.log("words=");
        console.log(_.reject(words,function(word) {return word==""}));
        return _.reject(words,function(word) {return word==""});
    }

});