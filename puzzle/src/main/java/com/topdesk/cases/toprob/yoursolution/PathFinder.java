package com.topdesk.cases.toprob.yoursolution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import com.topdesk.cases.toprob.Coordinate;
import com.topdesk.cases.toprob.Grid;
import com.topdesk.cases.toprob.Instruction;

/**
 * Finds a path from the room to the kitchen and back, if a path 
 * of not more than 100 steps exists. 
 * <p> 
 * To find a path from one cell to another, {@code PathFinder} depth-first traverses 
 * the graph of the {@link Grid} cells. 
 * 
 * @see #discoveryStack
 * @see #grid
 * @see #instructionList
 * @see #stepsToRoom
 * @see #scope
 * @see #tick
 * @see #time 
 * @see<p>
 * @see #PathFinder(Grid, int)
 * @see<p>
 * @see #checkSearchEffort(Node)
 * @see #foundPathToRoom()
 * @see #makeSandwich()
 * @see #prepareStepsToRoom()
 * @see <p>
 * @see Node
 */
public final class PathFinder {
	/**
	 * The plane where {@code PathFinder} explores.
	 */
	private final Grid grid;
	
	/**
	 * {@link Grid} boundaries
	 */
	private Coordinate[] scope;
	
	/**
	 * Stack to manage depth-first graph traversal.
	 */
	private Stack<Node> discoveryStack;
	
	/**
	 * The instruction list of a successful mission. 
	 * <p>
	 * The first instruction is the instruction 
	 * of the {@code Node} at the position described by {@link Grid#getRoom()}.
	 * <p>
	 * The last instruction is the instruction of the {@link Node} that targets the room
	 * at the end of Rob's mission.
	 * @see
	 * 		{@link Instruction}
	 */
	private List<Instruction> instructionList;	
	
	/**
	 * Steps targeting the room from the kitchen. 
	 * <p>
	 * This stack will contain {@link Node} objects of {@link #discoveryStack}. These steps have 
	 * already been recorded from the room to the kitchen.  The same path between the room and the kitchen 
	 * may be used in both directions; steps in reverse order and with the opposite direction. 
	 */
	private Stack<Node> stepsToRoom;
	
	/**
	 * The point of time for the actual step of this {@link PathFinder}
	 */
	private int tick;

	/**
	 * The initial point of time, when Rob's mission starts.
	 * This time value shifts {@link PathFinder}'s tick to match the {@link Grid} time
	 */
	private final int time;
	
	/**
	 * @param grid 
	 * 		space where {@link PathFinder} explores
	 * @param time 
	 * 		value that shifts {@code PathFinder} tick to match 
	 * the {@link Grid} time
	 */
	public PathFinder(Grid grid, int time) {
		this.grid = grid;
		this.time = time;
		this.tick = time;
		
		setPathFinderScope();
	}
	
	private void addNodeToPath(Node node) {		
		discoveryStack.push(node);
		
		increaseTick();
	}
	
	private void checkLethalMove(Node node) {
		if(moveIsLethal(node)) {
			Node lethalStep = discoveryStack.pop();
			pause(lethalStep.getCell());
			discoveryStack.push(lethalStep);					
		}
	}
	

	
	private void decreaseTick() {
		tick--;
	}
	
	private boolean foundKitchen(Node node) {
		Coordinate nodeCoordinate = node.getCell();
		Coordinate kitchenCoordinate = grid.getKitchen();
		
		return kitchenCoordinate.equals(nodeCoordinate);
	}
	
	/**
	 * Steps to the kitchen have already been mapped. The same steps may be the
	 * path fragment from the kitchen to the room, in reverse order and with
	 * opposite direction.
	 * @return 
	 * 		{@code true} if the mission is completed; {@code false} otherwise
	 */
	private boolean foundPathToRoom() {
		
		int savedStackSize = discoveryStack.size();
		
		while( ! stepsToRoom.empty() ) {
			Node node = stepsToRoom.pop();			
			Node nextNode = node.getNodeForParentCoordinate();
			
			if(nextNode == null) {
				continue;
			}
			
			if(moveIsLethal(nextNode)) {
				Node lethalStep = discoveryStack.pop();
				pause(lethalStep.getCell());
				discoveryStack.push(lethalStep);	
			}
			
			if(discoveryStack.size() == 100) {
				int stackSize = discoveryStack.size();
				
				List<Node> failedNodes = discoveryStack.subList(savedStackSize, stackSize);
				discoveryStack.removeAll(failedNodes);
				
				return false;
			}
			
			addNodeToPath(nextNode);
		} 
		
		return true;
	}
	
	/**
	 * @return
	 * 	{@code discoveryStack}
	 * @see #discoveryStack
	 */
	public Stack<Node> getDiscoveryStack() {
		return discoveryStack;
	}
	
	/**
	 * @return
	 * 	{@code grid}
	 * @see #grid
	 */
	public Grid getGrid() {
		return grid;
	}
	
	/**
	 * @return 
	 * 		{@code instructionList} 
	 * <p>
	 * the list of instructions that have completed Rob's mission. 
	 * If the mission cannot be completed an empty list is returned.
	 * @see #instructionList
	 */
	public List<Instruction> getPath() {
		discoveryStack = new Stack<>();
		
		Node node = getRoomNode();
		discoveryStack.push(node);
		
		while( node != null ) {
			Node nextNode = node.getNeighborToVisit();
			
			if(nextNode != null) {
				checkLethalMove(nextNode);
				addNodeToPath(nextNode);
				
				if(foundKitchen(nextNode)) {
					prepareStepsToRoom();
					makeSandwich();
					
					if(foundPathToRoom()) {
						prepareInstructionList();
						return instructionList;
						
					} else {
						node = parentNodeRevisited();
					}
				}
				
				if(noTimeToReturn()) {
					node = parentNodeRevisited();
				} else {
					node = nextNode;
				}
			} else {
				node = parentNodeRevisited();
			}
		}
		
		return Collections.<Instruction>emptyList();
	}
	
	private Node parentNodeRevisited() {
		removeNodeFromPath();		
		Node node = previousVisitedNode();
		
		if(node != null) {
			Instruction direction = node.getDirection();
			if(direction.equals(Instruction.PAUSE)) {
				removeNodeFromPath();		
				node = previousVisitedNode();
			}
		}
		
		return node;
	}
	
	private Node getRoomNode() {
		Coordinate roomCoordinate = grid.getRoom();
		Node node = new Node(roomCoordinate, null, this);
		
		return node;
	}
	
	/**
	 * @return
	 * 		{@code scope}
	 * @see {@link #scope}
	 */
	public Coordinate[] getScope() {
		return scope;
	}
	
	private void increaseTick() {
		tick++;
	}
	
	/**
	 * Remove the kitchen node so as not to interpret the
	 * instruction it represents. {@link #prepareStepsToRoom()}}
	 * has saved the kitchen node for a later operation.
	 * <p>
	 * Spend time with the sandwich.
	 */
	private void makeSandwich() {
		removeNodeFromPath();
		
		for(int counter = 0; counter < 5; counter++) {
			pause(grid.getKitchen());
		}
	}
	
	private boolean moveIsLethal(Node node) {	
		if(node == null) {
			return false;
		}
		
		Coordinate nodeCoordinate = node.getCell();
		Coordinate bugCoordinate = grid.getBug(tick + 1);
		
		return bugCoordinate.equals(nodeCoordinate);
	}
	
	private boolean noTimeToReturn() {
		return (tick - time) > 48;
	}

	private void pause(Coordinate cell) {
		Node pause =  new Node(Instruction.PAUSE, cell);			
		discoveryStack.push(pause);

		increaseTick();
	}

	private void prepareInstructionList() {
		instructionList = new ArrayList<>();

		for(int index = 0; index < discoveryStack.size(); index++) {
			Node node = discoveryStack.get(index);
			
			if(node == null) {
				return;
			}
			
			Instruction instruction = node.getDirection();
			instructionList.add(instruction);			
		}
	}
	
	/**
	 * {@link stepsToRoom} records steps targeting the room from the kitchen. 
	 * These steps are the same as the steps from the room to the kitchen. 
	 * They will be added to the path in reverse order and with the opposite direction.
	 * <p>
	 * The first node that will look for its parent's cell is the kitchen. It is on the 
	 * top of the stack of steps. 
	 * <p>
	 * As the room has no further instructions (end of mission), it will not be 
	 * included in this collection.
	 * <p>
	 * Note that pauses are not steps in the path. 
	 * Those nodes only serve as instruction wrappers.
	 */
	private void prepareStepsToRoom() {
		stepsToRoom = new Stack<>();
		
		for(int index = 1; index < discoveryStack.size(); index++) {
			Node node = discoveryStack.get(index);
			if(node.getDirection().equals(Instruction.PAUSE)) {
				continue;
			}
			
			stepsToRoom.push(node);
		}
	}
	
	private Node previousVisitedNode() {
		if(discoveryStack.empty()) {
			return null;
		}
		
		return discoveryStack.peek();
	}

	private void removeNodeFromPath() {
		discoveryStack.pop();
		
		decreaseTick();		
	}

	private void setPathFinderScope() {
		scope = new Coordinate[2];
		
		int width = grid.getWidth() - 1;
		int height = grid.getHeight() - 1;
		
		scope[0] = new Coordinate(0, 0);
		scope[1] = new Coordinate(width, height);		
	}
	
}
