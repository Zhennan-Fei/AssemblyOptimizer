package org.supremica.external.assemblyOptimizer;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;

// The class maintains a set global variables read and written by operations.
// Note that right now the types only support for integers. It works right now,
// since all of the variables (parts) in the example is integers. It should support
// all of numbers later.

public class ConcreteVariables{
	
	private static ConcreteVariables instance = null;
	
	// keep track of the initial values for each variable
	private Map<String, Integer> varNameToDefaultValueMap;
	
	private Map<String, Integer> varNameToValueMap; 
	
	private ConcreteVariables()
	{
		varNameToValueMap = new HashMap<String, Integer>();
		varNameToDefaultValueMap = new HashMap<String, Integer>();
	}
	
	public static ConcreteVariables getInstance()
	{
		if(instance == null)
		{
			instance = new ConcreteVariables();
		}
		
		return instance;
	}
	
	public void addVarName(String varName, Integer value)
	{
		if(!varNameToValueMap.containsKey(varName))
		{
			varNameToValueMap.put(varName, value);
			varNameToDefaultValueMap.put(varName, value);
		}
	}
	
	public int getValueForVarName(String varName)
	{
		return varNameToValueMap.get(varName);
	}
	
	public void setValueForVarName(String varName, int value)
	{
		varNameToValueMap.put(varName, value);
	}
	
	public Set<String> getVarNameSet()
	{
		return varNameToValueMap.keySet();
	}
	
	public boolean isEmpty()
	{
		return varNameToValueMap.isEmpty();
	}
	
	public void resetVariables()
	{
		varNameToValueMap.clear();
		for(Map.Entry<String, Integer> entry: varNameToDefaultValueMap.entrySet())
		{
			String varName = entry.getKey();
			Integer defaultValue = entry.getValue();
			varNameToValueMap.put(varName, defaultValue);
		}
	}
        public void printVarValues(){
            for(String id: varNameToValueMap.keySet())
		{
			System.out.println(id+"=="+varNameToValueMap.get(id).toString());
		}
        }
}










