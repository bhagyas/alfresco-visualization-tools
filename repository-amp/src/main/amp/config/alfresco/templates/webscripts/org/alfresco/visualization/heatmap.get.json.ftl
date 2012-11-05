{
    "data" : [
    <#escape x as jsonUtils.encodeJSONString(x)>
        <#list heatmapEntries as entry>
        {
            "nodeRef":      "${entry.node.nodeRef}",
            "name":         "${entry.node.name}",
            "updateCounts": ${entry.count}
        }<#if entry_has_next>,</#if>
        </#list>
    </#escape>]
}