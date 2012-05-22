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

import org.supremica.external.assemblyOptimizer.GenericResource;
import org.supremica.external.assemblyOptimizer.AssemblyStructureProtos.Resource;

/**
 * From a resource object, build a generic resource
 *
 * @author Zhennan, Knut
 */

public class GenericResourceBuilder{
	
	
	public static final Logger logger = Logger.getLogger(GenericResourceBuilder.class.getName());
	
	private static final String PACKAGE_NAME = "org.supremica.external.assemblyOptimizer.runtime";
	private static final String CLASSNAME_SUFFIX = "ConcreteResource_";
	
	private final String classPath = "." + System.getProperty("path.separator") + "target/classes" + 
	System.getProperty("path.separator") + 
	System.getProperty("java.class.path");
	
	private final CharSequenceCompiler<GenericResource> compiler = new CharSequenceCompiler<GenericResource>(
	         getClass().getClassLoader(), Arrays.asList(new String[] {"-classpath", classPath}));

    private String templateFileName = "ConcreteResource.java.template";
	private String template;


    GenericResource newGenericResource(Resource resource) {
      
	  try {
         // generate semi-secure unique package and class names
         final String packageName = PACKAGE_NAME;
         final String className = CLASSNAME_SUFFIX + resource.getName();
         final String qName = packageName + '.' + className;
         
		 // generate the source class as String
         final String source = fillTemplate(packageName, className, resource.getName());
         
		 // compile the generated Java source
         final DiagnosticCollector<JavaFileObject> errs = new DiagnosticCollector<JavaFileObject>();
         Class<GenericResource> compiledGenericResource = compiler.compile(qName, source, errs,
               new Class<?>[] { GenericResource.class });
		 log(errs);
		 GenericResource genericResource = compiledGenericResource.newInstance();
		 genericResource.setCapacity(Integer.parseInt(resource.getCapacity().getValue()));
         return genericResource;

      } catch (CharSequenceCompilerException e) {
         log(e.getDiagnostics());
      }	catch (InstantiationException e) {
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
	
    private String fillTemplate(String packageName, String className, String resourceName)
         throws IOException {
   
      String source = readTemplate().replace("$packageName", packageName)
            .replace("$className", className)
            .replace("$resource", resourceName);
	  //System.err.println(source);
      return source;
    }

    private String readTemplate() throws IOException 
    {
	
      if(template == null)
	  {
		  InputStream is = GenericResourceBuilder.class.getResourceAsStream(templateFileName);

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
	
}