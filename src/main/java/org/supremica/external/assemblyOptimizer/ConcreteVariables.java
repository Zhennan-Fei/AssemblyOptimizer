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










