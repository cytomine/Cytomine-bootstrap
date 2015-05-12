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

var AuthController = Backbone.Router.extend({
    loginDialog : null,
    logoutDialog : null,

    routes: {
    },


    login: function () {
        this.loginDialog = new LoginDialogView({});
        this.loginDialog.render();
    },
    logout: function () {
        this.logoutDialog = new LogoutDialogView({});
        this.logoutDialog.render();
    },
    doForgotUsername : function () {
        var app = new ApplicationView(); //in order to use message function
        var data = $("#login-form").serialize(); //should be in LoginDIalogView
        var self = this;
        $.ajax({
            url: 'login/forgotUsername',
            type: 'post',
            dataType: 'json',
            data: data,
            success: function (data) {
                app.message("Success", "Check your inbox", "success");
                self.loginDialog.restoreLogin();
            },
            error: function (data) {
                var resp = $.parseJSON(data.responseText);
                app.message("Error", resp.message, "error");
            }
        });
        return false;
    },
    doForgotPassword : function () {
        var app = new ApplicationView(); //in order to use message function
        var data = $("#login-form").serialize(); //should be in LoginDIalogView
        var self = this;
        $.ajax({
            url: 'login/forgotPassword',
            type: 'post',
            dataType: 'json',
            data: data,
            success: function (data) {
                app.message("Success", "Check your inbox", "success");
                self.loginDialog.restoreLogin();
            },
            error: function (data) {
                var resp = $.parseJSON(data.responseText);
                app.message("Error", resp.message, "error");
            }
        });
        return false;
    },
    doLogin: function () {
        var app = new ApplicationView(); //in order to use message function
        var data = $("#login-form").serialize(); //should be in LoginDIalogView
        var self = this;
        $.ajax({
            url: 'j_spring_security_check',
            type: 'post',
            dataType: 'json',
            data: data,
            success: function (data) {
                app.message("Welcome", "You are logged as " + data.fullname, "", "success");
                new UserModel({id: "current"}).fetch({
                    success: function (model, response) {
                        window.app.status.user = {
                            authenticated: true,
                            id: data.id,
                            model: model,
                            filenameVisible : true
                        }
                        self.loginDialog.close();
                        window.app.startup();
                    }
                });

            },
            error: function (data) {
                var resp = $.parseJSON(data.responseText);
                $('#submit-login').attr('disabled', 'disabled');
                $('#login-confirm').effect("shake", { times: 2 }, 100);
                setTimeout(function () {
                    $('#submit-login').removeAttr('disabled');
                }, 400);
                app.message("Error", resp.message, "error");
            }
        });
        return false;
    }
});