package org.kurator.akka.actors;

import java.util.Map;

import org.kurator.akka.AkkaActor;
import org.python.core.PyObject;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;

public abstract class PythonActorBase extends AkkaActor {

    public Class<? extends Object> inputType = Object.class;
    public Class<? extends Object> outputType = Object.class;
    public boolean broadcastNulls = false;
    public boolean outputTypeIsInputType = false;
    
    protected static final String inputName = "_KURATOR_INPUT_";
    protected static final String outputName = "_KURATOR_OUTPUT_";

    protected PythonInterpreter interpreter;
    protected PyObject none;
    
    @Override
    protected void onInitialize() {
        
        // create a python interpreter
        PySystemState.initialize(System.getProperties( ), null, new String[] {""});
        interpreter = new PythonInterpreter();
        
        // set output streams of interpreter to that set for this actor
        interpreter.setOut(super.outStream);
        interpreter.setErr(super.errStream);
        
        interpreter.exec("import sys");
        
        // expand wrapper function template using custom function name
        prependSysPath("src/main/resources/python");
        prependSysPath("kurator-jython");
        prependSysPath("../kurator-jython");
        
        // read the script into the interpreter
        if (script != null) interpreter.execfile(script);
        if (code != null) interpreter.exec(code);
        
        // cache a python None object
        none = interpreter.eval("None");
    }

    private void prependSysPath(String path) {
        interpreter.eval(String.format("sys.path.insert(0, '%s')%s", path, EOL));
    }
    
    @Override
    protected void onStart() throws Exception {

        if (settings != null) {
            for(Map.Entry<String, Object> setting : settings.entrySet()) {
                String name = setting.getKey();
                Object value = setting.getValue();
                interpreter.set(name, value);
            }
        }        
    }
    
    protected void handleOutput(Object output) {
        if (output != null || broadcastNulls) {
            broadcast(output);
        }
    }
    
    @Override
    protected void onEnd() {
        
        // call script end function if defined
        if (onEnd != null) {
            interpreter.eval(onEnd + "()");
        }
        
        // shut down the interpreter
        interpreter.cleanup();
    }    
}
