package org.supremica.external.assemblyOptimizer;

import org.supremica.external.assemblyOptimizer.AssemblyStructureProtos.Resource;

/**
 * @author Zhennan, Knut
 *
**/

public interface GenericResource {
	
	void setCapacity(int capacity);
	
	boolean isAvailable();
	
	boolean isAvailable(int units);
	
	void allocate();
	
	void allocate(int units);
	
	void deallocate();
	
	void deallocate(int units);
	
	int getAvailable();
	
	int getCapacity();
}