package usecases;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import entities.stateSpace.Relation;
import entities.stateSpace.State;

public class DefaultPathSearchStrategy extends PathSearchStrategy{
		
	private final int MAX_DEPTH = 8;
	
	public DefaultPathSearchStrategy(RelationCreator relationCreator) {
		super(relationCreator);
	}
	
	
	@Override
	public List<Relation> findPath(State rootState) {
			
		init(rootState);
		
		start(rootState);
		
		//RESULT
		List<Relation> results = new ArrayList<Relation>();
		
		State currentState = this.localMinimum;
		while(currentState.getSourceRelation() != null){
			results.add(currentState.getSourceRelation());
			currentState = currentState.getSourceRelation().getFromState();
		}
		
		Collections.reverse(results);
			
		/*//DEBUG
		System.out.println("");
		System.out.println("RESULT");
		for(Relation r : results){
			System.out.println("-------------");
			currentState = r.getFromState();
			System.out.println("S_" + currentState.getId()+ " [ Fitness: " + currentState.getFitness() + ", NumOfSmells: " +currentState.getSmells().size() + ", Depth: " + currentState.getDepth() + "] " + currentState);
			System.out.println(r.getUsedRepair().getName());
			currentState = r.getToState();
			System.out.println("S_" + currentState.getId()+ " [ Fitness: " + currentState.getFitness() + ", NumOfSmells: " +currentState.getSmells().size() + ", Depth: " + currentState.getDepth() + "] " + currentState);
		}
		System.out.println(currentState);
		//DEBUG	*/	
		return results;
	}


	protected void start(State rootState) {
		
		this.lastStateId = 0;
		// add relations from rootState to queue
		this.addRelationsToQueue(rootState.getRelations());
		
		Relation currentRelation = null;
		State currentState = null;
		
		while(!this.queue.isEmpty()){
			
			//get next state for visiting
			currentRelation = this.queue.remove().getRelation();
			currentState = currentRelation.getToState();
			
			
			//Skip the state contains same smells as any of visited state (node)
			if(isVisited(currentState)){
				continue;
			}
			
			currentState.setId(lastStateId++); 
			
			
			//if currentState is better then local minimum
			if(currentState.getFitness() < this.localMinimum.getFitness()){
				
				this.localMinimum = currentState;
				
			}
			
			if(currentState.getDepth() < MAX_DEPTH){
				expandCurrentState(currentState);
			}
			
			//System.out.println(currentState.getDepth() + ", " + currentState.getFitness() + ", " + (this.localMinimum.getDepth()+ this.localMinimum.getFitness()));
			
		}
		
	}	

	protected int calculateHeuristic(Relation r){
		
		int result = 0;	

		result += r.getToState().getFitness();
		result += r.getUsedRepair().getWeight();
		result += r.getToState().getDepth();
		
		return result; 
	}
	
}
