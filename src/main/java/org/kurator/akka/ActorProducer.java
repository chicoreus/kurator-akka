package org.kurator.akka;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kurator.akka.actors.Transformer;

import akka.actor.IndirectActorProducer;

public class ActorProducer implements IndirectActorProducer {

    private Class<? extends Transformer> actorClass;
    private Map<String, Object> defaults;
    private Map<String, Object> parameters;
    private List<ActorBuilder> listenerBuilders;
    private WorkflowBuilder workflowBuilder;
    private Transformer actor;
    private PrintStream outStream;
    private PrintStream errStream;

    public ActorProducer(Class<? extends Transformer> actorClass, Map<String, Object> defaults, Map<String, Object> parameters, List<ActorBuilder> listenerConfigs, 
            PrintStream outStream, PrintStream errStream, WorkflowBuilder workflowBuilder) {
        this.actorClass = actorClass;
        this.defaults = defaults;
        this.parameters = parameters;
        this.listenerBuilders = listenerConfigs;
        this.workflowBuilder = workflowBuilder;
        this.outStream = outStream;
        this.errStream = errStream;
    }

    @Override
    public Class<? extends Transformer> actorClass() {
        return actorClass;
    }

    @Override
    public Transformer produce() {
        
        try {
            actor = actorClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        actor.setListenerConfigs(listenerBuilders);
        actor.setWorkflowRunner(workflowBuilder);
        actor.outputStream(outStream);
        actor.errorStream(errStream);
        
        Map<String,Object> parameterSettings = new HashMap<String,Object>();
        parameterSettings.putAll(defaults);
        parameterSettings.putAll(parameters);

        for (Map.Entry<String,Object> setting : parameterSettings.entrySet()) {
            setParameter(setting.getKey(), setting.getValue());
        }
            
        return actor;
    }
    
    
    private void setParameter(String name, Object value) {        
        try {
            Field field = actor.getClass().getField(name);
            field.setAccessible(true);
            field.set(actor, value);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        } 
    }
}
