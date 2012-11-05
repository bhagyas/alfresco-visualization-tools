<#-- Uncomment to use a client-side JavaScript component in conjunction with the dashlet -->
<script type="text/javascript">//<![CDATA[
   new DevCon.dashlet.Timeline("${args.htmlid}").setOptions(
   {
      "key": "value"
   }).setMessages(${messages});
   new Alfresco.widget.DashletResizer("${args.htmlid}", "${instance.object.id}");
//]]></script>
<#-- Resizing Disabled -->
<#--
<script type="text/javascript">//<![CDATA[
   new Alfresco.widget.DashletResizer("${args.htmlid}", "${instance.object.id}");
//]]></script>
-->
<div class="dashlet">
   <div class="title">${msg("header")}</div>
   <div id="alfresco-timeline" class="body scrollableList"<#-- if args.height??> style="height: ${args.height}px;"</#if -->>
     <div class="detail-list-item first-item last-item">
        <span><#if greeting=="hello">${msg("label.hello", user.firstName)}<#else>${msg("label.goodbye", user.firstName)}</#if></span>
     </div>
   </div>
</div>