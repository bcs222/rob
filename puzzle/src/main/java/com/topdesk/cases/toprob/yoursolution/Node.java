package com.topdesk.cases.toprob.yoursolution;

import static com.topdesk.cases.toprob.Instruction.EAST;
import static com.topdesk.cases.toprob.Instruction.NORTH;
import static com.topdesk.cases.toprob.Instruction.SOUTH;
import static com.topdesk.cases.toprob.Instruction.WEST;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import com.topdesk.cases.toprob.Coordinate;
import com.topdesk.cases.toprob.Instruction;


/**
 * Grid cells are represented in {@code Node} instances and they are treated 
 * as vertices of a graph.
 * <p>
 * A node object is aware of its neighborhood. It is also aware of the 
 * path finding process' strategy and state.
 * <p>
 * A node can offer the next node of the mission path, if one exists in its neighborhood. 
 * 
 * @see #cell
 * @see #direction
 * @see #initialDirection
 * @see #neighboringCells
 * @see #parentNode
 * @see #pathFinder
 * @see #strategy
 * @see<p>
 * @see #Node(Coordinate, Node, PathFinder)
 * @see #Node(Instruction, Coordinate)
 * @see<p>
 * @see #checkKitchen()
 * @see #checkSearchEffort()
 * @see #getNeighborToVisit()
 * @see #getNodeForParentCoordinate()
 * @see #reviewSearchEffort()
 */
public final class Node {
	
	/**
	 * The position of this {@code Node} in the grid
	 * @see Coordinate
	 */
	private Coordinate cell;
	
	/**
	 * The direction of the neighboring cell to join the mission path.
	 * @see #strategy 
	 * @see Instruction
	 */
	private Instruction direction;	
	
	/**
	 * The first calculation of the neighboring cell's direction. If there were no holes,
	 * this direction would be optimal.
	 * @see PathFinder
	 * @see Instruction
	 */
	private Instruction initialDirection;
	
	/**
	 * Together with {@link #direction}, this property governs how to get closer to the target.
	 * <p>
	 * Whenever the depth-first algorithm steps back and revisits a node, 
	 * a new search effort of the visited node will be selected based on its {@link #strategy}.
	 * Selecting the next {@link #direction}, the node iterates on its neighbors clockwise or 
	 * counterclockwise to explore the first available neighbor, if any. When all neighbors 
	 * have been visited, or no other neighbors are available, the node is removed from the
	 * {@link PathFinder#discoveryStack}, and its parent node will be visited.
	 * @see Instruction
	 * @see SearchStrategy
	 */
	private SearchStrategy strategy;
	
	/**
	 * Neighborhood of the node's cell.
	 * <p>
	 * {@code Map<Instruction, Coordinate> neighboringCells}}
	 * @see Instruction
	 * @see Coordinate
	 */
	private Map<Instruction, Coordinate> neighboringCells;
	
	/**
	 * The node that offered {@code this} node to join the path.
	 * 
	 */
	private Node parentNode;
	
	
	/**
	 * The service to build the path of Rob's mission
	 * @see {@link PathFinder}
	 */
	private PathFinder pathFinder;
	
	/**
	 * This constructor is used to create nodes aware of their neighbors
	 * and also the {@code PathFinder}'s state.
	 * <p> 
	 * These nodes serve the next available neighbor, if any, to build 
	 * Rob's partial path to the kitchen.
	 * 
	 * @param cell the node's {@link Coordinate} in the grid.
	 * @param parentNode {@link #parentNode}
	 * @param pathFinder {@link #pathFinder}
	 * <p>
	 * @see {@link PathFinder}
	 */
	public Node(Coordinate cell, Node parentNode, PathFinder pathFinder) {
		
		this.cell = cell;
		this.parentNode = parentNode;
		this.pathFinder = pathFinder;
		this.neighboringCells = new HashMap<>();
		
		checkSearchEffort();
		
		this.direction = this.initialDirection;
		
		mapNeighbors();		
		reviewSearchEffort();
		
		checkKitchen();
	}
	
	/**
	 * This constructor creates a node object that does not need to be aware of
	 * its neighborhood. 
	 * <p>
	 * Nodes representing a pause are merely wrappers of
	 * {@link Instruction#PAUSE} information. 
	 * <p>
	 * Also, as nodes of the partial mission to the kitchen are already known 
	 * at the stage of targeting the room, the new steps are based on less computation. 
	 * The algorithm uses the same steps in reverse order to build the remaining path 
	 * fragment of the mission.
	 * 
	 * @param instruction {@link Instruction}
	 * @param cell {@link Coordinate}
	 * @see #direction
	 */
	public Node(Instruction instruction, Coordinate cell) {
		this.direction = instruction;
		this.cell = cell;
	}
	
	private boolean cellIsInScope(Coordinate neighbor) {
		
		Coordinate[] pathFinderScope = pathFinder.getScope();
		
		boolean b1 = neighbor.getX() >= pathFinderScope[0].getX();
		boolean b2 = neighbor.getX() <= pathFinderScope[1].getX();
		boolean b3 = neighbor.getY() >= pathFinderScope[0].getY();
		boolean b4 = neighbor.getY() <= pathFinderScope[1].getY();
		
		return b1 && b2 && b3 && b4;
	}
	
	/**
	 * When in the kitchen, turn back to the room, to traverse the same path
	 * in the opposite direction.
	 */
	private void checkKitchen() {
		if(this.cell.equals(pathFinder.getGrid().getKitchen())) {
			direction = strategy.getOppositeDirection(direction);
		}
	}	
	
	/**
	 * Calculate search effort ({@link #initialStrategy} and {@link #initialDirection}).
	 * Search effort is only planned for the partial mission to the kitchen.
	 * <p>
	 * To return to the room, the same path may be used.
	 * @param node
	 * 		the new {@link Node} in the partial mission path 
	 * 		that explores best effort to reach the kitchen 
	 */
	private void checkSearchEffort() {
		int nodeX = cell.getX();
		int kitchenX = pathFinder.getGrid().getKitchen().getX();
		int nodeY = cell.getY();
		int kitchenY = pathFinder.getGrid().getKitchen().getY();
		
		boolean nodeXIsGreater = nodeX > kitchenX;
		boolean nodeYIsGreater = nodeY > kitchenY;
		
		boolean targetIsNorthEast = ! nodeXIsGreater && nodeYIsGreater;
		boolean targetIsNorthWest = nodeXIsGreater && nodeYIsGreater;
		boolean targetIsSouthEast = ! nodeXIsGreater && ! nodeYIsGreater;
		boolean targetIsSouthWest = nodeXIsGreater && ! nodeYIsGreater;
		
		int horizontalDistance = Math.abs(nodeX - kitchenX);
		int verticalDistance = Math.abs(nodeY - kitchenY);
		
		boolean effortIsHorizontal = horizontalDistance > verticalDistance;
				
		if(effortIsHorizontal) {
			if(targetIsNorthEast) {
				initialDirection = Instruction.EAST;
				strategy = SearchStrategy.COUNTERCLOCKWISE;
			} else if(targetIsNorthWest) {
				initialDirection = Instruction.WEST;
				strategy = SearchStrategy.CLOCKWISE;
			} else if(targetIsSouthEast) {
				initialDirection = Instruction.EAST;
				strategy = SearchStrategy.CLOCKWISE;
			} else if(targetIsSouthWest) {
				initialDirection = Instruction.WEST;
				strategy = SearchStrategy.COUNTERCLOCKWISE;
			}			
		} else {
			if(targetIsNorthEast) {
				initialDirection = Instruction.NORTH;
				strategy = SearchStrategy.CLOCKWISE;
			} else if(targetIsNorthWest) {
				initialDirection = Instruction.NORTH;
				strategy = SearchStrategy.COUNTERCLOCKWISE;
			} else if(targetIsSouthEast) {
				initialDirection = Instruction.SOUTH;
				strategy = SearchStrategy.COUNTERCLOCKWISE;
			} else if(targetIsSouthWest) {
				initialDirection = Instruction.SOUTH;
				strategy = SearchStrategy.CLOCKWISE;
			}	
		}
		
	}
	
	/**
	 * @return the {@link Coordinate} of {@code this} node
	 */
	public Coordinate getCell() {
		return cell;
	}
	
	/**
	 * @return the instruction the node wraps. 
	 * @see Instruction
	 */
	public Instruction getDirection() {		
		return direction;
	}
	
	/**
	 * Approaching the kitchen, {@code this} node checks which of its neighbors 
	 * may be the next element of the path. 
	 * @return the next available neighbor {@code Node}, if any.
	 * <p>
	 * If all neighbors have been visited, {@code null} is returned.
	 */
	public Node getNeighborToVisit() {		
		Node neighbor = null;
		
		if(this.hasNeighborToVisit()) {
			
			setDirection();
			Coordinate cell = neighboringCells.get(direction);

			neighboringCells.put(direction, null);
			neighbor =  new Node(cell, this, pathFinder);
		}
		
		return neighbor;
	}
	
	/**
	 * The path to the kitchen is also a path to the room in the reverse order of the steps.
	 * <p>
	 * As {@code this} node was created based on its {@code parentNode}'s {@link Instruction},
	 * To step back to the parent {@link Coordinate}, the {@code Instruction} opposite 
	 * {@link #parentNode}'s instruction applies.
	 * @return {@code Node} instance with {@code Instruction} of opposite direction.
	 */
	public Node getNodeForParentCoordinate() {
		if(parentNode == null) {
			return null;
		}
		
		Instruction instruction = parentNode.direction;
		instruction = strategy.getOppositeDirection(instruction);
		
		Node node = new Node (instruction, this.cell);
		node.parentNode = this;
		
		return node;
	}
	
	private boolean hasNeighborToVisit() {		
		
		if(neighboringCells.get(initialDirection) != null) {
			return true;
		}
		
		Instruction nextDirection = strategy.getNextDirection(initialDirection);
		while( ! nextDirection.equals(initialDirection)) {
			if(neighboringCells.get(nextDirection) != null) {
				return true;
			}
			
			nextDirection = strategy.getNextDirection(nextDirection);
		}
		
		return false;
	}
	
	private void mapNeighbors() {
		neighboringCells.put(NORTH, NORTH.execute(cell));
		neighboringCells.put(EAST, EAST.execute(cell));
		neighboringCells.put(SOUTH, SOUTH.execute(cell));
		neighboringCells.put(WEST, WEST.execute(cell));
		
		neighboringCells.forEach((location, neighbor) -> {			
			Set<Coordinate> holesToAvoid = pathFinder.getGrid().getHoles();
			
			boolean isAHole = holesToAvoid.contains(neighbor);
			boolean isOutOfScope = ! cellIsInScope(neighbor);
			boolean isAlreadyVisited = pathContainsNode(neighbor);			
			boolean isParentCell = neighborIsParent(neighbor);
			
			boolean b = 
					isAHole || isOutOfScope || isParentCell || isAlreadyVisited;

			if(b) {				
				neighboringCells.put(location, null);
			}
		});
	}
	
	private boolean neighborIsParent(Coordinate neighbor) {
		
		if(parentNode == null) {
			return false;
		}
		
		return neighbor.equals(parentNode.getCell());
	}
	
	private boolean pathContainsNode(Coordinate cell) {
		Stack<Node> discoveryStack = pathFinder.getDiscoveryStack();
		
		for (Node node : discoveryStack) {
			Coordinate nodeCell = node.getCell();
			if(nodeCell.equals(cell)) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Having mapped its neighborhood, the node may review its search effort. 
	 */
	private void reviewSearchEffort() {
		Instruction nextDirection = strategy.getNextDirection(direction);
		
		boolean b1 = neighboringCells.get(direction) == null;
		boolean b2 = neighboringCells.get(nextDirection) == null;
		
		if(b1 && b2) {
			strategy = strategy.getReverseStrategy();
		}
	}
	
	private void setDirection() {
		
		Coordinate neighbor = neighboringCells.get(initialDirection);
		if(neighbor != null) {			
			return;
		}
		
		Instruction nextDirection = strategy.getNextDirection(initialDirection);
		while( ! nextDirection.equals(initialDirection)) {
			if(neighboringCells.get(nextDirection) != null) {				
				direction = nextDirection;
				
				return;
			}
			
			nextDirection = strategy.getNextDirection(nextDirection);
		}

	}
	
	@Override
	public String toString() {
		return cell + "\t" + direction;
	}
	
}
