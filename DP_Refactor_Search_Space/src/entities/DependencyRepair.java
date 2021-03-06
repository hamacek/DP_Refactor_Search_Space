package entities;

import java.util.ArrayList;
import java.util.List;

public class DependencyRepair extends Repair {
	
	//private Map<DependencyType, List<SmellType>> dependencies;
	private List<Dependency> dependencies; 
	
	public DependencyRepair(String name) {
		super(name);
		this.dependencies = new ArrayList<Dependency>();
	}
		
	public DependencyRepair(String name, List<RepairUse> repairUses) {
		super(name, repairUses);
		this.dependencies = new ArrayList<Dependency>();
	}
	
	public List<Dependency> getDependencies() {
		return dependencies;
	}
	
	public void addDependency(DependencyType type, SmellType smell, Double probability, LocationPartType locationPartType, DependencyPlaceType dependencyPlaceType){
		this.dependencies.add(new Dependency(type, smell, probability, locationPartType, dependencyPlaceType));
	}
	
	@Override
	public double calculateProbability() {	
		double probability = 1.0;
		
		for(Dependency dep : this.dependencies){
			probability *= dep.getProbability();
		}
		
		return probability;
	}
}
