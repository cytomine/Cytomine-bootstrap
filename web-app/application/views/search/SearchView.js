var SearchView = Backbone.View.extend({
    resultTpls : null,
    initialize: function (options) {

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
        _.each(words,function(word) {
            str = str.replace(word.toUpperCase(),"<strong>"+word.toUpperCase()+"</strong>")
            str = str.replace(word.toLowerCase(),"<strong>"+word.toLowerCase()+"</strong>")
        });
        return str;
    },
    refresh : function(project) {

    }


});