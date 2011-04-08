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
  <script type="text/javascript" src="application/lib/jquery.isotope.js"></script>
  <script type="text/javascript" src="application/lib/jquery.infinitescroll.js"></script>
  <script type="text/javascript" src="application/lib/mustache.js"></script>
  <script type="text/javascript" src="application/lib/ICanHaz.js"></script>
  <script type="text/javascript" src="${resource(dir:'js',file:'openlayers/OpenLayers.js')}" ></script>
<script type="text/javascript" src="${resource(dir:'js',file:'openlayers/OpenURL.js')}" ></script>
   <!-- Libs JStree-->
  <script type="text/javascript" src="http://static.jstree.com/v.1.0rc2/_docs/syntax/!script.js"></script>
  <script type="text/javascript" src="http://static.jstree.com/v.1.0rc2/jquery.cookie.js"></script>
  <script type="text/javascript" src="http://static.jstree.com/v.1.0rc2/jquery.hotkeys.js"></script>
  <script type="text/javascript" src="http://static.jstree.com/v.1.0rc2/jquery.jstree.js"></script>
   <!-- Libs JStree -->

<!-- Styles -->
<link rel='stylesheet' href='application/css/custom-theme/jquery-ui-1.8.7.custom.css' type='text/css'/>
<link rel='stylesheet' href='application/css/reset.css' type='text/css'/>
<link rel='stylesheet' href='application/css/cytomine.css' type='text/css'/>
<link rel='stylesheet' href='application/css/jquery.pnotify.default.css' type='text/css'/>
<link rel='stylesheet' href='application/css/isotope.css' type='text/css'/>


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
    </div>
</script>

<script type="text/html" id="warehousetpl">
  <div id="warehouse">
    <div class="main project"></div>
    <div class="main image"></div>
    <div class="main term"></div>
    <div class="main ontology"></div>

    <div class='sidebar'>
      <ul class='menu fixed'><li class="handle"><a href="#project" name="project" class="title">Projects</a></li></ul>
      <ul class='menu fixed'><li class="handle"><a href="#image" name="image" class="title">Images</a></li></ul>
      <ul class='menu fixed'><li class="handle"><a href="#ontology" name="ontology" class="title">Ontologies</a></li></ul>      
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

<script type="text/html" id="termviewtpl">

</script>

<script type="text/html" id="ontologyviewtpl">
  <div id='ontologytree'></div>
</script>

<script type="text/html" id="imageontologyviewtpl">
  <div class="tree"></div>
</script>

<script type="text/html" id="termitemviewtpl">
  <div class='thumb-info'><a href='#browse/{{ id }}'>Hello</a></div>
</script>

<script type="text/html" id="menubuttontpl">
  <a id="{{ id }}" href="{{ route }}" style="margin-right:5px;">{{ text }}</a>
</script>


<script type="text/html" id="imagethumbtpl">
          <div class='thumb-wrap' id='thumb{{ id }}'>
          <div class='thumb'><a href='#browse/{{ id }}'><img src='{{ thumb }}' alt='{{ filename }}' /></a></div>
  <div class='thumb-info'><a href='#browse/{{ id }}'>{{filename}}</a></div>
  </div>
</script>

<script type="text/html" id="taptpl">
  <div class="tabs">
    <ul></ul>
  </div>
</script>

<script type="text/html" id="browseimagetpl">
  <div id="tabs-{{id}}">
    <div class="toolbar" id="toolbar{{id}}">

        <span class="draw">
          <input type="radio" id="none{{id}}"      name="draw" checked="checked" /><label for="none{{id}}">None</label>
          <input type="radio" id="select{{id}}"    name="draw" /><label for="select{{id}}">Select</label>
          <input type="radio" id="regular4{{id}}"  name="draw" /><label for="regular4{{id}}">Regular4</label>
          <input type="radio" id="regular30{{id}}" name="draw" /><label for="regular30{{id}}">Regular30</label>
          <input type="radio" id="polygon{{id}}"   name="draw" /><label for="polygon{{id}}">Polygon</label>
          <input type="radio" id="modify{{id}}"    name="draw" /><label for="modify{{id}}">Modify</label>
        </span>
        <button id="delete{{id}}" name="delete">delete</button>
        <input type="checkbox" name="rotate" id="rotate{{id}}" /><label for="rotate{{id}}">Rotate</label>
        <input type="checkbox" name="resize" id="resize{{id}}" /><label for="resize{{id}}">Resize</label>
        <input type="checkbox" name="drag" id="drag{{id}}" /><label for="drag{{id}}">Drag</label>
        <input type="checkbox" name="irregular" id="irregular{{id}}" /><label for="irregular{{id}}">Irregular</label>

    </div>
    <div class="map" id="map{{id}}"></div>
    <div>
      <div class="inline overview" id="overviewMap{{id}}"></div>
      <div class="inline layerSwitcher" id="layerSwitcher{{id}}">
        <div class="slider"></div>
      </div>
      <div class="inline ontologypanel" id="ontology{{id}}"></div>
      <div class="clearboth"></div>
    </div>
  </div>
</script>

<!-- Application -->
<script type="text/javascript" src="application/Utilities.js" ></script>
<!-- controllers -->
<script type="text/javascript" src="application/controllers/ApplicationController.js" ></script>
<script type="text/javascript" src="application/controllers/ProjectController.js" ></script>
<script type="text/javascript" src="application/controllers/ImageController.js" ></script>
<script type="text/javascript" src="application/controllers/BrowseController.js" ></script>
<script type="text/javascript" src="application/controllers/TermController.js" ></script>
<script type="text/javascript" src="application/controllers/OntologyController.js" ></script>
<!-- Models -->
<script type="text/javascript" src="application/models/ImageModel.js" ></script>
<script type="text/javascript" src="application/models/TermModel.js" ></script>
<script type="text/javascript" src="application/models/OntologyModel.js" ></script>
<script type="text/javascript" src="application/models/UserModel.js" ></script>
<script type="text/javascript" src="application/models/ProjectModel.js" ></script>
<!-- View -->
<script type="text/javascript" src="application/views/ApplicationView.js" ></script>
<script type="text/javascript" src="application/views/ConfirmDialogView.js" ></script>
<script type="text/javascript" src="application/views/Component.js" ></script>
<script type="text/javascript" src="application/views/ProjectView.js" ></script>
<script type="text/javascript" src="application/views/ImageView.js" ></script>
<script type="text/javascript" src="application/views/BrowseImageView.js" ></script>
<script type="text/javascript" src="application/views/ImageThumbView.js" ></script>
<script type="text/javascript" src="application/views/TermView.js" ></script>
<script type="text/javascript" src="application/views/OntologyView.js" ></script>
<script type="text/javascript" src="application/views/ProjectView.js" ></script>
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