package org.kurator.akka;

import java.io.InputStream;
import java.io.StringBufferInputStream;

import org.kurator.log.Logger;

@SuppressWarnings("deprecation")
public class YamlStringWorkflowRunner extends YamlWorkflowRunner {
    
    public YamlStringWorkflowRunner() throws Exception {
        super();
    }

    public YamlStringWorkflowRunner yamlString(String yamlString) throws Exception {
        super.createActorSystem();
        logger.info("Reading YAML definition from string");
        logger.value("Workflow definition", "yaml", yamlString);
        InputStream stream = new StringBufferInputStream(yamlString);
        yamlBeanReader.loadBeanDefinitions(stream, "-");
        super.loadWorkflowFromSpringContext();
        return this;
    }

    public YamlStringWorkflowRunner logger(Logger customLogger) {
        super.logger(customLogger);
        return this;
    }
}
