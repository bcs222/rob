package com.topdesk.cases.toprob.yoursolution;

import java.util.List;

import com.topdesk.cases.toprob.Coordinate;
import com.topdesk.cases.toprob.Grid;
import com.topdesk.cases.toprob.Instruction;
import com.topdesk.cases.toprob.Solution;

public final class YourSolution implements Solution {
	
	public YourSolution() {}
	
	/**
	 * Finds a path from the room to the kitchen and back. After the last move Rob
	 * should be in his room. The solution is at most 100 instructions, i.e. the
	 * size of the returned {@code List} is smaller than or equal to 100. This
	 * solution is not guaranteed to be the shortest path.
	 * <p>
	 * Rob starts his journey at the given {@code time} in his room.
	 * 
	 * @param grid the grid to find a solution for
	 * @param time the time that Rob starts his journey. This value is bigger than
	 *             or equals to zero
	 * @return a {@code List} with all instructions
	 * @throws NullPointerException if {@code grid} is {@code null}
	 * @throws IllegalArgumentException if {@code time} is negative.
	 */

	@Override
	public List<Instruction> solve(Grid grid, int time) 
			throws NullPointerException, IllegalArgumentException{
		
		checkInput(grid, time);
		
		return new PathFinder(grid, time).getPath();
	}

	private void checkInput(Grid grid, int time) 
			throws NullPointerException, IllegalArgumentException{
		
		if(time < 0) {
			throw new IllegalArgumentException("Time parameter must be greater than zero...");
		}
		
		if(grid == null) {
			throw new NullPointerException("Grid object is null...");
		}
		
		int gridHeight = grid.getHeight();
		int gridWidth = grid.getWidth();
		
		Coordinate kitchen = grid.getKitchen();
		Coordinate room = grid.getRoom();
		
		checkCoordinate(room, gridWidth, gridHeight);
		checkCoordinate(kitchen, gridWidth, gridHeight);
		
		if(kitchen.equals(room)) {
			throw new IllegalArgumentException();
		}
		
	}
	
	private void checkCoordinate(Coordinate coordinate, int gridWidth, int gridHeight) 
			throws NullPointerException, IllegalArgumentException {
		
		if(coordinate == null) {
			throw new NullPointerException("Coordinate object is null...");
		}
		
		boolean b1 = coordinate.getX() < 0;
		boolean b2 = coordinate.getX() >= gridWidth;
		boolean b3 = coordinate.getY() < 0;
		boolean b4 = coordinate.getY() >= gridHeight;
		
		if(b1 || b2 || b3 || b4) {
			throw new IllegalArgumentException("Coordinate is out of grid scope...");
		}
	}
}
