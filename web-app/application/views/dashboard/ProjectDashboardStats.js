var ProjectDashboardStats = Backbone.View.extend({

    initialize : function () {
        this.noDataAlert = _.template("<br /><br /><div class='alert alert-block'>No data to display</div>",{});
    },
    fetchStats : function (terms) {
        var self = this;

        //Annotations by terms
        var statsCollection = new StatsTermCollection({project:self.model.get('id')});
        var statsCallback = function(collection, response) {
            //Check if there is something to display
            self.drawPieChart(collection, response);
            self.drawColumnChart(collection, response);
        };
        statsCollection.fetch({
            success : function(model, response) {
                statsCallback(model, response); //fonctionne mais très bourrin de tout refaire à chaque fois...
            }
        });
        //Annotations by user
        new StatsUserCollection({project:self.model.get('id')}).fetch({
            success : function(collection, response) {
                self.drawUserNbAnnotationsChart(collection, response);

            }
        });
        new StatsUserAnnotationCollection({project:self.model.get('id')}).fetch({
            success : function(collection, response) {
                var nbCharts = _.size(collection);
                self.drawUserAnnotationsChart(collection, undefined, response);
            }
        });
        new StatsTermSlideCollection({project:self.model.get('id')}).fetch({
            success : function(collection, response) {
                self.drawTermSlideChart(collection, response);
            }
        });

        new StatsUserSlideCollection({project:self.model.get('id')}).fetch({
            success : function(collection, response) {
                self.drawUserSlideChart(collection, response);
            }
        });

   },
   drawUserAnnotationsChart : function (collection, currentUser, response) {
      var self = this;
      // Create and populate the data table.
      var data = new google.visualization.DataTable();
      var cpt = -1;
      var first = collection.at(0);

        //init users
        data.addColumn('string', 'Terms');
        collection.each(function (item){
            cpt++;
            if (cpt != currentUser && currentUser != undefined) return;
            data.addColumn('number', item.get("key"));
        });

        data.addRows(_.size(first.get("terms")));

        //init terms
        var j = 0;
        _.each(first.get("terms"), function (term){
            data.setValue(j, 0, term.name);
            j++;
        });
        cpt = -1;
        var i = 1;
        collection.each(function (item){
            cpt++;
            if (cpt != currentUser && currentUser != undefined) return;
            var j = 0;
            _.each(item.get("terms"), function (term){
                data.setValue(j, i, term.value);
                j++;
            });
            i++;

        });
        var width = Math.round($(window).width() - 95);
        // Create and draw the visualization.
        var chart = new google.visualization.ColumnChart(document.getElementById('userAnnotationsChart'));
        chart.draw(data,
                {title:"Term by users",
                    backgroundColor : "whiteSmoke",
                    width:width, height:350,
                    hAxis: {title: "Terms" }
                }
        );
        var handleClick = function(){
            var row = chart.getSelection()[0]['row'];
            var col = chart.getSelection()[0]['column'];
            var user = collection.at(col-1).get('id');
            var term = collection.at(col-1).get('terms')[row].id;
            var url = "#tabs-annotations-"+self.model.get("id")+"-"+term+"-"+user;
            window.app.controllers.browse.tabs.triggerRoute = false;
            window.app.controllers.browse.navigate(url, true);
            window.app.controllers.browse.tabs.triggerRoute = true;
        };
        google.visualization.events.addListener(chart,'select', handleClick);
    },
    drawUserNbAnnotationsChart : function (collection, response) {
        var self = this;
        $("#userNbAnnotationsChart").empty();
        var dataToShow = false;
        // Create and populate the data table.
        var data = new google.visualization.DataTable();

        data.addRows(_.size(collection));

        data.addColumn('string', 'Number');
        data.addColumn('number', 0);
        //var colors = [];
        var j = 0;
        collection.each(function(stat) {
            //colors.push(stat.get('color'));
            if (stat.get('value') > 0) dataToShow = true;
            data.setValue(j, 0, stat.get("key"));
            data.setValue(j, 1, stat.get("value"));
            j++;
        });
        var width = Math.round($(window).width()/2 - 95);
        // Create and draw the visualization.
        var chart = new google.visualization.ColumnChart(document.getElementById("userNbAnnotationsChart"));
        chart.draw(data,
                {title:"",
                    legend : "none",
                    width:width,
                    height:350,
                    backgroundColor : "whiteSmoke",
                    vAxis: {title: "Number of annotations"},
                    hAxis: {title: "Users" }
                }
        );

        var handleClick = function(){
            var row = chart.getSelection()[0]['row'];
            var column = chart.getSelection()[0]['column'];
            var key = data.getValue(row, 0);
            collection.each(function(stat) {
                if (stat.get("key") == key) {
                    var user = stat.get("id");
                    var url = "#tabs-annotations-"+self.model.get("id")+"-all-"+user;
                    window.app.controllers.browse.tabs.triggerRoute = false;
                    window.app.controllers.browse.navigate(url, true);
                    window.app.controllers.browse.tabs.triggerRoute = true;
                }
            });
        };
        google.visualization.events.addListener(chart,'select', handleClick);
        $("#userNbAnnotationsChart").show();
    },
    drawPieChart : function (collection, response) {
        if (this.model.get('numberOfAnnotations') == 0) {
            //set no data to display if pie Chart
            $("#projectPieChart").html(this.noDataAlert);
            return;
        };
        $("#projectPieChart").empty();
        var self = this;
        // Create and populate the data table.
        var data = new google.visualization.DataTable();
        data.addColumn('string', 'Term');
        data.addColumn('number', 'Number of annotations');
        data.addRows(_.size(collection));
        var i = 0;
        var colors = [];
        collection.each(function(stat) {
            colors.push(stat.get('color'));
            data.setValue(i,0, stat.get('key'));
            data.setValue(i,1, stat.get('value'));
            i++;
        });
        var width = Math.round($(window).width()/2 - 95);
        // Create and draw the visualization.
        var chart = new google.visualization.PieChart(document.getElementById('projectPieChart'));
        chart.draw(data, {width: width, height: 350,title:"", backgroundColor : "whiteSmoke",colors : colors});
        var handleClick = function(){
            var row = chart.getSelection()[0]['row'];
            var column = chart.getSelection()[0]['column'];
            var key = data.getValue(row, 0);
            collection.each(function(stat) {
                if (stat.get("key") == key) {
                    var term = stat.get("id");
                    var url = "#tabs-annotations-"+self.model.get("id")+"-"+term+"-all";
                    window.app.controllers.browse.tabs.triggerRoute = false;
                    window.app.controllers.browse.navigate(url, true);
                    window.app.controllers.browse.tabs.triggerRoute = true;
                }
            });
        };
        google.visualization.events.addListener(chart,'select', handleClick);
    },
    drawColumnChart : function (collection, response) {
        var self = this;
        $("#projectColumnChart").empty();
        var dataToShow = false;
        // Create and populate the data table.
        var data = new google.visualization.DataTable();

        data.addRows(_.size(collection));

        data.addColumn('string', 'Number');
        data.addColumn('number', 0);
        var colors = [];
        var j = 0;
        collection.each(function(stat) {
            colors.push(stat.get('color'));
            if (stat.get('value') > 0) dataToShow = true;
            data.setValue(j, 0, stat.get("key"));
            data.setValue(j, 1, stat.get("value"));
            j++;
        });
        var width = Math.round($(window).width()/2 - 95);
        // Create and draw the visualization.
        var chart = new google.visualization.ColumnChart(document.getElementById("projectColumnChart"));
        chart.draw(data,
                {title:"",
                    legend : "none",
                    backgroundColor : "whiteSmoke",
                    width:width, height:350,
                    vAxis: {title: "Number of annotations"},
                    hAxis: {title: "Terms" }
                }
        );
        var handleClick = function(){
            var row = chart.getSelection()[0]['row'];
            var column = chart.getSelection()[0]['column'];
            var key = data.getValue(row, 0);
            collection.each(function(stat) {
                if (stat.get("key") == key) {
                    var term = stat.get("id");
                    var url = "#tabs-annotations-"+self.model.get("id")+"-"+term+"-all";
                    window.app.controllers.browse.tabs.triggerRoute = false;
                    window.app.controllers.browse.navigate(url, true);
                    window.app.controllers.browse.tabs.triggerRoute = true;
                }
            });
        };
        google.visualization.events.addListener(chart,'select', handleClick);
        $("#projectColumnChart").show();

    },


    drawTermSlideChart : function(collection, response){
        var self = this;
        var dataToShow = false;
        // Create and populate the data table.
        var data = new google.visualization.DataTable();

        data.addRows(_.size(collection));

        data.addColumn('string', 'Term');
        data.addColumn('number', 0);
        var colors = [];
        var j = 0;
        collection.each(function(stat) {
            data.setValue(j, 0, stat.get("key"));
            data.setValue(j, 1, stat.get("value"));
            j++;
        });
        var width = Math.round($(window).width()/2 - 95);
        // Create and draw the visualization.
        var chart = new google.visualization.ColumnChart(document.getElementById("termSlideAnnotationsChart"));
        chart.draw(data,
                {title:"",
                    legend : "none",
                    backgroundColor : "whiteSmoke",
                    width:width, height:350,
                    vAxis: {title: "Slides"},
                    hAxis: {title: "Terms" }
                }
        );
        var handleClick = function(){
            var row = chart.getSelection()[0]['row'];
            var column = chart.getSelection()[0]['column'];
            var key = data.getValue(row, 0);
            collection.each(function(stat) {
                if (stat.get("key") == key) {
                    var term = stat.get("id");
                    var url = "#tabs-annotations-"+self.model.get("id")+"-"+term+"-";
                    window.app.controllers.browse.tabs.triggerRoute = false;
                    window.app.controllers.browse.navigate(url, true);
                    window.app.controllers.browse.tabs.triggerRoute = true;
                }
            });
        };
        google.visualization.events.addListener(chart,'select', handleClick);
    },
    drawUserSlideChart : function(collection, response){
        var self = this;

        // Create and populate the data table.
        var data = new google.visualization.DataTable();

        data.addRows(_.size(collection));

        data.addColumn('string', 'User');
        data.addColumn('number', 0);
        var colors = [];
        var j = 0;
        collection.each(function(stat) {
            data.setValue(j, 0, stat.get("key"));
            data.setValue(j, 1, stat.get("value"));
            j++;
        });
        var width = Math.round($(window).width()/2 - 95);
        // Create and draw the visualization.
        var chart = new google.visualization.ColumnChart(document.getElementById("userSlideAnnotationsChart"));
        chart.draw(data,
                {title:"",
                    legend : "none",
                    backgroundColor : "whiteSmoke",
                    enableInteractivity : true,
                    width:width, height:350,
                    vAxis: {title: "Slides"},
                    hAxis: {title: "Users"}
                }
        );
        var handleClick = function(){
            var row = chart.getSelection()[0]['row'];
            var column = chart.getSelection()[0]['column'];
            var key = data.getValue(row, 0);
            collection.each(function(stat) {
                if (stat.get("key") == key) {
                    var user = stat.get("id");
                    var url = "#tabs-annotations-"+self.model.get("id")+"-all-"+user;
                    window.app.controllers.browse.tabs.triggerRoute = false;
                    window.app.controllers.browse.navigate(url, true);
                    window.app.controllers.browse.tabs.triggerRoute = true;
                }
            });
        };
        google.visualization.events.addListener(chart,'select', handleClick);

   }
});