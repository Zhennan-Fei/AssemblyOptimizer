/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.supremica.external.assemblyOptimizer;

import java.util.*;
import org.supremica.external.assemblyOptimizer.AssemblyStructureProtos.Operation;

/**
 *
 * @author kbe
 */
public class RelationIdentifier3StateOps {
    
    private TheBuilder builder;
    private int noOfIterations = 1000;
    private int breakAfterSameValue = 500;
    private Map<GenericOperation, Integer> operationPosMap;  
    private List<List<Set<Integer>>> uppEventList = null;
    private List<List<Set<Integer>>> downEventList = null;
    
    private Random random = new Random();   
    private int[][] relMap;  // ev change this to list?   
    private List<AssemblyOptimizer.ConcurrentProcess> currentBestSeq;
    
    private boolean pathFound = false;
    private boolean uppEventUpdated = false;
    private boolean downEventUpdated = false;
    
    private boolean relCalculated = false;
    
    private Set<GenericOperation> didNotComplete;

    
    public RelationIdentifier3StateOps(TheBuilder builder, int noOfIterations, int breakAfterSameValue) {      	
        this.builder = builder;
        this.noOfIterations = noOfIterations;
        this.breakAfterSameValue = breakAfterSameValue;
        didNotComplete = new HashSet<GenericOperation>();
        
        relMap = new int[builder.genericOperationList.size()][builder.genericOperationList.size()];
        operationPosMap = new HashMap<GenericOperation,Integer>();
        uppEventList = new ArrayList<List<Set<Integer>>>(builder.genericOperationList.size());
        downEventList = new ArrayList<List<Set<Integer>>>(builder.genericOperationList.size());
        for (int i = 0; i<builder.genericOperationList.size(); i++){
            operationPosMap.put(builder.genericOperationList.get(i),new Integer(i));
            List<Set<Integer>> l = new ArrayList<Set<Integer>>(builder.genericOperationList.size());
            for (int j = 0; j<builder.genericOperationList.size(); j++){ 
                l.add(new HashSet<Integer>());
            }
            uppEventList.add(l);
            downEventList.add(l);
        }              
    }
    
    public void createRelationMap(){
        createEventMap();
        transformEventsToRelations();
        relCalculated = true;
    }
    
    
//    public List<Operation> getOptimizedOperationList() {
//        List<Operation> result = new ArrayList<Operation>(); 
//        for(AssemblyOptimizer.ConcurrentProcess process: this.currentBestSeq){
//            for(Map.Entry<Integer, GenericOperation> e: process.getStartingTimeToOperation().entrySet()){
//                Operation o = builder.genericOperationToOperation.get(e.getValue());
//                result.add(AssemblyOptimizer.buildOperationWithStartingTime(o,e.getKey()));
//            }    
//        }
//        return result;
//    }
    
    // Fix so we use guid and enum for relations and for name matching
    public int getRelation(String source, String target){
        if (!relCalculated) createRelationMap(); 
        if (source.equals(target)) return -1;
        GenericOperation sourceOp = builder.nameToGenericOperationMap.get(source);
        GenericOperation targetOp = builder.nameToGenericOperationMap.get(target);
        if (sourceOp != null && targetOp != null){
            int sourcePos = this.operationPosMap.get(sourceOp);
            int targetPos = this.operationPosMap.get(targetOp);
            return this.relMap[sourcePos][targetPos];
        }
        return -1;
        
    }
    
    
    private void createEventMap(){    
        int sameRelCounter = 0;	
        boolean onePathFound = false;
        
        for (int i = 0 ; i<noOfIterations;i++){     
            List<List<Set<Integer>>> uEclone = createClone(uppEventList);
            List<List<Set<Integer>>> dEclone = createClone(downEventList);
            
            findAPath(uppEventList, downEventList);
            if (this.pathFound){   
                onePathFound = true;
                if (!this.uppEventUpdated && !this.downEventUpdated )sameRelCounter++;
                else {
                    sameRelCounter = 0;
                }             

                if (sameRelCounter > breakAfterSameValue){ 
                    System.out.println("Break due to the same value!");
                    System.out.println("Same relations counter: " + sameRelCounter);
                    System.out.println("no of iterations:" + i);
                    break; 
                }
            } else {
                uppEventList = uEclone;
                downEventList = dEclone;
            }
        }
        if (!onePathFound){
            System.out.println("No path was found! The following operations did not complete:");
            for (GenericOperation o : this.didNotComplete){
                System.out.println(o.getName());
            }
        }
    }
    
    
    private boolean findAPath(List<List<Set<Integer>>> lUppEvent, List<List<Set<Integer>>> lDownEvent){
        Set<GenericOperation> opsToExecute = new HashSet<GenericOperation>();
        Set<GenericOperation> opsInExecute = new HashSet<GenericOperation>();
        this.uppEventUpdated = false;
        this.downEventUpdated = false;
        
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
            enabledOps.addAll(opsInExecute);
            if (!enabledOps.isEmpty()){
                this.uppEventUpdated = updateEventMap(enabledOps,lUppEvent) || uppEventUpdated; 
                this.downEventUpdated = updateEventMap(enabledOps,lDownEvent) || downEventUpdated;
                int r = 0;
                if (enabledOps.size()>1)
                    r = random.nextInt(enabledOps.size());
                GenericOperation exec = enabledOps.get(r);
                if (exec.isExecute()){
                    exec.performExitActions();
                    opsInExecute.remove(exec);
                } else {
                    exec.performEnterActions();
                    opsToExecute.remove(exec);
                    opsInExecute.add(exec);
                }

                if (exec.evaluateTerminalCondition()) {
                    this.pathFound = true;
                    return true;
                }
            } else {
                if (opsToExecute.isEmpty()){
                    // All operations have executed whitout termination
                    // for now assume that this defines true termination
                    this.pathFound = true;
                    return true;
                }
                this.pathFound = false;
                didNotComplete.addAll(opsToExecute);
                return false;
            }            
        }
    }
    
    private boolean updateEventMap(List<GenericOperation> enabledOps, List<List<Set<Integer>>> eventList){        
        int[] state = createState();
        boolean result = false;
        for (GenericOperation o : enabledOps){
            int opPos = this.operationPosMap.get(o).intValue();
            for (int i = 0; i<builder.genericOperationList.size(); i++){
                result = eventList.get(opPos).get(i).add(state[i]) || result;
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
            }else  if (o.isExecute()){
                i = new Integer(1);
            } else
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
    

    
    
    private void transformEventsToRelations(){
        if (this.uppEventList == null) return;       
        for (int i = 0; i<builder.genericOperationList.size()-1; i++){
            for (int j = i+1; j<builder.genericOperationList.size(); j++){
                int sourcePos = this.operationPosMap.get(builder.genericOperationList.get(i));
                int targetPos = this.operationPosMap.get(builder.genericOperationList.get(j));
                
                Set<Integer> targetUppEsourceStates = uppEventList.get(targetPos).get(sourcePos);
                Set<Integer> sourceUppEtargetStates = uppEventList.get(sourcePos).get(targetPos);
                Set<Integer> targetDownEsourceStates = downEventList.get(targetPos).get(sourcePos);
                Set<Integer> sourceDownEtargetStates = downEventList.get(sourcePos).get(targetPos);
                
                
                this.relMap[i][j] = getRelNo(targetUppEsourceStates,sourceUppEtargetStates,targetDownEsourceStates,sourceDownEtargetStates);
                this.relMap[j][i] = getRelNo(sourceUppEtargetStates, targetUppEsourceStates,sourceDownEtargetStates,targetDownEsourceStates);
            }
            this.relMap[i][i] = -1;
        }
    }
    
    // fix this to a enum
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

    // targetUppEsourceStates,sourceUppEtargetStates,targetDownEsourceStates,sourceDownEtargetStates
    private int getRelNo(Set<Integer> targetUppEsourceStates, Set<Integer> sourceUppEtargetStates,
                         Set<Integer> targetDownEsourceStates, Set<Integer> sourceDownEtargetStates ) {
        
        boolean tUEsInit = targetUppEsourceStates.contains(new Integer(0));
        boolean tUEsExec = targetUppEsourceStates.contains(new Integer(1));
        boolean tUEsFin = targetUppEsourceStates.contains(new Integer(2));
        
        boolean tDEsInit = targetDownEsourceStates.contains(new Integer(0));
        boolean tDEsExec = targetDownEsourceStates.contains(new Integer(1));
        boolean tDEsFin = targetDownEsourceStates.contains(new Integer(2));
        
        boolean sUEtInit = sourceUppEtargetStates.contains(new Integer(0));
        boolean sUEtExec = sourceUppEtargetStates.contains(new Integer(1));
        boolean sUEtFin = sourceUppEtargetStates.contains(new Integer(2));
        
        boolean sDEtInit = sourceDownEtargetStates.contains(new Integer(0));
        boolean sDEtExec = sourceDownEtargetStates.contains(new Integer(1));
        boolean sDEtFin = sourceDownEtargetStates.contains(new Integer(2));
        
        
        if ((sUEtInit && sUEtExec && sUEtFin) &&
            (sDEtInit && sDEtExec && sDEtFin) &&
            (tUEsInit && tUEsExec && tUEsFin) &&
            (tDEsInit && tDEsExec && tDEsFin) 
                ) return PARALLEL;
        
        if ((sUEtInit && !sUEtExec && !sUEtFin) &&
            (sDEtInit && !sDEtExec && !sDEtFin) &&
            (!tUEsInit && !tUEsExec && tUEsFin) &&
            (!tDEsInit && !tDEsExec && tDEsFin) 
                ) return ALWAYS_IN_SEQUENCE_12;
        
        if ((!sUEtInit && !sUEtExec && sUEtFin) &&
            (!sDEtInit && !sDEtExec && sDEtFin) &&
            (tUEsInit && !tUEsExec && !tUEsFin) &&
            (tDEsInit && !tDEsExec && !tDEsFin) 
                ) return ALWAYS_IN_SEQUENCE_21;
        
        if ((sUEtInit && !sUEtExec && !sUEtFin) &&
            (sDEtInit && !sDEtExec && !sDEtFin) &&
            (tUEsInit && !tUEsExec && tUEsFin) &&
            (tDEsInit && !tDEsExec && tDEsFin) 
                ) return SOMETIMES_IN_SEQUENCE_12;
        
        if ((sUEtInit && !sUEtExec && sUEtFin) &&
            (sDEtInit && !sDEtExec && sDEtFin) &&
            (tUEsInit && !tUEsExec && !tUEsFin) &&
            (tDEsInit && !tDEsExec && !tDEsFin) 
                ) return SOMETIMES_IN_SEQUENCE_21;
        
        if ((sUEtInit && !sUEtExec && !sUEtFin) &&
            (sDEtInit && !sDEtExec && !sDEtFin) &&
            (tUEsInit && !tUEsExec && !tUEsFin) &&
            (tDEsInit && !tDEsExec && !tDEsFin) 
                ) return ALTERNATIVE;
        
        if ((sUEtInit && !sUEtExec && sUEtFin) &&
            (sDEtInit && !sDEtExec && sDEtFin) &&
            (tUEsInit && !tUEsExec && tUEsFin) &&
            (tDEsInit && !tDEsExec && tDEsFin) 
                ) return ARBITRARY_ORDER;
        
        if ((sUEtInit && !sUEtExec && !sUEtFin) &&
            (!sDEtInit && !sDEtExec && sDEtFin) &&
            (!tUEsInit && tUEsExec && !tUEsFin) &&
            (!tDEsInit && tDEsExec && !tDEsFin) 
                ) return HIERARCHY_12;
        
        if ((!sUEtInit && sUEtExec && !sUEtFin) &&
            (!sDEtInit && sDEtExec && !sDEtFin) &&
            (tUEsInit && !tUEsExec && !tUEsFin) &&
            (!tDEsInit && !tDEsExec && tDEsFin) 
                ) return HIERARCHY_21;
        
        if ((sUEtInit && !sUEtExec && !sUEtFin) &&
            (sDEtInit && !sDEtExec && sDEtFin) &&
            (!tUEsInit && tUEsExec && !tUEsFin) &&
            (!tDEsInit && tDEsExec && !tDEsFin) 
                ) return SOMETIMES_IN_HIERARCHY_12;
        
        if ((!sUEtInit && sUEtExec && !sUEtFin) &&
            (!sDEtInit && sDEtExec && !sDEtFin) &&
            (tUEsInit && !tUEsExec && !tUEsFin) &&
            (tDEsInit && !tDEsExec && tDEsFin) 
                ) return SOMETIMES_IN_HIERARCHY_21;
        
        return OTHER;
        
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
    
    
    
}
