/**
 * Helper method that returns elements (org.apache.abdera.model.Element)
 * in the list (java.util.List) having an element tag name matching elTagName
 *
 * @method getElementByTagName
 * @param list {string} (mandatory) The list to look for elements in
 * @param elTagName {string} (optional) The tagName to look for
 * @return {array|object} An element in the list matching elTagName
 * @private
 */
function getElementByTagName(list, elTagName)
{
   var el;
   for (var i = 0, l = list.size(); i < l; i++)
   {
      el = list.get(i);
      if (el.QName.localPart == elTagName)
      {
         return el;
      }
   }
   return null;
}

/**
 * Takes the articles and groups them by status.
 *
 * @method groupArticlesByStatus
 * @return {object} An object using statuses as keys and arrays of articles as values
 */
function groupArticlesByStatus(files)
{
   var fileGroups = {},
       author,
       file;
   for (var i = 0, l = files.length; i < l; i++)
   {
      file = files[i];
      
      //group by owner
      author = file.properties ? file.properties["cm_createdBy"] : null;
      if (author)
      {
         if (fileGroups[author] === undefined)
         {
            fileGroups[author] = [];
         }
         fileGroups[author].push(file);
      }
   }
   return fileGroups;
}



/**
 * CMIS Version
 *
 * Performs an exhaustive CMIS searh
 * 
 * @author bhagyas
 *
 * @method cmisSeachFolders
 * @param docLibNodeId {string} The node Id to search in
 * @param pageSize {int} The maximum number of articles to return
 */
function cmisSearchFolders(docLibNodeId, pageSize)
{
   // Get the nodeRef for the sites documentLibrary
   var connector = remote.connect("alfresco");
   var result;

   var url = "/cmis/i/" + docLibNodeId + "/children";
   
   logger.log("Calling : " + url);
   result = connector.get(url);
   var feed = atom.toFeed(result.response),
       entries = feed.entries,
       entriesLength = entries.size(),
       entryEl = null,
       objEl,
       propertiesEl,
       propertiesList,
       propertyEl,
       folders = [];

   // Convert to object format similar to the json response so the template can be reused
   for (var ei = 0; ei < entriesLength; ei++)
   {
      entryEl = entries.get(ei);
      var child = {
         properties: {}
      };
      objEl = getElementByTagName(entryEl.getExtensions(), "object");
      propertiesEl = getElementByTagName(objEl.getElements(), "properties");
      propertiesList = propertiesEl.getElements();
      logger.log("************************************");
      for (var pi = 0, pl = propertiesList.size(); pi < pl; pi++)
      {
    	 
         try{
        	 propertyEl = propertiesList.get(pi);
        	 
        	 //logging features
             logger.log("propertyEl: " + propertyEl);
             logger.log("propertyEl.getAttributeValue('propertyDefinitionId'):" + propertyEl.getAttributeValue("propertyDefinitionId"))
             logger.log("propertyEl.firstChild.text:" + propertyEl.firstChild.text);
             
             var propertyDefinitionId = propertyEl.getAttributeValue("propertyDefinitionId");
             logger.log("propertyDefinitionId:" + propertyDefinitionId);
             
             
             if (propertyDefinitionId.equals("cmis:objectId"))
             {
            	child.nodeRef = propertyEl.firstChild.text;
            	child.nodeId = getNodeId(child.nodeRef);
             }
             else if (propertyDefinitionId.equals("cmis:name"))
             {
            	child.name = propertyEl.firstChild.text;
             } 
             else if (propertyDefinitionId.equals("cmis:createdBy"))
             {
            	child.properties["cm_createdBy"] = propertyEl.firstChild.text;
             }
             
             else if (propertyDefinitionId.equals("cmis:objectTypeId"))
             {
            	child.objectTypeId = propertyEl.firstChild.text;
             }
             
             
         }catch(e){
        	 logger.log(e);
         }
      }
      logger.log("pushing child :" + child);
      logger.log("child.nodeRef:"+ child.nodeRef);
      logger.log("child.name:" + child.name);
      logger.log("child.objectTypeId:" + child.objectTypeId);
      logger.log("child.properties['cm_createdBy']:" + child.properties["cm_createdBy"]);
      logger.log("************************************");
      
      //determine if its a folder
      
      if(child.objectTypeId.equals("cmis:folder")){
    	  //deep dive
    	  child.isFolder = true;
    	  logger.log("Getting children for NodeID : "  + child.nodeId);
    	  child.children = cmisSearchFolders(child.nodeId,pageSize);
      }else{
    	  child.isFolder = false;
    	  child.children = null;
      }
      
      folders.push(child);
      
   }
   return folders;
}

/**
 * Returns the nodeId from a Node Ref
 */
function getNodeId(nodeRef){
	//sample noderef= workspace://SpacesStore/995b91c4-cc5b-4023-ac3d-b3b9dbcfe219
	logger.log("Looking up NodeId for NodeRef : " + nodeRef);
	var stringArray = nodeRef.split("/");
	
	logger.log("StringArray");
	logger.log(stringArray);
	var nodeId = stringArray[3];
	logger.log("Returning NodeID:" + nodeId);
	return nodeId;
}