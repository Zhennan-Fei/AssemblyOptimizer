/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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

    public TheBuilder(List<AssemblyStructureProtos.Operation> operationSet, List<AssemblyStructureProtos.Resource> resourceSet) {
        	
        this.operationSet = operationSet;
        this.resourceSet = resourceSet;		

        genericResourceList = new ArrayList<GenericResource>(resourceSet.size());
        genericOperationList = new ArrayList<GenericOperation>(operationSet.size());

        nameToResourceMap = new HashMap<String, GenericResource>(resourceSet.size());
        nameToOperationMap = new HashMap<String, GenericOperation>(operationSet.size());
        genericOperationToOperation = new HashMap<GenericOperation, AssemblyStructureProtos.Operation>(operationSet.size());

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
