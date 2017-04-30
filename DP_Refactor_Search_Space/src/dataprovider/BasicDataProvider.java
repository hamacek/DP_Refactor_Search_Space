package dataprovider;

import java.util.LinkedList;
import java.util.List;

import entities.Repair;
import entities.SmellType;
import entities.stateSpace.SmellOccurance;
import entities.stateSpace.State;

public class BasicDataProvider implements DataProvider{

	private List<Repair> repairs;
	private List<SmellType> smells;
	private State root;
	
	@Override
	public List<Repair> getRepairs() {
		
		return this.repairs;
	}

	@Override
	public List<SmellType> getSmellTypes() {
		return this.smells;
	}

	@Override
	public State getRootState() {
		return this.root;
	}
	
	public BasicDataProvider(){
			
		this.smells = new LinkedList<SmellType>();
		
		SmellType smell_1 = new SmellType("Smell_1");
		SmellType smell_2 = new SmellType("Smell_2");
		SmellType smell_3 = new SmellType("Smell_3");
		
		smell_2.setWeight(8);
		smell_3.setWeight(3);
		
		this.smells.add(smell_1);
		this.smells.add(smell_2);
		this.smells.add(smell_3);
		
		this.repairs = new LinkedList<Repair>();
		
		List<SmellType> r1_smells = new LinkedList<SmellType>();
		r1_smells.add(smell_1);
		Repair repair_1 = new Repair("Repair_1", r1_smells);
		
		List<SmellType> r2_smells = new LinkedList<SmellType>();
		r2_smells.add(smell_2);
		Repair repair_2 = new Repair("Repair_2", r2_smells);
		
		this.repairs.add(repair_1);
		this.repairs.add(repair_2);
		
		//init root state
		this.root = new State();
		this.root.setSmells(new LinkedList<SmellOccurance>());
		this.root.getSmells().add(new SmellOccurance(smell_1));
		this.root.getSmells().add(new SmellOccurance(smell_2));
		this.root.getSmells().add(new SmellOccurance(smell_3));
		
	}
	
}
