function sidebar() {
   var html = [
'     <div class="span3 sidebar">',
'       <div class="well" style="padding: 8px 0px; position: fixed;"">',
'         <ul class="nav nav-list">',
'           <li><a href="#Simple">Simple Example</a></li>',
'         </ul>',
'       </div><!-- well -->',
'      &nbsp; <!-- Content to keep span filled -->',
'   </div><!-- sidebar -->'
   ];
   for(x in html) { document.write(html[x] + "\n"); }
}