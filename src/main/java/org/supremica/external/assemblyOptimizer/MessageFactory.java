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

import java.util.List;
import java.util.Map;

import org.supremica.external.assemblyOptimizer.AssemblyStructureProtos.VariableType;
import org.supremica.external.assemblyOptimizer.AssemblyStructureProtos.Variable;
import org.supremica.external.assemblyOptimizer.AssemblyStructureProtos.Attribute;
import org.supremica.external.assemblyOptimizer.AssemblyStructureProtos.Expression;
import org.supremica.external.assemblyOptimizer.AssemblyStructureProtos.Guard;
import org.supremica.external.assemblyOptimizer.AssemblyStructureProtos.Operation;
import org.supremica.external.assemblyOptimizer.AssemblyStructureProtos.Resource;


/**
 * A bunch of static methods, which are used to instantiate operation and resource messages.  
 *
 * @author Zhennan, Knut
 *
 */

public class MessageFactory {
	
	
	public static Variable createVariable(String name, VariableType type, String initial)
	{
		Variable.Builder variable = Variable.newBuilder();
		
		variable.setName(name);
		variable.setType(type);
		variable.setInitial(initial);
		
		return variable.build();
	}
	
	// create a variable with name and interger initial value.
	public static Variable createVariable(String name, int initial)
	{
		return createVariable(name, VariableType.UINT32, String.valueOf(initial));
	}
	
	public static Attribute createAttribute(String name, VariableType type, String value)
	{
		Attribute.Builder attribute = Attribute.newBuilder();
		
		attribute.setName(name);
		attribute.setType(type);
		attribute.setValue(value);
		
		return attribute.build();
	}
	
	public static Expression createExpression(String expr)
	{
		Expression.Builder expression = Expression.newBuilder();

		expression.setExpression(expr);

		return expression.build();
	}
	
	public static Guard createGuard(String expression)
	{
		Guard.Builder guard = Guard.newBuilder();

		guard.setBooleanExpression(createExpression(expression));

		return guard.build();
	}
	
	public static Resource createResource(String name, String capacity)
	{
		Resource.Builder resource = Resource.newBuilder();
		
		resource.setName(name);
		resource.setCapacity(createAttribute("capacity", VariableType.UINT32, capacity));
		return resource.build();
	}

	public static Operation createOperation(String name, String guard, 
											List<String> enter_action, List<String> exit_action, 
											List<String> use_resource, 
											int cost_time, boolean terminal,
											List<String> start_after_operations,
											List<Variable> variableList)
	{
        Operation.Builder operation = Operation.newBuilder();

        operation.setName(name);
        operation.setGuard(createGuard(guard));

		// set the exter_action and exit_action assignments 
		for(String anEnterAction: enter_action)
			operation.addEnterAction(createExpression(anEnterAction));
		
		for(String anExitAction: exit_action)
			operation.addExitAction(createExpression(anExitAction));
			
		for(String aResource: use_resource){
                    if (!aResource.isEmpty())
			operation.addUseResource(aResource);
                }
                
		operation.setCostTime(cost_time);
		operation.setTerminal(terminal);
		
		for(String opName: start_after_operations)
			operation.addStartAfterOperations(opName);
		
		for(Variable variable: variableList)	
		operation.addVariable(variable);
		
        return operation.build();
	}
	
	public static Operation createOperation(String name, String guard, 
											List<String> enter_action, List<String> exit_action, 
											List<String> use_resource, 
											int cost_time, boolean terminal,
											List<String> start_after_operations,
											List<Variable> variableList,
											String terminalCondition)
	{
        Operation.Builder operation = Operation.newBuilder();

        operation.setName(name);
        operation.setGuard(createGuard(guard));

		// set the exter_action and exit_action assignments 
		for(String anEnterAction: enter_action)
			operation.addEnterAction(createExpression(anEnterAction));
		
		for(String anExitAction: exit_action)
			operation.addExitAction(createExpression(anExitAction));
			
		for(String aResource: use_resource){
                    if (!aResource.isEmpty())
                        operation.addUseResource(aResource);
                }
			
			
		operation.setCostTime(cost_time);
		operation.setTerminal(terminal);
		
		for(String opName: start_after_operations)
			operation.addStartAfterOperations(opName);
		
		for(Variable variable: variableList)	
		operation.addVariable(variable);
		
		operation.setTerminalCondition(createExpression(terminalCondition));
		
        return operation.build();
	}
	
	public static Operation createOperation(String name, String guard, 
											List<String> enter_action, List<String> exit_action, 
											List<String> use_resource, 
											int cost_time, boolean terminal,
											int start_time,
											List<String> start_after_operations,
											List<Variable> variableList,
											String terminalCondition)
	{
        Operation.Builder operation = Operation.newBuilder();

        operation.setName(name);
        operation.setGuard(createGuard(guard));

		// set the exter_action and exit_action assignments 
		for(String anEnterAction: enter_action)
			operation.addEnterAction(createExpression(anEnterAction));
		
		for(String anExitAction: exit_action)
			operation.addExitAction(createExpression(anExitAction));
			
		for(String aResource: use_resource){
                    if (!aResource.isEmpty())
			operation.addUseResource(aResource);
                }
		operation.setCostTime(cost_time);
		operation.setStartTime(start_time);
		operation.setTerminal(terminal);
		
		for(String opName: start_after_operations)
			operation.addStartAfterOperations(opName);
		
		for(Variable variable: variableList)	
		operation.addVariable(variable);
		
		operation.setTerminalCondition(createExpression(terminalCondition));
		
        return operation.build();
	}
}