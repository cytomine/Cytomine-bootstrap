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
  <script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/2.0.0/jquery.min.js"></script>
</head>
<body>
    Login without LDAP:
      <form id="login-form" class="form-verticalwell">
            <div class="control-group">
                <input id="j_username" name="j_username" type="text" class="input-large" placeholder="Username" required>
            </div>
            <div class="control-group">
                <input id="j_password" name="j_password" type="password" class="input-large" placeholder="Password" required>
            </div>
            <div class="control-group">
                <button id="submit-login" type="submit" class="btn btn-inverse">Sign in</button>
                &nbsp;&nbsp;&nbsp;
                <input id="remember_me" name="remember_me" type="checkbox" checked> Remember me
            </div>
        </form>

 <script>
   console.log("script");
  var register = function()
  {
    console.log("register");
      var data = $("#login-form").serialize(); //should be in LoginDIalogView
    console.log(data);



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