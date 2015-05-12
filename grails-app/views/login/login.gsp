<!--
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
-->
<%--
  Created by IntelliJ IDEA.
  User: lrollus
  Date: 4/11/13
  Time: 9:20
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>Login without LDAP</title>
    <link rel="stylesheet" href="../lib/bootstrap-3.0.3/css/bootstrap.min.css"/>

    <style type="text/css">
    body {
        padding-top: 40px;
        padding-bottom: 40px;
        background-color: #eee;
    }

    .form-signin {
        max-width: 330px;
        padding: 15px;
        margin: 0 auto;
    }
    .form-signin .form-signin-heading,
    .form-signin .checkbox {
        margin-bottom: 10px;
    }
    .form-signin .checkbox {
        font-weight: normal;
    }
    .form-signin .form-control {
        position: relative;
        height: auto;
        -webkit-box-sizing: border-box;
        -moz-box-sizing: border-box;
        box-sizing: border-box;
        padding: 10px;
        font-size: 16px;
    }
    .form-signin .form-control:focus {
        z-index: 2;
    }
    .form-signin input[type="email"] {
        margin-bottom: -1px;
        border-bottom-right-radius: 0;
        border-bottom-left-radius: 0;
    }
    .form-signin input[type="password"] {
        margin-bottom: 10px;
        border-top-left-radius: 0;
        border-top-right-radius: 0;
    }

    </style>


    <script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/2.0.0/jquery.min.js"></script>
</head>
<body>
    <div class="container">

        <form id="login-form" class="form-signin" role="form">
            <h2 class="form-signin-heading">Login without LDAP:</h2>
            <input id="j_username" name="j_username" type="text" class="form-control" placeholder="Username" required>
            <input id="j_password" name="j_password" type="password" class="form-control" placeholder="Password" required>
            <div class="checkbox">
                <label>
                    <input id="remember_me" name="remember_me" type="checkbox" checked> Remember me
                </label>
            </div>
            <button class="btn btn-lg btn-primary btn-block" type="submit">Sign in</button>
        </form>

    </div> <!-- /container -->


     <script>
    console.log("script");
    var register = function() {
        console.log("register");
        var data = $("#login-form").serialize(); //should be in LoginDIalogView
        // console.log(data);



        $.ajax({
            url: 'j_spring_security_check',
            type: 'post',
            dataType: 'json',
            data: data,
            success: function (data) {
                window.location = "/";
            },
            error: function (data) {
                console.log(data);
                if(data.status==403) {
                    alert("Error: bad login or bad password!");
                } else if(data.status==200) {
                    window.location = "/";
                }
            }
        });
    }
    console.log($('#submit-login').length);

   //$("#submit-login").click(register);

    $( "#login-form" ).submit(function( event ) {
        event.preventDefault();
        register();
    });
     </script>


</body>
</html>