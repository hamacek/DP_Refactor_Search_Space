package usecases;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import entities.stateSpace.Relation;
import entities.stateSpace.State;
import entities.stateSpace.State.MonteCarloState;

public class MonteCarloSearchStrategy extends PathSearchStrategy {

	private static final int numOfThreads = 1;
	private static final double constant = 2;
	private static final int maxIterations = 100000;

	private List<MonteCarloAgent> agents;
	private MonteCarloState bestState;
	private MonteCarloState rootState;
	private boolean end = false;
	private static int iteration = 0;

	public MonteCarloSearchStrategy(RelationCreator relationCreator) {
		super(relationCreator);
		agents = new ArrayList<MonteCarloAgent>();
	}

	@Override
	public List<Relation> findPath(State rootState, int depth) {
		this.rootState = State.getMonteCarloStateInstance();
		this.rootState.setSmells(rootState.getSmells());
		this.rootState.setN(0);
		this.rootState.setT(0);
		expandCurrentState(this.rootState);
		StateProcessor.initializeState(this.rootState);
		bestState = this.rootState;
		bestState.setFitness(0);

		MonteCarloAgent curent;

		for (int i = 0; i < numOfThreads; i++) {
			curent = new MonteCarloAgent(this.rootState);
			agents.add(curent);
			curent.start(i);
			try {
				curent.t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		List<Relation> results = new ArrayList<Relation>();

		if (end) {
			while (bestState.getSourceRelation() != null) {
				results.add(bestState.getSourceRelation());
				bestState = (MonteCarloState) bestState.getSourceRelation().getFromState();
			}
			Collections.reverse(results);
		}

		System.out.println("");
		System.out.println("RESULT");
		State currentState = null;
		for (Relation r : results) {
			System.out.println("-------------");
			currentState = r.getFromState();
			System.out.println("S_" + currentState.getId() + " [ Fitness: " + currentState.getFitness()
					+ ", NumOfSmells: " + currentState.getSmells().size() + ", Depth: " + currentState.getDepth() + "] "
					+ currentState);
			System.out.println(r.getUsedRepair().getName() + " -> " + r.getFixedSmellOccurance().getSmell().getName()
					+ " P: " + r.getProbability());
			currentState = r.getToState();
			System.out.println("S_" + currentState.getId() + " [ Fitness: " + currentState.getFitness()
					+ ", NumOfSmells: " + currentState.getSmells().size() + ", Depth: " + currentState.getDepth() + "] "
					+ currentState);
		}
		System.out.println(currentState);

		return results;

	}

	public MonteCarloState UCB1(MonteCarloState s) {
		MonteCarloState result = null;
		double maxUCB1 = Double.MIN_VALUE;
		for (Relation r : s.getRelations()) {
			double UCB1;
			MonteCarloState mcs = (MonteCarloState) r.getToState();
			if (mcs.getN() == 0) {
				UCB1 = Double.MAX_VALUE;
			} else {
				double avgValue = mcs.getT() / mcs.getN();
				UCB1 = avgValue + constant * Math.sqrt((2 * Math.log(rootState.getN()) / mcs.getN()));
			}
			if (maxUCB1 < UCB1) {
				maxUCB1 = UCB1;
				result = mcs;
			}
		}

		return result;
	}

	@Override
	protected void applyRepair(List<Relation> rels) {
		Relation rel = null;
		int length = rels.size();
		for (int i = 0; i < length; i++) {

			rel = rels.get(i);
			State s = StateProcessor.applyRepairMonteCarlo(rel.getFromState(), rel.getUsedRepair(),
					rel.getFixedSmellOccurance());
			s.setSourceRelation(rel);
			s.setDepth(rel.getFromState().getDepth() + 1);
			rel.setToState(s);

			// sort smells in new state by ID
			s.getSmells().sort((o1, o2) -> o1.getSmell().getId().compareTo(o2.getSmell().getId()));

		}
	}

	@Override
	protected void calculateEndNodeFitness(List<Relation> relations) {
		for (Relation rel : relations) {
			StateProcessor.calculateFitnessForAnts(rel.getToState());
		}
	}

	private class MonteCarloAgent implements Runnable {
		MonteCarloState curentState;
		private Thread t;

		public MonteCarloAgent(MonteCarloState rootState) {
			curentState = rootState;
		}

		public void start(int i) {
			if (t == null) {
				t = new Thread(this, "thread " + i);
				t.start();
			}
		}

		@Override
		public void run() {
			while (!end) {
				iteration++;
				if (iteration > maxIterations) {
					end = true;
				}
				while (!isLeafNode(curentState)) {
					moveAgent();
					if (curentState.getFitness() > bestState.getFitness()) {
						bestState = curentState;
						printCurentState();
						// System.out.println("new best ");
					}
				}
				if (curentState.getN() == 0) {
					// System.out.println("n = 0 - rollout");
					rollout();
				} else {
					// System.out.println("n! = 0 - move to first child");
					expandCurrentState(curentState);
					moveAgentToFirstChild();
					rollout();
				}
			}
		}

		private boolean isLeafNode(State state) {
			if (state.getRelations() == null || state.getRelations().isEmpty()) {
				// System.out.println("leafnode");
				return true;
			}
			return false;
		}

		private void moveAgent() {
			curentState = UCB1(curentState);

		}

		private void moveAgentToFirstChild() {
			if (!curentState.getRelations().isEmpty() && curentState.getRelations().get(0) != null) {
				curentState = (MonteCarloState) curentState.getRelations().get(0).getToState();
			}
		}

		private void rollout() {
			double terminalFitnes = simulate();
			backPropagate(terminalFitnes);

		}

		private void backPropagate(double simultatedFitness) {
			// System.out.println("curent end state " +
			// curentState.getFitness());
			while (curentState != rootState) {
				((MonteCarloState) curentState).addT(simultatedFitness);
				((MonteCarloState) curentState).incremetN();
				curentState = (MonteCarloState) curentState.getSourceRelation().getFromState();
			}
			((MonteCarloState) curentState).addT(simultatedFitness);
			((MonteCarloState) curentState).incremetN();

		}

		private double simulate() {
			// TODO Auto-generated method stub
			double fitnes = curentState.getFitness();
			return fitnes;
		}

		private void printCurentState() {
			System.out.println("agent moving, curent state: N:" + curentState.getN() + "  T: " + curentState.getT()
					+ " depth: " + curentState.getDepth() + " num of smells: " + curentState.getSmells().size()
					+ " fitness " + curentState.getFitness() + "\t in iteration " + iteration);
		}
	}
}