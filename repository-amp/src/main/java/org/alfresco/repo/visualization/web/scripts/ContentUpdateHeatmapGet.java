/**
 * 
 */
package org.alfresco.repo.visualization.web.scripts;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
public class ContentUpdateHeatmapGet extends DeclarativeWebScript
{

    private static final Logger LOGGER = LoggerFactory.getLogger(ContentUpdateHeatmapGet.class);

    private static final String VISUALIZATION_AUDIT_APP_NAME = "Alfresco Visualization Data";
    private static final String VISUALIZATION_AUDIT_APP_ROOT = "/alfresco-visualization";

    private static final String VISUALIZATION_AUDIT_SITE_PATH = VISUALIZATION_AUDIT_APP_ROOT + "/contentUpdate/siteShortName";
    private static final String VISUALIZATION_AUDIT_NODE_REF_PATH = VISUALIZATION_AUDIT_APP_ROOT + "/contentUpdate/nodeRef";

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

        final Map<NodeRef, UpdateCounter> aggregatedCounts = aggregator.getAggregatedCounts();

        // transform into model for script / template
        final Map<String, Object> model = new HashMap<String, Object>();

        final List<Map<String, Serializable>> nodeEntries = new ArrayList<Map<String, Serializable>>();
        for (final Entry<NodeRef, UpdateCounter> countEntry : aggregatedCounts.entrySet())
        {
            final NodeRef nodeRef = countEntry.getKey();

            if (this.nodeService.exists(nodeRef))
            {
                final Map<String, Serializable> nodeEntry = new HashMap<String, Serializable>();
                nodeEntry.put("node", nodeRef);
                nodeEntry.put("count", countEntry.getValue().getCount());

                nodeEntries.add(nodeEntry);
            }
        }
        model.put("heatmapEntries", nodeEntries);

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

    public static class UpdateCounter
    {

        private final NodeRef node;
        private int count;

        public UpdateCounter(final NodeRef node)
        {
            this.node = node;
        }

        /**
         * @return the node
         */
        public NodeRef getNode()
        {
            return node;
        }

        /**
         * @return the count
         */
        public int getCount()
        {
            return count;
        }

        private void increment()
        {
            this.count++;
        }
    }

    private static class Aggregator implements AuditQueryCallback
    {

        private final Map<NodeRef, UpdateCounter> aggregatedCounts = new HashMap<NodeRef, UpdateCounter>();

        @Override
        public boolean valuesRequired()
        {
            return true;
        }

        @Override
        public boolean handleAuditEntry(Long entryId, String applicationName, String user, long time, Map<String, Serializable> values)
        {
            final NodeRef nodeRef = (NodeRef) values.get(VISUALIZATION_AUDIT_NODE_REF_PATH);

            if (!this.aggregatedCounts.containsKey(nodeRef))
            {
                this.aggregatedCounts.put(nodeRef, new UpdateCounter(nodeRef));
            }

            // every entry is one update
            this.aggregatedCounts.get(nodeRef).increment();

            return true;
        }

        @Override
        public boolean handleAuditEntryError(Long entryId, String errorMsg, Throwable error)
        {
            LOGGER.warn("Error loading audit entry", error);
            return true;
        }

        /**
         * @return the aggregatedCounts
         */
        public Map<NodeRef, UpdateCounter> getAggregatedCounts()
        {
            return this.aggregatedCounts;
        }

    }
}
