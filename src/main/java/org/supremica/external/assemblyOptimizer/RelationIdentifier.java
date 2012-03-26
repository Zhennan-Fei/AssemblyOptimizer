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
    private Map<GenericOperation, Map<GenericOperation,Set<Integer>>> uppEventMap = null;
    private Random random = new Random();
    
    private int[][] relMap;
    private Map<GenericOperation, Integer> operationPosMap;
    
    private List<AssemblyOptimizer.ConcurrentProcess> currentBestSeq;

    
    public RelationIdentifier(TheBuilder builder) {      	
        this.builder = builder;
        relMap = new int[builder.genericOperationList.size()][builder.genericOperationList.size()];
        operationPosMap = new HashMap<GenericOperation,Integer>();
        for (int i = 0; i<builder.genericOperationList.size()-1; i++){
            operationPosMap.put(builder.genericOperationList.get(i),new Integer(i));
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
        Map<GenericOperation, Map<GenericOperation,Set<Integer>>> 
                eventMap = new HashMap<GenericOperation, Map<GenericOperation,Set<Integer>>>();
        prepareEventMap(eventMap);      
        int prevHC;
        int sameRelCounter = 0;
        int sameBestPathCounter = 0;
        
        List<GenericOperation> bestSolution = null;
	List<GenericOperation> tempSolution = new LinkedList<GenericOperation>();
        LinkedList<AssemblyOptimizer.ConcurrentProcess> tempProcessList = null;
	int minCost = Integer.MAX_VALUE;	
        
        for (int i = 0 ; i<1000;i++){
            Map<GenericOperation, Map<GenericOperation,Set<Integer>>> 
                    tempUppEventMap = new HashMap<GenericOperation, Map<GenericOperation,Set<Integer>>>();
            prepareEventMap(tempUppEventMap); 
            
            
            if (findAPath(tempUppEventMap, tempSolution)){
                tempProcessList = AssemblyOptimizer.getMinimalCostToTheSolution(tempSolution);                
                int tempCost = tempProcessList.getLast().getPossibleStartingTimeForNextOperation();
			
                if (tempCost < minCost){
                    bestSolution = tempSolution;
                    currentBestSeq = tempProcessList;
                    sameBestPathCounter = 0;
                    minCost = tempCost;
                } else sameBestPathCounter++;
                
                if (!mergeMaps(eventMap, tempUppEventMap))sameRelCounter++;
                else sameRelCounter = 0;             

                if (sameRelCounter > 100 && sameBestPathCounter > 100) 
                    break;           
            }
        }
       
        this.uppEventMap = eventMap;
        //printEventMap(uppEventMap);
    }
    
    
    private boolean findAPath(Map<GenericOperation, Map<GenericOperation,Set<Integer>>> tempUppEventMap, List<GenericOperation> tempSolution){
        Set<GenericOperation> opsToExecute = new HashSet<GenericOperation>();
        ConcreteVariables.getInstance().resetVariables();
        tempSolution.clear();
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
                updateEventMap(enabledOps,tempUppEventMap);   
                GenericOperation exec = enabledOps.get(random.nextInt(enabledOps.size()-1));
                opsToExecute.remove(exec);
                exec.performEnterActions();
                exec.performExitActions();
                tempSolution.add(exec);
                if (exec.evaluateTerminalCondition()) return true;
            } else {
                return false;
            }            
        }
    }
    
    private void updateEventMap(List<GenericOperation> enabledOps, Map<GenericOperation, Map<GenericOperation,Set<Integer>>> tempUppEventMap){        
        Map<GenericOperation,Integer> state = createState();
        
        for (GenericOperation o : enabledOps){
            Map<GenericOperation,Set<Integer>> opMap = tempUppEventMap.get(o);
            for (Map.Entry<GenericOperation,Set<Integer>> e : opMap.entrySet()){
                e.getValue().add(state.get(e.getKey()));
            }
        }
    }
    
    private Map<GenericOperation,Integer> createState(){
        Map<GenericOperation,Integer> result = new HashMap<GenericOperation,Integer>();
        for (GenericOperation o : builder.genericOperationList){
            Integer i;
            if (o.isFinished()) {
                i = new Integer(2);
            }else 
                i = new Integer(0);
            result.put(o, i);
        }
        return result;
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
        if (uppEventMap == null) return;
        
        for (int i = 0; i<builder.genericOperationList.size()-2; i++){
            for (int j = i+1; j<builder.genericOperationList.size()-1; j++){
                GenericOperation sourceOp = builder.genericOperationList.get(i);
                GenericOperation targetOp = builder.genericOperationList.get(j);
                Set<Integer> sourceStates = uppEventMap.get(targetOp).get(sourceOp);
                Set<Integer> targetStates = uppEventMap.get(sourceOp).get(targetOp);
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
