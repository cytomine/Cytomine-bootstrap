<head>
  <meta name='layout' content='ext' />
  <title>Cytomine</title>
</head>
<body>
<div id="header">
  <div style="float:left; margin-bottom: 10px;margin-left: 10px;color: #CCC">
    <span style="font-weight: bold; font-size : 22px;">Cytomine</span>
  </div>
  <div style="float:right; margin-bottom: 10px;margin-right: 10px;color: #CCC">
    <a href="http://www.cytomine.be/" style="padding:5px">Cytomine Project</a> |
    <a href="http://www.giga.ulg.ac.be/" style="padding:5px">GIGA ULg</a> |
    <sec:ifLoggedIn><sec:username /> (<g:link controller="logout">Logout</g:link>)</sec:ifLoggedIn>
  </div>
</div>
<div id="content"></div>
</body>
