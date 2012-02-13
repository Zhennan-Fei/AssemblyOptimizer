package org.supremica.external.assemblyOptimizer;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

import org.supremica.external.assemblyOptimizer.AssemblyStructureProtos.Operation;
import org.supremica.external.assemblyOptimizer.AssemblyStructureProtos.Resource;
import org.supremica.external.assemblyOptimizer.AssemblyStructureProtos.Variable;
import org.supremica.external.assemblyOptimizer.AssemblyStructureProtos.VariableType;

/**
 * Hard code Fish and Thompson 2 * 3 and 6 * 6 (later)
 *
 * @author Zhennan, Knut
 */

public class Example_FT
{
	private String description;
	private List<Resource> resourceSet;
	private List<Operation> operationSet;
	
	List<String> enterActionList = new ArrayList<String>();
	List<String> exitActionList = new ArrayList<String>();
	List<String> useResource = new ArrayList<String>();
	List<String> startAfterOperations = new ArrayList<String>();
	List<Variable> variableList = new ArrayList<Variable>();

	public Example_FT()
	{
		resourceSet = new ArrayList<Resource>();
		operationSet = new ArrayList<Operation>();
	}

	public void addResource(String name, String capacity)
	{
		Resource resource = MessageFactory.createResource(name, capacity);
		resourceSet.add(resource);
	}

	public void addOperation(String name, String guard,
											List<String> enter_action, List<String> exit_action,
											List<String> use_resource,
											int cost_time, boolean terminal,
											List<String> start_after_operations,
											List<Variable> varibleList)
	{
		Operation operation = MessageFactory.createOperation(name, guard, enter_action, exit_action, use_resource,
											 cost_time, terminal, start_after_operations, varibleList);
		operationSet.add(operation);
	}
	
	public void addOperation(Operation operation)
	{
		operationSet.add(operation);
	}

	public List<Operation> getOperationSet()
	{
		return operationSet;
	}

	public List<Resource> getResourceSet()
	{
		return resourceSet;
	}

	private void clean (List<String> enter_action, List<String> exit_action,
						List<String> use_resource, List<String> start_after_operations, List<Variable> varibleList)
	{
		enter_action.clear();
		exit_action.clear();
		use_resource.clear();
		start_after_operations.clear();
		varibleList.clear();
	}

	public String getDescription()
	{
		return description;
	}
	
	// a method to build an operation for Fisher and Thompson example
	private Operation getAnOperation(String name, String guardExpression, String resource, int costTome, boolean terminal,
									 String bookingResource, String unbookingResource, String setFinish, String operation)
	{
		clean(enterActionList, exitActionList, useResource, startAfterOperations, variableList);
		enterActionList.add(bookingResource);
		exitActionList.add(unbookingResource);
		exitActionList.add(setFinish);
		useResource.add(resource);
		
		if(operation != null)
		{
			startAfterOperations.add(operation);
			guardExpression = guardExpression + "&&" + operation + ".isFinished()";
		}
		
		return MessageFactory.createOperation(name, guardExpression, enterActionList, exitActionList, useResource, costTome, terminal, startAfterOperations, variableList);
	}
	
	public void buildExample_23()
	{
		description = "Fish & Thompson 2 * 3";

		addResource("m0", "1");
		addResource("m1", "1");
		addResource("m2", "1");
		
		addOperation(getAnOperation("op_11", "m0.isAvailable()", "m0", 1, false, "m0.allocate()", "m0.deallocate()", "setFinished()", null)); // op11
		addOperation(getAnOperation("op_12", "m1.isAvailable()", "m1", 4, false, "m1.allocate()", "m1.deallocate()", "setFinished()", "op_11")); // op12
		addOperation(getAnOperation("op_13", "m2.isAvailable()", "m2", 1, true, "m2.allocate()", "m2.deallocate()", "setFinished()", "op_12")); // op13
		
		addOperation(getAnOperation("op_21", "m2.isAvailable()", "m2", 2, false, "m2.allocate()", "m2.deallocate()", "setFinished()", null)); // op21
		addOperation(getAnOperation("op_22", "m1.isAvailable()", "m1", 1, false, "m1.allocate()", "m1.deallocate()", "setFinished()", "op_21")); // op22
		addOperation(getAnOperation("op_23", "m0.isAvailable()", "m0", 3, true, "m0.allocate()", "m0.deallocate()", "setFinished()", "op_22")); // op23

	}
	
	public void buildExample_66()
	{
		description = "Standard Fish & Thompson 6 * 6";
		
		addResource("m0", "1");
		addResource("m1", "1");
		addResource("m2", "1");
		addResource("m3", "1");
		addResource("m4", "1");
		addResource("m5", "1");
		
		addOperation(getAnOperation("op_11", "m2.isAvailable()", "m2", 1, false, "m2.allocate()", "m2.deallocate()", "setFinished()", null)); // op11
		addOperation(getAnOperation("op_12", "m0.isAvailable()", "m0", 3, false, "m0.allocate()", "m0.deallocate()", "setFinished()", "op_11")); // op12
		addOperation(getAnOperation("op_13", "m1.isAvailable()", "m1", 6, false, "m1.allocate()", "m1.deallocate()", "setFinished()", "op_12")); // op13
		addOperation(getAnOperation("op_14", "m3.isAvailable()", "m3", 7, false, "m3.allocate()", "m3.deallocate()", "setFinished()", "op_13")); // op14
		addOperation(getAnOperation("op_15", "m5.isAvailable()", "m5", 3, false, "m5.allocate()", "m5.deallocate()", "setFinished()", "op_14")); // op15
		addOperation(getAnOperation("op_16", "m4.isAvailable()", "m4", 6, true,  "m4.allocate()", "m4.deallocate()", "setFinished()", "op_15")); // op16
		
		addOperation(getAnOperation("op_21", "m1.isAvailable()", "m1", 8, false, "m1.allocate()", "m1.deallocate()", "setFinished()", null)); // op21
		addOperation(getAnOperation("op_22", "m2.isAvailable()", "m2", 5, false, "m2.allocate()", "m2.deallocate()", "setFinished()", "op_21")); // op22
		addOperation(getAnOperation("op_23", "m4.isAvailable()", "m4", 10, false, "m4.allocate()", "m4.deallocate()", "setFinished()", "op_22")); // op23
		addOperation(getAnOperation("op_24", "m5.isAvailable()", "m5", 10, false, "m5.allocate()", "m5.deallocate()", "setFinished()", "op_23")); // op24
		addOperation(getAnOperation("op_25", "m0.isAvailable()", "m0", 10, false, "m0.allocate()", "m0.deallocate()", "setFinished()", "op_24")); // op25
		addOperation(getAnOperation("op_26", "m3.isAvailable()", "m3", 4, true,  "m3.allocate()", "m3.deallocate()", "setFinished()", "op_25")); // op26
		
		addOperation(getAnOperation("op_31", "m2.isAvailable()", "m2", 5, false, "m2.allocate()", "m2.deallocate()", "setFinished()", null)); // op31
		addOperation(getAnOperation("op_32", "m3.isAvailable()", "m3", 4, false, "m3.allocate()", "m3.deallocate()", "setFinished()", "op_31")); // op32
		addOperation(getAnOperation("op_33", "m5.isAvailable()", "m5", 8, false, "m5.allocate()", "m5.deallocate()", "setFinished()", "op_32")); // op33
		addOperation(getAnOperation("op_34", "m0.isAvailable()", "m0", 9, false, "m0.allocate()", "m0.deallocate()", "setFinished()", "op_33")); // op34
		addOperation(getAnOperation("op_35", "m1.isAvailable()", "m1", 1, false, "m1.allocate()", "m1.deallocate()", "setFinished()", "op_34")); // op35
		addOperation(getAnOperation("op_36", "m4.isAvailable()", "m4", 7, true,  "m4.allocate()", "m4.deallocate()", "setFinished()", "op_35")); // op36
		
		addOperation(getAnOperation("op_41", "m1.isAvailable()", "m1", 5, false, "m1.allocate()", "m1.deallocate()", "setFinished()", null)); // op41
		addOperation(getAnOperation("op_42", "m0.isAvailable()", "m0", 5, false, "m0.allocate()", "m0.deallocate()", "setFinished()", "op_41")); // op42
		addOperation(getAnOperation("op_43", "m2.isAvailable()", "m2", 5, false, "m2.allocate()", "m2.deallocate()", "setFinished()", "op_42")); // op43
		addOperation(getAnOperation("op_44", "m3.isAvailable()", "m3", 3, false, "m3.allocate()", "m3.deallocate()", "setFinished()", "op_43")); // op44
		addOperation(getAnOperation("op_45", "m4.isAvailable()", "m4", 8, false, "m4.allocate()", "m4.deallocate()", "setFinished()", "op_44")); // op45
		addOperation(getAnOperation("op_46", "m5.isAvailable()", "m5", 9, true,  "m5.allocate()", "m5.deallocate()", "setFinished()", "op_45")); // op46
		
		addOperation(getAnOperation("op_51", "m2.isAvailable()", "m2", 9, false, "m2.allocate()", "m2.deallocate()", "setFinished()", null)); // op51
		addOperation(getAnOperation("op_52", "m1.isAvailable()", "m1", 3, false, "m1.allocate()", "m1.deallocate()", "setFinished()", "op_51")); // op52
		addOperation(getAnOperation("op_53", "m4.isAvailable()", "m4", 5, false, "m4.allocate()", "m4.deallocate()", "setFinished()", "op_52")); // op53
		addOperation(getAnOperation("op_54", "m5.isAvailable()", "m5", 4, false, "m5.allocate()", "m5.deallocate()", "setFinished()", "op_53")); // op54
		addOperation(getAnOperation("op_55", "m0.isAvailable()", "m0", 3, false, "m0.allocate()", "m0.deallocate()", "setFinished()", "op_54")); // op55
		addOperation(getAnOperation("op_56", "m3.isAvailable()", "m3", 1, true,  "m3.allocate()", "m3.deallocate()", "setFinished()", "op_55")); // op56
		
		addOperation(getAnOperation("op_61", "m1.isAvailable()", "m1", 3, false, "m1.allocate()", "m1.deallocate()", "setFinished()", null)); // op61
		addOperation(getAnOperation("op_62", "m3.isAvailable()", "m3", 3, false, "m3.allocate()", "m3.deallocate()", "setFinished()", "op_61")); // op62
		addOperation(getAnOperation("op_63", "m5.isAvailable()", "m5", 9, false, "m5.allocate()", "m5.deallocate()", "setFinished()", "op_62")); // op63
		addOperation(getAnOperation("op_64", "m0.isAvailable()", "m0", 10, false, "m0.allocate()", "m0.deallocate()", "setFinished()", "op_63")); // op64
		addOperation(getAnOperation("op_65", "m4.isAvailable()", "m4", 4, false, "m4.allocate()", "m4.deallocate()", "setFinished()", "op_64")); // op65
		addOperation(getAnOperation("op_66", "m2.isAvailable()", "m2", 1, true,  "m2.allocate()", "m2.deallocate()", "setFinished()", "op_65")); // op66
		
	}
	
	public void buildExample_66_noBuffer()
	{
		description = "Modified Fish & Thompson 6 * 6: no buffer";
		
		addResource("m0", "1");
		addResource("m1", "1");
		addResource("m2", "1");
		addResource("m3", "1");
		addResource("m4", "1");
		addResource("m5", "1");
		
		addOperation(getAnOperation("op_11", "m0.isAvailable()", "m0", 1, false, "m0.allocate()", "m0.deallocate()", "setFinished()", null)); // op11
		addOperation(getAnOperation("op_12", "m1.isAvailable()", "m1", 3, false, "m1.allocate()", "m1.deallocate()", "setFinished()", "op_11")); // op12
		addOperation(getAnOperation("op_13", "m2.isAvailable()", "m2", 6, false, "m2.allocate()", "m2.deallocate()", "setFinished()", "op_12")); // op13
		addOperation(getAnOperation("op_14", "m3.isAvailable()", "m3", 7, false, "m3.allocate()", "m3.deallocate()", "setFinished()", "op_13")); // op14
		addOperation(getAnOperation("op_15", "m4.isAvailable()", "m4", 3, false, "m4.allocate()", "m4.deallocate()", "setFinished()", "op_14")); // op15
		addOperation(getAnOperation("op_16", "m5.isAvailable()", "m5", 6, true,  "m5.allocate()", "m5.deallocate()", "setFinished()", "op_15")); // op16
		
		addOperation(getAnOperation("op_21", "m5.isAvailable()", "m5", 8, false, "m5.allocate()", "m5.deallocate()", "setFinished()", null)); // op21
		addOperation(getAnOperation("op_22", "m4.isAvailable()", "m4", 5, false, "m4.allocate()", "m4.deallocate()", "setFinished()", "op_21")); // op22
		addOperation(getAnOperation("op_23", "m3.isAvailable()", "m3", 10, false, "m3.allocate()", "m3.deallocate()", "setFinished()", "op_22")); // op23
		addOperation(getAnOperation("op_24", "m2.isAvailable()", "m2", 10, false, "m2.allocate()", "m2.deallocate()", "setFinished()", "op_23")); // op24
		addOperation(getAnOperation("op_25", "m1.isAvailable()", "m1", 10, false, "m1.allocate()", "m1.deallocate()", "setFinished()", "op_24")); // op25
		addOperation(getAnOperation("op_26", "m0.isAvailable()", "m0", 4, true,  "m0.allocate()", "m0.deallocate()", "setFinished()", "op_25")); // op26
		
		addOperation(getAnOperation("op_31", "m2.isAvailable()", "m2", 5, false, "m2.allocate()", "m2.deallocate()", "setFinished()", null)); // op31
		addOperation(getAnOperation("op_32", "m3.isAvailable()", "m3", 4, false, "m3.allocate()", "m3.deallocate()", "setFinished()", "op_31")); // op32
		addOperation(getAnOperation("op_33", "m5.isAvailable()", "m5", 8, false, "m5.allocate()", "m5.deallocate()", "setFinished()", "op_32")); // op33
		addOperation(getAnOperation("op_34", "m0.isAvailable()", "m0", 9, false, "m0.allocate()", "m0.deallocate()", "setFinished()", "op_33")); // op34
		addOperation(getAnOperation("op_35", "m1.isAvailable()", "m1", 1, false, "m1.allocate()", "m1.deallocate()", "setFinished()", "op_34")); // op35
		addOperation(getAnOperation("op_36", "m4.isAvailable()", "m4", 7, true,  "m4.allocate()", "m4.deallocate()", "setFinished()", "op_35")); // op36
		
		addOperation(getAnOperation("op_41", "m1.isAvailable()", "m1", 5, false, "m1.allocate()", "m1.deallocate()", "setFinished()", null)); // op41
		addOperation(getAnOperation("op_42", "m0.isAvailable()", "m0", 5, false, "m0.allocate()", "m0.deallocate()", "setFinished()", "op_41")); // op42
		addOperation(getAnOperation("op_43", "m2.isAvailable()", "m2", 5, false, "m2.allocate()", "m2.deallocate()", "setFinished()", "op_42")); // op43
		addOperation(getAnOperation("op_44", "m3.isAvailable()", "m3", 3, false, "m3.allocate()", "m3.deallocate()", "setFinished()", "op_43")); // op44
		addOperation(getAnOperation("op_45", "m4.isAvailable()", "m4", 8, false, "m4.allocate()", "m4.deallocate()", "setFinished()", "op_44")); // op45
		addOperation(getAnOperation("op_46", "m5.isAvailable()", "m5", 9, true,  "m5.allocate()", "m5.deallocate()", "setFinished()", "op_45")); // op46
		
		addOperation(getAnOperation("op_51", "m2.isAvailable()", "m2", 9, false, "m2.allocate()", "m2.deallocate()", "setFinished()", null)); // op51
		addOperation(getAnOperation("op_52", "m1.isAvailable()", "m1", 3, false, "m1.allocate()", "m1.deallocate()", "setFinished()", "op_51")); // op52
		addOperation(getAnOperation("op_53", "m4.isAvailable()", "m4", 5, false, "m4.allocate()", "m4.deallocate()", "setFinished()", "op_52")); // op53
		addOperation(getAnOperation("op_54", "m5.isAvailable()", "m5", 4, false, "m5.allocate()", "m5.deallocate()", "setFinished()", "op_53")); // op54
		addOperation(getAnOperation("op_55", "m0.isAvailable()", "m0", 3, false, "m0.allocate()", "m0.deallocate()", "setFinished()", "op_54")); // op55
		addOperation(getAnOperation("op_56", "m3.isAvailable()", "m3", 1, true,  "m3.allocate()", "m3.deallocate()", "setFinished()", "op_55")); // op56
		
		addOperation(getAnOperation("op_61", "m1.isAvailable()", "m1", 3, false, "m1.allocate()", "m1.deallocate()", "setFinished()", null)); // op61
		addOperation(getAnOperation("op_62", "m3.isAvailable()", "m3", 3, false, "m3.allocate()", "m3.deallocate()", "setFinished()", "op_61")); // op62
		addOperation(getAnOperation("op_63", "m5.isAvailable()", "m5", 9, false, "m5.allocate()", "m5.deallocate()", "setFinished()", "op_62")); // op63
		addOperation(getAnOperation("op_64", "m0.isAvailable()", "m0", 10, false, "m0.allocate()", "m0.deallocate()", "setFinished()", "op_63")); // op64
		addOperation(getAnOperation("op_65", "m4.isAvailable()", "m4", 4, false, "m4.allocate()", "m4.deallocate()", "setFinished()", "op_64")); // op65
		addOperation(getAnOperation("op_66", "m2.isAvailable()", "m2", 1, true,  "m2.allocate()", "m2.deallocate()", "setFinished()", "op_65")); // op66
		
	}

	public static void main(String[] args)
		throws Exception
	{
		Example_FT example_2_3 = new Example_FT();
		example_2_3.buildExample_23();

		System.out.println(example_2_3.getDescription());
		AssemblyOptimizer app_2_3 = new AssemblyOptimizer(example_2_3.getOperationSet(), example_2_3.getResourceSet());
		app_2_3.parse(args);
		app_2_3.computeMinimalCost();
		System.out.println("The minimal cost time is " + app_2_3.getMinCost());
		
		Example_FT example_6_6 = new Example_FT();
		example_6_6.buildExample_66();
	

		System.out.println(example_6_6.getDescription());
		AssemblyOptimizer app_6_6 = new AssemblyOptimizer(example_6_6.getOperationSet(), example_6_6.getResourceSet());
		app_6_6.parse(args);
		app_6_6.computeMinimalCost();
		System.out.println("The minimal cost time is " + app_6_6.getMinCost());
		

		Example_FT example_6_6_noBuffer = new Example_FT();
		example_6_6_noBuffer.buildExample_66_noBuffer();
		
		System.out.println(example_6_6_noBuffer.getDescription());
		AssemblyOptimizer app_6_6_noBuffer = new AssemblyOptimizer(example_6_6_noBuffer.getOperationSet(), example_6_6_noBuffer.getResourceSet());
		app_6_6_noBuffer.parse(args);
		app_6_6_noBuffer.computeMinimalCost();
		System.out.println("The minimal cost time is " + app_6_6_noBuffer.getMinCost());
	}

}