<%--
  Created by IntelliJ IDEA.
  User: stevben
  Date: 30/03/11
  Time: 17:05
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>

  <title>Cytomine</title>

  <link rel="icon" type="image/png" href="favicon.ico">

  <!-- Libs -->
  <script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.5/jquery.js"></script>
  <script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8.7/jquery-ui.js"></script>
  <script type="text/javascript" src="https://github.com/documentcloud/underscore/raw/master/underscore.js"></script>
  <script type="text/javascript" src="https://github.com/documentcloud/backbone/raw/master/backbone.js"></script>
  <script type="text/javascript" src="application/lib/json2.js" ></script>
  <script type="text/javascript" src="application/lib/jquery.pnotify.js" ></script>
  <script type="text/javascript" src="application/lib/jquery.pnotify.js" ></script>
  <script type="text/javascript" src="application/lib/jquery.isotope.js"></script>
  <script type="text/javascript" src="application/lib/jquery.infinitescroll.js"></script>
  <script type="text/javascript" src="application/lib/mustache.js"></script>
  <script type="text/javascript" src="application/lib/ICanHaz.js"></script>

  <!-- Styles -->
  <link rel='stylesheet' href='application/css/reset.css' type='text/css'/>
  <link rel='stylesheet' href='application/css/cytomine.css' type='text/css'/>
  <link rel='stylesheet' href='application/css/jquery.pnotify.default.css' type='text/css'/>
  <link rel='stylesheet' href='application/css/isotope.css' type='text/css'/>
  <link rel='stylesheet' href='application/css/custom-theme/jquery-ui-1.8.7.custom.css' type='text/css'/>

  <!-- Templates -->
  <script type="text/html" id="baselayouttpl">
    <div id='header' class='header clearfix'>
      <h1 class='breadcrumb'>
        <a class='home' href='#'><span class='logo'></span>Cytomine</a>
      </h1>
      <div id="menu" class="ui-buttonset actions">
        <a href="#undo" style="margin-right:5px;" class="ui-button ui-widget ui-state-default ui-corner-all ui-button-text-icon-primary ui-state-hover" role="button"><span class="ui-icon ui-icon-circle-arrow-w"></span><span class="ui-button-text">Undo</span></a>
        <a href="#redo" style="margin-right:5px;" class="ui-button ui-widget ui-state-default ui-corner-all ui-button-text-icon-primary ui-state-hover" role="button"><span class="ui-icon ui-icon-circle-arrow-e"></span><span class="ui-button-text">Redo</span></a>
      </div>
    </div>
    <div id="content">
    </div>
  </script>

  <script type="text/html" id="logindialogtpl">
          <div id="login-confirm" title="Login Area">
          <p><span class="ui-icon ui-icon-alert" style="float:left; margin:0 7px 20px 0;" />Login</p>
          </div>
</script>


  <script type="text/html" id="serverdowntpl">
    <div id="server-down" title="Server down">
      <p><span class="ui-icon ui-icon-alert" style="float:left; margin:0 7px 20px 0;" />The Cytomine server could not be reached</p>
    </div>
  </script>

  <script type="text/html" id="logoutdialogtpl">
          <div id="logout-confirm" title="Confirmation required">
          <p><span class="ui-icon ui-icon-alert" style="float:left; margin:0 7px 20px 0;" />Do you want to leave ?</p>
  </div>
</script>

  <script type="text/html" id="explorertpl">
    <div id="explorer">
      <div class="main browser"></div>
      <div class="main project"></div>
      <div class="main image"></div>

      <div class='sidebar'>
        <ul class='menu fixed'><li class="handle"><a href="#project" class="title">Projects</a></li></ul>
        <ul class='menu fixed'><li class="handle"><a href="#image" class="title">Images</a></li></ul>
        <ul class='menu libraries'></ul>
        <div class='buttons'>
          <!--<a class='add button' href='#'><span class='icon reverse add'></span>Add library</a>-->
        </div>
      </div>
  </script>

  <script type="text/html" id="admintpl">
    <div id="admin">
      <h1>admin</h1>
      <div class='main'>admin</div>
    </div>
  </script>

  <script type="text/html" id="logouttpl">

  </script>

  <script type="text/html" id="logintpl">

  </script>

  <script type="text/html" id="projectviewtpl">
    PROJECT
  </script>


  <script type="text/html" id="imageviewtpl">
  </script>

  <script type="text/html" id="menubuttontpl">
    <a id="{{ id }}" href="{{ route }}" style="margin-right:5px;">{{ text }}</a>
  </script>


  <script type="text/html" id="imagethumbtpl">
          <div class='thumb-wrap' id='thumb{{ id }}'>
          <div class='thumb'><a href='#browse/{{ id }}'><img src='{{ thumb }}' alt='{{ filename }}' /></a></div>
  <div class='thumb-info'><a href='#browse/{{ id }}'>{{ filename }}</a></div>
  </div>
</script>

  <script type="text/html" id="taptpl">
    <div class="tabs">
      <ul>
        <li><a href="#tabs-1">Nunc tincidunt</a></li>
        <li><a href="#tabs-2">Proin dolor</a></li>
        <li><a href="#tabs-3">Aenean lacinia</a></li>
      </ul>
      <div id="tabs-1">
        <p>Proin elit arcu, rutrum commodo, vehicula tempus, commodo a, risus. Curabitur nec arcu. Donec sollicitudin mi sit amet mauris. Nam elementum quam ullamcorper ante. Etiam aliquet massa et lorem. Mauris dapibus lacus auctor risus. Aenean tempor ullamcorper leo. Vivamus sed magna quis ligula eleifend adipiscing. Duis orci. Aliquam sodales tortor vitae ipsum. Aliquam nulla. Duis aliquam molestie erat. Ut et mauris vel pede varius sollicitudin. Sed ut dolor nec orci tincidunt interdum. Phasellus ipsum. Nunc tristique tempus lectus.</p>
      </div>
      <div id="tabs-2">
        <p>Morbi tincidunt, dui sit amet facilisis feugiat, odio metus gravida ante, ut pharetra massa metus id nunc. Duis scelerisque molestie turpis. Sed fringilla, massa eget luctus malesuada, metus eros molestie lectus, ut tempus eros massa ut dolor. Aenean aliquet fringilla sem. Suspendisse sed ligula in ligula suscipit aliquam. Praesent in eros vestibulum mi adipiscing adipiscing. Morbi facilisis. Curabitur ornare consequat nunc. Aenean vel metus. Ut posuere viverra nulla. Aliquam erat volutpat. Pellentesque convallis. Maecenas feugiat, tellus pellentesque pretium posuere, felis lorem euismod felis, eu ornare leo nisi vel felis. Mauris consectetur tortor et purus.</p>
      </div>
      <div id="tabs-3">
        <p>Mauris eleifend est et turpis. Duis id erat. Suspendisse potenti. Aliquam vulputate, pede vel vehicula accumsan, mi neque rutrum erat, eu congue orci lorem eget lorem. Vestibulum non ante. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Fusce sodales. Quisque eu urna vel enim commodo pellentesque. Praesent eu risus hendrerit ligula tempus pretium. Curabitur lorem enim, pretium nec, feugiat nec, luctus a, lacus.</p>
        <p>Duis cursus. Maecenas ligula eros, blandit nec, pharetra at, semper at, magna. Nullam ac lacus. Nulla facilisi. Praesent viverra justo vitae neque. Praesent blandit adipiscing velit. Suspendisse potenti. Donec mattis, pede vel pharetra blandit, magna ligula faucibus eros, id euismod lacus dolor eget odio. Nam scelerisque. Donec non libero sed nulla mattis commodo. Ut sagittis. Donec nisi lectus, feugiat porttitor, tempor ac, tempor vitae, pede. Aenean vehicula velit eu tellus interdum rutrum. Maecenas commodo. Pellentesque nec elit. Fusce in lacus. Vivamus a libero vitae lectus hendrerit hendrerit.</p>
      </div>
    </div>
  </script>

  <!-- Application -->
  <script type="text/javascript" src="application/Utilities.js" ></script>
  <script type="text/javascript" src="application/controllers/ApplicationController.js" ></script>
  <script type="text/javascript" src="application/controllers/ProjectController.js" ></script>
  <script type="text/javascript" src="application/controllers/ImageController.js" ></script>
  <script type="text/javascript" src="application/controllers/BrowseController.js" ></script>
  <script type="text/javascript" src="application/models/ImageModel.js" ></script>
  <script type="text/javascript" src="application/views/ApplicationView.js" ></script>
  <script type="text/javascript" src="application/views/ConfirmDialogView.js" ></script>
  <script type="text/javascript" src="application/views/Component.js" ></script>
  <script type="text/javascript" src="application/views/ProjectView.js" ></script>
  <script type="text/javascript" src="application/views/ImageView.js" ></script>
  <script type="text/javascript" src="application/views/BrowseImageView.js" ></script>
  <script type="text/javascript" src="application/views/ImageThumbView.js" ></script>
  <script type="text/javascript" src="application/views/Tabs.js" ></script>


  <script type="text/javascript">
    $(function() {
      // Create the app.
      new ApplicationController().startup();
    });
  </script>



<body>
<div id='app'></div>
<div id='dialogs'></div>
</body>
</html>