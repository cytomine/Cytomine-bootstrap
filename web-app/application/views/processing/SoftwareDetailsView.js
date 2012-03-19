var SoftwareDetailsView = Backbone.View.extend({
    software: null,
    project : null,
    initialize: function(options) {
        this.software = options.software;
        this.project = options.project;
    },
    render : function() {
        var self = this;
        require([
            "text!application/templates/processing/SoftwareDetails.tpl.html"
        ],
               function(SoftwareDetailsTpl) {
                   self.loadResult(SoftwareDetailsTpl);
               });
        return this;
    },
    loadResult : function (SoftwareDetailsTpl) {
        var self = this;
        var content = _.template(SoftwareDetailsTpl, {});
        $(self.el).empty();
        $(self.el).append(content);

        var width = ($(window).width() - 200);
        var height = ($(window).height() - 200);
        $(self.el).dialog({
            width: width,
            height: height,
            modal:true,
            buttons: [
                {
                    text: "Close",
                    click: function() {
                        $(this).dialog("close");
                    }
                }
            ]
        });

        self.printSoftwareName();
        self.printSoftwareInfo();
        self.printSoftwareState();
        self.printSoftwareParams();

    },
    printSoftwareName : function() {
        var self = this;
        $("#softwareDetailsPanel").find("#softwareName").append("<h2>"+self.software.get('name')+"</h2>");
    },
    printSoftwareInfo : function() {
        var self = this;
        $("#softwareDetailsPanel").find("#softwareInfo").append("<h4>ID: "+self.software.id+"</h4>");
        $("#softwareDetailsPanel").find("#softwareInfo").append("<h4>Service Name: "+self.software.get('serviceName')+"</h4>");
        $("#softwareDetailsPanel").find("#softwareInfo").append("<h4>Result Type: "+self.software.get('resultName')+"</h4>");


    },
    printSoftwareState : function() {
        var self = this;
        var detailsElem = $("#softwareDetailsPanel").find("#softwareInfoList");
        detailsElem.append('<ul></ul>');

        var ulDetailsElem = detailsElem.find("ul");
        ulDetailsElem.append('<li>Total Job : '+self.software.get('numberOfJob')+'</li>');
        ulDetailsElem.append('<li>Not Launch : <span class="badge badge-inverse">'+self.software.get('numberOfNotLaunch')+'</span></li>');
        ulDetailsElem.append('<li>In Queue : <span class="badge badge-inverse">'+self.software.get('numberOfInQueue')+'</span></li>');
        ulDetailsElem.append('<li>Running : <span class="badge badge-info">'+self.software.get('numberOfRunning')+'</span></li>');
        ulDetailsElem.append('<li>Success : <span class="badge badge-success">'+self.software.get('numberOfSuccess')+'</span></li>');
        ulDetailsElem.append('<li>Indeterminate : <span class="badge">'+self.software.get('numberOfIndeterminate')+'</span></li>');
        ulDetailsElem.append('<li>Wait : <span class="badge badge-warning">'+self.software.get('numberOfWait')+'</span></li>');
        ulDetailsElem.append('<li>Failed : <span class="badge badge-error">'+self.software.get('numberOfFailed')+'</span></li>');


        self.printAcitivtyDiagram();
    },
    printSoftwareParams : function() {
        var self = this;
        $('#softwareParamsTable').find("tbody").empty();
        var datatable = $('#softwareParamsTable').dataTable();
        datatable.fnClearTable();
        //print data from project image table
        var tbody = $('#softwareParamsTable').find("tbody");


         _.each(self.software.get('parameters'), function (param) {
            var name = '<td>'+param.name+'</td>';
            var type = '<td style="text-align:center;">'+param.type+'</td>';
            var defaultVal = '<td>'+param.defaultParamValue+'</td>';
            //var require = '<td style="text-align:center;">'+param.required+'</td>';
            var checked = "";
             if(param.required) checked = 'checked="yes"';
            var require = '<td style="text-align:center;"><input type="checkbox" '+checked+' name="sports" value="basketball" /></td>';


            var index = '<td style="text-align:center;">'+param.index+'</td>';


            tbody.append('<tr>'+ name + type +defaultVal+require+index+'</tr>');
         });

        var width = Math.min($("#softwareParams").width()-100,1000);
        console.log("width="+width);
        $('#softwareParamsTable').dataTable( {
            //"sDom": "<'row'<'span6'l><'span6'f>r>t<'row'<'span6'i><'span6'p>>",
            "sPaginationType": "bootstrap",
            "oLanguage": {
                "sLengthMenu": "_MENU_ records per page"
            },
            "iDisplayLength": 999999 ,
            "bLengthChange" : false,
            bDestroy: true,
                 "aoColumnDefs": [
                { "sWidth": "30%", "aTargets": [ 0 ] },
                { "sWidth": "10%", "aTargets": [ 1 ] },
                { "sWidth": "40%", "aTargets": [ 2 ] },
                { "sWidth": "10%", "aTargets": [ 3 ] },
                { "sWidth": "10%", "aTargets": [ 4 ] }
             ]
        });
    },
    printAcitivtyDiagram : function() {
        var self = this;
        var software = self.software;
        var data = new google.visualization.DataTable();
        data.addColumn('string', 'Success');
        data.addColumn('number', 'Succes rate');
         console.log("printAcitivtyDiagram="+software.id);
         data.addRows([
             ['Not Launch',software.get('numberOfNotLaunch')],
             ['In Queue',software.get('numberOfInQueue')],
             ['Running',software.get('numberOfRunning')],
             ['Success',software.get('numberOfSuccess')],
             ['Failed',software.get('numberOfFailed')],
             ['Indeterminate',software.get('numberOfIndeterminate')],
             ['Wait',software.get('numberOfWait')]
        ]);
        var width = $("#softwareInfoChart").width()-100;
        var options = {
          title: 'Job software status for all project',
          legend: {position : "right"},
          width: width, height: 150,
          vAxis: {title: "Success rate"},
          hAxis: {title: "#"},
          backgroundColor : "whiteSmoke",
            strictFirstColumnType: false,
            is3D: true,
          lineWidth: 1,
          colors : ["#434141","#65d7f8","#005ccc","#52a652","#c43c35","#434343","#faaa38"]
        };

        var chart = new google.visualization.PieChart(document.getElementById('softwareInfoChart'));
        chart.draw(data, options);
    }
});