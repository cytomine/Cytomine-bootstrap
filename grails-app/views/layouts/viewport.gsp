<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="grails.util.Environment; org.codehaus.groovy.grails.commons.GrailsApplication" %>
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
<meta charset="UTF-8" />
<!-- Of course it is advisable to have touch icons ready for each device -->
<meta name="apple-mobile-web-app-capable" content="yes" />
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1, user-scalable=0" />
<meta name="apple-mobile-web-app-status-bar-style" content="black" />
<link rel="apple-touch-icon" href="images/logoGIGA.gif" />
<link rel="apple-touch-icon" sizes="72x72" href="images/logoGIGA.gif" />
<link rel="apple-touch-icon" sizes="114x114" href="images/logoGIGA.gif" />

<link href="lib/dynatree/skin/ui.dynatree.css" rel="stylesheet" type="text/css"/>
<link rel='stylesheet' href='lib/stepy/css/jquery.stepy.bootstrap.css' type='text/css'/>
<link rel='stylesheet' href='lib/multiselect-next/css/jquery.uix.multiselect.css' type='text/css'/>
<link rel="stylesheet" href="//code.jquery.com/ui/1.9.2/themes/base/jquery-ui.css" />
<link rel='stylesheet' href='lib/OpenLayers-2.13.1/theme/default/style.css' type='text/css'/>
<link rel="stylesheet" href="lib/bootstrap-3.0.3/css/bootstrap.min.css"/>


<style type="text/css">
body {
    padding-top: 50px;
}
</style>

<link rel='stylesheet' href='css/cytomine-layout.css' type='text/css'/>
<link rel='stylesheet' href='lib/fileupload-8.8.3/css/jquery.fileupload-ui.css' type='text/css'/>
<link rel="stylesheet" type="text/css" href="lib/bootstrap-wysihtml5-0.0.2/bootstrap-wysihtml5-0.0.2.css"/>

<script type="text/javascript" src="lib/mousetrap.min.js"></script>

<wthr:isOldMsie>
    <script src="//ajax.googleapis.com/ajax/libs/jquery/1.11.0/jquery.min.js"></script>-
    <script src="/lib/r2d3/r2d3.js" charset="utf-8"></script>
    <script src="lib/html5.js"></script>
    <script src="lib/Respond/src/respond.js"></script>
</wthr:isOldMsie>
<wthr:isNotOldMsie>
    <script type="text/javascript" src ="lib/jquery-2.0.3.min.js"></script>
    %{--<script src="lib/nvd3-1.1.15/lib/d3.v3.js"></script>--}%
    %{--<script src="lib/nvd3-1.1.15/nv.d3.js"></script>--}%

    <script src="lib/nvd3/d3.min.js" charset="utf-8"></script>
    <script src="lib/nvd3/nv.d3.min.js" charset="utf-8"></script>
    <link rel="stylesheet" href="lib/nvd3/nv.d3.css" />

</wthr:isNotOldMsie>

<script type="text/javascript" src="lib/jquery-ui-bootstrap/js/jquery-ui-1.9.2.custom.min.js"></script>
<link rel="stylesheet" type="text/css" href="lib/jquery-ui-bootstrap/css/custom-theme/jquery-ui-1.9.2.custom.css" />
<link rel="stylesheet" type="text/css" href="lib/jquery-ui-bootstrap/css/custom-theme/jquery.ui.1.9.2.ie.css" />

%{--<link rel='stylesheet' href='lib/magicsuggest/magicsuggest-1.2.7-min.css'/>--}%
%{--<script type="text/javascript" src="lib/magicsuggest/magicsuggest-1.2.7-min.js"></script>--}%

<script type="text/javascript" src="lib/bowser/bowser.min.js"></script>


%{--<link rel="stylesheet" href="https://cdn.rawgit.com/novus/nvd3/v1.7.1/build/nv.d3.css" /> <!-- bug with svg style in nb.d3.min.css -->--}%

%{--<script type="text/javascript" src="lib/OpenLayers-2.12/Openlayers-cytomine.js"></script>--}%
<script type="text/javascript" src="lib/OpenLayers-2.13.1/OpenLayers.js"></script>

<!-- fileupload -->
<script type="text/javascript" src="lib/fileupload-8.8.3/js/jquery.iframe-transport.js"></script>
<script type="text/javascript" src="lib/fileupload-8.8.3/js/jquery.fileupload.js"></script>
<script type="text/javascript" src="lib/fileupload-8.8.3/js/cors/jquery.xdr-transport.js"></script>
<script type="text/javascript" src="lib/fileupload-8.8.3/js/cors/jquery.postmessage-transport.js"></script>



<script type="text/javascript" src="lib/cookie/jquery.cookie.js"></script>



<!-- Datatables -->
<script src="lib/DataTables-1.9.4/media/js/jquery.dataTables.min.js" type="text/javascript"></script>

<script type="text/javascript" src="lib/pick-a-color-master/build/1.2.2/js/pick-a-color-1.2.2.min.js"></script>
<script type="text/javascript" src="lib/TinyColor-master/dist/tinycolor-min.js"></script>
<link rel="stylesheet" href="lib/pick-a-color-master/build/1.2.2/css/pick-a-color-1.2.2.min.css"/>

<!-- RequireJS -->
<script type="text/javascript" src="lib/requirejs/require.js"></script>

<!-- Twitter bootstrap -->
<script type="text/javascript" src="lib/bootstrap-3.0.3/js/bootstrap.js"></script>
%{--<script type="text/javascript" src="lib/bootstrap-3.2.0/js/bootstrap-transition.js"></script>--}%
%{--<script type="text/javascript" src="lib/bootstrap-3.2.0/js/bootstrap-collapse.js"></script>--}%
<script type="text/javascript" src="lib/typeahead.js/typeahead.js"></script>


<script src="lib/ckeditor/ckeditor.js"></script>

<link href="lib/magicsuggest2//magicsuggest-min.css" rel="stylesheet"/>
<script type="text/javascript" src="lib/magicsuggest2/magicsuggest-min.js"></script>

<g:if test="${Environment.getCurrent() == Environment.DEVELOPMENT || Environment.getCurrent() == Environment.CUSTOM}">

<!-- Core Libs -->
<script type="text/javascript" src="lib/underscore.js"></script>
<script type="text/javascript" src="lib/backbone.js"></script>
<script type="text/javascript" src="lib/backbone.paginator.js"></script>

<!-- wysihtml5 -->

<script src="lib/bootstrap-wysihtml5-0.0.2/libs/js/wysihtml5-0.3.0_rc2.min.js"></script>
<script src="lib/bootstrap-wysihtml5-0.0.2/bootstrap-wysihtml5-0.0.2.min.js"></script>

<!-- wysihtml5 parser rules -->
%{--<script src="lib/wysiwyg/parser_rules/advanced.js"></script>--}%
%{--<!-- Library -->--}%
%{--<script src="lib/wysiwyg/dist/wysihtml5-0.3.0.min.js"></script>--}%

<script type="text/javascript" src="lib/json2.js"></script>


<script src="lib/strftime-min.js" type="text/javascript"></script>

<script src="lib/dynatree/jquery.dynatree.js" type="text/javascript"></script>

<script src="lib/ui.panel/ui.panel.min.js" type="text/javascript"></script>








<script type="text/javascript" src="lib/multiselect-next/js/jquery.uix.multiselect.js"></script>

<script type="text/javascript" src="lib/multiselectResolveConflict.js"></script>

<script type="text/javascript" src="lib/multiselect/src/jquery.multiselect.js"></script>
<script type="text/javascript" src="lib/multiselect/src/jquery.multiselect.filter.js"></script>

<script type="text/javascript" src="lib/stepy/js/jquery.stepy.min.js"></script>


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
<script type="text/javascript" src="application/controllers/SearchController.js" ></script>
<script type="text/javascript" src="application/controllers/AccountController.js" ></script>
<script type="text/javascript" src="application/controllers/PhonoController.js" ></script>
<script type="text/javascript" src="application/controllers/UserDashboardController.js" ></script>


<!-- Models -->

<script type="text/javascript" src="application/models/PaginatorModel.js" ></script>

<script type="text/javascript" src="application/models/DescriptionModel.js" ></script>
<script type="text/javascript" src="application/models/DisciplineModel.js" ></script>

<script type="text/javascript" src="application/models/UploadedFileModel.js" ></script>
<script type="text/javascript" src="application/models/AnnotationsFilterModel.js" ></script>
<script type="text/javascript" src="application/models/ImageModel.js" ></script>
<script type="text/javascript" src="application/models/TermModel.js" ></script>
<script type="text/javascript" src="application/models/ImageFilter.js" ></script>
<script type="text/javascript" src="application/models/OntologyModel.js" ></script>

<script type="text/javascript" src="application/models/UserModel.js" ></script>
<script type="text/javascript" src="application/models/GroupModel.js" ></script>
<script type="text/javascript" src="application/models/ProjectModel.js" ></script>
<script type="text/javascript" src="application/models/AnnotationModel.js" ></script>
<script type="text/javascript" src="application/models/SlideModel.js" ></script>
<script type="text/javascript" src="application/models/StatsModel.js" ></script>
<script type="text/javascript" src="application/models/CommandModel.js" ></script>
<script type="text/javascript" src="application/models/RelationModel.js" ></script>
<script type="text/javascript" src="application/models/SearchModel.js" ></script>
<script type="text/javascript" src="application/models/SecRoleModel.js" ></script>
<script type="text/javascript" src="application/models/SuggestedAnnotationTermModel.js" ></script>
<script type="text/javascript" src="application/models/JobModel.js" ></script>
<script type="text/javascript" src="application/models/JobDataModel.js" ></script>
<script type="text/javascript" src="application/models/SoftwareModel.js" ></script>
<script type="text/javascript" src="application/models/PingModel.js" ></script>
<script type="text/javascript" src="application/models/TaskModel.js" ></script>
<script type="text/javascript" src="application/models/StorageModel.js" ></script>
<script type="text/javascript" src="application/models/AnnotationPropertyModel.js" ></script>
<script type="text/javascript" src="application/models/ImageInstancePropertyModel.js" ></script>
<script type="text/javascript" src="application/models/ProjectPropertyModel.js" ></script>




<!-- View -->
<script type="text/javascript" src="application/views/user/UserDashboardView.js"></script>
<script type="text/javascript" src="application/views/activity/ActivityView.js" ></script>
<script type="text/javascript" src="application/views/search/SearchEngineFilterView.js" ></script>
<script type="text/javascript" src="application/views/search/SearchView.js" ></script>
<script type="text/javascript" src="application/views/search/SearchResultView.js" ></script>

<script type="text/javascript" src="application/views/phono/PhonoMenu.js" ></script>
<script type="text/javascript" src="application/views/account/AccountDetails.js" ></script>

<script type="text/javascript" src="application/views/auth/LoginDialogView.js" ></script>
<script type="text/javascript" src="application/views/auth/LoadingDialogView.js" ></script>
<script type="text/javascript" src="application/views/auth/LogoutDialogView.js" ></script>

<script type="text/javascript" src="application/views/utils/MultiSelectView.js" ></script>


<script type="text/javascript" src="application/views/dashboard/AddImageToProjectDialog.js" ></script>
<script type="text/javascript" src="application/views/dashboard/AnnotationThumbView.js" ></script>
<script type="text/javascript" src="application/views/dashboard/AnnotationView.js" ></script>
<script type="text/javascript" src="application/views/dashboard/ProjectDashboardView.js" ></script>
<script type="text/javascript" src="application/views/dashboard/ProjectDashboardStats.js" ></script>
<script type="text/javascript" src="application/views/dashboard/ProjectDashboardImages.js" ></script>
<script type="text/javascript" src="application/views/dashboard/ProjectDashboardAnnotations.js" ></script>
<script type="text/javascript" src="application/views/dashboard/ProjectDashboardAlgos.js" ></script>
<script type="text/javascript" src="application/views/dashboard/ProjectDashboardConfig.js" ></script>
<script type="text/javascript" src="application/views/dashboard/ProjectDashboardProperties.js" ></script>

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
<script type="text/javascript" src="application/views/explorer/AnnotationPropertyLayer.js" ></script>
<script type="text/javascript" src="application/views/explorer/BrowseImageView.js" ></script>
<script type="text/javascript" src="application/views/explorer/LeafletView.js" ></script>
<script type="text/javascript" src="application/views/explorer/ExplorerTabs.js" ></script>
<script type="text/javascript" src="application/views/explorer/AnnotationsPanel.js" ></script>
<script type="text/javascript" src="application/views/explorer/LayerSwitcherPanel.js" ></script>
<script type="text/javascript" src="application/views/explorer/ImageFiltersPanel.js" ></script>
<script type="text/javascript" src="application/views/explorer/OverviewMapPanel.js" ></script>
<script type="text/javascript" src="application/views/explorer/OntologyPanel.js" ></script>
<script type="text/javascript" src="application/views/explorer/ReviewPanel.js" ></script>
<script type="text/javascript" src="application/views/explorer/MultiDimensionPanel.js" ></script>
<script type="text/javascript" src="application/views/explorer/InformationsPanel.js" ></script>
<script type="text/javascript" src="application/views/explorer/JobTemplatePanel.js" ></script>
<script type="text/javascript" src="application/views/explorer/AnnotationPropertyPanel.js" ></script>
<script type="text/javascript" src="application/views/explorer/AnnotationPopupPanel.js" ></script>


<script type="text/javascript" src="application/views/upload/UploadFormView.js" ></script>

<script type="text/javascript" src="application/views/image/ImageReviewAction.js" ></script>
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
<script type="text/javascript" src="application/views/project/ProjectAddDialog.js" ></script>
<script type="text/javascript" src="application/views/project/ProjectCommandView.js" ></script>
<script type="text/javascript" src="application/views/project/ProjectSearchPanel.js" ></script>
<script type="text/javascript" src="application/views/project/ProjectDescriptionDialog.js" ></script>

<script type="text/javascript" src="application/views/project/ProjectInfoDialog.js" ></script>


<script type="text/javascript" src="application/views/annotation/AnnotationListView.js" ></script>
<script type="text/javascript" src="application/views/annotation/AnnotationRetrievalView.js" ></script>
<script type="text/javascript" src="application/views/annotation/AnnotationQuestionableView.js" ></script>
<script type="text/javascript" src="application/views/annotation/ShareAnnotationView.js" ></script>

<script type="text/javascript" src="application/views/review/ReviewStatsListing.js" ></script>
<script type="text/javascript" src="application/views/review/ReviewLastReviewListing.js" ></script>
<script type="text/javascript" src="application/views/review/ReviewTermListing.js" ></script>
<script type="text/javascript" src="application/views/review/ReviewAnnotationListing.js" ></script>
<script type="text/javascript" src="application/views/review/DashboardReviewPanel.js" ></script>

<script type="text/javascript" src="application/views/utils/CrudGridView.js" ></script>
<script type="text/javascript" src="application/views/Component.js" ></script>
<script type="text/javascript" src="application/views/ApplicationView.js" ></script>
<script type="text/javascript" src="application/views/ConfirmDialogView.js" ></script>

<script type="text/javascript" src="application/utils/CustomUI.js" ></script>
<script type="text/javascript" src="application/utils/CustomModal.js" ></script>
<script type="text/javascript" src="application/utils/BrowserSupport.js" ></script>
<script type="text/javascript" src="application/utils/HotKeys.js" ></script>
<script type="text/javascript" src="application/utils/processing/image/Utils.js" ></script>
<script type="text/javascript" src="application/utils/processing/image/Invert.js" ></script>
<script type="text/javascript" src="application/utils/processing/image/MagicWand.js" ></script>
<script type="text/javascript" src="application/utils/processing/image/Outline.js" ></script>
<script type="text/javascript" src="application/utils/processing/image/Threshold.js" ></script>
<script type="text/javascript" src="application/utils/processing/image/ColorChannel.js" ></script>
</g:if>



<g:if test="${Environment.getCurrent() != Environment.PRODUCTION}">
    <script type="text/javascript">
        //Prevent IE to cache AJAX request
        $.ajaxSetup({ cache: false });
        setTimeout(function(){
            if (navigator.appVersion.indexOf("Linux")!=-1) {
                $("#j_username").val("lrollus");
                $("#j_password").val("lR$2011");
            }
            if (navigator.appVersion.indexOf("Mac")!=-1) {
                $("#j_username").val("stevben");
            }

        }, 1000);

        //disable console for not supported by browser (ie lesser than 8)
        var alertFallback = false;
        if (typeof console === "undefined" || typeof console.log === "undefined") {
            console = {};
            if (alertFallback) {
                console.log = function(msg) {
                    alert(msg);
                };
            } else {
                console.log = function() {};
            }
        }
    </script>
</g:if>
</head>
<body>

<div id="modals">
</div>
<div id="hotkeys">
</div>
<div id="similarannotationmodal">
</div>
<div id='dialogs'>
</div>
<div id="alerts"></div>
<div id="phono-messages"></div>

<script type="text/javascript">
    function showClassicWidget() {
        /*		FreshWidget.show();
         return false;*/
    }
</script>


</body>

<g:if test="${Environment.getCurrent() == Environment.PRODUCTION}">
    <script type="text/javascript" src="http://assets.freshdesk.com/widget/freshwidget.js"></script>
    <script type="text/javascript">

        FreshWidget.init("", {"queryString": "&amp;widgetType=popup", "widgetType": "popup", "buttonType": "text", "buttonText": "Support", "buttonColor": "white", "buttonBg": "#0064eb", "alignment": "4", "offset": "-1500px", "formHeight": "500px", "url": "https://cytomine.freshdesk.com"} );

    </script>

    <script type="text/javascript" src="lib.js?version=${grailsApplication.metadata.'app.version'}" ></script>
    <script type="text/javascript" src="application.js?version=${grailsApplication.metadata.'app.version'}" ></script>

</g:if>

<g:if test="${Environment.getCurrent() == Environment.DEVELOPMENT || Environment.getCurrent() == Environment.CUSTOM }">
    <script type="text/javascript" src="http://assets.freshdesk.com/widget/freshwidget.js"></script>
    <script type="text/javascript">

        FreshWidget.init("", {"queryString": "&amp;widgetType=popup", "widgetType": "popup", "buttonType": "text", "buttonText": "Support", "buttonColor": "white", "buttonBg": "#0064eb", "alignment": "4", "offset": "-1500px", "formHeight": "500px", "url": "https://cytomine.freshdesk.com"} );

    </script>


    <script type="text/javascript">
        $(function() {
            require(
                    { urlArgs: "bust=" + (new Date()).getTime() }
            );
            window.app = new ApplicationController();
            window.app.coreServer =  "${grailsApplication.config.grails.serverURL}";
            window.app.uploadServer =  "${grailsApplication.config.grails.uploadURL}";
            window.app.params =  {};
            window.app.params.readOnlyProjectsByDefault =  "${grailsApplication.config.grails.readOnlyProjectsByDefault}";
        });
    </script>
</g:if>

<g:if test="${Environment.getCurrent() == Environment.PRODUCTION}">
    <script type="text/javascript">
        $(function() {
            require(
                    { urlArgs: "bust=${grailsApplication.metadata.'app.version'}" }
            );
            window.app = new ApplicationController();
            window.app.coreServer =  "${grailsApplication.config.grails.serverURL}";
            window.app.uploadServer =  "${grailsApplication.config.grails.uploadURL}";
            window.app.params =  {};
            window.app.params.readOnlyProjectsByDefault =  "${grailsApplication.config.grails.readOnlyProjectsByDefault}";
        });
    </script>




</g:if>



</html>
