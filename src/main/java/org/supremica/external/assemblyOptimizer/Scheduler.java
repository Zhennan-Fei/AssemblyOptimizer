package org.supremica.external.assemblyOptimizer;


/**
 * From a feasible solution (a sequence of operations), 
 * calculate the minimal total cost.
 *
 */


public interface Scheduler { 
   double f(double x);
}
