/* 
   Copyright (c) 2012, Knut Åkesson and Zhennan Fei, Chalmers University of Technology
   Copyright (c) 2012, Kristofer Bengtsson, Sekvensa AB,
   Developed with the sponsorship of the Defense Advanced Research Projects Agency (DARPA).
   Permission is hereby granted, free of charge, to any person obtaining a copy of this data, including any
   software or models in source or binary form, specifications, algorithms, and documentation (collectively
   "the Data"), to deal in the Data without restriction, including without limitation the rights to use, copy,
   modify, merge, publish, distribute, sublicense, and/or sell copies of the Data, and to permit persons to
   whom the Data is furnished to do so, subject to the following conditions:
   The above copyright notice and this permission notice shall be included in all copies or substantial
   portions of the Data.
   THE DATA IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
   INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
   PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS,
   SPONSORS, DEVELOPERS, CONTRIBUTORS, OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
   CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
   OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE DATA OR THE USE OR
   OTHER DEALINGS IN THE DATA.
*/


package org.supremica.external.assemblyOptimizer;

import java.io.InputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.Random;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;

import javaxtools.compiler.CharSequenceCompiler;
import javaxtools.compiler.CharSequenceCompilerException;

import org.supremica.external.assemblyOptimizer.GenericOperation;
import org.supremica.external.assemblyOptimizer.AssemblyStructureProtos.Operation;
import org.supremica.external.assemblyOptimizer.AssemblyStructureProtos.Expression;
import org.supremica.external.assemblyOptimizer.AssemblyStructureProtos.Variable;
import org.supremica.external.assemblyOptimizer.AssemblyStructureProtos.VariableType;

/**
 * From a operation object, build a generic operation
 *
 * @author Zhennan, Knut, Kristofer
 * 
 * The code is not optimized and needs to be refactored later.
 * Changed variable handling by adding an abstract operation class containing
 * static variables. This increased the performance by x5 - x10.
 */

public class GenericOperationBuilder{
	
	public static final Logger logger = Logger.getLogger(GenericOperationBuilder.class.getName());
	
	private static final String PACKAGE_NAME = "org.supremica.external.assemblyOptimizer.runtime";
	private static final String CLASSNAME_SUFFIX = "ConcreteOperation_";
	
	private final String classPath = "." + System.getProperty("path.separator") + "target/classes" + 
	System.getProperty("path.separator") + 
	System.getProperty("java.class.path");
	
	private final CharSequenceCompiler<GenericOperation> compiler = new CharSequenceCompiler<GenericOperation>(
	         getClass().getClassLoader(), Arrays.asList(new String[] { "-classpath",  classPath}));

	private String templateFileName = "ConcreteOperation.java.template";
	private String template;
	
	private ConcreteVariables variableSet = ConcreteVariables.getInstance();


    GenericOperation newGenericOperation(Operation operation) 
	{    
	    try 
		{
               // try with abstract class
                String absClass = createAbsClassAsString(operation);
                    
                    
         	// generate semi-secure unique package and class names
         	final String packageName = PACKAGE_NAME;
         	final String className = CLASSNAME_SUFFIX + operation.getName();
         	final String qName = packageName + '.' + className;
         
		 	// generate the source class as String
         	final String source = fillTemplate(packageName, className, operation);
         
		 	// compile the generated Java source
         	final DiagnosticCollector<JavaFileObject> errs = new DiagnosticCollector<JavaFileObject>();
                
                Class absOp = compiler.compile(PACKAGE_NAME+".AbstractOperation", absClass, errs, new Class<?>[] { });
                
         	Class<GenericOperation> compiledGenericOperation = compiler.compile(qName, source, errs,
               												new Class<?>[] { GenericOperation.class });
		 
		 	log(errs);
         	return compiledGenericOperation.newInstance();

      	} catch (CharSequenceCompilerException e) {
         	log(e.getDiagnostics());
      	} catch (InstantiationException e) {
		 	e.printStackTrace();
		} catch (IllegalAccessException e) {
		    e.printStackTrace();
		} catch (IOException e) {
		    e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	
      	return null;
    }
	
    private String fillTemplate(String packageName, String className, Operation operation) throws IOException 
	{
   
		String guard = "true";
		if(operation.getGuard().getBooleanExpression().getExpression().length() != 0)
		{
			guard = operation.getGuard().getBooleanExpression().getExpression();
		}
		
		String source = readTemplate().replace("$packageName", packageName)
        	.replace("$className", className)
            .replace("$guard", guard);

	  	StringBuilder presb = new StringBuilder();
	  	for(Expression preAction: operation.getEnterActionList()){
	  		presb.append(preAction.getExpression()).append(";\n\t\t");			
	  	}
	
		if(presb.length() == 0)
			presb.append(" ");
			
	  	source = source.replace("$enterActions", presb.toString());
	
	  	StringBuilder postsb = new StringBuilder();
	  	for(Expression postAction: operation.getExitActionList()){
			postsb.append(postAction.getExpression()).append(";\n\t\t");
	  	}
	
		if(postsb.length() == 0)
			postsb.append(" ");
	
	  	source = source.replace("$exitActions", postsb.toString());
	
	
		// add terminal condition
		String terminalCondition = "false";
		if(operation.getTerminalCondition().getExpression().length() != 0)
		{
			terminalCondition = operation.getTerminalCondition().getExpression();
		}
		
		source = source.replace("$terminalCondition", terminalCondition);
	
	
		String resourceFieldPrefix = "private GenericResource ";
		String operationFieldPrefix = "private GenericOperation ";
		
		String resourceMethodPrefix = "public void setGenericResource_";
		String operationMethodPrefix = "public void setGenericOperation_";
		
		StringBuilder rfmsb = new StringBuilder();
		StringBuilder ofmsb = new StringBuilder();
		StringBuilder vfmsb = new StringBuilder();
               // StringBuilder vrmsb = new StringBuilder();
		
		for(String useResource: operation.getUseResourceList())
		{
			// fields
			rfmsb.append("\t" + resourceFieldPrefix + useResource + ";\n");
			// set methods
			rfmsb.append("\t" + resourceMethodPrefix + useResource  + "(GenericResource resource)" + "\n");
			rfmsb.append("\t{\n");
			rfmsb.append("\t\t" + "if(" + useResource + "!= null)" + " throw new RuntimeException" + 
																"(\"The resource cannot be set\");\n");
			rfmsb.append("\t\t" + useResource + " = " + "resource;\n");
			rfmsb.append("\t}\n\n");
		}
		
		for(String op: operation.getStartAfterOperationsList())
		{
			// fields
			ofmsb.append("\t" + operationFieldPrefix + op + ";\n");
			// methods
			ofmsb.append("\t" + operationMethodPrefix + op + "(GenericOperation operation)" + "\n");
			ofmsb.append("\t{\n");
			ofmsb.append("\t\t" + "if(" + op + "!= null)" + " throw new RuntimeException" + 
																"(\"The operation cannot be set\");\n");
			ofmsb.append("\t\t" + op + " = " + "operation;\n");
			ofmsb.append("\t}\n\n");
		}
		
		// The concrete variables only support for Integer type. Improve later.
//		for(Variable variable: operation.getVariableList())
//		{
//			String varName = variable.getName();
//			String varInitial = variable.getInitial();
//			VariableType type = variable.getType();
//			switch(type)
//			{
//				case DOUBLE:
//					vfmsb.append("\t" + "double " + varName + " = " + Double.parseDouble(varInitial)  + ";\n");
//					break;
//				case FLOAT:
//					vfmsb.append("\t" + "float " + varName + " = " + Float.parseFloat(varInitial) + ";\n");
//					break;
//				case UINT32:
//					vfmsb.append("\t" + "int " + varName + " = " + Integer.parseInt(varInitial) + ";\n");
//					variableSet.addVarName(varName, Integer.valueOf(varInitial));
//					break;
//				case INT32:
//					vfmsb.append("\t" + "int " + varName + " = " + Integer.parseInt(varInitial) + ";\n");
//					variableSet.addVarName(varName, Integer.valueOf(varInitial));
//					break;
//				case UINT64:
//					vfmsb.append("\t" + "long " + varName + " = " + Long.parseLong(varInitial) + ";\n");
//					break;
//				case INT64:
//				 	vfmsb.append("\t" + "long " + varName + " = " + Long.parseLong(varInitial) + ";\n");
//					break;
//				case BOOL:
//					vfmsb.append("\t" + "boolean " + varName + " = " + Boolean.valueOf(varInitial) + ";\n");
//					break;
//				case STRING:
//					vfmsb.append("\t" + "String " + varName + " = " + varInitial + ";\n");
//			}
//		}
                
                
        /*        vrmsb.append("\n");
                vrmsb.append("\t" + "public void resetVariables(){" + "\n");
                for(Variable variable: operation.getVariableList())
		{
			String varName = variable.getName();
			String varInitial = variable.getInitial();
			vrmsb.append("\t\t" +  varName + " = " + Integer.parseInt(varInitial) + ";\n");
                        vrmsb.append("\t\t" +  "ConcreteVariables.getInstance().setValueForVarName(\"" 
                                                + varName +"\","+Integer.parseInt(varInitial) + ");\n");
                        
		}
                
                vrmsb.append("\t}");*/
			
		rfmsb.append(ofmsb);
		rfmsb.append(vfmsb);
 //               rfmsb.append(vrmsb);
		
		if(rfmsb.length() == 0)
			rfmsb.append(" ");
			
		source = source.replace("$resourceAndOperationFieldsAndMethods", rfmsb.toString());
	  	//System.err.println(source);
                
                // Reset Command in each operation for external data
                // temp to test. Should be sent via Operation proto
                source = source.replace("$resetCommand", "sequenceplanner.IO.optimizer.ProductLocker.INSTANCE.reset()");
                
                
      	return source;
    }

    private String readTemplate() throws IOException 
    {
	
      	if(template == null)
	  	{
		  	InputStream is = GenericOperationBuilder.class.getResourceAsStream(templateFileName);

		  	int size = is.available();
	      	byte bytes[] = new byte[size];

	      	if (size != is.read(bytes, 0, size)) throw new IOException();
		  
		  	template = new String(bytes, "US-ASCII");
	  	}

	  	return template;
    }

    /**
     * Log diagnostics
     *
     * @param diagnostics
     *           iterable compiler diagnostics
     */
    private void log(final DiagnosticCollector<JavaFileObject> diagnostics) 
	{
		final StringBuilder msgs = new StringBuilder();
       	for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) 
	   	{
          	msgs.append(diagnostic.getMessage(null)).append("\n");
       	}
    }

    private String createAbsClassAsString(Operation operation) {
        
        StringBuilder varDef = new StringBuilder();
        StringBuilder varReset = new StringBuilder();
        StringBuilder stateCreator = new StringBuilder();
        
        if (!operation.getVariableList().isEmpty())
            stateCreator.append("\t\t int[] result = new int[" +operation.getVariableList().size() + "];\n");
        else stateCreator.append("\t\t int[] result = new int[];\n");
        
        int pos = 0;
        for(Variable variable: operation.getVariableList()){
            String varName = variable.getName();
            String varInitial = variable.getInitial();
            VariableType type = variable.getType();
            switch(type){
                case DOUBLE:
                        varDef.append("static "+ "double " + varName + " = " + Double.parseDouble(varInitial)  + ";\n");
                        varReset.append("\t\t" + varName + " = " + Double.parseDouble(varInitial)  + ";\n");
                        stateCreator.append("\t\t result["+pos+"] = (new Double("+varName+")).hashCode();\n");
                        break;
                case FLOAT:
                        varDef.append("static "+  "float " + varName + " = " + Float.parseFloat(varInitial) + ";\n");
                        varReset.append("\t\t" + varName + " = " + Float.parseFloat(varInitial) + ";\n");
                        stateCreator.append("\t\t result["+pos+"] = (new Float("+varName+")).hashCode();\n");
                        break;
                case UINT32:
                        varDef.append("static "+  "int " + varName + " = " + Integer.parseInt(varInitial) + ";\n");
                        varReset.append("\t\t" + varName + " = " + Integer.parseInt(varInitial) + ";\n");
                        stateCreator.append("\t\t result["+pos+"] = "+varName+";\n");
                        break;
                case INT32:
                        varDef.append("static "+  "int " + varName + " = " + Integer.parseInt(varInitial) + ";\n");
                        varReset.append("\t\t" + varName + " = " + Integer.parseInt(varInitial) + ";\n");
                        stateCreator.append("\t\t result["+pos+"] = "+varName+";\n");
                        break;
                case UINT64:
                        varDef.append("static "+  "long " + varName + " = " + Long.parseLong(varInitial) + ";\n");
                        varReset.append("\t\t" + varName + " = " + Long.parseLong(varInitial) + ";\n");   
                        stateCreator.append("\t\t result["+pos+"] = "+varName+";\n");
                        break;
                case INT64:
                        varDef.append("static "+  "long " + varName + " = " + Long.parseLong(varInitial) + ";\n");
                        varReset.append("\t\t" + varName + " = " + Long.parseLong(varInitial) + ";\n");
                        stateCreator.append("\t\t result["+pos+"] = "+varName+";\n");
                        break;
                case BOOL:
                        varDef.append("static "+  "boolean " + varName + " = " + Boolean.valueOf(varInitial) + ";\n");
                        varReset.append("\t\t" + varName + " = " + Boolean.valueOf(varInitial) + ";\n");
                        stateCreator.append("\t\t result["+pos+"] = (new Boolean("+varName+")).hashCode();\n");
                        break;
                case STRING:
                        varDef.append("static "+  "String " + varName + " = " + varInitial + ";\n");
                        varReset.append("\t\t" + varName + " = " + varInitial + ";\n");
                        stateCreator.append("\t\t result["+pos+"] = "+varName+".hashCode();\n");
            }
            pos++;
        }
        
        stateCreator.append("\treturn result;\n");
        
        StringBuilder result = new StringBuilder();
        result.append("package "+PACKAGE_NAME+";\n\n");
        result.append("public abstract class AbstractOperation{ \n");
        result.append(varDef);
        result.append("\n\t public void resetVariables(){\n");
        result.append(varReset);
        result.append("\t }\n");
        
        result.append("\t public int[] getCurrentState(){\n");
        result.append(stateCreator);
        result.append("\t }\n");
        
        
        result.append("}\n");
        
                
        return result.toString();
    }
	

}