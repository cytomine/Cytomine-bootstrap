var SearchResultView = Backbone.View.extend({
    totalSize : null,
    totalPerPage : null,
    page : 0,
    initialize: function (options) {
        this.listIds = options.listIds;
    },
    render: function () {
        console.log("render");
        var self = this;
        require([
            "text!application/templates/search/SearchResult.tpl.html"
        ], function (tpl) {
            self.resultTpls = tpl;
            self.doLayout();

        });
        return this;
    },
    doLayout: function () {
        console.log("search doLayout");
        var self = this;
        console.log(self.el);
        console.log($(self.el).length);
        $(self.el).find(".search-button").click(function() {

            self.doRequestFirstStep();
        })
    },
    doRequestFirstStep : function() {
        var self = this;
        var resultArea = $(self.el).find(".search-result");
        var requestString = $(self.el).find(".search-area").val();
        var words = requestString.split(" ");
        resultArea.empty();
        $.get("/api/search-engine.json?expr="+words.join(","),function (data) {
            var results = data.collection;
            var ids = _.pluck(results, 'id');

            $.get("/api/search-result.json?expr="+words.join(",") + "&ids="+ids.join(","),function (data2) {
                var results = data2.collection;

                _.each(results,function(result) {
                    resultArea.append(self.buildItemBox(result,words));
                })

            }).fail(function (json) {
                    console.log("failed! " + json)
            });

        }).fail(function (json) {
            console.log("failed! " + json)
        });
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
            matchinStr = matchinStr + "<tr><td>"+match.type+"</td><td>"+ self.hightlightMatching(match.value,words) + "</td></tr>";
        });
//
//        <tr>
//            <td>1</td>
//            <td>Mark</td>
//            <td>Otto</td>
//            <td>@mdo</td>
//        </tr>

        return _.template(self.resultTpls,{id:result.id,name:result.name,fullClass: result.className,className:self.extractClassName(result.className), matching:matchinStr});

//        var str = "<div class='col-12 col-sm-12 col-lg-12'><h3>"+
//            self.extractClassName(result.className) + "</h3>" +
//            "<p>" +
//            "<ul>"+matchinStr+"</ul>" +
//            "</p>" +
//            '<a class="btn btn-default" href="#" role="button">View details »</a></p>'
//
//        return str;

    },
    extractClassName : function(fullClass) {
        var fullClassSplit = fullClass.split(".");
        if(fullClassSplit.length>0) {
            return fullClassSplit[fullClassSplit.length-1]
        } else {
            return "Undefined"
        }
    },
    hightlightMatching : function(value, words) {
        var str = value;
        _.each(words,function(word) {//.replace(,"test")
            str = str.replace(new RegExp(word, "ig"),"<strong>"+word.toUpperCase()+"</strong>");
        });
        return str;
    },




    initPagination: function () {
        var self = this;

        var nbPages = Math.ceil(self.totalSize/self.totalPerPage);
        console.log("initPagination="+nbPages);
        if(nbPages<2) {
            return;
        } else {

            require(["text!application/templates/dashboard/Pagination.tpl.html"], function (paginationTpl) {
                var pagination = _.template(paginationTpl, { term: "search"});
                $(self.el).append(pagination);
                var $pagination = $(self.el).find("#pagination-term-search").find("ul");

                var className = (self.page == 0) ? "prev disabled" : "";

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

            });



        }
    },
    switchToPage: function (page) {
        var resultArea = $(self.el).find(".search-result");
        var self = this;
        self.page = page;
        resultArea.empty();
        self.refresh();
    },
    refresh : function() {

    }
});