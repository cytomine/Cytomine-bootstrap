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

var SearchResultView = Backbone.View.extend({
    totalSize : null,
    totalPerPage : 10,
    pagination_window: 3,
    page : 0,
    resultTpls : null,
    resultTplsList : null,
    initialize: function (options) {
        this.listIds = options.listIds;
        this.criteria = options.criteria;
        this.words = options.words;
        this.totalSize = options.listIds.length;
    },
    render: function () {
        console.log("render");
        var self = this;
        require([
            "text!application/templates/search/SearchResult.tpl.html","text!application/templates/search/SearchResultList.tpl.html"
        ], function (tpl,tplList) {
            self.resultTpls = tpl;
            self.resultTplsList = tplList;
            self.doLayout();

        });
        return this;
    },
    doLayout: function () {
        var self = this;
        $(self.el).empty();
        $(self.el).append('<img class="img-responsive center-block" src="images/loadingbig.gif"/></div>');

        var resultToPrint = self.getResultsForPage(self.listIds,self.page,self.totalPerPage);

        //add result
        $.get("/api/search-result.json?"+this.criteria.toQueryString()+"&ids="+resultToPrint.join(","),function (data2) {
            $(self.el).empty();
            $(self.el).append(self.resultTplsList);
            //add pagination button TOP
            self.initPagination("#pagination-search-top");
            self.initPagination("#pagination-search-bottom");
            var results = data2.collection;
            self.buildResultsView(results);

        }).fail(function (data) {
                $(self.el).empty();
                $(self.el).append('<div class="alert alert-danger" role="alert">'+data.responseJSON.errors+'</div>');
            });



    },
    getResultsForPage : function(listIds,page,totalPerPage) {
        var minI = page * totalPerPage;
        var maxI = minI + totalPerPage;
        return listIds.slice(minI, maxI);
    },
    buildItemBox : function(result,words) {
        var self = this;

        var matching = _.sortBy(result.matching, function(match){
            if(match.type=="domain") return 0;
            if(match.type=="property") return 1;
            if(match.type=="description") return 2;
            return 9;
        });
        var matchinStr = "";
        _.each(matching,function(match) {
            matchinStr = matchinStr + "<tr><td>"+match.type+"</td><td>"+ self.hightlightMatching(self.formatMatchingValue(words,match.value,match.type),words) + "</td></tr>";
        });

        return _.template(self.resultTpls,{id:result.id,name:result.name,url:result.url,fullClass: result.className,className:self.extractClassName(result.className), matching:matchinStr});

    },
    extractClassName : function(fullClass) {
        var fullClassSplit = fullClass.split(".");
        if(fullClassSplit.length>0) {
            return fullClassSplit[fullClassSplit.length-1];
        } else {
            return "Undefined";
        }
    },
    //just for description, keep only some piece of string around the word
    formatMatchingValue : function(words,value,type) {
        if(type=="property" || type=="domain") return value;
        var phrases = [];
        var numberOfCharAround = 30;
        var str = value;//value.replace(/<[^>]*>/g, ""); //remove html <...>
        _.each(words,function(word) {
            var index = str.indexOf(word,0);
            while(index!=-1) {
                var someWords = str.substring(index-numberOfCharAround,index+word.length+numberOfCharAround);
                console.log(someWords);
                phrases.push(someWords);
                index = str.indexOf(word,index+1);
            }
        });

        return "[...]" + phrases.join("[...]") + "[...]";
    },
    hightlightMatching : function(value, words) {
        var str = value;
        _.each(words,function(word) {//.replace(,"test")
            str = str.replace(new RegExp(word, "ig"),"<strong>"+word.toUpperCase()+"</strong>");
        });
        return str;
    },
    buildResultsView : function(results) {
        var self = this;

        $(self.el).find("#result-size").empty();
        $(self.el).find("#result-size").append("There are "+self.totalSize +" result").append((self.totalSize == 1) ? "" : "s");
        _.each(results,function(result) {
            console.log($(self.el).find(".result-box"));
            $(self.el).find(".search-result").append(self.buildItemBox(result,self.words));
        })
    },

    initPagination : function(paginateLocation) {
        var self = this;
        var nbPages = Math.ceil(self.totalSize/self.totalPerPage);
        console.log("initPagination="+nbPages);
        if(nbPages<2) {
            return;
        }

        var paginationTpl = '<div  id="pagination-search" style="margin-left: 10px;"><ul class="pagination" style="text-align: center;"></ul></div>';
        $(self.el).find(paginateLocation).append(paginationTpl);
        var className = (self.page == 0) ? "prev disabled" : "";
        var $pagination = $(self.el).find(paginateLocation).find("#pagination-search").find("ul");
        var pageLink = _.template("<li class='<%= className %>'><a data-page='<%= page %>' href='#'>&larr; Previous</a></li>", { className: className, page: self.page - 1});
        $pagination.append(pageLink);
        var shiftUp = (self.page - self.pagination_window < 0) ? Math.abs(self.page - self.pagination_window) : 0;
        var shiftDown = (self.page + self.pagination_window >= nbPages) ? Math.abs(self.pagination_window + self.page - nbPages + 1) : 0;

        for (var i = Math.max(0, self.page - self.pagination_window - shiftDown); i < Math.min(nbPages, self.page + self.pagination_window + shiftUp + 1); i++) {
            var linkID = "term-" + self.term + "-page-" + i;
            className = (i == self.page) ? "active" : "";
            pageLink = _.template("<li class='<%= className %>'><a data-page='<%= page %>' href='#'><%= page %></a></li>", {
                className: className,
                linkID: linkID,
                page: i
            });
            $pagination.append(pageLink);
        }
        var className = (self.page == nbPages - 1) ? "next disabled" : "";
        pageLink = _.template("<li class='<%= className %>'><a data-page='<%= page %>' href='#'>Next &rarr;</a></li>", { className: className, page: self.page + 1});
        $pagination.append(pageLink);
        console.log("initPagination="+$pagination.length);
        $pagination.find("a").click(function (event) {
            event.preventDefault();
            var page = parseInt($(this).attr("data-page"));
            if (page >= 0 && page < nbPages) {
                self.switchToPage(page);
            }
            return false;
        });

    },
    switchToPage: function (page) {
        var self = this;
        var resultArea = $(self.el).find(".search-result");
        var self = this;
        console.log("switch to page " + page);
        self.page = page;
//        resultArea.empty();
        self.refresh();
    },
    refresh : function() {
        this.doLayout();
    }
});