package com.marklogic.mapreduce;

import java.io.IOException;
import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;

import com.marklogic.xcc.AdhocQuery;
import com.marklogic.xcc.Session;
import com.marklogic.xcc.exceptions.RequestException;
import com.marklogic.xcc.types.ValueType;

/**
 * MarkLogicRecordWriter that sets a property for a document.
 * 
 * @author jchen
 */
public class PropertyWriter 
extends MarkLogicRecordWriter<DocumentURI, MarkLogicNode> 
implements MarkLogicConstants {

    public static final Log LOG =
        LogFactory.getLog(PropertyWriter.class);
    public static final String DOCURI_VARIABLE_NAME = "uri";
    public static final String NODE_VARIABLE_NAME = "node";
    
    private String query;
    
    public PropertyWriter(URI serverUri, Configuration conf) {
        super(serverUri, conf);
        String propOpType = conf.get(PROPERTY_OPERATION_TYPE, 
                DEFAULT_PROPERTY_OPERATION_TYPE);
        PropertyOpType opType = PropertyOpType.valueOf(propOpType);
        query = opType.getQuery(conf);
        if (LOG.isDebugEnabled()) {
            LOG.debug(query);
        }
    }

    @Override
    public void write(DocumentURI uri, MarkLogicNode record)
            throws IOException, InterruptedException {
        // initialize recordString
        String recordString = record == null ? "()" : record.toString();

        // execute query
        Session session = getSession();
        try {
            AdhocQuery request = session.newAdhocQuery(query);
            request.setNewStringVariable(DOCURI_VARIABLE_NAME, 
                    uri.getUnparsedUri());
            request.setNewVariable(NODE_VARIABLE_NAME, ValueType.ELEMENT, 
                    recordString);
            session.submitRequest(request);
            commitIfNecessary();
        } catch (RequestException e) {    
            LOG.error(e);
            LOG.error(query);
            throw new IOException(e);
        }
    }

}
