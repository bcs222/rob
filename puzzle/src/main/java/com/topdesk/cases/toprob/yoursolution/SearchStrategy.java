package com.topdesk.cases.toprob.yoursolution;

import com.topdesk.cases.toprob.Instruction;

/**
 * Serves search effort calculation.
 * 
 */
public enum SearchStrategy {
	
	/**
	 * Shifts directions clockwise.
	 */
	CLOCKWISE,
	
	/**
	 * Shifts directions counterclockwise. 
	 */
	COUNTERCLOCKWISE;

	public Instruction getNextDirection(Instruction instruction) {
		switch(this) {
		case CLOCKWISE:
			switch(instruction) {
			case NORTH: return Instruction.EAST;  
			case EAST: return Instruction.SOUTH;
			case SOUTH: return Instruction.WEST;
			case WEST: return Instruction.NORTH;
			default:
				return Instruction.PAUSE;
			}
		case COUNTERCLOCKWISE:
			switch(instruction) {
			case NORTH: return Instruction.WEST; 
			case EAST: return Instruction.NORTH;
			case SOUTH: return Instruction.EAST;
			case WEST: return Instruction.SOUTH;
			default:
				return Instruction.PAUSE;
			}
		default:
			break;
		}

		return null;
	}
	
	public Instruction getOppositeDirection(Instruction instruction) {
		switch(instruction) {
		case NORTH:
			return Instruction.SOUTH;
		case EAST:
			return Instruction.WEST;
		case SOUTH:
			return Instruction.NORTH;
		case WEST:
			return Instruction.EAST;
		default:
			return null;
		}
	}
	
	public SearchStrategy getReverseStrategy() {
		switch(this) {
		case CLOCKWISE:
			return COUNTERCLOCKWISE;
		case COUNTERCLOCKWISE:
			return CLOCKWISE;
		default:
			return null;
		}
	}
}
