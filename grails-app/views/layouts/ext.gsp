<html>
<head>
  <title><g:layoutTitle default="Grails" /></title>
  <script type="text/javascript" src="${createLinkTo(dir:'js',file:'ext-3.3.1/adapter/ext/ext-base.js')}" ></script>
  <script type="text/javascript" src="${createLinkTo(dir:'js',file:'ext-3.3.1/ext-all-debug.js')}" ></script>
  <link rel="stylesheet" href="${createLinkTo(dir:'js',file:'ext-3.3.1/resources/css/xtheme-gray.css')}" />
  <link rel="stylesheet" href="${createLinkTo(dir:'js',file:'ext-3.3.1/resources/css/ext-all.css')}" />
  <script type="text/javascript" src="${createLinkTo(dir:'js',file:'be/cytomine/layout/layout.js')}" ></script>
  <script type="text/javascript" src="${createLinkTo(dir:'js',file:'be/cytomine/layout/fr.js')}" ></script>
  <script type="text/javascript" charset="utf-8">
    Ext.BLANK_IMAGE_URL = "${createLinkTo(dir:'js',file:'ext-3.3.1/resources/images/default/s.gif')}";
  </script>
  <link rel="stylesheet" href="${createLinkTo(dir:'css',file:'loading.css')}" />
  <link rel="stylesheet" href="${createLinkTo(dir:'css',file:'main.css')}" />
  <!--
<script type="text/javascript" src="javascript/ext/ext-base.js"></script>
    <script type="text/javascript" src="javascript/ext/ext-all.js"></script>
    <script type="text/javascript" src="javascript/ext/ext-ulg.js"></script>
    <script type="text/javascript" src="javascript/ext/ext-slidermenu.js"></script>
    <script type="text/javascript" src="javascript/ext/ext-linkbutton.js"></script>
    <script type="text/javascript" src="javascript/ext/ext-rowactions.js"></script>

    <script type="text/javascript" src="javascript/raphael/raphael.js"></script>

     <script type="text/javascript" src="javascript/ULg/annotations.js"></script>
    <script type="text/javascript" src="javascript/ULg/ontologies.js"></script>
    <script type="text/javascript" src="javascript/ULg/shapes.js"></script>
    <script type="text/javascript" src="javascript/ULg/viewer.js"></script>
    <script type="text/javascript" src="javascript/ULg/notifications.js"></script>

    -->
  <g:javascript library="application" />
  <g:layoutHead />
  <script type="text/javascript">
    Ext.onReady(function() {
      setTimeout(function(){
        Ext.get('loading').remove();
        Ext.get('loading-mask').fadeOut({remove:true});
      }, 250);
    });
  </script>
</head>
<body onload="${pageProperty(name:'body.onload')}">
<!--<g:layoutBody />-->
<div id="loading-mask"></div>
<div id="loading">
  <div class="loading-indicator">
    Chargement...
  </div>
</div>
<div id="dCenter" class="x-hide-display" style="width:100%;height:100%;margin-left:auto;margin-right:auto">

</div>
<div id="navwin" class="x-toolbar" style="margin:auto;position: relative; border:none; background-image:none; padding:0;"></div>

</body>
</html>

