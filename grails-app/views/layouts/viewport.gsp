<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
  <title>Cytomine</title>

  <link rel="icon" type="image/png" href="favicon.ico">

  <!-- JQuery & JQuery UI -->
  <script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.6.1/jquery.js"></script>
  <script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8.7/jquery-ui.js"></script>
  <link rel='stylesheet' href='application/css/custom-theme/jquery-ui-1.8.7.custom.css' type='text/css'/>

  <!-- Core Libs -->
  <script type="text/javascript" src="application/lib/underscore.js"></script>
  <script type="text/javascript" src="application/lib/backbone.js"></script>
  <script type="text/javascript" src="application/lib/mustache.js"></script>
  <script type="text/javascript" src="application/lib/ICanHaz.js"></script>
  <script type="text/javascript" src="application/lib/json2.js" ></script>

  <!-- Modules -->
  <script type="text/javascript" src="application/lib/pnotify/jquery.pnotify.js" ></script>
  <link rel='stylesheet' href='application/lib/pnotify/jquery.pnotify.default.css' type='text/css'/>

  <script type="text/javascript" src="application/lib/fileupload/jquery.fileupload-ui.js"></script>
  <script type="text/javascript" src="application/lib/fileupload/jquery.fileupload.js"></script>
  <link rel='stylesheet' href='application/lib/fileupload/jquery.fileupload-ui.css' type='text/css'/>

  <script type="text/javascript" src="application/lib/jqplot/jquery.jqplot.js"></script>
  <script type="text/javascript" src="application/lib/jqplot/jqplot.pieRenderer.js"></script>
  <link rel='stylesheet' href='application/lib/jqplot/jquery.jqplot.css' type='text/css'/>

  <script type="text/javascript" src="application/lib/ui.panel/ui.panel.min.js"></script>
  <link type="text/css" href="application/lib/ui.panel/ui.panel.css" rel="stylesheet" />

  <link href="application/lib/dynatree/skin/ui.dynatree.css" rel="stylesheet" type="text/css">
  <script src="application/lib/dynatree/jquery.dynatree.js" type="text/javascript"></script>

  <!-- OpenLayers -->
  <script type="text/javascript" src="application/lib/openlayers/OpenLayers.js"></script>
  <script type="text/javascript" src="application/lib/openlayers/OpenURL.js"></script>

  <!-- Styles -->
  <link rel='stylesheet' href='application/css/reset.css' type='text/css'/>
  <link rel='stylesheet' href='application/css/cytomine.css' type='text/css'/>
<body>
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
  <div id="popup-wrapper"></div>
</script>

  <script type="text/html" id="logindialogtpl">
          <div id="login-confirm" title="Login">
          <div align="center" style="margin:auto;">
          <img src="images/cytomine.jpg" width="200" alt="Cytomine" />
          <p>Version {{version}}</p>
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
  <div class="layerSwitcher" id="layerswitchercontent{{id}}">
    <table>
      <tr>
        <td style="color:#FFF;padding-right:8px">Images</td>
        <td><ul class="baseLayers"></ul></td>
      </tr>
      <tr><td colspan="2"><hr /></tr>
      <tr>
        <td style="color:#FFF;padding-right:8px;">Annotations</td>
        <td><ul class="annotationLayers"></ul></td>
      </tr>
    </table>



  </div>
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
    <div class="main browser" style="display:none">
    </div>
    <div class="main noProject">
      <div id="noProjectDialog" class="centralPanel">
        <h3>Explorer</h3>
        <div>
          Please open a project
        </div>
      </div>

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
    <div class="main dashboard"></div>

    <div class='sidebar'>
      <!--<ul class='menu fixed'><li class="handle"><a href="#project" name="project" class="title">Projects</a></li></ul>-->

      <ul class='menu fixed'><li class="handle"><a href="#project" name="project" class="title">Projects</a></li></ul>
      <ul class='menu fixed'><li class="handle"><a href="#ontology" name="ontology" class="title">Ontologies</a></li></ul>
      <!--<ul class='menu fixed'><li class="handle"><a href="#image" name="image" class="title">Images</a></li></ul>
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
  <input type=checkbox name=ontology id=ontologies{{id}} class="searchProjectCriteria" style="display:inline;"><label for=ontologies{{id}} class="searchProjectCriteria">{{name}}</label><br></input>
</script>

  <script type="text/html" id="ontologieschoiceradiotpl">
          <input type=radio name="ontologyradio" id=ontologiesradio{{id}}  value="{{id}}" class="ontologyChoice">
          <label for="ontologies{{id}}" class="ontologyChoice">{{name}}</label>
          </input>
          <br>
</script>

<script type="text/html" id="userschoicetpl">
  <input type=checkbox name=usercheckbox id=users{{id}} value="{{id}}" class="userChoice"><label for=users{{id}} class="userChoice">{{username}}</label></input><br>
</script>

<script type="text/html" id="addprojectdialogtpl">
  <div id="addproject" title="Create project">
    <div align="center" style="margin:auto;">
      <img src="images/cytomine.jpg" width="200" alt="Cytomine" />
    </div>
    <form id="login-form-add-project">
      <fieldset>
        <div id="projecterrorlabel" class="ui-state-error ui-corner-all" style="padding: 0 .7em;">
          <p><span class="ui-icon ui-icon-alert" style="float: left; margin-right: .3em;"></span>
            <strong>Error:</strong><div id="errormessage"></div></p>
        </div>
        <label for="project-name" >Name:</label>
        <input type="text" size="20" id="project-name" value="" class="text ui-widget-content ui-corner-all" >
        <table class="projecttable">
          <tr>
            <td>
              <label for="projectontology" >Ontology:</label>
              <div id="projectontology"></div>
            </td>
            <td>

              <label for="projectuser" >Users:</label>
              <div id="projectuser"></div>
            </td>
          </tr>
        </table>
        <!--<input type="password" size="20" id="j_password" value="password" name="j_password"  class="text ui-widget-content ui-corner-all">
      <label for="remember_me" >Remember me</label>
      <input type="checkbox" id="remember_me" name="remember_me"  class="text ui-widget-content ui-corner-all"> -->
      </fieldset>
    </form>
  </div>
</script>

  <script type="text/html" id="projectsviewtpl">
          <div>
          <br><br>
          <div id="projectViewNorth"></div>
          <br><br>
          </div>
          <div id="lCenter">
          <div class="projectlist" id="projectlist"><br></div>
          </div>
          <br /><br />
          </div>

</script>


<script type="text/html" id="projectviewsearchtpl">

  <div id="searchProjectPanel" class="centralPanel">
    <h3>Project Panel</h3>
    <div>
      <table class='projecttable'>
        <tr>
          <td colspan="1" width="25%"> Project Name: <input id="projectsearchtextbox" /></td>
          <td colspan="1" width="25%"> Ontology type:<div id="ontologyChoiceList"></div></td>
          <td colspan="1" width="25%">
            <label for="amountNumberOfSlides" style="display:inline">Slides:
              <input type="text" id="amountNumberOfSlides" style="display:inline;border:0; font-weight:bold;" />
            </label>
            <div id="numberofslideSlider"></div>
            <br>
            <label for="amountNumberOfImages" style="display:inline">Images:
              <input type="text" id="amountNumberOfImages" style="display:inline;border:0; font-weight:bold;" />
            </label>
            <div id="numberofimageSlider"></div>
            <br>
            <label for="amountNumberOfAnnotations" style="display:inline">Annotations:
              <input type="text" id="amountNumberOfAnnotations" style="display:inline;border:0; font-weight:bold;" />
            </label>
            <div id="numberofannotationSlider"></div>
            <br>
          </td>
          <td colspan="1" width="25%" align="right">
            <!--                 <button id='projectrefreshbutton' type="button">Refresh</button>  -->
            <button id='projectallbutton' type="button" class="showAllProject">Show All projects</button> <br><br>
            <button id='projectaddbutton' type="button" class="addProject">Add Project</button><br>
            <!--                  <button id='projectsearchbutton' type="button">Search</button>  -->
          </td>
        </tr>
      </table>
    </div>
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
          <li>Ontology: {{ontology}} </li>
          </ul>
          </td>
  <td colspan="1" width="33%">
          <ul>
          <li>{{numberOfSlides}} samples </li>
          <li>{{numberOfImages}} images </li>
          <li>{{numberOfAnnotations}} annotations </li>
          </ul>
          </td>
  <td colspan="1" width="33%">
          <li>{{users}} </li>
          </td>
          </tr>
          <tr>
          <td>
          <!--<button class="seeSlide" id='projectopenimages{{id}}' type="button">Preview</button>-->
          </td>
          <td><button class="addSlide" id='projectaddimages{{id}}' type="button">Manage samples</button> </td>
          <!--<td><input class="changeProject" id='radioprojectchange{{id}}' type="radio" name="project"><label for='radioprojectchange{{id}}'>Explore</label></input> </td>-->
          <td><a class="changeProject" id='radioprojectchange{{id}}' href="#dashboard/{{id}}">Explore</a></td>
          <tr>
          </table>

          </div>
          <br><br>
  </div>
</script>






<script type="text/html" id="userlisttpl">
  <li><div align="center"><span class="ui-icon ui-icon-person"></span></div><label style="display: inline">{{name}}</label></li>
</script>



  <script type="text/html" id="annotationcommandlisttpl">
          <li>
            <hr>
            <div align="center">
            <img src="images/icons/{{icon}}" style="display: inline"></img>
            <br>
              {{datestr}}
            <br>
            <label style="display: inline">{{text}}</label>
            <img class="thumbcommand" src="{{image}}" width=150></img>
            </div>
            <br>
          </li>
</script>

<script type="text/html" id="annotationtermcommandlisttpl">
  <li>
    <hr>
    <div align="center">
      <span class="ui-icon {{icon}}"></span>


      {{datestr}} <br>
      <label style="display: inline">{{text}}</label>
    </div>
    <br>
  </li>
</script>




  <script type="text/html" id="addlisttpl">
          <li><div align="center"><span class="ui-icon ui-icon-plus"></span></div>{{datestr}}<label style="display: inline">{{text}}</label></li>
</script>
<script type="text/html" id="editlisttpl">
  <li><div align="center"><span class="ui-icon ui-icon-pencil"></span></div>{{datestr}}<label style="display: inline">{{text}}</label></li>
</script>
  <script type="text/html" id="deletelisttpl">
          <li><div align="center"><span class="ui-icon ui-icon-trash"></span></div>{{datestr}}<label style="display: inline">{{text}}</label></li>
</script>





<script type="text/html" id="projectdashboardviewtpl">
  <div id="tabs-0" style="overflow : auto; height: 100%;">
    <div id='nameDashboardInfo{{id}}' class="projectname"><h3></h3><div style="font-size: x-large; text-align : center;">{{name}}</div></div>

    <div id="lLeft">
      <div id="projectInfoPanel" class="navPanel" style="text-align:center; ">
        <h3>Project Info</h3>
        <div>
          <ul>
            <li>Name: <label id="projectInfoName" style="display: inline;">{{name}}</label></li>
            <li>Ontology: <label id="projectInfoOntology" style="display: inline;">{{ontology}}</label> </li>
            <br>
            <li><label id="projectInfoNumberOfSlides" style="display: inline;">{{numberOfSlides}}</label> slides </li>
            <li><label id="projectInfoNumberOfImages" style="display: inline;">{{numberOfImages}}</label> images </li>
            <li><label id="projectInfoNumberOfAnnotations" style="display: inline;">{{numberOfAnnotations}}</label> annotations </li>
            <br>
            <li><div id="projectInfoUserList"></div> </li>
            <br>
            <li>Created <label id="projectInfoCreated" style="display: inline;">{{created}}</label> </li>
            <li>Updated <label id="projectInfoUpdated" style="display: inline;">{{updated}}</label> </li>
          </ul>
        </div>
      </div>
    </div>


    <div id="lRight">
      <div id="projectLastCommandPanel" class="navPanel">
        <h3>Last action...</h3>
        <div>
          Last actions:
          <ul>
            <div id="lastactionitem"></div>
          </ul>
          <b>Notes:</b>
          <ul>
            <li>Not yet filter by project :-)</li>
          </ul>
        </div>
      </div>
    </div>

    <div id="centerpanel">
      <div id="lCenter">
        <div id="desktop">
          <p>

          </p>
          <div id="projectStatsPanel" class="centralPanel">
            <h3>Project stats</h3>
            <div>
              Project stats by term on annotation:
              <div id="plotterms"></div>
              <div id="flotterms" class="graph"></div>

            </div>
          </div>
        </div>
      </div>


      <div id="lCenter2">
        <div id="desktop2">
          <p>
          </p>
          <div id="projectImagesPanel" class="centralPanel">
            <h3>Project images</h3>
            <div id="projectImageList">

            </div>
          </div>
        </div>
      </div>
    </div>

    <div id="lCenter3" style="margin-bottom:100px">
      <div id="desktop3">
        <p>
        </p>


        <div id="projectAnnotationsPanel" class="centralPanel">
          <h3>Project annotations</h3>

          <div id="tabsannotation">
            <ul id="ultabsannotation">
              <!-- add a -->
            </ul>
            <!-- add div -->
            <div id="listtabannotation"></div>
          </div>







          <div id="projectAnnotationList">

          </div>




          <br> <br><br><br><br><br><br><br><br><br><br><br><br><br>   <!-- Pour afficher l'ascenceur jusqu'en bas...c'ets bourrin...-->
        </div>
      </div>
    </div>
  </div>


  <!-- <div id='projectdashboardinfo{{id}}' class="projectstat">
    <h3>{{name}}</h3>
    <div>
      <table class='projecttable'>
       <tr>
         <td colspan="1">
           <ul>
              <li>Name: {{name}} </li>
             <li>Ontology: {{ontology}} </li>
             </ul>
           </td>
         <td colspan="1">
           <ul>
             <li>{{numberOfSlides}} slides </li>
              <li>{{numberOfImages}} images </li>
              <li>{{numberOfAnnotations}} annotations </li>
             </ul>
         </td>
         <td colspan="1"">
            <li>{{users}} </li>
         </td>
      </tr>
      <tr>
      <td></td>
      <td><button class="addSlide" id='projectdashboardaddimages{{id}}' type="button">Manage slides</button> </td>
      <td></td>
      <tr>
         </table>

         <div class="galleria">
         </div>
    </div>
    <br><br>
    </div>-->
</div>
</script>

  <script type="text/html" id="termtitletabtpl">
          <li><a href="#tabsterm-{{id}}">{{name}}</a></li>
</script>

<script type="text/html" id="termdivtabtpl">
  <div id="tabsterm-{{id}}"></div>
</script>












<script type="text/html" id="projectchangedialog">
  <div id='projectchangedialog{{id}}' title="Change current project">
    <p>You want to switch to project {{name}}.</p>
    <p>Some images from other projects are already open. Do you want to close them?</p>
  </div>
</script>

<script type="text/html" id="projectaddimageitem">
  <li id="projectaddimageitemli{{id}}" class="slide{{slide}} image{{id}}">
    <input name="jqdemo" value="value1" type="checkbox" id="choice{{id}}"/>
    <label for="choice{{id}}"><b>{{name}}</b></label>
    <div id="projectaddimageitempict{{id}}" style="padding-left:20px;" alt=""/></div>
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


<script type="text/html" id="dashboardviewtpl">
</script>


<script type="text/html" id="imageviewtpl">
</script>

<script type="text/html" id="annotationviewtpl">
</script>


<script type="text/html" id="termviewtpl">

</script>


<script type="text/html" id="ontologyviewtpl">
  <div id='ontologytreepanel'>

  </div>
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
    <!--<a href='#browse/{{ id }}'>-->
          <a href="{{preview}}">
          <img  title="{{filename}}"
  alt="{{info}}"
  src="{{preview}}">
          </a>
          <!--</a>-->
</script>

<script type="text/html" id="imageselecttpl">
  <img src='{{ thumb }}' alt='{{ filename }}'  style="max-height:70px; max-width:70px; "  />
</script>

  <script type="text/html" id="imagethumbtpl">
          <div class='thumb'><a href='#browse/{{project}}/{{ id }}'><img src='{{ thumb }}' alt='{{ filename }}'  style="max-height:180px; max-width:180px; "  /></a></div>
  <div class='thumb-info'><a href='#browse/{{project}}/{{ id }}'>{{filename}}</a></div>
</script>


<script type="text/html" id="annotationthumbtpl">
  <div class='thumb'><a href='#browse/{{project}}/{{ image }}/{{id}}'><img src='{{ cropURL }}' alt='{{ name }}' style="max-height:180px; max-width:180px; "  /></a></div>
  <div class='thumb-info'>{{name}} <br> {{termList}}</div>
</script>

  <script type="text/html" id="taptpl">
          <div class="tabs">
          <ul></ul>
          </div>
</script>

<script type="text/html" id="browseimagetpl">
  <div id="tabs-{{id}}">
    <div class="toolbar" id="toolbar{{id}}" class="ui-widget-header ui-corner-all">

      <span class="nav-toolbar">
        <input type="radio" id="none{{id}}" name="toolbar" /><label for="none{{id}}">Navigate</label>
        <input type="radio" id="select{{id}}" name="toolbar" /><label for="select{{id}}">Select</label>
      </span>
      <span class="draw-toolbar">
        <input type="radio" id="regular4{{id}}" name="toolbar" /><label for="regular4{{id}}">Quadrilateral</label>
        <input type="radio" id="regular30{{id}}" name="toolbar" /><label for="regular30{{id}}">Ellipse</label>
        <input type="radio" id="polygon{{id}}" name="toolbar" /><label for="polygon{{id}}">Polygon</label>
      </span>
      <span class="edit-toolbar">
        <input type="radio" id="modify{{id}}" name="toolbar" /><label for="modify{{id}}">Edit</label>
        <input type="radio" id="rotate{{id}}" name="toolbar" /><label for="rotate{{id}}">Rotate</label>
        <input type="radio" id="resize{{id}}" name="toolbar" /><label for="resize{{id}}">Resize</label>
        <input type="radio" id="drag{{id}}"  name="toolbar" /><label for="drag{{id}}">Drag</label>
      </span>
      <span class="delete-toolbar">
        <input type="radio" id="delete{{id}}" name="toolbar" /><label for="delete{{id}}">Delete</label>
      </span>

      <span class="ruler-toolbar">
        <input type="radio" id="ruler{{id}}" name="toolbar" /><label for="ruler{{id}}">Ruler</label>
      </span>
      <!--<button id="delete{{id}}" name="delete">delete</button>
      <input type="checkbox" name="rotate" id="rotate{{id}}" /><label for="rotate{{id}}">Rotate</label>
      <input type="checkbox" name="resize" id="resize{{id}}" /><label for="resize{{id}}">Resize</label>
      <input type="checkbox" name="drag" id="drag{{id}}" /><label for="drag{{id}}">Drag</label>
      <input type="checkbox" name="irregular" id="irregular{{id}}" /><label for="irregular{{id}}">Irregular</label>-->

    </div>
    <div class="map" id="map{{id}}"></div>
    <div>
      <div class="overviewPanel" id="overviewMap{{id}}"></div>
      <div class="layerSwitcherPanel" id="layerSwitcher{{id}}"></div>
      <div class="ontologypanel" id="ontologyTree{{id}}"></div>
    </div>
  </div>
</script>


<script type="text/html" id="popupannotationtpl">
  <div class="popupPanel">
    <ul>
      <li>Area : {{area}}</li>
      <li>Perimeter : {{perimeter}}</li>
    </ul>
  </div>
</script>

<script type="text/html" id="popupmeasuretpl">
  <div class="popupPanel">
    <ul>
      <li>Length : {{length}}</li>
    </ul>
  </div>
</script>
<!-- Application -->
<script type="text/javascript" src="application/Utilities.js" ></script>
<!-- controllers -->
<script type="text/javascript" src="application/controllers/ApplicationController.js" ></script>
<script type="text/javascript" src="application/controllers/AuthController.js" ></script>
<script type="text/javascript" src="application/controllers/ProjectController.js" ></script>
<script type="text/javascript" src="application/controllers/DashboardController.js" ></script>
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
<script type="text/javascript" src="application/models/TransactionModel.js" ></script>
<script type="text/javascript" src="application/models/StatsModel.js" ></script>
<script type="text/javascript" src="application/models/CommandModel.js" ></script>
<!-- View -->
<script type="text/javascript" src="application/views/ApplicationView.js" ></script>
<script type="text/javascript" src="application/views/ConfirmDialogView.js" ></script>
<script type="text/javascript" src="application/views/DraggablePanelView.js" ></script>
<script type="text/javascript" src="application/views/Component.js" ></script>
<script type="text/javascript" src="application/views/ProjectView.js" ></script>
<script type="text/javascript" src="application/views/ImageView.js" ></script>
<script type="text/javascript" src="application/views/BrowseImageView.js" ></script>
<script type="text/javascript" src="application/views/ImageThumbView.js" ></script>
<script type="text/javascript" src="application/views/ImageRowView.js" ></script>
<script type="text/javascript" src="application/views/TermView.js" ></script>
<script type="text/javascript" src="application/views/OntologyView.js" ></script>
<script type="text/javascript" src="application/views/OntologyTreeView.js" ></script>
<script type="text/javascript" src="application/views/ProjectView.js" ></script>
<script type="text/javascript" src="application/views/ProjectPanelView.js" ></script>
<script type="text/javascript" src="application/views/ProjectManageSlideDialog.js" ></script>
<script type="text/javascript" src="application/views/ProjectAddDialog.js" ></script>
<script type="text/javascript" src="application/views/ProjectDashboardView.js" ></script>
<script type="text/javascript" src="application/views/AnnotationThumbView.js" ></script>
<script type="text/javascript" src="application/views/AnnotationView.js" ></script>
<script type="text/javascript" src="application/views/ProjectSearchPanel.js" ></script>
<script type="text/javascript" src="application/views/LoginDialogView.js" ></script>
<script type="text/javascript" src="application/views/LogoutDialogView.js" ></script>
<script type="text/javascript" src="application/views/Tabs.js" ></script>

<script type="text/javascript">
  $(function() {
    _.templateSettings = {
      interpolate : /\{\{(.+?)\}\}/g
    };
    // Create the app.
    window.app = new ApplicationController();

  });
</script>


<div id='app'></div>
<div id='dialogs'></div>

</body>
</html>