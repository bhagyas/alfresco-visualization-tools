/**
 * 
 */
package org.alfresco.repo.visualization.web.scripts;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.audit.AuditQueryParameters;
import org.alfresco.service.cmr.audit.AuditService;
import org.alfresco.service.cmr.audit.AuditService.AuditQueryCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * @author <a href="mailto:axel.faust@prodyna.com">Axel Faust</a>, <a href="http://www.prodyna.com">PRODYNA AG</a>
 */
public class ContentUpdateTimelineGet extends DeclarativeWebScript
{

    private static final Logger LOGGER = LoggerFactory.getLogger(ContentUpdateTimelineGet.class);

    private static final String VISUALIZATION_AUDIT_APP_NAME = "Alfresco Visualization Data";
    private static final String VISUALIZATION_AUDIT_APP_ROOT = "/alfresco-visualization";

    private static final String VISUALIZATION_AUDIT_SITE_PATH = VISUALIZATION_AUDIT_APP_ROOT + "/contentUpdate/siteShortName";
    private static final String VISUALIZATION_AUDIT_NODE_REF_PATH = VISUALIZATION_AUDIT_APP_ROOT + "/contentUpdate/nodeRef";
    private static final String VISUALIZATION_AUDIT_NAME_PATH = VISUALIZATION_AUDIT_APP_ROOT + "/contentUpdate/name";

    private static final String SITE_FILTER_PARAM = "siteShortName";

    private AuditService auditService;
    private NodeService nodeService;

    /**
     * {@inheritDoc}
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        // TODO: additional filters / limits

        final AuditQueryParameters params = new AuditQueryParameters();
        params.setApplicationName(VISUALIZATION_AUDIT_APP_NAME);
        params.setForward(true);

        final String siteFilter = req.getParameter(SITE_FILTER_PARAM);
        if (siteFilter != null)
        {
            params.addSearchKey(VISUALIZATION_AUDIT_SITE_PATH, siteFilter);
        }

        final Aggregator aggregator = new Aggregator();
        this.auditService.auditQuery(aggregator, params, Integer.MAX_VALUE);

        final Collection<UpdateEvent> events = aggregator.getEvents();

        // transform into model for script / template
        final Map<String, Object> model = new HashMap<String, Object>();

        final List<UpdateEvent> visibleEvents = new ArrayList<UpdateEvent>();
        for (final UpdateEvent event : events)
        {
            if (this.nodeService.exists(event.getNode()))
            {
                visibleEvents.add(event);
            }
        }
        model.put("timelineEntries", visibleEvents);

        return model;
    }

    /**
     * @param auditService
     *            the auditService to set
     */
    public void setAuditService(AuditService auditService)
    {
        this.auditService = auditService;
    }

    /**
     * @param nodeService
     *            the nodeService to set
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public static class UpdateEvent
    {

        private final NodeRef node;
        private final String name;
        private final Date date;
        private final String user;

        public UpdateEvent(final NodeRef node, final String user, final String name, final Date date)
        {
            this.node = node;
            this.name = name;
            this.date = date;
            this.user = user;
        }

        /**
         * @return the node
         */
        public NodeRef getNode()
        {
            return node;
        }

        /**
         * @return the name
         */
        public String getName()
        {
            return name;
        }

        /**
         * @return the date
         */
        public Date getDate()
        {
            return date;
        }

        /**
         * @return the user
         */
        public String getUser()
        {
            return user;
        }

    }

    private static class Aggregator implements AuditQueryCallback
    {

        private final Collection<UpdateEvent> events = new HashSet<UpdateEvent>();

        @Override
        public boolean valuesRequired()
        {
            return true;
        }

        @Override
        public boolean handleAuditEntry(Long entryId, String applicationName, String user, long time, Map<String, Serializable> values)
        {
            final NodeRef nodeRef = (NodeRef) values.get(VISUALIZATION_AUDIT_NODE_REF_PATH);
            final String name = (String) values.get(VISUALIZATION_AUDIT_NAME_PATH);

            this.events.add(new UpdateEvent(nodeRef, user, name, new Date(time)));

            return true;
        }

        @Override
        public boolean handleAuditEntryError(Long entryId, String errorMsg, Throwable error)
        {
            LOGGER.warn("Error loading audit entry", error);
            return true;
        }

        /**
         * @return the events
         */
        public Collection<UpdateEvent> getEvents()
        {
            return events;
        }

    }
}
