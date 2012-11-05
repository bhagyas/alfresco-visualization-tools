package org.alfresco.repo.visualization.policy;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.audit.AuditComponent;
import org.alfresco.repo.node.NodeServicePolicies.BeforeUpdateNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnUpdateNodePolicy;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.EqualsHelper;
import org.alfresco.util.PropertyCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author Axel Faust, <a href="http://www.prodyna.com">PRODYNA AG</a>
 */
public class ContentEventAuditor extends TransactionListenerAdapter implements InitializingBean, OnUpdateNodePolicy, BeforeUpdateNodePolicy
{
    private static Logger LOGGER = LoggerFactory.getLogger(ContentEventAuditor.class);

    private static final String RESOURCE_PREFIX = ContentEventAuditor.class.getCanonicalName();
    private static final String RESOURCE_NODES_PREFIX = ContentEventAuditor.class.getCanonicalName() + "-nodes-";
    private static final String RESOURCE_BEFORE_PREFIX = RESOURCE_PREFIX + "-before-";
    private static final String RESOURCE_AFTER_PREFIX = RESOURCE_PREFIX + "-after-";
    private static final String RESOURCE_CONTEXT_PREFIX = RESOURCE_PREFIX + "-context-";

    private static final String ROOT_PATH = "/alfresco-visualization/contentUpdate";

    private PolicyComponent policyComponent;

    private AuditComponent auditComponent;

    private ContentService contentService;
    private NodeService nodeService;
    private SiteService siteService;

    @Override
    public void onUpdateNode(NodeRef nodeRef)
    {
        final String nodeKey = RESOURCE_AFTER_PREFIX + nodeRef;
        final Map<String, Serializable> nodeData = TransactionalResourceHelper.getMap(nodeKey);

        // update after view
        nodeData.clear();
        putCurrentData(nodeRef, nodeData);
    }

    @Override
    public void beforeUpdateNode(NodeRef nodeRef)
    {
        final String nodeKey = RESOURCE_BEFORE_PREFIX + nodeRef;
        Map<String, Serializable> nodeData = TransactionalResourceHelper.getMap(nodeKey);
        if (nodeData.isEmpty())
        {
            final Set<NodeRef> nodes = TransactionalResourceHelper.getSet(RESOURCE_NODES_PREFIX);
            if (nodes.isEmpty())
            {
                AlfrescoTransactionSupport.bindListener(this);
            }
            nodes.add(nodeRef);

            // initialize before-view
            putCurrentData(nodeRef, nodeData);

            // add context
            final Map<String, Serializable> context = TransactionalResourceHelper.getMap(RESOURCE_CONTEXT_PREFIX + nodeRef);
            final SiteInfo site = this.siteService.getSite(nodeRef);
            if (site != null)
            {
                context.put("siteShortName", site.getShortName());
            }
            context.put("nodeRef", nodeRef);
            context.put("name", this.nodeService.getProperty(nodeRef, ContentModel.PROP_NAME));

            // TODO additional context?
        }
    }

    private void putCurrentData(final NodeRef nodeRef, final Map<String, Serializable> nodeData)
    {
        nodeData.put("aspects", (Serializable) this.nodeService.getAspects(nodeRef));
        nodeData.put("properties", (Serializable) this.nodeService.getProperties(nodeRef));

        final ContentReader contentReader = this.contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
        if (contentReader != null)
        {
            final Map<String, Serializable> contentData = new HashMap<String, Serializable>();
            contentData.put("mimetype", contentReader.getMimetype());
            contentData.put("size", contentReader.getSize());
            contentData.put("encoding", contentReader.getEncoding());
            contentData.put("url", contentReader.getContentUrl());
            nodeData.put("content", (Serializable) contentData);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "policyComponent", this.policyComponent);
        this.policyComponent.bindClassBehaviour(BeforeUpdateNodePolicy.QNAME, ContentModel.TYPE_CONTENT, new JavaBehaviour(this,
                "beforeUpdateNode"));
        this.policyComponent.bindClassBehaviour(OnUpdateNodePolicy.QNAME, ContentModel.TYPE_CONTENT,
                new JavaBehaviour(this, "onUpdateNode"));
    }

    @Override
    public void afterCommit()
    {
        final Set<NodeRef> nodes = TransactionalResourceHelper.getSet(RESOURCE_NODES_PREFIX);
        for (final NodeRef node : nodes)
        {
            final Map<String, Serializable> before = TransactionalResourceHelper.getMap(RESOURCE_BEFORE_PREFIX + node);
            final Map<String, Serializable> after = TransactionalResourceHelper.getMap(RESOURCE_AFTER_PREFIX + node);

            final Map<String, Serializable> auditMap = new HashMap<String, Serializable>();

            // diff
            diffAspects(before, after, auditMap);
            diffProperties(before, after, auditMap);
            diffContent(before, after, auditMap);

            final Map<String, Serializable> context = TransactionalResourceHelper.getMap(RESOURCE_CONTEXT_PREFIX + node);
            auditMap.putAll(context);

            this.auditComponent.recordAuditValues(ROOT_PATH, auditMap);
        }
    }

    private void diffAspects(final Map<String, Serializable> before, final Map<String, Serializable> after,
            final Map<String, Serializable> auditMap)
    {
        @SuppressWarnings("unchecked")
        final Set<QName> aspectsBefore = (Set<QName>) before.get("aspects");
        @SuppressWarnings("unchecked")
        final Set<QName> aspectsAfter = (Set<QName>) after.get("aspects");

        final Set<QName> aspectsRemoved = new HashSet<QName>(aspectsBefore);
        aspectsRemoved.removeAll(aspectsAfter);

        final Set<QName> aspectsAdded = new HashSet<QName>(aspectsAfter);
        aspectsAdded.removeAll(aspectsBefore);

        auditMap.put("aspects/removed", (Serializable) aspectsRemoved);
        auditMap.put("aspects/added", (Serializable) aspectsAdded);
    }

    private void diffProperties(final Map<String, Serializable> before, final Map<String, Serializable> after,
            final Map<String, Serializable> auditMap)
    {
        @SuppressWarnings("unchecked")
        final Map<QName, Serializable> propsBefore = (Map<QName, Serializable>) before.get("properties");
        @SuppressWarnings("unchecked")
        final Map<QName, Serializable> propsAfter = (Map<QName, Serializable>) after.get("properties");

        final Set<QName> propsRemoved = new HashSet<QName>(propsBefore.keySet());
        propsRemoved.removeAll(propsAfter.keySet());

        final Map<QName, Serializable> propsAdded = new HashMap<QName, Serializable>(propsAfter);
        for (final QName prop : propsBefore.keySet())
        {
            propsAdded.remove(prop);
        }

        for (final QName prop : propsRemoved)
        {
            propsBefore.remove(prop);
        }

        final Set<QName> propsToIterate = new HashSet<QName>(propsBefore.keySet());
        propsToIterate.addAll(propsAfter.keySet());
        for (final QName prop : propsToIterate)
        {
            final Serializable beforeValue = propsBefore.get(prop);
            final Serializable afterValue = propsAfter.get(prop);

            if (EqualsHelper.nullSafeEquals(beforeValue, afterValue))
            {
                // remove as no change
                propsBefore.remove(prop);
                propsAfter.remove(prop);
            }
        }

        auditMap.put("properties/removed", (Serializable) propsRemoved);
        auditMap.put("properties/change/from", (Serializable) propsBefore);
        auditMap.put("properties/change/to", (Serializable) propsAfter);
    }

    private void diffContent(final Map<String, Serializable> before, final Map<String, Serializable> after,
            final Map<String, Serializable> auditMap)
    {
        @SuppressWarnings("unchecked")
        final Map<String, Serializable> contentDataBefore = (Map<String, Serializable>) before.get("content");
        @SuppressWarnings("unchecked")
        final Map<String, Serializable> contentDataAfter = (Map<String, Serializable>) after.get("content");

        // add final state as overall data
        auditMap.put("content/size", contentDataAfter != null ? contentDataAfter.get("size") : 0);
        if (contentDataAfter != null)
        {
            auditMap.put("content/mimetype", contentDataAfter.get("mimetype"));
        }

        boolean changed = true;
        if (contentDataBefore != null && contentDataAfter != null)
        {
            final Serializable urlBefore = contentDataBefore.get("url");
            final Serializable urlAfter = contentDataAfter.get("url");
            changed = !EqualsHelper.nullSafeEquals(urlBefore, urlAfter);
        }
        else if (contentDataBefore != null || contentDataAfter != null)
        {
            changed = true;
        }
        else
        {
            changed = false;
        }

        if (changed)
        {
            auditMap.put("content/change/from", (Serializable) contentDataBefore);
            auditMap.put("content/change/to", (Serializable) contentDataAfter);
        }
    }

    /**
     * @param policyComponent
     *            the policyComponent to set
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    /**
     * @param auditComponent
     *            the auditComponent to set
     */
    public void setAuditComponent(AuditComponent auditComponent)
    {
        this.auditComponent = auditComponent;
    }

    /**
     * @param contentService
     *            the contentService to set
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    /**
     * @param nodeService
     *            the nodeService to set
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param siteService
     *            the siteService to set
     */
    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }

}
