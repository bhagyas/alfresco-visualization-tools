<#macro printContentJson searchFolderRoot isRoot>
	<#if searchFolderRoot.isFolder == true>
		{
			"name":"${searchFolderRoot.name!msg("label.articleNameNotFound")}",
			"href": "${url.context}/page/site/${siteShortName!""}/folder-details?nodeRef=${searchFolderRoot.nodeRef!"NODEREF"}",
			"size": 100
		<#if searchFolderRoot.children??>
			,"children" : [
				<#list searchFolderRoot.children?sort_by("name") as child>
		   			<@printContentJson searchFolderRoot=child isRoot=false /><#if child_has_next>,</#if>
		   		</#list>
	   		]}
	   	<#else>
   		}
		</#if> 
   <#else>
   		{
   			"name":"${searchFolderRoot.name!msg("label.articleNameNotFound")}",
   			"href":"${url.context}/page/site/${siteShortName!""}/document-details?nodeRef=${searchFolderRoot.nodeRef!"NODEREF"}",
   			"size": 200
   		}
   </#if>
</#macro>
<@printContentJson searchFolderRoot=siteFolderRoot isRoot=true/>