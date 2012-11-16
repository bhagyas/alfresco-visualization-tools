/**
* DevCon root namespace.
* 
* @namespace DevCon
*/
if (typeof DevCon == "undefined" || !DevCon)
{
   var DevCon = {};
}

/**
* DevCon dashlet namespace.
* 
* @namespace DevCon.dashlet
*/
if (typeof DevCon.dashlet == "undefined" || !DevCon.dashlet)
{
   DevCon.dashlet = {};
}

/**
 * Sample Hello World dashboard component.
 * 
 * @namespace DevCon.dashlet
 * @class DevCon.dashlet.SiteView
 * @author 
 */
(function()
{
   /**
    * YUI Library aliases
    */
   var Dom = YAHOO.util.Dom,
      Event = YAHOO.util.Event;

   /**
    * Alfresco Slingshot aliases
    */
   var $html = Alfresco.util.encodeHTML,
      $combine = Alfresco.util.combinePaths;


   /**
    * Dashboard SiteView constructor.
    * 
    * @param {String} htmlId The HTML id of the parent element
    * @return {DevCon.dashlet.SiteView} The new component instance
    * @constructor
    */
   DevCon.dashlet.SiteView = function SiteView_constructor(htmlId)
   {
      return DevCon.dashlet.SiteView.superclass.constructor.call(this, "DevCon.dashlet.SiteView", htmlId);
   };

   /**
    * Extend from Alfresco.component.Base and add class implementation
    */
   YAHOO.extend(DevCon.dashlet.SiteView, Alfresco.component.Base,
   {
      /**
       * Object container for initialization options
       *
       * @property options
       * @type object
       */
      options:
      {
      },
      
      tl : null,
      resizeTimerID : null,
      /**
       * Fired by YUI when parent element is available for scripting
       * 
       * @method onReady
       */
      onReady: function SiteView_onReady()
      {
    	  //on ready code goes here
      },
      
      onResize: function SiteView_onResize()
      {
    	  //on resize code goes here
      }
      
   });
})();
