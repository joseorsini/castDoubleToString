package com.dotmarketing.osgi.override;

import org.osgi.framework.BundleContext;
import com.dotmarketing.osgi.GenericBundleActivator;
import com.dotmarketing.portlets.folders.model.Folder;
import com.dotcms.keyvalue.model.KeyValue;
import com.dotcms.repackage.org.apache.logging.log4j.LogManager;
import com.dotcms.repackage.org.apache.logging.log4j.core.LoggerContext;
import com.dotmarketing.loggers.Log4jUtil;
import com.dotmarketing.util.Logger;

/**
 * @author joseorsini
 * @version 0.1
 */
public class Activator extends GenericBundleActivator {

    private LoggerContext pluginLoggerContext;
    
    @SuppressWarnings ("unchecked")
    public void start ( BundleContext context ) throws Exception {
        
        //Initializing log4j...
        LoggerContext dotcmsLoggerContext = Log4jUtil.getLoggerContext();
        //Initialing the log4j context of this plugin based on the dotCMS logger context
        pluginLoggerContext = (LoggerContext) LogManager.getContext(this.getClass().getClassLoader(),
                false,
                dotcmsLoggerContext,
                dotcmsLoggerContext.getConfigLocation());

        //Initializing services...
        initializeServices( context );

        //Expose bundle elements
        publishBundleServices( context );
        
        KeyValueParser kvParser =new KeyValueParser();
        
        kvParser.keyValueFieldsHandler();

    }

    public void stop ( BundleContext context ) throws Exception {

        //Unpublish bundle services
        unpublishBundleServices();
        
        //Shutting down log4j in order to avoid memory leaks
        Log4jUtil.shutdown(pluginLoggerContext);
    }

}