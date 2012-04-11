package org.supremica.external.assemblyOptimizer;

import org.supremica.external.assemblyOptimizer.AssemblyStructureProtos.Operation;
import java.util.List;


/**
 * @author Zhennan, Knut
 *
**/

public interface GenericOperation extends Comparable<GenericOperation>{ 
   
	void setName(String name);
	String getName();
	void setCostTime(int costTime);
	int getCostTime();
	
	boolean evaluateGuard ();

	void performEnterActions ();
	void performExitActions ();
	
	boolean isFinished();
        boolean isExecute();
	void resetFinished();
	void setFinished();
	
	int getRemainingTime();
	void setRemainingTime(int time);
	void resetRemainingTime();
	
	boolean isTerminal();
	void setTerminal(boolean terminal);
	
	int getStartingTime();
	void setStartingTime(int time);
	
	boolean evaluateTerminalCondition();
        public int[] getCurrentState();
        
    //void resetVariables();
	
}