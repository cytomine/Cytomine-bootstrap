var UserDashboardView = Backbone.View.extend({
    tagName: "div",
    searchProjectPanelElem: "#searchProjectPanel",
    projectListElem: "#projectlist",
    projectList: null,
    addSlideDialog: null,
    initialize: function (options) {
        this.container = options.container;
        this.model = options.model;
        this.el = options.el;
        this.searchProjectPanel = null;
        this.addProjectDialog = null;
    },
    render: function () {
        console.log("UserDashboardView.render");
        var self = this;
        require([
            "text!application/templates/user/UserDashboardComponent.tpl.html",
            "text!application/templates/image/ImageThumbLight.tpl.html",
            "text!application/templates/user/UserStats.tpl.html",
            "text!application/templates/user/ProjectBox.tpl.html"
        ],
            function (tpl,tplImg,tpStat,tplProj) {
                self.doLayout(tpl,tplImg,tpStat, tplProj);
            });

        return this;
    },
    doLayout: function (tpl,tplImg,tpStat, tplProj) {
        var self = this;
        self.nbreAnnotation= null;
        self.nbreReviewedAnnotation= null;
        $("#userdashboard").html(_.template(tpl, {}));
        $("#userdashboard").css("display","inline");
        self.el = $("#userdashboard");

        self.initStats(tpStat);
        self.initLastAction();
////      //self.initLastNews();
        self.initGotoProject();
        self.initGotoImage();
//        self.initLastOpenedImage(tplImg);
//        self.initLastOpenProject(tplProj);

        return this;
    },
    initLastOpenProject : function(tplProj) {
        var self = this;
        var elem = $(self.el).find("#userDashboardLastProject");
        console.log("initLastOpenProject");
        $.get("/api/project/method/lastopened.json?max=5", function(data) {
            var collection = data.collection;
            elem.empty();
            elem.append('<div class="col-md-2 col-sm-2 stat well stat-first"><div class="data"><span class="number"></span>Last opened&nbsp;&nbsp;<br />Projects »</div><span class="date"></span></div>');
            _.each(collection,function(item) {

                var project = window.app.models.projects.get(item.id);

                if(project) {
                    //user has still access to this project
                    project.set({"dateConnection" : window.app.convertLongToDate(item.date)});
                    elem.append(_.template(tplProj, project.toJSON()));
                }
            });
        });

    },
    initStats : function(tpStat) {
        var self = this;
        var count = 0;
        var elem = $(self.el).find("#userStatsDashboard");

        var allGetLoaded = function() {
            if(self.nbreAnnotation!=null && self.nbreReviewedAnnotation!=null) {
                var imagesCount = 0;
                window.app.models.projects.each(function(project) {
                    imagesCount += project.get("numberOfImages");
                });
                elem.html(_.template(tpStat,{
                    prettyName : window.app.status.user.model.prettyName(),
                    projectCount : window.app.models.projects.length,
                    imagesCount : imagesCount,
                    annotationCount : self.nbreAnnotation,
                    reviewedCount : self.nbreReviewedAnnotation
                }));

            }

        }

        $.get("/api/user/"+window.app.status.user.id+"/userannotation/count.json", function(data) {
            self.nbreAnnotation = data.total;
            allGetLoaded();
        });
        $.get("/api/user/"+window.app.status.user.id+"/reviewedannotation/count.json", function(data) {
            self.nbreReviewedAnnotation = data.total;
            allGetLoaded();
        });


    },
    initLastAction : function() {
        var self = this;
        var elem =  $(self.el).find("#userdashboardLastAction");
        $.get("api/commandhistory.json?user="+window.app.status.user.id+"&max=100&offset=0", function(data) {
            var collection = data;
            var chartData = [
                {
                    "key" : "Activity" ,
                    "bar": false,
                    "values" : []
                }
            ];
            var refDateWhithoutHour = null;
            var refNbActivities = 0;
            _.each(collection,function(item) {
                var strDate = window.app.convertLongToDate(item.created);
                var action = _.template("<li><b><%= date %></b> : <%= action %> <%= message %></li>", {
                    date : strDate,
                    action : item.prefixAction,
                    message : item.message
                });
                var dateWhithoutHour = strDate.split(" ")[0];
                if (refDateWhithoutHour == null) {
                    refDateWhithoutHour =  dateWhithoutHour;
                    refNbActivities++;
                }
                else if (refDateWhithoutHour == dateWhithoutHour) {
                    refNbActivities++;
                } else if (refDateWhithoutHour != dateWhithoutHour) {
                    var dateSplit = refDateWhithoutHour.split("-").reverse().join("-");
                    chartData[0].values.push({ date : dateSplit, value : refNbActivities});
                    refDateWhithoutHour =  dateWhithoutHour;
                    refNbActivities = 1;
                }

                elem.append(action);
            });
            console.log("charts");
            console.log(chartData[0].values);

            var minValue=0;
            var maxValue=0;

            if(chartData[0].values.length>0) {
                minValue = chartData[0].values[0].value;
                maxValue = chartData[0].values[0].value;
            }


            _.each(chartData[0].values, function (d) {
                minValue = Math.min(minValue, d.value);
                maxValue = Math.max(maxValue, d.value);
            });


            chartData.map(function(series) {
                series.values = series.values.map(function(d) { return {x: d.date, y: d.value } }).reverse();
                return series;
            });

            var chart;

//            nv.addGraph(function() {
//                chart = nv.models.linePlusBarChart()
//                    .margin({top: 30, right: 60, bottom: 50, left: 70})
//                    .x(function(d,i) { return i })
//                    .color(d3.scale.category10().range());
//
//                chart.xAxis.tickFormat(function(d) {
//                    var dx = chartData[0].values[d] && chartData[0].values[d].x || 0;
//                    return dx;// ? d3.time.format('%x')(new Date(dx)) : '';
//                })
//                    .showMaxMin(false);
//
//                chart.y1Axis
//                    .tickFormat(d3.format(',f'));
//
//                chart.y2Axis
//                    .tickFormat(function(d) {
//                        if (d == minValue) return "low";
//                        else if (d == maxValue) return "high";
//                        else return "";
//                    });
//
//                chart.bars.forceY([0]).padData(false);
//                //chart.lines.forceY([0]);
//
//                d3.select('#chartActivity svg')
//                    .datum(chartData)
//                    .transition().duration(500).call(chart);
//
//                nv.utils.windowResize(chart.update);
//
//                chart.dispatch.on('stateChange', function(e) { nv.log('New State:', JSON.stringify(e)); });
//
//                return chart;
//            });
        });
    },
    initLastNews : function() {
        var self = this;
        var elem =  $(self.el).find("#userdashboardLastNews");


        $.get("api/news.json?max=10", function(data) {
            elem.find(".alert-info").replaceWith("");
            var collection = data.collection;
            _.each(collection,function(item) {
                var date = window.app.convertLongToDateShort(item.added)
                var today=new Date()
                var newInfo = "";
                var one_day=1000*60*60*24
                var days = Math.ceil((item.added-today.getTime())/(one_day));
                if(days>-30) {
                    newInfo =  '<span class="label label-warning">New!</span>&nbsp;';
                }
                var action = "<li>"+newInfo+"<b>"+date +"</b> : "+ item.text + "</li>"
                elem.append(action);
            });
        });
    },
    initGotoProject : function() {
        var maggicProject = [];
        var selectProject = $("#goToProjectUser");
        window.app.models.projects.each(function (project) {
            maggicProject.push(project.get('name'));
        });
        selectProject.typeahead({
            local:maggicProject,
            minLength:0
        });
        selectProject.bind('typeahead:selected', function(obj, datum, name) {
            window.app.models.projects.each(function (project) {
                if(project.get('name')==datum.value) {
                    window.location = "#tabs-dashboard-" + project.id;
                }
            });
        });
    },
    initGotoImage : function() {
        var self = this;
        var maggicImage = [];

        console.log(bowser);
        if(bowser.msie && bowser.version <= 8) {
            $("#goToImagePanel").empty();
        }
        else {
            $.get("/api/user/"+window.app.status.user.id+"/imageinstance/light.json", function(data) {

                var selectImage = $("#goToImageUser");
                self.images = data.collection;
                _.each (self.images, function (item){
                    maggicImage.push(item.originalFilename + " in " + item.projectName);
                });

                selectImage.typeahead({
                    local:maggicImage,
                    minLength:0
                });
                selectImage.bind('typeahead:selected', function(obj, datum, name) {
                    _.each (self.images, function (item){
                        if(item.originalFilename + " in " + item.projectName==datum.value) {
                            window.location = "#tabs-image-"+item.project+"-"+item.id+"-"
                        }
                    })
                });
            });
        }

    },

    initLastOpenedImage : function(tpl) {
        var self = this;
        var elem = $(self.el).find("#lastOpenImage");
        $.get("/api/imageinstance/method/lastopened.json?max=5", function(data) {
            elem.empty();
            elem.append('<div class="col-md-2 col-sm-2 stat well stat-first"><div class="data"><span class="number"></span>Last opened&nbsp;&nbsp;<br />Images »</div><span class="date"></span></div>');
            _.each (data.   collection, function (item){
                item.lastOpen = window.app.convertLongToDate(item.date)
                item.maxW = Math.min(elem.width(),300);
                var maxNumberOfChar = 15;
                var title = item.originalFilename;
                if (title.length > maxNumberOfChar) {
                    title = title.substr(0, maxNumberOfChar) + "...";
                }
                item.originalFilename = title;
                elem.append(_.template(tpl,item));
            });
        });
    }
});
