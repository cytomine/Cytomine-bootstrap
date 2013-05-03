var ReviewStatsListing = Backbone.View.extend({
    project: null,
    user : null,
    currentPage : 0,
    max : 5,
    collection : null,
    initialize: function (options) {
        this.container = options.container;
       this.project = options.project;
       this.image = options.image;
        this.user = options.user;
    },
    render: function () {
        var self = this;
        $("#buttonStatsReviewListing").find("button.previous").click(function() {
            self.previous();
        });
        $("#buttonStatsReviewListing").find("button.next").click(function() {
            self.next();
        });

        self.refresh();
        return this;
    },
    refresh :function() {
        var self = this;

        $.get("/api/imageinstance/"+self.image+"/reviewedannotation/stats.json", function(data) {
            $(self.el).find("#userStatsReviewListing").empty();
            $(self.el).find("#userStatsReviewListing").append("<ul></ul>");


            var i = 0;
            _.each (data.collection, function (item){
                self.collection = data.collection;

                if(i>=(self.currentPage*self.max) && i<((self.currentPage+1)*self.max)) {
                    var user = window.app.models.projectUser.get(item.user)
                    if(!user) {
                        user = window.app.models.projectUserJob.get(item.user)
                    }
                    console.log($(self.el).find("#userStatsReviewListing").find("li").length);
                    console.log(user.prettyName());

                    var name = user.prettyName()
                    var printed = name+ ": " + item.reviewed  + " / " + item.all + " reviewed";
                    if(self.user == user.id) {
                        printed = '<strong>'+printed+'</strong>';
                    }

                    $(self.el).find("#userStatsReviewListing").find("ul").append(printed+"<br>");
                }
                i++;
            });
        });
    } ,
    next : function() {
        var self = this;
         if(self.collection && self.collection.length>((this.currentPage+1)*this.max)) {
             this.currentPage=this.currentPage+1;
             self.refresh();
         }
    },
    previous : function() {
        var self = this;
        if(self.collection && self.currentPage>0) {
            this.currentPage=this.currentPage-1;
            self.refresh();
        }
    }
});