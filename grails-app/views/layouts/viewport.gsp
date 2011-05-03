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
  <script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.5.2/jquery.js"></script>
  <script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8.7/jquery-ui.js"></script>
  <script type="text/javascript" src="application/lib/jquery.fileupload-ui.js"></script>
  <script type="text/javascript" src="application/lib/jquery.fileupload.js"></script>
  <script type="text/javascript" src="https://github.com/documentcloud/underscore/raw/master/underscore.js"></script>
  <script type="text/javascript" src="https://github.com/documentcloud/backbone/raw/master/backbone.js"></script>
  <script type="text/javascript" src="application/lib/json2.js" ></script>
  <script type="text/javascript" src="application/lib/jquery.pnotify.js" ></script>
  <script type="text/javascript" src="application/lib/jquery.isotope.js"></script>
  <script type="text/javascript" src="application/lib/jquery.infinitescroll.js"></script>
  <script type="text/javascript" src="application/lib/mustache.js"></script>
  <script type="text/javascript" src="application/lib/ICanHaz.js"></script>
  <script type="text/javascript" src="application/lib/jquery.nivo.slider.pack.js"></script>
  <script type="text/javascript" src="${resource(dir:'js',file:'openlayers/OpenLayers.js')}" ></script>
  <script type="text/javascript" src="${resource(dir:'js',file:'openlayers/OpenURL.js')}" ></script>
  <script type="text/javascript" src="application/lib/ui.panel.min.js"></script>
  <link type="text/css" href="application/css/ui.panel.css" rel="stylesheet" />

<!-- Libs JStree
<script type="text/javascript" src="http://static.jstree.com/v.1.0rc2/_docs/syntax/!script.js"></script>
<script type="text/javascript" src="http://static.jstree.com/v.1.0rc2/jquery.cookie.js"></script>
<script type="text/javascript" src="http://static.jstree.com/v.1.0rc2/jquery.hotkeys.js"></script>-->
<!--<script type="text/javascript" src="http://static.jstree.com/v.1.0rc2/jquery.jstree.js"></script>-->
<script type="text/javascript" src="application/lib/jquery.jstree.js"></script>

<!-- Styles -->
<link rel='stylesheet' href='application/css/reset.css' type='text/css'/>
<link rel='stylesheet' href='application/css/custom-theme/jquery-ui-1.8.7.custom.css' type='text/css'/>
<link rel='stylesheet' href='application/css/jquery.fileupload-ui.css' type='text/css'/>
<link rel='stylesheet' href='application/css/jquery.pnotify.default.css' type='text/css'/>
<link rel='stylesheet' href='application/css/isotope.css' type='text/css'/>
<link rel='stylesheet' href='application/css/cytomine.css' type='text/css'/>
<link rel='stylesheet' href='application/css/nivo-slider.css' type='text/css'/>

<!-- Templates -->
<script type="text/html" id="baselayouttpl">
    <div id='header' class='header clearfix'>
        <h1 class='breadcrumb'>
            <a class='home' href='#'><span class='logo'></span>Cytomine</a>
        </h1>
        <div id="menu" class="ui-buttonset actions">
            <a id="undo"  style="margin-right:5px;" class="ui-button ui-widget ui-state-default ui-corner-all ui-button-text-icon-primary ui-state-hover" role="button"><span class="ui-icon ui-icon-circle-arrow-w"></span><span class="ui-button-text">Undo</span></a>
            <a id="redo" style="margin-right:5px;" class="ui-button ui-widget ui-state-default ui-corner-all ui-button-text-icon-primary ui-state-hover" role="button"><span class="ui-icon ui-icon-circle-arrow-e"></span><span class="ui-button-text">Redo</span></a>
        </div>
    </div>
    <div id="content">
    </div>
</script>

<script type="text/html" id="logindialogtpl">
  <div id="login-confirm" title="Login">
  <div align="center" style="margin:auto;">
    <img src="images/cytomine.jpg" width="200" alt="Cytomine" />
  </div>
  <form id="login-form">
    <fieldset>
      <input type="text" size="20" id="j_username" value="username" name="j_username"  class="text ui-widget-content ui-corner-all" >
      <input type="password" size="20" id="j_password" value="password" name="j_password"  class="text ui-widget-content ui-corner-all">
      <label for="remember_me" >Remember me</label>
      <input type="checkbox" id="remember_me" name="remember_me"  class="text ui-widget-content ui-corner-all">
    </fieldset>
  </form>
  </div>
</script>


<script type="text/html" id="loadingdialogtpl">
    <div id="loading-dialog" title="Loading">
        <div align="center" style="margin:auto;">
            <img src="images/cytomine.jpg" width="200" alt="Cytomine" />
        </div>
        <div id="progress" style="text-align:center;padding-top:30px;">
            <h1>Loading data...</h1>
            <div id="login-progressbar" style="margin-top:10px;"></div>
        </div>
    </div>
</script>

<script type="text/html" id="overviewmapcontenttpl">
  <!--<div id="overviewmapdialog{{id}}" title="Minimap">-->
  <div id="overviewmapcontent{{id}}"></div>
  <!--</div>-->
</script>

<script type="text/html" id="layerswitchercontenttpl">
    <!--<div id="layerswitcherdialog{{id}}" title="Layer Switcher">-->
    <div id="layerswitchercontent{{id}}"></div>
    <div class="slider"></div>
    <!--</div>-->
</script>

<script type="text/html" id="ontologytreecontenttpl">
    <!--<div id="ontologytreedialog{{id}}" title="Ontology">-->
    <div id="ontologytreecontent{{id}}"></div>
    <!--</div>-->
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
        <div class="main browser">
        </div>
    </div>
</script>

<script type="text/html" id="uploadtpl">
    <div id="upload">
        <div class='main upload'>
            <form id="file_upload" action="upload/image" method="POST" enctype="multipart/form-data">
                <input type="file" name="file" multiple>
                <button>Upload</button>
                <div>Upload files</div>
            </form>
            <button id="start_uploads">Start uploads</button>
            <table id="files"></table>
        </div>
    </div>
</script>



<script type="text/html" id="warehousetpl">
    <div id="warehouse">
        <div class="main project"></div>
        <div class="main image"></div>
        <div class="main term"></div>
        <div class="main ontology"></div>

        <div class='sidebar'>
            <!--<ul class='menu fixed'><li class="handle"><a href="#project" name="project" class="title">Projects</a></li></ul>-->
            <ul class='menu fixed'><li class="handle"><a href="#image" name="image" class="title">Images</a></li></ul>
            <ul class='menu fixed'><li class="handle"><a href="#ontology" name="ontology" class="title">Ontologies</a></li></ul>
            <ul class='menu fixed'><li class="handle"><a href="#project" name="project" class="title">Projects</a></li></ul>
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

<script type="text/html" id="ontologieschoicetpl">
     <input type=checkbox name=ontology id=ontologies{{id}}><label for=ontologies{{id}}>{{name}}</label></input><br>
</script>

<script type="text/html" id="projectsviewtpl">
  <div>
     <br> <br>
    <div id="searchProjectPanel" class="centralPanel">
		<h3>Search panel</h3>
		<div>
		    <table class='projecttable'>
		      <tr>
                <td colspan="1" width="33%"> Project Name: <input id="projectsearchtextbox" /></td>
                <td colspan="1" width="33%"> Ontology type:<div id="ontologyChoiceList"></div></td>
                <td colspan="1" width="33%" align="right">
 <!--                 <button id='projectrefreshbutton' type="button">Refresh</button>  -->
                  <button id='projectallbutton' type="button">All projects</button>
                  <button id='projectsearchbutton' type="button">Search</button>
                  </td>
              </tr>
             </table>
		</div>
	</div>
       <br><br>
    </div>
    <div id="projectlist"></div>
    <br /><br />
  </div>

</script>


<script type="text/html" id="projectviewtpl">
  <div id='projectlist{{id}}' class="projectlist">
    <h3>{{name}}</h3>
    <div>
        <table class='projecttable'>
          <tr>
             <td colspan="1" width="33%">
                <ul>
                    <li>Name: {{name}} </li>
                    <li>Id: {{id}} </li>
                    <li>Ontology: {{ontology}} </li>
                </ul>
            </td>
            <td colspan="1" width="33%">
                <ul>
                    <li>{{images}} images </li>
                    <li>{{annotations}} annotations </li>
                </ul>
            </td>
            <td colspan="1" width="33%">
                <li>{{users}} </li>
            </td>
        </tr>
        <tr>
            <td><button  id='projectopenimages{{id}}' type="button">Browse slides</button></td>
            <td><button class="addSlide" id='projectaddimages{{id}}' type="button">Add images</button> </td>
            <td><input id='radioprojectchange{{id}}' type="radio" name="project"><label for='radioprojectchange{{id}}'>Use this project</label></input> </td>
        </tr>
        </table>
        <div class="scroll-content"></div>
    </div>
    <br><br>
    </div>
</script>

<script type="text/html" id="projectchangedialog">
  <div id='projectchangedialog{{id}}' title="Change current project">
    <p>You want to switch to project {{name}}.</p>
    <p>Some images from other projects are already open. Do you want to close them?</p>
  </div>
</script>

<script type="text/html" id="projectaddimageitem">
  <li id="projectaddimageitemli{{id}}">
    <input name="jqdemo" value="value1" type="checkbox" id="choice{{id}}"/>
    <label for="choice{{id}}">..................... {{name}}</label>
    <div id="projectaddimageitempict{{id}}" alt=""/>
    <a class="checkbox-select" href="#">Select</a>
    <a class="checkbox-deselect" href="#">Cancel</a>
  </li>
</script>

<script type="text/html" id="projectaddimagedialog">
  <div id='projectaddimagedialog{{id}}' title="Images project">
    <form action="">
      <fieldset>
        <legend>Add images to project {{name}}</legend><br>
        <ul id="projectaddimagedialoglist{{id}}" class="checklist">
        </ul>
        <div style="clear: both;"></div>
        <!--<button class="sendit" type="submit" name="submitbutton" title="Submit the form">Send it!</button>-->
      </fieldset>
    </form>
  </div>
</script>





<script type="text/html" id="imageviewtpl">
</script>


<script type="text/html" id="termviewtpl">

</script>

<script type="text/html" id="ontologyviewtpl">
  <div id='ontologytreepanel'>
    <button id='ontologytreeaddontologybutton' type="button" >Add Ontology</button>
    <button id='ontologytreeaddtermbutton' type="button" >Add Term</button>
    <button id='ontologytreerenamebutton' type="button" >Rename</button>
    <button id='ontologytreedeletebutton' type="button" >Remove</button>
    <button id='ontologytreeprintselectbutton' type="button" >Print selected</button>
    TODO: add ontology + add term
  </div>
  <div id='ontologytree'></div>

  <div id='ontologytreedebug'></div>
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


<script type="text/html" id="imagerowtpl">
    <a href='#browse/{{ id }}'><img src='{{ thumb }}' alt='{{ filename }}' title='{{filename}}' /></a>
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
    <div class="toolbar" id="toolbar{{id}}" class="ui-widget-header ui-corner-all">

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
      <div class="overviewPanel" id="overviewMap{{id}}"></div>
      <div class="layerSwitcherPanel" id="layerSwitcher{{id}}"></div>
      <div class="ontologypanel" id="ontologyTree{{id}}"></div>
    </div>
  </div>
</script>

<!-- Application -->
<script type="text/javascript" src="application/Utilities.js" ></script>
<!-- controllers -->
<script type="text/javascript" src="application/controllers/ApplicationController.js" ></script>
<script type="text/javascript" src="application/controllers/AuthController.js" ></script>
<script type="text/javascript" src="application/controllers/ProjectController.js" ></script>
<script type="text/javascript" src="application/controllers/ImageController.js" ></script>
<script type="text/javascript" src="application/controllers/BrowseController.js" ></script>
<script type="text/javascript" src="application/controllers/TermController.js" ></script>
<script type="text/javascript" src="application/controllers/OntologyController.js" ></script>
<script type="text/javascript" src="application/controllers/CommandController.js" ></script>
<!-- Models -->
<script type="text/javascript" src="application/models/ImageModel.js" ></script>
<script type="text/javascript" src="application/models/TermModel.js" ></script>
<script type="text/javascript" src="application/models/OntologyModel.js" ></script>
<script type="text/javascript" src="application/models/UserModel.js" ></script>
<script type="text/javascript" src="application/models/ProjectModel.js" ></script>
<script type="text/javascript" src="application/models/AnnotationModel.js" ></script>
<script type="text/javascript" src="application/models/SlideModel.js" ></script>
<!-- View -->
<script type="text/javascript" src="application/views/ApplicationView.js" ></script>
<script type="text/javascript" src="application/views/ConfirmDialogView.js" ></script>
<script type="text/javascript" src="application/views/DraggablePanelView.js" ></script>
<script type="text/javascript" src="application/views/Component.js" ></script>
<script type="text/javascript" src="application/views/ProjectView.js" ></script>
<script type="text/javascript" src="application/views/ImageView.js" ></script>
<script type="text/javascript" src="application/views/BrowseImageView.js" ></script>
<script type="text/javascript" src="application/views/ImageThumbView.js" ></script>
<script type="text/javascript" src="application/views/TermView.js" ></script>
<script type="text/javascript" src="application/views/OntologyView.js" ></script>
<script type="text/javascript" src="application/views/OntologyTreeView.js" ></script>
<script type="text/javascript" src="application/views/ProjectView.js" ></script>
<script type="text/javascript" src="application/views/ProjectPanelView.js" ></script>
<script type="text/javascript" src="application/views/AddImageProjectDialog.js" ></script>
<script type="text/javascript" src="application/views/Tabs.js" ></script>


<script type="text/javascript">
  $(function() {
    // Create the app.
    window.app = new ApplicationController();
  });
</script>



<body>
<div id='app'></div>
<div id='dialogs'></div>

</body>
</html>