var ActivityView = Backbone.View.extend({
    historyCollection : null,
    users : null,
    userJobs : null,
    usersOnline : null,
    idProject : null,
    idUser : null,
    command : null,
    initialize: function (options) {
        this.el = options.el;
        this.page = 0;
    },

    render: function () {
        console.log("render");
        var self = this;
        require([
            "text!application/templates/activity/ActivityComponent.tpl.html"
        ], function (tpl) {
            self.doLayout(tpl);

        });
        return this;
    },
    doLayout: function (tpl) {
        var self = this;
        $(self.el).html(tpl);
        var divTarget = $("#activity-content");
        divTarget.empty();

        self.createProjectListView();


    },
    createProjectListView : function() {
        $("#projectListActivity").empty();
        var startLetter=null;

        $("#projectListActivity").append('<li class="divider"></li>');
        $("#projectListActivity").append('<li id="projectChoiceActivityALL"><a href="#activity-">ALL PROJECTS</a></li>');
        $("#projectListActivity").append('<li class="divider"></li>');

        window.app.models.projects.each(function(project) {
            var newStartLetter =  project.get('name').substr(0,1);
            if(startLetter!=newStartLetter) {
                startLetter = newStartLetter;
                $("#projectListActivity").append('<li class="nav-header">'+startLetter+'</li>');
            }
            $("#projectListActivity").append('<li id="projectChoiceActivity'+project.id+'"><a href="#activity-'+project.id+'-">'+project.get('name')+'</a></li>');
        });
    },
    createProjectHeaderView : function(project) {
        $("#projectHeaderActivity").empty();
        if(project) {
            $("#projectHeaderActivity").append("<h1>Activity for "+project.get("name")+" </h1>");
            $("#projectHeaderActivity").append('<br><p><a href="#tabs-dashboard-'+project.id+'" class="btn btn-primary btn-large">Open this project</a></p>');
            $("#projectHeaderActivity").append('<br><div class="row-fluid" id="onlineUserActivity"></div>');
        } else {
            $("#projectHeaderActivity").append("<h1>Activity for All projects</h1>");
            $("#projectHeaderActivity").append("<h4>For the last month</h4>");
        }
    },
    createProjectOnlineUserView : function() {
        var self = this;
        console.log("createProjectOnlineUserView");
               var refreshData = function () {
                   require(["text!application/templates/dashboard/OnlineUser.tpl.html"],
                       function (userOnlineTpl) {
                           console.log("UserOnlineCollection");
                           new UserOnlineCollection({project: self.model.id}).fetch({
                               success: function (collection, response) {
                                   console.log("collection:"+collection.length);
                                   $("#onlineUserActivity").empty();
                                   collection.each(function (user) {
                                       //if undefined => user is cytomine admin but not in project!
                                       if (self.users.get(user.id) == undefined) {
                                           return;
                                       }

                                       var positions = "";
                                       _.each(user.get('position'), function (position) {
                                           var position = _.template(userOnlineTpl, {project: self.model.id, filename: window.app.minString(position.filename, 15, 10), image: position.image});
                                           positions += position;
                                       });

                                       //
                                       //var onlineUser = _.template("<br><div id='onlineUser-<%= id %>'><%= user %><ul><%= positions %></ul></div>", {
                                       var onlineUser = _.template("<div class='span4'><h3><%= user %></h3><ul><%= positions %></ul></div>", {
                                           id: user.id,
                                           user: self.users.get(user.id).prettyName(),
                                           positions: positions
                                       });
                                       $("#onlineUserActivity").append(onlineUser);
                                   });
                               }
                           });
                       }
                   )
               };
               refreshData();
               var interval = setInterval(refreshData, 5000);
               $(window).bind('hashchange', function () {
                   clearInterval(interval);
               });
    },

    createLastJobPanel : function() {
        var self = this;

        require(["text!application/templates/activity/LastJobActivity.tpl.html"],
           function (LastJobActivityTpl) {

                   new JobCollection({project: self.idProject, max: 20}).fetch({
                       success: function (collection, response) {
                           self.jobs = collection;
                           $("#jobPanelActivity").empty();


                           self.jobs.each(function(job) {
                               var json = job.toJSON();
                               json.created = window.app.convertLongToDate(job.get('created'));

                               var algoView = new ProjectDashboardAlgos({model:{id:-1}});
                                var item = algoView.getStatusElement(job,200);

                               $("#jobPanelActivity").append(_.template(LastJobActivityTpl,json));
                               $("#jobActivityStatus"+job.id).append(item);
                           })
                       }
                   });
         });
    },

    refresh : function (project, idUser) {
        console.log("refresh: "+project + " " + idUser);
        var self = this;
        self.model = project;

        self.createProjectHeaderView(project);


        var nbCollectionToFetch = 0;
        var nbCollectionToFetched = 0;

        var collectionFetched = function (expected) {
            nbCollectionToFetched++;
            if (nbCollectionToFetched < expected) {
                return;
            }

            $("#projectListActivity").find("li").removeClass("active");

            if(project) {
                $("#projectListActivity").find("li#projectChoiceActivity"+project.id).addClass("active");
                self.createProjectOnlineUserView();
            } else {
                $("#projectListActivity").find("li#projectChoiceActivityALL").addClass("active");
            }



            self.createUserSelect(project,idUser);
            if(idUser) {
                $("select#activityUser").val(idUser);
            }
            self.page = 0;
            var idProj = null;
            if(self.model) {
                idProj = self.model.id; //if idProj = null, get all projects data
            }
            new CommandHistoryCollection({project: idProj,user:idUser,max: 30}).fetch({
                success: function (collection, response) {
                    self.historyCollection=collection;
                    self.createCommandPanel();
                }
            });

            self.idUser = idUser;
            self.idProject = idProj;

                self.createLastJobPanel();
        };

        nbCollectionToFetch++;
        self.userJobs = null;
        if(project!=null && self.idProject!=project.id) {
            nbCollectionToFetch++;
            new UserJobCollection({project: self.model.id}).fetch({
               success: function (collection, response) {
                   self.userJobs = collection;
                   collectionFetched(nbCollectionToFetch);
               }
           });
        }
        var userColl = new UserCollection({project: self.model? self.model.id: null});

        userColl.fetch({
            success: function (collection, response) {

                self.users = collection;
                collectionFetched(nbCollectionToFetch);
            }
        });
    },
    createUserSelect : function() {
        var self = this;
        var selectElem = $("select#activityUser");
        selectElem.empty();
        selectElem.show();
        selectElem.append('<option value="-1" id="-1">All users/jobs</option>');

            //fill select with all possible layers
        self.users.each(function (user) {
            selectElem.append('<option value="' + user.id + '" id="' + user.id + '">' + user.layerName() + '</option>');
        });

        if(self.userJobs) {
            self.userJobs.each(function (user) {
                selectElem.append('<option value="' + user.id + '" id="' + user.id + '">' + user.layerName() + '</option>');
            });
        }

        selectElem.bind("change", function() {
            if(selectElem.val()==-1) {
                window.location = "#activity-"+self.idProject+"-";
            } else {
                window.location = "#activity-"+self.idProject+"-"+selectElem.val()+"-";
            }

       });
    },
    createCommandPanel : function() {
        var self = this;
        var divTarget = $("#activity-content");
        divTarget.empty();

        self.appendCommand(self.historyCollection);

        $(window).scroll(function () {
            //1. Look if the tabs is active. don't append thumbs if not
            var currentUrl = "" + window.location;
            if (currentUrl.search("#activity-") == -1) {
                return;
            }
            //2. Look if we are already appending thumbs. If yes, return
            if (self.appendingCommand) {
                return;
            }

            if (($(window).scrollTop() + 50) >= $(document).height() - $(window).height()) {
                /*console.log("$(window).scrollTop() : " + $(window).scrollTop());
                 console.log("$(document).height()- $(window).height() " + ($(document).height() - $(window).height()));
                 */

                self.page++;
                self.historyCollection.goTo(self.page,{
                    success: function (collection, response) {
                        self.historyCollection = collection;
                        self.appendCommand(self.historyCollection);
                    }
                });
            }
        });
    },
    appendCommand : function(collection) {
        var self = this;
        self.appendingCommand = true;

        if(collection.length==0) {
            window.app.view.message("Activity", "There is no other activities...", "warning");
            $("#getMoreActivity").replaceWith("");
        } else {
            $("#getMoreActivity").replaceWith("");
            collection.each(function (command) {


                var date = window.app.convertLongToDate(command.get('created'));
                var shortDate = date.substring(0,10);

                if(self.startDate!=shortDate) {
                    self.startDate=shortDate;
                    $(self.el).append('<li class="nav-header">'+self.startDate+'</li>');
                }

                var projectName ="";
                 if(self.model==null) {
                     projectName = "- "+ window.app.models.projects.get(command.get('project')).get('name');
                 }
                var commandHTML = _.template("<li><%=date%> <%=project%>: <%= prefix %> <%= message %></li>", { date: date, prefix: command.get("prefix"),message: command.get("message"),project:projectName});

                $(self.el).append(commandHTML);
            });
            $(self.el).append("<button id='getMoreActivity' class='btn btn-primary'>Get more...</button>");

            $("#getMoreActivity").click(function() {
                self.page++;
                self.historyCollection.goTo(self.page,{
                    success: function (collection, response) {
                        self.historyCollection = collection;
                        self.appendCommand(self.historyCollection);
                    }
                });
            });
        }
        self.appendingCommand = false;
    },
    startDate : null

});