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

<!-- Of course it is advisable to have touch icons ready for each device -->
<meta name="apple-mobile-web-app-capable" content="yes" />
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1, user-scalable=0" />
<meta name="apple-mobile-web-app-status-bar-style" content="black" />
<link rel="apple-touch-icon" href="images/logoGIGA.gif" />
<link rel="apple-touch-icon" sizes="72x72" href="images/logoGIGA.gif" />
<link rel="apple-touch-icon" sizes="114x114" href="images/logoGIGA.gif" />

<link rel='stylesheet' href='css/bootstrap/jquery-ui-1.8.16.custom.css' type='text/css'/>
<link rel='stylesheet' href='lib/ui.panel/ui.panel.css' type='text/css'/>
<link href="lib/dynatree/skin/ui.dynatree.css" rel="stylesheet" type="text/css"/>
<link rel="stylesheet" type="text/css" media="screen" href="lib/jqgrid/css/ui.jqgrid.css" />
<link rel='stylesheet' href='lib/stepy/css/jquery.stepy.bootstrap.css' type='text/css'/>
<link rel='stylesheet' href='lib/multiselect-next/css/ui.multiselect.css' type='text/css'/>


<link rel="stylesheet" href="lib/bootstrap-2.1.1/css/bootstrap.css"/>
<style type="text/css">
body {
    padding-top: 44px;

}
</style>
<link rel="stylesheet" href="lib/bootstrap-2.1.1/css/bootstrap-responsive.min.css"/>
<!--<link rel="stylesheet" href="css/darkstrap-v0.9.0.css"/>-->


<link rel='stylesheet' href='css/cytomine-layout.css' type='text/css'/>
<link rel='stylesheet' href='css/glyphicon.css' type='text/css'/>
<link rel='stylesheet' href='lib/fileupload/jquery.fileupload-ui.css' type='text/css'/>
<!--<link rel='stylesheet' href='http://danneu.com/bag/darkstrap/css/darkstrap.css' type='text/css'/>-->

<!-- JQuery & JQuery UI -->
<script type="text/javascript" src="//ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js"></script>
<script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8.16/jquery-ui.min.js"></script>

<!--
<link rel="stylesheet" href="lib/add-to-homescreen/style/add2home.css" />
<script type="text/javascript" src="lib/add-to-homescreen/src/add2home.js" charset="utf-8"></script>
-->

<!--[if lt IE 9]>
    <script src="//html5shiv.googlecode.com/svn/trunk/html5.js"></script>
<![endif]-->
    <script type="text/javascript" src="lib/OpenLayers-2.12/OpenLayers.custom.js"></script>

<g:if test="${GrailsUtil.environment != GrailsApplication.ENV_PRODUCTION}">

    <!-- RequireJS -->
    <script type="text/javascript" src="lib/requirejs/require.js"></script>

    <!-- Twitter bootstrap -->
    <script type="text/javascript" src="lib/bootstrap-2.1.1/js/bootstrap.js"></script>

    <!-- Core Libs -->
    <script type="text/javascript" src="lib/underscore.js"></script>
    <script type="text/javascript" src="lib/backbone.js"></script>
    <script type="text/javascript" src="lib/json2.js"></script>

    <!-- Phono -->
    <script type="text/javascript" src="http://s.phono.com/releases/0.3/jquery.phono.js"></script>

    <!-- fileupload -->
    <script type="text/javascript" src="lib/fileupload/vendor/jquery.ui.widget.js"></script>
    <script type="text/javascript" src="lib/fileupload/jquery.iframe-transport.js"></script>
    <script type="text/javascript" src="lib/fileupload/jquery.fileupload.js"></script>
    <script type="text/javascript" src="lib/fileupload/cors/jquery.xdr-transport.js"></script>


    <script src="lib/dynatree/jquery.dynatree.js" type="text/javascript"></script>

    <script src="lib/ui.panel/ui.panel.min.js" type="text/javascript"></script>

    <script type="text/javascript" src="lib/farbtastic/farbtastic.js"></script>



    <script type="text/javascript" src="lib/multiselect-next/js/ui.multiselect.js"></script>
    <script type="text/javascript" src="lib/multiselect-next/js/plugins/tmpl/jquery.tmpl.1.1.1.js"></script>

    <script type="text/javascript" src="lib/multiselectResolveConflict.js"></script>

    <script type="text/javascript" src="lib/multiselect/src/jquery.multiselect.js"></script>
    <script type="text/javascript" src="lib/multiselect/src/jquery.multiselect.filter.js"></script>

    <script type="text/javascript" src="lib/stepy/js/jquery.stepy.min.js"></script>

    <!--<link href="lib/colorpicker/css/colorpicker.css" rel="stylesheet" type="text/css"/>
    <script type="text/javascript" src="lib/colorpicker/js/colorpicker.js"></script>-->

    <!--jqgrid -->
    <script src="lib/jqgrid/js/i18n/grid.locale-en.js" type="text/javascript"></script>
    <script src="lib/jqgrid/js/jquery.jqGrid.src.js" type="text/javascript"></script>

    <!-- Datatables -->
    <script src="lib/DataTables-1.9.0/media/js/jquery.dataTables.js" type="text/javascript"></script>


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
    <script type="text/javascript" src="application/controllers/ActivityController.js" ></script>
    <script type="text/javascript" src="application/controllers/AccountController.js" ></script>
    <script type="text/javascript" src="application/controllers/PhonoController.js" ></script>
    <!-- Models -->
    <script type="text/javascript" src="application/models/UploadedFileModel.js" ></script>
    <script type="text/javascript" src="application/models/AnnotationsFilterModel.js" ></script>
    <script type="text/javascript" src="application/models/ImageModel.js" ></script>
    <script type="text/javascript" src="application/models/TermModel.js" ></script>
    <script type="text/javascript" src="application/models/ImageFilter.js" ></script>
    <script type="text/javascript" src="application/models/OntologyModel.js" ></script>
    <script type="text/javascript" src="application/models/DisciplineModel.js" ></script>
    <script type="text/javascript" src="application/models/UserModel.js" ></script>
    <script type="text/javascript" src="application/models/ProjectModel.js" ></script>
    <script type="text/javascript" src="application/models/AnnotationModel.js" ></script>
    <script type="text/javascript" src="application/models/SlideModel.js" ></script>
    <script type="text/javascript" src="application/models/StatsModel.js" ></script>
    <script type="text/javascript" src="application/models/CommandModel.js" ></script>
    <script type="text/javascript" src="application/models/RelationModel.js" ></script>
    <script type="text/javascript" src="application/models/SecRoleModel.js" ></script>
    <script type="text/javascript" src="application/models/SuggestedAnnotationTermModel.js" ></script>
    <script type="text/javascript" src="application/models/JobModel.js" ></script>
    <script type="text/javascript" src="application/models/JobDataModel.js" ></script>
    <script type="text/javascript" src="application/models/SoftwareModel.js" ></script>
    <script type="text/javascript" src="application/models/PingModel.js" ></script>
    <script type="text/javascript" src="application/models/TaskModel.js" ></script>
    <script type="text/javascript" src="application/models/StorageModel.js" ></script>

    <!-- View -->
    <script type="text/javascript" src="application/views/activity/ActivityView.js" ></script>
    <script type="text/javascript" src="application/views/phono/PhonoMenu.js" ></script>
    <script type="text/javascript" src="application/views/account/AccountDetails.js" ></script>

    <script type="text/javascript" src="application/views/auth/LoginDialogView.js" ></script>
    <script type="text/javascript" src="application/views/auth/LoadingDialogView.js" ></script>
    <script type="text/javascript" src="application/views/auth/LogoutDialogView.js" ></script>

    <script type="text/javascript" src="application/views/utils/MultiSelectView.js" ></script>

    <script type="text/javascript" src="application/views/dashboard/AnnotationThumbView.js" ></script>
    <script type="text/javascript" src="application/views/dashboard/AnnotationView.js" ></script>
    <script type="text/javascript" src="application/views/dashboard/ProjectDashboardView.js" ></script>
    <script type="text/javascript" src="application/views/dashboard/ProjectDashboardStats.js" ></script>
    <script type="text/javascript" src="application/views/dashboard/ProjectDashboardImages.js" ></script>
    <script type="text/javascript" src="application/views/dashboard/ProjectDashboardAnnotations.js" ></script>
    <script type="text/javascript" src="application/views/dashboard/ProjectDashboardAlgos.js" ></script>
    <script type="text/javascript" src="application/views/dashboard/ProjectDashboardConfig.js" ></script>

    <script type="text/javascript" src="application/views/processing/JobListingView.js" ></script>
    <script type="text/javascript" src="application/views/processing/JobComparatorView.js" ></script>
    <script type="text/javascript" src="application/views/processing/JobSelectionView.js" ></script>
    <script type="text/javascript" src="application/views/processing/LaunchJobView.js" ></script>
    <script type="text/javascript" src="application/views/processing/JobResultView.js" ></script>
    <script type="text/javascript" src="application/views/processing/SoftwareDetailsView.js"></script>
    <script type="text/javascript" src="application/views/processing/JobSearchView.js"></script>
    <script type="text/javascript" src="application/views/processing/JobTableView.js"></script>
    <script type="text/javascript" src="application/views/processing/JobSearchEngineView.js"></script>
    <script type="text/javascript" src="application/views/processing/JobDeleteAllDataView.js"></script>

    <script type="text/javascript" src="application/views/processing/result/RetrievalAlgoResult.js"></script>
    <script type="text/javascript" src="application/views/processing/result/EvolutionAlgoResult.js"></script>
    <script type="text/javascript" src="application/views/processing/result/DefaultResult.js"></script>
    <script type="text/javascript" src="application/views/processing/result/DownloadFiles.js"></script>

    <script type="text/javascript" src="application/views/explorer/SideBarPanel.js" ></script>
    <script type="text/javascript" src="application/views/explorer/AnnotationLayer.js" ></script>
    <script type="text/javascript" src="application/views/explorer/BrowseImageView.js" ></script>
    <script type="text/javascript" src="application/views/explorer/ExplorerTabs.js" ></script>
    <script type="text/javascript" src="application/views/explorer/AnnotationsPanel.js" ></script>
    <script type="text/javascript" src="application/views/explorer/LayerSwitcherPanel.js" ></script>
    <script type="text/javascript" src="application/views/explorer/ImageFiltersPanel.js" ></script>
    <script type="text/javascript" src="application/views/explorer/OverviewMapPanel.js" ></script>
    <script type="text/javascript" src="application/views/explorer/OntologyPanel.js" ></script>
    <script type="text/javascript" src="application/views/explorer/ReviewPanel.js" ></script>
    <script type="text/javascript" src="application/views/explorer/InformationsPanel.js" ></script>

    <script type="text/javascript" src="application/views/upload/UploadFormView.js" ></script>

    <script type="text/javascript" src="application/views/image/ImageThumbView.js" ></script>
    <script type="text/javascript" src="application/views/image/ImageSelectView.js" ></script>
    <script type="text/javascript" src="application/views/image/ImageView.js" ></script>
    <script type="text/javascript" src="application/views/image/ImageTabsView.js" ></script>
    <script type="text/javascript" src="application/views/image/ImagePropertiesView.js" ></script>

    <script type="text/javascript" src="application/views/ontology/OntologyUsersDialog.js" ></script>
    <script type="text/javascript" src="application/views/ontology/OntologyPanelView.js" ></script>
    <script type="text/javascript" src="application/views/ontology/OntologyView.js" ></script>
    <script type="text/javascript" src="application/views/ontology/OntologyAddOrEditTermView.js" ></script>
    <script type="text/javascript" src="application/views/ontology/OntologyTreeView.js" ></script>
    <script type="text/javascript" src="application/views/ontology/OntologyEditDialog.js" ></script>
    <script type="text/javascript" src="application/views/ontology/OntologyAddDialog.js" ></script>

    <script type="text/javascript" src="application/views/project/ProjectUsersDialog.js" ></script>
    <script type="text/javascript" src="application/views/project/ProjectView.js" ></script>
    <script type="text/javascript" src="application/views/project/ProjectPanelView.js" ></script>
    <script type="text/javascript" src="application/views/project/ProjectManageSlideDialog.js" ></script>
    <script type="text/javascript" src="application/views/project/ProjectAddDialog.js" ></script>
    <script type="text/javascript" src="application/views/project/ProjectEditDialog.js" ></script>
    <script type="text/javascript" src="application/views/project/ProjectSearchPanel.js" ></script>

    <script type="text/javascript" src="application/views/project/ProjectAddImageListingDialog.js" ></script>
    <script type="text/javascript" src="application/views/project/ProjectAddImageThumbDialog.js" ></script>
    <script type="text/javascript" src="application/views/project/ProjectAddImageSearchPanel.js" ></script>
    <script type="text/javascript" src="application/views/project/ProjectInfoDialog.js" ></script>


    <script type="text/javascript" src="application/views/annotation/AnnotationListView.js" ></script>
    <script type="text/javascript" src="application/views/annotation/AnnotationRetrievalView.js" ></script>
    <script type="text/javascript" src="application/views/annotation/AnnotationQuestionableView.js" ></script>
    <script type="text/javascript" src="application/views/annotation/ShareAnnotationView.js" ></script>

    <script type="text/javascript" src="application/views/utils/CrudGridView.js" ></script>
    <script type="text/javascript" src="application/views/Component.js" ></script>
    <script type="text/javascript" src="application/views/ApplicationView.js" ></script>
    <script type="text/javascript" src="application/views/ConfirmDialogView.js" ></script>

    <script type="text/javascript" src="application/utils/processing/image/Utils.js" ></script>
    <script type="text/javascript" src="application/utils/processing/image/Invert.js" ></script>
    <script type="text/javascript" src="application/utils/processing/image/MagicWand.js" ></script>
    <script type="text/javascript" src="application/utils/processing/image/Outline.js" ></script>
    <script type="text/javascript" src="application/utils/processing/image/Threshold.js" ></script>
    <script type="text/javascript" src="application/utils/processing/image/ColorChannel.js" ></script>
</g:if>



<g:if test="${GrailsUtil.environment != GrailsApplication.ENV_PRODUCTION}">
    <script type="text/javascript">
        setTimeout(function(){
            if (navigator.appVersion.indexOf("Linux")!=-1) {
                $("#j_username").val("lrollus");
                $("#j_password").val("lR$2011");
            }
            if (navigator.appVersion.indexOf("Mac")!=-1) {
                $("#j_username").val("stevben");
            }

        }, 1000);
    </script>
</g:if>
</head>
<body>

<div id='dialogs'></div>
<div id="alerts"></div>
<div id="phono-messages"></div>
<g:if test="${GrailsUtil.environment == GrailsApplication.ENV_PRODUCTION}">
<script type="text/javascript">
    var uvOptions = {};
    (function() {
        var uv = document.createElement('script'); uv.type = 'text/javascript'; uv.async = true;
        uv.src = ('https:' == document.location.protocol ? 'https://' : 'http://') + 'widget.uservoice.com/Ck6lCDC4t7hqSRznqq9yw.js';
        var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(uv, s);
    })();
</script>
</g:if>
</body>
<g:if test="${GrailsUtil.environment == GrailsApplication.ENV_PRODUCTION}">
    <script type="text/javascript" src="lib.js" ></script>
    <script type="text/javascript" src="application.js" ></script>

</g:if>
<script type="text/javascript">
    $(function() {
        require(
                { urlArgs: "bust=" + (new Date()).getTime() }
        );
        window.app = new ApplicationController();
    });
</script>

<!-- Google Charts -->
<script type="text/javascript" src="http://www.google.com/jsapi"></script>
<script type="text/javascript">
    google.load('visualization', '1', {packages: ['corechart','table']});
</script>

</html>