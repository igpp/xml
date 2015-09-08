function sidebar() {
   var html = [
'     <div class="span3 sidebar">',
'       <div class="well" style="padding: 8px 0px; position: fixed;"">',
'         <ul class="nav nav-list">',
'           <li><a href="prettyxml/index.html">prettyxml</a></li>',
'           <li><a href="toxhtml/index.html">toxhtml</a></li>',
'           <li><a href="topdf/index.html">topdf</a></li>',
'           <li><a href="transform/index.html">transform</a></li>',
'           <li><a href="xmlgrep/index.html">xmlgrep</a></li>',
'         </ul>',
'       </div><!-- well -->',
'      &nbsp; <!-- Content to keep span filled -->',
'   </div><!-- sidebar -->'
   ];
   for(x in html) { document.write(html[x] + "\n"); }
}