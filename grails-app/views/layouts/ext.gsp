<html>
<head>
  <title><g:layoutTitle default="Grails" /></title>
  <script type="text/javascript" src="${resource(dir:'js',file:'ext-3.3.1/adapter/ext/ext-base.js')}" ></script>
  <script type="text/javascript" src="${resource(dir:'js',file:'ext-3.3.1/ext-all-debug.js')}" ></script>
  <script type="text/javascript" src="${resource(dir:'js',file:'ext-3.3.1/examples/shared/extjs/App.js')}" ></script>
  <script type="text/javascript" src="${resource(dir:'js',file:'openlayers/OpenLayers.js')}" ></script>
  <script type="text/javascript" src="${resource(dir:'js',file:'openlayers/OpenURL.js')}" ></script>
  <link rel="stylesheet" href="${resource(dir:'js',file:'ext-3.3.1/resources/css/ext-all.css')}" />
  <link rel="stylesheet" href="${resource(dir:'js',file:'ext-3.3.1/resources/css/xtheme-gray.css')}" />
  <script type="text/javascript" charset="utf-8">
    Ext.BLANK_IMAGE_URL = "${resource(dir:'js',file:'ext-3.3.1/resources/images/default/s.gif')}";
  </script>

  <!-- EXT JS Extensions -->
  <script type="text/javascript" src="${resource(dir:'js',file:'ext-3.3.1/examples/ux/RowEditor.js')}" ></script>
  <script type="text/javascript" src="${resource(dir:'js',file:'ext-3.3.1/examples/ux/TabCloseMenu.js')}" ></script>

  <!-- Views -->
  <script type="text/javascript" src="${resource(dir:'js',file:'be/cytomine/views/Notifications.js')}" ></script>
  <script type="text/javascript" src="${resource(dir:'js',file:'be/cytomine/views/User.js')}" ></script>
  <script type="text/javascript" src="${resource(dir:'js',file:'be/cytomine/views/Project.js')}" ></script>
  <script type="text/javascript" src="${resource(dir:'js',file:'be/cytomine/views/Project.js')}" ></script>
  <script type="text/javascript" src="${resource(dir:'js',file:'be/cytomine/views/Retrieval.js')}" ></script>
  <script type="text/javascript" src="${resource(dir:'js',file:'be/cytomine/views/Dashboard.js')}" ></script>
  <script type="text/javascript" src="${resource(dir:'js',file:'be/cytomine/views/Image.js')}" ></script>
  <script type="text/javascript" src="${resource(dir:'js',file:'be/cytomine/views/AnnotationLayer.js')}" ></script>
  <script type="text/javascript" src="${resource(dir:'js',file:'be/cytomine/views/Browser.js')}" ></script>
  <script type="text/javascript" src="${resource(dir:'js',file:'be/cytomine/views/Admin.js')}" ></script>


  <!-- Langs -->
  <script type="text/javascript" src="${resource(dir:'js',file:'be/cytomine/languages/fr.js')}" ></script>

  <!-- Models -->
  <script type="text/javascript" src="${resource(dir:'js',file:'be/cytomine/models/Annotation.js')}" ></script>
  <script type="text/javascript" src="${resource(dir:'js',file:'be/cytomine/models/User.js')}" ></script>
  <script type="text/javascript" src="${resource(dir:'js',file:'be/cytomine/models/Project.js')}" ></script>

  <!-- Cytomine -->
  <script type="text/javascript" src="${resource(dir:'js',file:'be/cytomine/Application.js')}" ></script>


  <link rel="stylesheet" href="${resource(dir:'css',file:'loading.css')}" />
  <link rel="stylesheet" href="${resource(dir:'css',file:'icons.css')}" />
  <link rel="stylesheet" href="${resource(dir:'css',file:'ext.css')}" />
  <g:layoutHead />

</head>
<body onload="${pageProperty(name:'body.onload')}">
<div id="loading-mask"></div>
<div id="loading">
  <div class="loading-indicator">
    Chargement...
  </div>
</div>

<div id="helpTabs">
  <li>Help</li>

</div>
<g:layoutBody />



</body>
</html>

