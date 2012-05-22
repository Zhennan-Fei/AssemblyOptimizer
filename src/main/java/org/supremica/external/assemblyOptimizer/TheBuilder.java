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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author kbe
 */
public class TheBuilder {
    
    private List<AssemblyStructureProtos.Operation> operationSet;
    private List<AssemblyStructureProtos.Resource> resourceSet;
    private GenericResourceBuilder resourceBuilder;
    private GenericOperationBuilder operationBuilder;
    public List<GenericOperation> genericOperationList;
    public List<GenericResource> genericResourceList;
	
    private Map<String, GenericResource> nameToResourceMap;
    private Map<String, GenericOperation> nameToOperationMap;
    public Map<GenericOperation, AssemblyStructureProtos.Operation> genericOperationToOperation;
    public Map<AssemblyStructureProtos.Operation, GenericOperation> operationToGenericOperation;
    public Map<String,GenericOperation> nameToGenericOperationMap;

    public TheBuilder(List<AssemblyStructureProtos.Operation> operationSet, List<AssemblyStructureProtos.Resource> resourceSet) {
        	
        this.operationSet = operationSet;
        this.resourceSet = resourceSet;		

        genericResourceList = new ArrayList<GenericResource>(resourceSet.size());
        genericOperationList = new ArrayList<GenericOperation>(operationSet.size());

        nameToResourceMap = new HashMap<String, GenericResource>(resourceSet.size());
        nameToOperationMap = new HashMap<String, GenericOperation>(operationSet.size());
        genericOperationToOperation = new HashMap<GenericOperation, AssemblyStructureProtos.Operation>(operationSet.size());
        operationToGenericOperation = new HashMap<AssemblyStructureProtos.Operation, GenericOperation>();
        nameToGenericOperationMap = new HashMap<String, GenericOperation>();
        
        resourceBuilder = new GenericResourceBuilder();
        operationBuilder = new GenericOperationBuilder();
        
        constructGenericCounterpart();
    }
    
    	private void constructGenericCounterpart()
	{
		// from the operations and resources, build corresponding generic operations and resources
		for(AssemblyStructureProtos.Resource resource: resourceSet) {
			GenericResource genericResource = resourceBuilder.newGenericResource(resource);
			genericResourceList.add(genericResource);
			// capacity could be set here.
			nameToResourceMap.put(resource.getName(), genericResource);
		}
		
		for(AssemblyStructureProtos.Operation operation: operationSet) {
			GenericOperation genericOperation = operationBuilder.newGenericOperation(operation);
			genericOperation.setName(operation.getName());
			genericOperation.setCostTime(operation.getCostTime());
			genericOperation.setTerminal(operation.getTerminal());
			genericOperationToOperation.put(genericOperation, operation);
                        operationToGenericOperation.put(operation, genericOperation);
                        nameToGenericOperationMap.put(operation.getName(), genericOperation);
			// set resources 
			Class operationClass = genericOperation.getClass();
			Method setMethod = null;
			for(String useResourceName: operation.getUseResourceList()){
                            try{
				setMethod = operationClass.getMethod("setGenericResource_"+useResourceName, 
					Class.forName("org.supremica.external.assemblyOptimizer.GenericResource"));
				setMethod.invoke(genericOperation, nameToResourceMap.get(useResourceName));
                            }catch(NoSuchMethodException e){
                                System.out.println("NoSuchMethodException:"+e.getMessage());
                                return;
                            }catch(IllegalAccessException e){
                                System.out.println("IllegalAccessException:"+e.getMessage());
                                return;
                            }catch(ClassNotFoundException e){
                                System.out.println("ClassNotFoundException:"+e.getMessage());
                                return;
                            }catch(InvocationTargetException e){
                                System.out.println("InvocationTargetException:"+e.getMessage());
                                return;
                            }
                            
                        }
			genericOperationList.add(genericOperation);
			nameToOperationMap.put(operation.getName(), genericOperation);
		}
		
		
		for(AssemblyStructureProtos.Operation operation: operationSet){
			
			GenericOperation genericOperation = nameToOperationMap.get(operation.getName());
			Class operationClass = genericOperation.getClass();
			
			Method setOperationMethod = null;
			for(String relatedOp: operation.getStartAfterOperationsList()){
                            try{
				setOperationMethod = operationClass.getMethod("setGenericOperation_" + relatedOp,
				 	Class.forName("org.supremica.external.assemblyOptimizer.GenericOperation"));
				setOperationMethod.invoke(genericOperation, nameToOperationMap.get(relatedOp));
                           }catch(NoSuchMethodException e){
                                System.out.println("NoSuchMethodException:"+e.getMessage());
                                return;
                            }catch(IllegalAccessException e){
                                System.out.println("IllegalAccessException:"+e.getMessage());
                                return;
                            }catch(ClassNotFoundException e){
                                System.out.println("ClassNotFoundException:"+e.getMessage());
                                return;
                            }catch(InvocationTargetException e){
                                System.out.println("InvocationTargetException:"+e.getMessage());
                                return;
                            }
                        }
		} 
		
	}
    
}
