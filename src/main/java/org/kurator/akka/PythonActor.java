package org.kurator.akka;

import java.util.Map;
import java.util.Properties;

import org.python.core.PyBoolean;
import org.python.core.PyInteger;
import org.python.core.PyObject;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;

public class PythonActor extends AkkaActor {
    
    protected static String DEFAULT_ON_INIT_FUNCTION    = "on_initialize";
    protected static String DEFAULT_ON_START_FUNCTION   = "on_start";
    protected static String DEFAULT_ON_DATA_FUNCTION    = "on_data";
    protected static String DEFAULT_ON_END_FUNCTION     = "on_end";
    
    public Class<? extends Object> inputType = Object.class;
    public Class<? extends Object> outputType = Object.class;
    public boolean broadcastNulls = false;
    public boolean outputTypeIsInputType = false;
    
    protected String functionQualifier = "";
    
    protected String onInit = null;
    protected String onStart = null;
    protected String onData = null;
    protected String onEnd = null;
    protected String script = null;
    
    protected PythonInterpreter interpreter;
    protected PyObject none;

    protected boolean onDataIsGenerator = false;
    
    protected String commonScriptHeader =
            "_KURATOR_INPUT_=None"                          + EOL +
            "_KURATOR_RESULT_=None"                         + EOL +
            "_KURATOR_OUTPUT_=None"                         + EOL +
            "_KURATOR_MORE_DATA_=False"                     + EOL +
            ""                                              + EOL +
            "import types"                                  + EOL +
            "import inspect"                                + EOL +
            ""                                              + EOL +
            "def _is_generator(g):"                         + EOL +
            "  return isinstance(g, types.GeneratorType)"   + EOL +
            ""                                              + EOL +
            "def _is_function(f):"                          + EOL +
            "  return f in globals() and inspect.isfunction(globals()[f])"   + EOL +
            ""                                              + EOL +
            "def _function_arg_count(f):"                   + EOL +
            "  return len(inspect.getargspec(f)[0])"        + EOL +
            ""                                              + EOL +
            "def _get_next_data():"                         + EOL +
            "  global _KURATOR_OUTPUT_"                     + EOL +
            "  global _KURATOR_RESULT_"                     + EOL +
            "  global _KURATOR_MORE_DATA_"                  + EOL +
            "  try:"                                        + EOL +
            "    _KURATOR_OUTPUT_=_KURATOR_RESULT_.next()"  + EOL +
            "    return"                                    + EOL +
            "  except StopIteration:"                       + EOL +
            "    _KURATOR_OUTPUT_=None"                     + EOL +
            "    _KURATOR_MORE_DATA_=False"                 + EOL;
    
    private static final String onStartWrapperFormat = 
            "def _call_onstart():"                          + EOL +
            "  global _KURATOR_OUTPUT_"                     + EOL +
            "  global _KURATOR_RESULT_"                     + EOL +
            "  global _KURATOR_MORE_DATA_"                  + EOL +
            "  _KURATOR_RESULT_ = %s%s()"                   + EOL +
            "  if _is_generator(_KURATOR_RESULT_):"         + EOL +
            "    _KURATOR_MORE_DATA_=True"                  + EOL +
            "  else:"                                       + EOL +
            "    _KURATOR_MORE_DATA_=False"                 + EOL +
            "    _KURATOR_OUTPUT_=_KURATOR_RESULT_"         + EOL;
   
    protected String onDataWrapperFormat = 
            "def _call_ondata():"                           + EOL +
            "  global _KURATOR_INPUT_"                      + EOL +
            "  global _KURATOR_OUTPUT_"                     + EOL +
            "  global _KURATOR_RESULT_"                     + EOL +
            "  global _KURATOR_MORE_DATA_"                  + EOL +
            "  _KURATOR_RESULT_ = %s%s(_KURATOR_INPUT_)"    + EOL +
            "  if _is_generator(_KURATOR_RESULT_):"         + EOL +
            "    _KURATOR_MORE_DATA_=True"                  + EOL +
            "  else:"                                       + EOL +
            "    _KURATOR_MORE_DATA_=False"                 + EOL +
            "    _KURATOR_OUTPUT_=_KURATOR_RESULT_"         + EOL;

    @Override
    protected void onInitialize() throws Exception {
        initializeJythonInterpreter();        
        configureJythonSysPath();
        loadCommonHelperFunctions();
        loadCustomCode();
        configureCustomCode();
        loadOnStartWrapper();
        loadOnDataWrapper();
        applySettings();
    }

    protected void configureCustomCode() throws Exception {
    }
    
    protected void loadCommonHelperFunctions() {
        interpreter.exec(commonScriptHeader);
    }
    
    protected void loadCustomCode() {
        // read the script into the interpreter
        interpreter.set("__name__",  "__kurator_actor__");
        this.script = (String)configuration.get("script");
        if (script != null) interpreter.execfile(script);
        
        String code = (String)configuration.get("code");
        if (code != null) interpreter.exec(code);        
    }

    
    protected void initializeJythonInterpreter() {
        
        Properties properties = System.getProperties();
        properties.put("python.import.site", "false");
                
        // create a python interpreter
        PySystemState.initialize(properties, null, new String[] {""});
        interpreter = new PythonInterpreter();
        
        // set output streams of interpreter to that set for this actor
        interpreter.setOut(super.outStream);
        interpreter.setErr(super.errStream);
        
        interpreter.exec("import sys"); 
        
        // cache a python None object
        none = interpreter.eval("None");
    }
    
    protected void configureJythonSysPath() {
        
        // add to python sys.path library directory from local Jython/Python installation
        String kuratorLocalPythonLib = System.getenv("KURATOR_LOCAL_PYTHON_LIB");
        if (kuratorLocalPythonLib != null) {
            prependSysPath(kuratorLocalPythonLib + "/site-packages");
            prependSysPath(kuratorLocalPythonLib);
        }

        // add to python sys.path jython libraries distributed via kurator-jython Git repo
        prependSysPath("kurator-jython");
        prependSysPath("../kurator-jython");
        
        // add to python sys.path library directory of packages bundled within Kurator jar
        prependSysPath("src/main/python");
        
        // add to python sys.path optional local packages directory
        String kuratorLocalPackages = System.getenv("KURATOR_LOCAL_PACKAGES");
        if (kuratorLocalPackages != null) {
            prependSysPath(kuratorLocalPackages); 
        }
    }
    
    private Boolean isFunction(String f) {
        PyBoolean result = (PyBoolean)interpreter.eval("_is_function('" + f + "')");
        return result.getBooleanValue();
    }
    
    private Integer getArgCount(String f) {
        PyInteger result = (PyInteger)interpreter.eval("_function_arg_count(" + f + ")");
        return result.asInt();
    }

    protected void loadOnDataWrapper() throws Exception {

        String onDataConfig = (String)configuration.get("onData");
        if (onDataConfig != null) {
            
            if (!isFunction(onDataConfig)) {
                throw new Exception("Custom onData handler '" + onDataConfig + "' not defined for actor");
            }
            onData = onDataConfig;
        
        } else if (isFunction(DEFAULT_ON_DATA_FUNCTION)) {

            onData = DEFAULT_ON_DATA_FUNCTION;
        
        } else {
            
            return;
        }
        
        interpreter.exec(String.format(onDataWrapperFormat, functionQualifier, onData));
    }
    
    private void loadOnStartWrapper() throws Exception {

        String onStartConfig = (String)configuration.get("onStart");
        if (onStartConfig != null) {
            
            if (!isFunction(onStartConfig)) {
                throw new Exception("Custom onStart handler '" + onStartConfig + "' not defined for actor");
            }
            onStart = onStartConfig;
        
        } else if (isFunction(DEFAULT_ON_START_FUNCTION)) {

            onStart = DEFAULT_ON_START_FUNCTION;
        
        } else {
            
            return;
        }
        
        interpreter.exec(String.format(onStartWrapperFormat, functionQualifier, onStart));
    }
    
    
    protected void applySettings() {
    
        if (settings != null) {
            for(Map.Entry<String, Object> setting : settings.entrySet()) {
                String name = setting.getKey();
                Object value = setting.getValue();
                interpreter.set(name, value);
            }
        }
    }    
    
    @Override
    protected void onStart() throws Exception {

        if (onStart != null) {
            interpreter.eval("_call_onstart()");
            broadcastOutputs();
        }

        if (onData == null) {
            endStreamAndStop();       
        }
    }
    
    @Override
    public void onData(Object value) throws Exception {  

        if (onData == null) {
            throw new Exception("No onData handler for actor " + this);
        }
        
        if (onData != null) {
        
            if (outputTypeIsInputType) {
                outputType = value.getClass();
            }
            
            interpreter.set("_KURATOR_INPUT_", value);
            interpreter.eval("_call_ondata()");
            broadcastOutputs();
        }
    }

    @Override
    protected void onEnd() {
        
        // call script end function if defined
        this.onEnd = (String)configuration.get("onEnd");
        if (onEnd != null) {
            interpreter.eval(onEnd + "()");
        }
        
        // shut down the interpreter
        interpreter.cleanup();
    }    

    protected void broadcastOutputs() {

        if (! interpreter.get("_KURATOR_MORE_DATA_", Boolean.class)) {
            broadcastOutput(interpreter.get("_KURATOR_OUTPUT_", outputType));
            return;
        }

        do {
            interpreter.eval("_get_next_data()");
            Object output = interpreter.get("_KURATOR_OUTPUT_", outputType);
            if (output != null) broadcastOutput(output);
        } while (interpreter.get("_KURATOR_MORE_DATA_", Boolean.class));
    }
    
    
    protected void broadcastOutput(Object output) {
        if (output != null || broadcastNulls) {
            broadcast(output);
        }
    }
         
    private void prependSysPath(String path) {
        interpreter.eval(String.format("sys.path.insert(0, '%s')%s", path, EOL));
    }
}
