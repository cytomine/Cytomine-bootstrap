<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="org.codehaus.groovy.grails.commons.GrailsApplication" %>
<%@ page import="grails.util.GrailsUtil" %>
<!DOCTYPE html>
<!--
 ______     __  __     ______   ______     __    __     __     __   __     ______
/\  ___\   /\ \_\ \   /\__  _\ /\  __ \   /\ "-./  \   /\ \   /\ "-.\ \   /\  ___\
\ \ \____  \ \____ \  \/_/\ \/ \ \ \/\ \  \ \ \-./\ \  \ \ \  \ \ \-.  \  \ \  __\
 \ \_____\  \/\_____\    \ \_\  \ \_____\  \ \_\ \ \_\  \ \_\  \ \_\\"\_\  \ \_____\
  \/_____/   \/_____/     \/_/   \/_____/   \/_/  \/_/   \/_/   \/_/ \/_/   \/_____/

-->
<html>
<head>
    <title>Cytomine</title>

    <link rel="icon" type="image/png" href="favicon.ico">

    <!-- Le HTML5 shim, for IE6-8 support of HTML elements -->
    <!--[if lt IE 9]>
      <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->

    <script type="text/javascript" src="http://www.google.com/jsapi"></script>
    <script type="text/javascript">
        google.load('visualization', '1', {packages: ['corechart']});
    </script>
    <!-- RequireJS -->
    <script type="text/javascript" src="lib/requirejs/require.min.js"></script>
    <link rel="stylesheet" href="lib/bootstrap-1.3/bootstrap.min.css">
    <!-- JQuery & JQuery UI-->
    <link rel='stylesheet' href='css/custom-theme/jquery-ui-1.8.7.custom.css' type='text/css'/>
    <!--<link rel='stylesheet' href='css/custom-ben/css/custom-theme/jquery-ui-1.8.16.custom.css' type='text/css'/>-->
    <script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.6.2/jquery.min.js"></script>
    <script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8.16/jquery-ui.min.js"></script>

    <!-- Twitter bootstrap -->


    <script type="text/javascript" src="lib/bootstrap-1.3/bootstrap-modal.js"></script>
    <script type="text/javascript" src="lib/bootstrap-1.3/bootstrap-dropdown.js"></script>
    <script type="text/javascript" src="lib/bootstrap-1.3/bootstrap-twipsy.js"></script>
    <script type="text/javascript" src="lib/bootstrap-1.3/bootstrap-popover.js"></script>
    <script type="text/javascript" src="lib/bootstrap-1.3/bootstrap-alerts.js"></script>


    <!-- Core Libs -->
    <script type="text/javascript" src="lib/underscore.min.js"></script>
    <script type="text/javascript" src="lib/backbone.min.js"></script>

    <!-- Modules -->


    <script type="text/javascript" src="lib/fileupload/jquery.fileupload-ui.min.js"></script>
    <script type="text/javascript" src="lib/fileupload/jquery.fileupload.min.js"></script>
    <link rel='stylesheet' href='lib/fileupload/jquery.fileupload-ui.css' type='text/css'/>

    <script type="text/javascript" src="lib/ui.panel/ui.panel.min.js"></script>
    <link type="text/css" href="lib/ui.panel/ui.panel.css" rel="stylesheet" />

    <link href="lib/dynatree/skin/ui.dynatree.css" rel="stylesheet" type="text/css">
    <script src="lib/dynatree/jquery.dynatree.min.js" type="text/javascript"></script>

    <!-- OpenLayers -->
    <!--<script type="text/javascript" src="lib/openlayers/OpenLayers.js"></script>
    <script type="text/javascript" src="lib/openlayers/OpenURL.min.js"></script>-->

    <script type="text/javascript" src="lib/farbtastic/farbtastic.min.js"></script>

    <link href="lib/colorpicker/css/colorpicker.css" rel="stylesheet" type="text/css">
    <script type="text/javascript" src="lib/colorpicker/js/colorpicker.js"></script>

    <!--jqgrid -->
    <link rel="stylesheet" type="text/css" media="screen" href="lib/jqgrid/css/ui.jqgrid.css" />
    <script src="lib/jqgrid/js/i18n/grid.locale-en.js" type="text/javascript"></script>
    <script src="lib/jqgrid/js/jquery.jqGrid.min.js" type="text/javascript"></script>
    <script src="lib/jqgrid/plugins/jquery.tablednd.min.js" type="text/javascript"></script>
    <script src="lib/jqgrid/plugins/jquery.contextmenu.min.js" type="text/javascript"></script>

    <!-- jcarrousel -->
    <script type="text/javascript" src="lib/jcarousel/jquery.carousel.min.js"></script>
    <script type="text/javascript" src="lib/tinysort/jquery.tinysort.min.js"></script>


    <!-- Styles -->
    <link rel='stylesheet' href='css/cytomine-layout.css' type='text/css'/>

    <g:if test="${GrailsUtil.environment == GrailsApplication.ENV_DEVELOPMENT}">
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
        <script type="text/javascript" src="application/controllers/AnnotationController.js" ></script>
        <script type="text/javascript" src="application/controllers/AdminController.js" ></script>
        <!-- Models -->
        <script type="text/javascript" src="application/models/ImageModel.js" ></script>
        <script type="text/javascript" src="application/models/TermModel.js" ></script>
        <script type="text/javascript" src="application/models/ImageFilter.js" ></script>
        <script type="text/javascript" src="application/models/OntologyModel.js" ></script>
        <script type="text/javascript" src="application/models/UserModel.js" ></script>
        <script type="text/javascript" src="application/models/ProjectModel.js" ></script>
        <script type="text/javascript" src="application/models/AnnotationModel.js" ></script>
        <script type="text/javascript" src="application/models/SlideModel.js" ></script>
        <script type="text/javascript" src="application/models/TransactionModel.js" ></script>
        <script type="text/javascript" src="application/models/StatsModel.js" ></script>
        <script type="text/javascript" src="application/models/CommandModel.js" ></script>
        <script type="text/javascript" src="application/models/RelationModel.js" ></script>
        <script type="text/javascript" src="application/models/SecRoleModel.js" ></script>
        <script type="text/javascript" src="application/models/SuggestedAnnotationTermModel.js" ></script>
        <!-- View -->
        <script type="text/javascript" src="application/views/auth/LoginDialogView.js" ></script>
        <script type="text/javascript" src="application/views/auth/LoadingDialogView.js" ></script>
        <script type="text/javascript" src="application/views/auth/LogoutDialogView.js" ></script>

        <script type="text/javascript" src="application/views/dashboard/AnnotationThumbView.js" ></script>
        <script type="text/javascript" src="application/views/dashboard/AnnotationView.js" ></script>
        <script type="text/javascript" src="application/views/dashboard/ProjectDashboardView.js" ></script>
        <script type="text/javascript" src="application/views/dashboard/ProjectDashboardStats.js" ></script>
        <script type="text/javascript" src="application/views/dashboard/ProjectDashboardImages.js" ></script>
        <script type="text/javascript" src="application/views/dashboard/ProjectDashboardAnnotations.js" ></script>
        <script type="text/javascript" src="application/views/dashboard/ProjectDashboardAlgos.js" ></script>


        <script type="text/javascript" src="application/views/explorer/AnnotationLayer.js" ></script>
        <script type="text/javascript" src="application/views/explorer/BrowseImageView.js" ></script>
        <script type="text/javascript" src="application/views/explorer/DraggablePanelView.js" ></script>
        <script type="text/javascript" src="application/views/explorer/ExplorerTabs.js" ></script>
        <script type="text/javascript" src="application/views/explorer/AnnotationsPanel.js" ></script>
        <script type="text/javascript" src="application/views/explorer/LayerSwitcherPanel.js" ></script>
        <script type="text/javascript" src="application/views/explorer/OverviewMapPanel.js" ></script>
        <script type="text/javascript" src="application/views/explorer/OntologyPanel.js" ></script>

        <script type="text/javascript" src="application/views/upload/UploadFormView.js" ></script>

        <script type="text/javascript" src="application/views/image/ImageThumbView.js" ></script>
        <script type="text/javascript" src="application/views/image/ImageSelectView.js" ></script>
        <script type="text/javascript" src="application/views/image/ImageView.js" ></script>
        <script type="text/javascript" src="application/views/image/ImageTabsView.js" ></script>
        <script type="text/javascript" src="application/views/image/ImagePropertiesView.js" ></script>

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
        <script type="text/javascript" src="application/views/annotation/AnnotationRetrievalView.js" ></script>

        <script type="text/javascript" src="application/views/utils/CrudGridView.js" ></script>
        <script type="text/javascript" src="application/views/Component.js" ></script>
        <script type="text/javascript" src="application/views/ApplicationView.js" ></script>
        <script type="text/javascript" src="application/views/ConfirmDialogView.js" ></script>

    </g:if>
    <g:if test="${GrailsUtil.environment == GrailsApplication.ENV_PRODUCTION}">
        <script type="text/javascript" src="application.js" ></script>
    </g:if>

    <script type="text/javascript">
        $(function() {
            //Change underscore _.template function delimiter
            _.templateSettings = {
                interpolate : /\{\{(.+?)\}\}/g
            };
            // Create the app.
            require(
                    { urlArgs: "bust=" + (new Date()).getTime() }
            );

            window.app = new ApplicationController();
        });
    </script>
    <script type="text/javascript" src="http://jqueryui.com/themeroller/themeswitchertool/"></script>
</head>
<body>

<div id='app'></div>
<div id='dialogs'></div>
<div id="switcher" style="position:absolute; top : 10px; left : 200px; z-index: 900;"></div>
<div id="alerts"></div>
</body>
</html>
