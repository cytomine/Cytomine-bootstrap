<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>Cytomine</title>

    <link rel="icon" type="image/png" href="favicon.ico">
    <script type="text/javascript" src="https://www.google.com/jsapi"></script>
    <script type="text/javascript">
        google.load('visualization', '1', {packages: ['corechart']});
    </script>
    <!-- RequireJS -->
    <script type="text/javascript" src="lib/requirejs/require.js"></script>
    <!-- JQuery & JQuery UI-->
    <script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.6.2/jquery.js"></script>
    <script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8.14/jquery-ui.js"></script>
    <!--<script type="text/javascript" src="lib/jquery/jquery-1.5.1.min.js"></script>
    <script type="text/javascript" src="lib/jquery/jquery-ui-1.8.1.custom.min.js"></script> -->

    <link rel='stylesheet' href='css/custom-theme/jquery-ui-1.8.7.custom.css' type='text/css'/>
    <!--<link rel='stylesheet' href='css/custom-ben/css/custom-theme/jquery-ui-1.8.14.custom.css' type='text/css'/>-->
    <!--<link rel='stylesheet' href='css/absolution/jquery.ui.all.css' type='text/css'/>-->

    <!-- Core Libs -->
    <script type="text/javascript" src="lib/underscore.js"></script>
    <script type="text/javascript" src="lib/backbone.js"></script>

    <!-- Modules -->
    <script type="text/javascript" src="lib/masonry/jquery.masonry.min.js" ></script>

    <script type="text/javascript" src="lib/pnotify/jquery.pnotify.js" ></script>
    <link rel='stylesheet' href='lib/pnotify/jquery.pnotify.default.css' type='text/css'/>

    <script type="text/javascript" src="lib/fileupload/jquery.fileupload-ui.js"></script>
    <script type="text/javascript" src="lib/fileupload/jquery.fileupload.js"></script>
    <link rel='stylesheet' href='lib/fileupload/jquery.fileupload-ui.css' type='text/css'/>

    <script type="text/javascript" src="lib/ui.panel/ui.panel.min.js"></script>
    <link type="text/css" href="lib/ui.panel/ui.panel.css" rel="stylesheet" />

    <link href="lib/dynatree/skin/ui.dynatree.css" rel="stylesheet" type="text/css">
    <script src="lib/dynatree/jquery.dynatree.js" type="text/javascript"></script>

    <!-- OpenLayers -->
    <script type="text/javascript" src="lib/openlayers/OpenLayers.js"></script>
    <script type="text/javascript" src="lib/openlayers/OpenURL.js"></script>

    <script type="text/javascript" src="lib/farbtastic/farbtastic.js"></script>

    <!-- MultiSelect -->
    <script type="text/javascript" src="lib/multiselect/src/jquery.multiselect.min.js"></script>
    <link rel='stylesheet' href="lib/multiselect/jquery.multiselect.css" type='text/css'/>
    <!-- Styles -->
    <link rel='stylesheet' href='css/reset.css' type='text/css'/>
    <link rel='stylesheet' href='css/cytomine.css' type='text/css'/>

    <!-- Application -->
    <script type="text/javascript" src="application/Utilities.js" ></script>

    <!--jqgrid -->
    <link rel="stylesheet" type="text/css" media="screen" href="lib/jqgrid/css/ui.jqgrid.css" />
    <script src="lib/jqgrid/js/i18n/grid.locale-en.js" type="text/javascript"></script>
    <script src="lib/jqgrid/js/jquery.jqGrid.min.js" type="text/javascript"></script>
    <script src="lib/jqgrid/plugins/jquery.tablednd.js" type="text/javascript"></script>
    <script src="lib/jqgrid/plugins/jquery.contextmenu.js" type="text/javascript"></script>

   <!-- jcarrousel -->
    <script type="text/javascript" src="lib/jcarousel/jquery.carousel.min.js"></script>

    <script type="text/javascript" src="lib/masonry/jquery.masonry.min.js"></script>


    <script type="text/javascript" src="application/views/explorer/AnnotationsPanel.js" ></script>
    <script type="text/javascript" src="application/views/explorer/LayerSwitcherPanel.js" ></script>
    <script type="text/javascript" src="application/views/explorer/OverviewMapPanel.js" ></script>
    <script type="text/javascript" src="application/views/explorer/OntologyPanel.js" ></script>


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
    <script type="text/javascript" src="application/controllers/AnnotationController.js" ></script>
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
    <script type="text/javascript" src="application/models/RelationModel.js" ></script>
    <!-- View -->
    <script type="text/javascript" src="application/views/auth/LoginDialogView.js" ></script>
    <script type="text/javascript" src="application/views/auth/LoadingDialogView.js" ></script>
    <script type="text/javascript" src="application/views/auth/LogoutDialogView.js" ></script>

    <script type="text/javascript" src="application/views/dashboard/AnnotationThumbView.js" ></script>
    <script type="text/javascript" src="application/views/dashboard/AnnotationView.js" ></script>
    <script type="text/javascript" src="application/views/dashboard/ProjectDashboardView.js" ></script>

    <script type="text/javascript" src="application/views/explorer/AnnotationLayer.js" ></script>
    <script type="text/javascript" src="application/views/explorer/BrowseImageView.js" ></script>
    <script type="text/javascript" src="application/views/explorer/DraggablePanelView.js" ></script>
    <script type="text/javascript" src="application/views/explorer/ExplorerTabs.js" ></script>

    <script type="text/javascript" src="application/views/image/ImageThumbView.js" ></script>
    <script type="text/javascript" src="application/views/image/ImageSelectView.js" ></script>
    <script type="text/javascript" src="application/views/image/ImageView.js" ></script>
    <script type="text/javascript" src="application/views/image/ImageTabsView.js" ></script>

    <script type="text/javascript" src="application/views/ontology/OntologyPanelView.js" ></script>
    <script type="text/javascript" src="application/views/ontology/OntologyView.js" ></script>
    <script type="text/javascript" src="application/views/ontology/OntologyAddOrEditTermView.js" ></script>
    <script type="text/javascript" src="application/views/ontology/OntologyTreeView.js" ></script>
    <script type="text/javascript" src="application/views/ontology/OntologyEditDialog.js" ></script>
    <script type="text/javascript" src="application/views/ontology/OntologyAddDialog.js" ></script>

    <script type="text/javascript" src="application/views/project/ProjectView.js" ></script>
    <script type="text/javascript" src="application/views/project/ProjectPanelView.js" ></script>
    <script type="text/javascript" src="application/views/project/ProjectManageSlideDialog.js" ></script>
    <script type="text/javascript" src="application/views/project/ProjectAddDialog.js" ></script>
    <script type="text/javascript" src="application/views/project/ProjectEditDialog.js" ></script>
    <script type="text/javascript" src="application/views/project/ProjectSearchPanel.js" ></script>

    <script type="text/javascript" src="application/views/project/ProjectAddImageListingDialog.js" ></script>
    <script type="text/javascript" src="application/views/project/ProjectAddImageThumbDialog.js" ></script>
    <script type="text/javascript" src="application/views/project/ProjectAddImageSearchPanel.js" ></script>


    <script type="text/javascript" src="application/views/annotation/AnnotationListView.js" ></script>

    <script type="text/javascript" src="application/views/Component.js" ></script>
    <script type="text/javascript" src="application/views/ApplicationView.js" ></script>
    <script type="text/javascript" src="application/views/ConfirmDialogView.js" ></script>
    <script type="text/javascript">
        $(function() {
            //Change underscore _.template function delimiter
            _.templateSettings = {
                interpolate : /\{\{(.+?)\}\}/g
            };
            // Create the app.
            var dev = true; //should be assigned dynamically by grails
            if (dev) {
                require(
                        { urlArgs: "bust=" + (new Date()).getTime() }
                );
            }
            window.app = new ApplicationController();
           /* $('#switcher').themeswitcher();*/
        });
    </script>
<script type="text/javascript" src="http://jqueryui.com/themeroller/themeswitchertool/"></script>

<body>
<div id='app'></div>
<div id='dialogs'></div>
<div id="switcher" style="position:absolute; top : 10px; left : 200px; z-index: 900;"></div>
</body>
</html>