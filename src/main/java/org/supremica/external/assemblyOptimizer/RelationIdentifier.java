/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.supremica.external.assemblyOptimizer;

import java.util.*;
import org.supremica.external.assemblyOptimizer.AssemblyStructureProtos.Operation;
import org.supremica.external.assemblyOptimizer.AssemblyStructureProtos.Resource;

/**
 *
 * @author kbe
 */
public class RelationIdentifier {
    
    private TheBuilder builder;
    private int noOfIterations = 1000;
    private int breakAfterSameValue = 500;
    private Map<GenericOperation, Integer> operationPosMap;  
    private List<List<Set<Integer>>> uppEventList = null;
    
    private Random random = new Random();   
    private int[][] relMap;  // ev change this to list?   
    private List<AssemblyOptimizer.ConcurrentProcess> currentBestSeq;
    
    private boolean pathFound = false;
    private boolean uppEventUpdated = false;

    
    public RelationIdentifier(TheBuilder builder, int noOfIterations, int breakAfterSameValue) {      	
        this.builder = builder;
        this.noOfIterations = noOfIterations;
        this.breakAfterSameValue = breakAfterSameValue;
        
        relMap = new int[builder.genericOperationList.size()][builder.genericOperationList.size()];
        operationPosMap = new HashMap<GenericOperation,Integer>();
        uppEventList = new ArrayList<List<Set<Integer>>>(builder.genericOperationList.size());
        for (int i = 0; i<builder.genericOperationList.size(); i++){
            operationPosMap.put(builder.genericOperationList.get(i),new Integer(i));
            List<Set<Integer>> l = new ArrayList<Set<Integer>>(builder.genericOperationList.size());
            for (int j = 0; j<builder.genericOperationList.size(); j++){ 
                l.add(new HashSet<Integer>());
            }
            uppEventList.add(l);
        }              
    }
    
    public void createRelationMap(){
        createEventMap();
        transformEventsToRelations();
    }
    
    
    public List<Operation> getOptimizedOperationList() {
        List<Operation> result = new ArrayList<Operation>(); 
        for(AssemblyOptimizer.ConcurrentProcess process: this.currentBestSeq){
            for(Map.Entry<Integer, GenericOperation> e: process.getStartingTimeToOperation().entrySet()){
                Operation o = builder.genericOperationToOperation.get(e.getValue());
                result.add(AssemblyOptimizer.buildOperationWithStartingTime(o,e.getKey()));
            }    
        }
        return result;
    }
    
    private void createEventMap(){    
        int sameRelCounter = 0;
        int sameBestPathCounter = 0;
        
        List<GenericOperation> bestSolution = null;
	List<GenericOperation> tempSolution = new LinkedList<GenericOperation>();
        LinkedList<AssemblyOptimizer.ConcurrentProcess> tempProcessList = null;
	int minCost = Integer.MAX_VALUE;	
        
        for (int i = 0 ; i<noOfIterations;i++){     
            List<List<Set<Integer>>> clone = createClone(uppEventList);
            findAPath(uppEventList, tempSolution);
            if (this.pathFound){
                tempProcessList = AssemblyOptimizer.getMinimalCostToTheSolution(tempSolution);                
                int tempCost = tempProcessList.getLast().getPossibleStartingTimeForNextOperation();
			
                if (tempCost < minCost){
                    bestSolution = tempSolution;
                    currentBestSeq = tempProcessList;
                    sameBestPathCounter = 0;
                    minCost = tempCost;
                } else sameBestPathCounter++;
                
                if (!this.uppEventUpdated)sameRelCounter++;
                else {
                    sameRelCounter = 0;
                }             

                if (sameRelCounter > breakAfterSameValue && sameBestPathCounter > breakAfterSameValue){ 
                    System.out.println("Break due to the same value!");
                    System.out.println("Same relations counter: " + sameRelCounter);
                    System.out.println("Same best time counter: " + sameBestPathCounter);
                    System.out.println("no of iterations:" + i);
                    break; 
                }
            } else {
                uppEventList = clone;
            }
        }
    }
    
    
    private boolean findAPath(List<List<Set<Integer>>> lUppEventList, List<GenericOperation> tempSolution){
        Set<GenericOperation> opsToExecute = new HashSet<GenericOperation>();
        //ConcreteVariables.getInstance().resetVariables();
        tempSolution.clear();
        this.uppEventUpdated = false;
        
        for (GenericOperation o : builder.genericOperationList){
            o.resetFinished();
            opsToExecute.add(o);
        }
        
        while(true){
            List<GenericOperation> enabledOps = new ArrayList<GenericOperation>();
            for (GenericOperation o : opsToExecute){
                if (o.evaluateGuard()){
                    enabledOps.add(o);
                }
            }
            if (!enabledOps.isEmpty()){
                this.uppEventUpdated = updateEventMap(enabledOps,lUppEventList) || uppEventUpdated;   
                int r = 0;
                if (enabledOps.size()>1)
                    r = random.nextInt(enabledOps.size()-1);
                GenericOperation exec = enabledOps.get(r);
                opsToExecute.remove(exec);
                exec.performEnterActions();
                exec.performExitActions();
                tempSolution.add(exec);
                if (exec.evaluateTerminalCondition()) {
                    this.pathFound = true;
                    return true;
                }
            } else {
                this.pathFound = false;
                return false;
            }            
        }
    }
    
    private boolean updateEventMap(List<GenericOperation> enabledOps, List<List<Set<Integer>>> lUppEventList){        
        int[] state = createState();
        boolean result = false;
        for (GenericOperation o : enabledOps){
            int opPos = this.operationPosMap.get(o).intValue();
            for (int i = 0; i<builder.genericOperationList.size(); i++){
                result = lUppEventList.get(opPos).get(i).add(state[i]) || result;
            }
        }
        return result;
    }
    
    private int[] createState(){
        int[] result = new int[builder.genericOperationList.size()];
        for (GenericOperation o : builder.genericOperationList){
            int i;
            if (o.isFinished()) {
                i = new Integer(2);
            }else 
                i = new Integer(0);
            int pos = this.operationPosMap.get(o).intValue();
            result[pos] = i;
        }
        return result;
    }
    
    private List<List<Set<Integer>>> createClone(List<List<Set<Integer>>> lUppEventList) {
        List<List<Set<Integer>>> clone = new ArrayList<List<Set<Integer>>>(lUppEventList.size());
        for (List<Set<Integer>> eList : lUppEventList){
            List<Set<Integer>> cloneiList = new ArrayList<Set<Integer>>(eList.size());
            for (Set<Integer> set : eList){
                Set<Integer> cloneSet = new HashSet<Integer>();
                for (Integer i : set)
                    cloneSet.add(new Integer(i.intValue()));
                cloneiList.add(cloneSet);
            }
            clone.add(cloneiList);
        }
        
        return clone;
    }
    
    private void prepareEventMap(Map<GenericOperation, Map<GenericOperation,Set<Integer>>> map){
        map.clear();
        for (GenericOperation o : builder.genericOperationList){
            HashMap<GenericOperation,Set<Integer>> stateMapper 
                    = new HashMap<GenericOperation,Set<Integer>>();
            for (GenericOperation g : builder.genericOperationList){
                stateMapper.put(g, new HashSet<Integer>());
            }
            map.put(o, stateMapper);
        }
    }
    
    private boolean mergeMaps(Map<GenericOperation, Map<GenericOperation,Set<Integer>>> to,
            Map<GenericOperation, Map<GenericOperation,Set<Integer>>> from){
        
        boolean r = false;
        for (Map.Entry<GenericOperation, Map<GenericOperation,Set<Integer>>> e : to.entrySet()){
            for (Map.Entry<GenericOperation,Set<Integer>> f : from.get(e.getKey()).entrySet()){
                r = e.getValue().get(f.getKey()).addAll(f.getValue()) || r;
            }       
        }
        return r;
    }
    
    private void printEventMap(Map<GenericOperation, Map<GenericOperation,Set<Integer>>> map){
        for (Map.Entry<GenericOperation, Map<GenericOperation,Set<Integer>>> e : map.entrySet()){
            String s = e.getKey().getName() + ": ";
            for (Map.Entry<GenericOperation,Set<Integer>> f : e.getValue().entrySet()){
                s += f.getKey().getName() +"<";
                for (Integer i : f.getValue()){
                    s += i.toString();
                }
                s += "> ";
            }
            System.out.println(s);
        }
    }
    
    private void printState(Map<GenericOperation,Integer> state){
        String s="";
        for (Map.Entry<GenericOperation,Integer> e : state.entrySet()){
            s += e.getKey().getName() +":"+e.getValue() +" ";
        }
        System.out.println(s);
    }
    
    
    private void transformEventsToRelations(){
        if (this.uppEventList == null) return;       
        for (int i = 0; i<builder.genericOperationList.size()-1; i++){
            for (int j = i+1; j<builder.genericOperationList.size(); j++){
                int sourcePos = this.operationPosMap.get(builder.genericOperationList.get(i));
                int targetPos = this.operationPosMap.get(builder.genericOperationList.get(j));
                
                Set<Integer> sourceStates = uppEventList.get(targetPos).get(sourcePos);
                Set<Integer> targetStates = uppEventList.get(sourcePos).get(targetPos);
                this.relMap[i][j] = getRelNo(sourceStates,targetStates);
                this.relMap[j][i] = getRelNo(targetStates, sourceStates);
            }
        }
    }
    
    int ALWAYS_IN_SEQUENCE_12 = 0;
    int ALWAYS_IN_SEQUENCE_21 = 1;
    int SOMETIMES_IN_SEQUENCE_12 = 2;
    int SOMETIMES_IN_SEQUENCE_21 = 3;
    int PARALLEL = 4;
    int ALTERNATIVE = 5;
    int ARBITRARY_ORDER = 6;
    int HIERARCHY_12 = 7;
    int HIERARCHY_21 = 8;
    int SOMETIMES_IN_HIERARCHY_12 = 9;
    int SOMETIMES_IN_HIERARCHY_21 = 10;
    int OTHER = 11;

    private int getRelNo(Set<Integer> sourceStates, Set<Integer> targetStates) {
        boolean sInit = sourceStates.contains(new Integer(0));
        //boolean sStart = sourceStates.contains(new Integer(1));
        boolean sFin = sourceStates.contains(new Integer(2));
        
        boolean tInit = targetStates.contains(new Integer(0));
        //boolean tStart = targetStates.contains(new Integer(1));
        boolean tFin = targetStates.contains(new Integer(2));
        
        if (sInit && sFin &&
            tInit && tFin ) return PARALLEL;
        
        if (!sInit && sFin &&
            tInit && !tFin ) return ALWAYS_IN_SEQUENCE_12;
        
        if (sInit && !sFin &&
            !tInit && tFin ) return ALWAYS_IN_SEQUENCE_21;
        
        if (sInit && sFin &&
            tInit && !tFin ) return SOMETIMES_IN_SEQUENCE_12;
        
        if (sInit && !sFin &&
            tInit && tFin ) return SOMETIMES_IN_SEQUENCE_21;
        
        if (sInit && !sFin &&
            tInit && !tFin ) return ALTERNATIVE;
        
        return OTHER;
        
    }


    
    
    
}
