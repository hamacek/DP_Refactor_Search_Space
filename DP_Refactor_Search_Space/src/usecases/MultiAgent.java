package usecases;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import entities.stateSpace.*;

public class MultiAgent {
	
	private static int NUM_AGENT = 10;
	
	public List<Relation> findPath(State rootState,PathSearchStrategy strategy){
		
			
		strategy.init(rootState);
		
		
		List<Agent> agents = new ArrayList<Agent>();
		List<Thread> threads = new ArrayList<Thread>();
		
		int subListSize = rootState.getRelations().size() / NUM_AGENT;
		List<List<Relation>> subLists = Lists.partition(rootState.getRelations(), subListSize);
		
		for(int i = 0; i < NUM_AGENT; i++){
			Agent a = new Agent(subLists.get(i), new DefaultPathSearchStrategy(strategy.relationCreator));
			agents.add(a);
			threads.add(new Thread(a));		
		}
		
		for(Thread t : threads){
			try {
				t.start();
				t.join();
			} catch (InterruptedException e) {
				
				e.printStackTrace();
			}
		}
		
		
		List<Relation> res = null;
		for(Agent a : agents){
			
			if(res != null){
				
				State bestState = res.get(res.size() - 1 ).getToState();
				double bestValue = res.get(res.size() - 1).getUsedRepair().getWeight() + bestState.getFitness() + bestState.getDepth();
				
				State currentState = a.getBestPath().get(a.getBestPath().size() - 1 ).getToState();
				double currentValue = a.getBestPath().get(a.getBestPath().size() - 1).getUsedRepair().getWeight() + currentState.getFitness() + currentState.getDepth();
				
				if(currentValue < bestValue){
					res = a.getBestPath();
				}
				
				
			}else{
				res = a.getBestPath();
			}
			
		}
		
		List<Relation> results = res;
		State currentState;
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
		
		return null;
	}
}