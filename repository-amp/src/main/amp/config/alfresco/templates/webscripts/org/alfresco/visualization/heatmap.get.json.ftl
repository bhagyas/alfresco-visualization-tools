{
    "data" : [
    <#escape x as jsonUtils.encodeJSONString(x)>
        <#list heatmapEntries as entry>
        {
            "nodeRef":      "${entry.node.nodeRef}",
            "name":         "${entry.node.name}",
            "updateCounts": ${entry.count},
            "siteShortName":"${entry.site!""},
            "mimetype":     "${entry.node.content.mimetype}"
        }<#if entry_has_next>,</#if>
        </#list>
    </#escape>]
}