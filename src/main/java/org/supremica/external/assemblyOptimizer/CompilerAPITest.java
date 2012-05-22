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

/**
 * A test class to test dynamic compilation API.
 *
 */
public class CompilerAPITest {
    final Logger logger = Logger.getLogger(CompilerAPITest.class.getName()) ;

    private static final String PACKAGE_NAME = "org.supremica.external.assemblyOptimizer.dynamic";

    private static final Random random = new Random();

	// options: set the classpath, otherwise Scheduler cannot be found.
	private final CharSequenceCompiler<Scheduler> compiler = new CharSequenceCompiler<Scheduler>(
	         getClass().getClassLoader(), Arrays.asList(new String[] { "-cp", ".:target/classes" }));


    private int classNameSuffix = 0;

   // the Java source template
   private String template;

   private Scheduler scheduler;

    /**Java source code to be compiled dynamically*/
    static String sourceCode = PACKAGE_NAME + ";" +
        "class DynamicCompilationHelloWorld{" +
            "public static void main (String args[]){" +
                "System.out.println (\"Hello, dynamic compilation world!\");" +
            "}" +
        "}" ;

   /**
    * @return random hex digits with a '_' prefix
    */
   private String digits() {
      return '_' + Long.toHexString(random.nextLong());
   }

   /**
    * Return the Plotter function Java source, substituting the given package
    * name, class name, and double expression
    *
    * @param packageName
    *           a valid Java package name
    * @param className
    *           a valid Java class name
    * @param expression
    *           text for a double expression, using double x
    * @return source for the new class implementing Function interface using the
    *         expression
    * @throws IOException
    */
   private String fillTemplate(String packageName, String className, String expression)
         throws IOException {
      if (template == null)
         template = readTemplate();
      // simplest "template processor":
      String source = template.replace("$packageName", packageName)
            .replace("$className", className)
            .replace("$expression", expression);
      return source;
   }

   /**
    * Read the Function source template
    *
    * @return a source template
    * @throws IOException
    */
   private String readTemplate() throws IOException {
	  String templateFileName = "Scheduler.java.template";
      InputStream is = CompilerAPITest.class.getResourceAsStream(templateFileName);
	  if(is == null){
		 System.err.println("Can't find the file");
		System.exit(-1);
	  }
	  int size = is.available();
      byte bytes[] = new byte[size];
      if (size != is.read(bytes, 0, size))
         throw new IOException();
      return new String(bytes, "US-ASCII");
   }


   /**
    * Generate Java source for a Function which computes f(x)=expr
    *
    * @param expr
    *           String representation of Java expression that returns a double
    *           value for an input value x. The class in which this expression
    *           is embedded uses static import for all the members of the
    *           java.lang.Math class so they can be accessed without
    *           qualification.
    * @return an object which computes the function denoted by expr
    */
   Scheduler newScheduler(final String expr) {
      try {
         // generate semi-secure unique package and class names
         final String packageName = PACKAGE_NAME + digits();
         final String className = "Fx_" + (classNameSuffix++) + digits();
         final String qName = packageName + '.' + className;
         // generate the source class as String
         final String source = fillTemplate(packageName, className, expr);
         // compile the generated Java source
         final DiagnosticCollector<JavaFileObject> errs = new DiagnosticCollector<JavaFileObject>();
         Class<Scheduler> compiledScheduler = compiler.compile(qName, source, errs,
               new Class<?>[] { Scheduler.class });
         log(errs);
         return compiledScheduler.newInstance();
      } catch (CharSequenceCompilerException e) {
         log(e.getDiagnostics());
      }	catch (InstantiationException e) {
	     e.printStackTrace();
	  } catch (IllegalAccessException e) {
	     e.printStackTrace();
	  } catch (IOException e) {
	     e.printStackTrace();
	  }
      return NULL_SCHEDULER;
   }

   /**
    * Log diagnostics into the error JTextArea
    *
    * @param diagnostics
    *           iterable compiler diagnostics
    */
   private void log(final DiagnosticCollector<JavaFileObject> diagnostics) {
      final StringBuilder msgs = new StringBuilder();
      for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics
            .getDiagnostics()) {
         msgs.append(diagnostic.getMessage(null)).append("\n");
      }
	  System.err.println(msgs.toString());
   }

   public Scheduler getAnScheduler(String source){
		scheduler = newScheduler(source);
		return scheduler; 
   }

    public static void main(String args[]){
		 double x = 1.0;
		 System.out.println("x: " + x + " scheduler(x): " + new CompilerAPITest().getAnScheduler("x * (sin(x) + cos(x))").f(x) );
    }

   /**
    * Null Object pattern to use when there are exceptions with the function
    * expression.
    */
   static final Scheduler NULL_SCHEDULER = new Scheduler() {
      public double f(final double x) {
         return 0.0;
      }
   };

}
