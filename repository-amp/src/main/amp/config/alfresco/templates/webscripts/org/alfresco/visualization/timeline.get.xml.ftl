<?xml version='1.0' encoding='UTF-8' ?>
<data>
    <#list timelineEntries as entry>
        <event start="${xmldate(entry.date)}" title="${msg("content.updated", entry.user, entry.name)?html}">
            ${msg("content.updated", entry.user, entry.name)?html}
        </event>
    </#list>
</data>