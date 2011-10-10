var ProjectDashboardStats = Backbone.View.extend({
   fetchStats : function () {
      var self = this;
      if (self.model.get('numberOfAnnotations') == 0) return;
      //Annotations by terms
      var statsCollection = new StatsTermCollection({project:self.model.get('id')});
      var statsCallback = function(collection, response) {
         //Check if there is something to display
         self.drawPieChart(collection, response);
         self.drawColumnChart(collection, response);
      }
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
   },
   drawUserAnnotationsChart : function (collection, currentUser, response) {
      // Create and populate the data table.
      var data = new google.visualization.DataTable();
      var cpt = -1;
      var first = collection.at(0);

      //init users
      data.addColumn('string', 'Users');
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
      var i = 0;
      collection.each(function (item){
         cpt++;
         if (cpt != currentUser && currentUser != undefined) return;
         var j = 1;
         _.each(item.get("terms"), function (term){
            data.setValue(j-1, i+1, term.value);
            j++;
         });
         i++;

      });
      var width = Math.round($(window).width()/2 - 200);
      // Create and draw the visualization.
      new google.visualization.ColumnChart(document.getElementById('userAnnotationsChart')).
          draw(data,
          {title:"Term by users",
             width:width, height:350,
             hAxis: {title: "Users"}}
      );
   },
   drawUserNbAnnotationsChart : function (collection, response) {
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
      var width = Math.round($(window).width()/2 - 200);
      // Create and draw the visualization.
      new google.visualization.ColumnChart(document.getElementById("userNbAnnotationsChart")).
          draw(data,
          {title:"",
             width:width, height:350,
             vAxis: {title: "Number of annotations"},
             hAxis: {title: "Users"}}
      );
      $("#userNbAnnotationsChart").show();
   },
   drawPieChart : function (collection, response) {
      $("#projectPieChart").empty();
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
      var width = Math.round($(window).width()/2 - 200);
      // Create and draw the visualization.
      new google.visualization.PieChart(document.getElementById('projectPieChart')).
          draw(data, {width: width, height: 350,title:"", colors : colors});
   },
   drawColumnChart : function (collection, response) {
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
      var width = Math.round($(window).width()/2 - 200);
      // Create and draw the visualization.
      new google.visualization.ColumnChart(document.getElementById("projectColumnChart")).
          draw(data,
          {title:"",
             width:width, height:350,
             vAxis: {title: "Number of annotations"},
             hAxis: {title: "Terms"}}
      );
      $("#projectColumnChart").show();

   }
});