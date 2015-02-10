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
        var projectListActivity = $("#projectListActivity");
        projectListActivity.empty();
        projectListActivity.append('<li class="divider"></li>');
//        projectListActivity.append('<li id="projectChoiceActivityALL"><a href="#activity-">ALL PROJECTS</a></li>');
//        projectListActivity.append('<li class="divider"></li>');

        var startLetter = null;
        var projectsNotBlindMode = new ProjectCollection(window.app.models.projects.models.filter(function(project) {return !project.get('blindMode')}));
        projectsNotBlindMode.each(function(project) {
            var newStartLetter =  project.get('name').substr(0,1);
            if(startLetter!=newStartLetter) {
                startLetter = newStartLetter;
                projectListActivity.append('<li class="nav-header">'+startLetter+'</li>');
            }
            projectListActivity.append(_.template('<li><a href="#activity-<%= id %>-"><%= name %></a></li>', project.toJSON()));
        });
    },
    createProjectHeaderView : function(project) {
        var projectListActivity = $("#projectHeaderActivity");
        if(project) {
            projectListActivity.html(_.template('<div style="padding-top: 9px;padding-bottom: 7px;"><h4 style="display: inline;">Activity for <%= name %> </h4><a href="#tabs-dashboard-<%= id %>" class="btn btn-default btn-primary btn-xs">Open</a><div class="row" id="onlineUserActivity"></div></div>', project.toJSON()));
        } else {
            projectListActivity.html('<h4>Activity for all projects</h4>');
        }
    },
    createProjectOnlineUserView : function() {
        var self = this;
        var refreshData = function () {
            require(["text!application/templates/dashboard/OnlineUser.tpl.html"],
                function (userOnlineTpl) {
                    new UserOnlineCollection({project: self.model.id}).fetch({
                        success: function (collection, response) {
                            $("#onlineUserActivity").empty();
                            collection.each(function (user) {
                                //if undefined => user is cytomine admin but not in project!
                                if (self.users.get(user.id) == undefined) {
                                    return;
                                }

                                var positions = "";
                                _.each(user.get('position'), function (position) {
                                    positions += _.template(userOnlineTpl, {project: self.model.id, filename: window.app.minString(position.filename, 15, 10), image: position.image});
                                });

                                //
                                //var onlineUser = _.template("<br><div id='onlineUser-<%= id %>'><%= user %><ul><%= positions %></ul></div>", {
                                var onlineUser = _.template('<div class="col-md-4"><h3><%= user %></h3><ul><%= positions %></ul></div>', {
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

        if(!project) {
            $("#activity-content").empty();
            $("#activityUser").hide();
            $("#activity-content").append('<div style="margin: 10px 10px 10px 0px" class="alert alert-warning"> <i class="icon-remove"/> Select a project</div>');
            return;
        } else {
            $("#activityUser").show();
        }

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
            new CommandHistoryCollection({project: idProj,user:idUser,max: 30, fullData: true}).fetch({
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

            var commandHistory = new ProjectCommandsView({idProject: self.model.id, collection: collection, splitByDay: true, displayAll: true});
            $(self.el).append(commandHistory.render().el);

            $(self.el).append("<button id='getMoreActivity' class='btn btn-default btn-primary'>Get more...</button>");

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