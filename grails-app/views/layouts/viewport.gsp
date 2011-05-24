<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>Cytomine</title>

    <link rel="icon" type="image/png" href="favicon.ico">

    <!-- RequireJS -->
    <script type="text/javascript" src="application/lib/requirejs/require.js"></script>


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
   <script type="text/html" id="addlisttpl">
            <li><div align="center"><span class="ui-icon ui-icon-plus"></span></div>{{datestr}}<label style="display: inline">{{text}}</label></li>
</script>
<script type="text/html" id="editlisttpl">
    <li><div align="center"><span class="ui-icon ui-icon-pencil"></span></div>{{datestr}}<label style="display: inline">{{text}}</label></li>
</script>
    <script type="text/html" id="deletelisttpl">
            <li><div align="center"><span class="ui-icon ui-icon-trash"></span></div>{{datestr}}<label style="display: inline">{{text}}</label></li>
</script>

<script type="text/html" id="projectchangedialog">
    <div id='projectchangedialog{{id}}' title="Change current project">
        <p>You want to switch to project {{name}}.</p>
        <p>Some images from other projects are already open. Do you want to close them?</p>
    </div>
</script>

<script type="text/html" id="projectaddimageitem">

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
        <div id="tabsontology">
            <ul id="ultabsontology">
                <!-- add a -->
            </ul>
            <!-- add div -->
            <div id="listtabontology"></div>
        </div>
    </div>
</script>

<script type="text/html" id="ontologytitletabtpl">
    <li><a href="#tabsontology-{{id}}">{{name}}</a></li>
</script>

<script type="text/html" id="ontologydivtabtpl">
    <div id="tabsontology-{{id}}">
        <button class="expanseOntology" id='buttonExpanseOntology{{id}}' type="button">Expand all</button>
        <button class="collapseOntology" id='buttonCollapseOntology{{id}}' type="button">Collapse all</button>

        <button class="addTerm" id='buttonAddTerm{{id}}' type="button">Add Term</button>
        <button class="renameTerm" id='buttonRenameTerm{{id}}' type="button">Rename Term</button>
        <button class="deleteTerm" id='buttonDeleteTerm{{id}}' type="button">Delete Term</button>
        <br>
        <div id="treeontology-{{id}}" style="background:black;"></div>
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



<!-- Application -->
<script type="text/javascript" src="application/Utilities.js" ></script>
<!-- controllers -->
<script type="text/javascript" src="application/controllers/ApplicationController.js" ></script>
<script type="text/javascript" src="application/controllers/UploadController.js" ></script>
<script type="text/javascript" src="application/controllers/AuthController.js" ></script>
<script type="text/javascript" src="application/controllers/ProjectController.js" ></script>
<script type="text/javascript" src="application/controllers/DashboardController.js" ></script>
<script type="text/javascript" src="application/controllers/ImageController.js" ></script>
<script type="text/javascript" src="application/controllers/ExplorerController.js" ></script>
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
        //Change underscore _.template function delimiter
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