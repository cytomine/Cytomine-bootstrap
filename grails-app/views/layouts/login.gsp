<html>
      <head>
       <title><g:layoutTitle default="Grails" /></title>
		<link rel="stylesheet" href="${resource(dir:'js',file:'ext-3.3.1/resources/css/ext-all.css')}" />
		<link rel="stylesheet" href="${resource(dir:'js',file:'ext-3.3.1/resources/css/xtheme-gray.css')}" />
        <link rel="stylesheet" href="${resource(dir:'css',file:'icons.css')}" />
        <script type="text/javascript" src="${resource(dir:'js',file:'ext-3.3.1/adapter/ext/ext-base.js')}" ></script>
        <script type="text/javascript" src="${resource(dir:'js',file:'ext-3.3.1/ext-all-debug.js')}"></script>
        <script type="text/javascript" src="${resource(dir:'js',file:'ext-3.3.1/examples/shared/extjs/App.js')}" ></script>
		<!--<script type="text/javascript" charset="utf-8">
			Ext.BLANK_IMAGE_URL = "${resource(dir:'js',file:'ext-3.3.1/resources/images/default/s.gif')}";
		</script>-->
        <script type="text/javascript" src="${resource(dir:'js',file:'be/cytomine/login/auth.js')}" ></script>
        <!-- DEV -->
        <script type="text/javascript" src="${resource(dir:'js',file:'dev.js')}" ></script>
        <!-- /DEV -->
        <g:layoutHead />
      </head>
      <body onload="${pageProperty(name:'body.onload')}">
            <div id="container">
			<div id="header">
			</div>
			<div id="content">
				<div id="include">
					<g:layoutBody />
				</div>
			</div>
		</div>
      </body>
</html>