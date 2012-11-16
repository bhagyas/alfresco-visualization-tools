<import resource="classpath:alfresco/site-webscripts/org/devcon/components/avt-data/birdview-content.lib.js">

/**
 * Loads Content Directory information from the server
 * @author bhagyas
 * @method main
 */
function main(){
	var site = args["site"], siteFolderRoot = {};
	
	//get root content node
	var connector = remote.connect("alfresco"),
    result = connector.get("/slingshot/doclib/container/" + stringUtils.urlEncodeComponent(site) + "/documentLibrary");
	
    var data = eval('(' + result + ')'),
    pageSize = args.maxResults ? parseInt(args.maxResults) : 50,
    container = data.container;
    var rootDocLibNodeRef = container ? container.nodeRef : null;

    //get root listing
    siteFolderRoot.nodeRef = rootDocLibNodeRef;
    siteFolderRoot.nodeId = getNodeId(rootDocLibNodeRef);
    siteFolderRoot.name = site; //siteName
    siteFolderRoot.isFolder = true;
    logger.log("Searching under root site Node for content : " + rootDocLibNodeRef)
    siteFolderRoot.children = cmisSearchFolders(siteFolderRoot.nodeId,pageSize);
	
    
    model.siteShortName = site;
    model.siteFolderRoot = siteFolderRoot;
	
}

// Start webscript
main();