package nl.knokko.builder.builders.building;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import nl.knokko.builder.builders.BuildTask;
import nl.knokko.util.blocks.BlockPlacer;
import nl.knokko.util.blocks.BlockType;

public class BuildTaskFlat extends BuildTask {
	
	public static final short CLASS_ID = 1;
	
	private static final byte STATE_NONE = -128;
	private static final byte STATE_ENTRANCE = -127;
	private static final byte STATE_OUTER_WALLS = -126;
	private static final byte STATE_ENTRANCE_ROOM = -125;
	
	/**
	 * The x-coordinate of the west wall
	 */
	private final int minX;
	
	/**
	 * The y-coordinate of the floor
	 */
	private final int minY;
	
	/**
	 * The z-coordinate of the north wall
	 */
	private final int minZ;
	
	private final Random random;
	
	private final BlockType floor;
	private final BlockType outerWall;
	private final BlockType innerWall;
	private final BlockType ceiling;
	private final BlockType roof;
	
	private Door door = new Door();
	private CurrentFloor currentFloor;
	
	private byte state;
	
	/**
	 * The amount of blocks (x-axis) between the east and west wall
	 */
	private int totalInnerWidth;
	
	/**
	 * The amount of blocks (z-axis) between the south and north wall
	 */
	private int totalInnerDepth;
	
	/**
	 * The amount of blocks between the blocks of the lowest floor and the roof blocks
	 */
	private int totalInnerHeight;
	
	/**
	 * The width of the building (x-axis) can no longer change if this is false
	 */
	private boolean confirmedInnerWidth;
	
	/**
	 * The depth of the building (z-axis) can no longer change if this is false
	 */
	private boolean confirmedInnerDepth;
	
	/**
	 * The height of the building can no longer change if this is false
	 */
	private boolean confirmedInnerHeight;
	
	/**
	 * Y-coordinate of the floor blocks of the current floor
	 */
	private int currentFloorY;
	
	/**
	 * Amount of air blocks between the floor and the ceiling of the current floor
	 */
	private int currentFloorHeight;
	
	public static BuildTaskFlat fromData(BlockPlacer world, DataInputStream input) throws IOException {
		int minX = input.readInt();
		int minY = input.readInt();
		int minZ = input.readInt();
		long seed = input.readLong();
		BlockType floor = new BlockType(input);
		BlockType outerWall = new BlockType(input);
		BlockType innerWall = new BlockType(input);
		BlockType ceiling = new BlockType(input);
		BlockType roof = new BlockType(input);
		Door door = new Door();
		door.side = input.readBoolean();
		door.min = input.readInt();
		door.max = input.readInt();
		door.height = input.readInt();
		byte state = input.readByte();
		int totalInnerWidth = input.readInt();
		int totalInnerDepth = input.readInt();
		int totalInnerHeight = input.readInt();
		boolean confirmedInnerWidth = input.readBoolean();
		boolean confirmedInnerDepth = input.readBoolean();
		boolean confirmedInnerHeight = input.readBoolean();
		int currentFloorY = input.readInt();
		int currentFloorHeight = input.readInt();
		BuildTaskFlat task = new BuildTaskFlat(world, minX, minY, minZ, new Random(seed), floor, outerWall, innerWall, ceiling, roof);
		task.door = door;
		task.state = state;
		task.totalInnerWidth = totalInnerWidth;
		task.totalInnerDepth = totalInnerDepth;
		task.totalInnerHeight = totalInnerHeight;
		task.confirmedInnerWidth = confirmedInnerWidth;
		task.confirmedInnerDepth = confirmedInnerDepth;
		task.confirmedInnerHeight = confirmedInnerHeight;
		task.currentFloorY = currentFloorY;
		task.currentFloorHeight = currentFloorHeight;
		return task;
	}
	
	private BuildTaskFlat(BlockPlacer world, int minX, int minY, int minZ, Random random, BlockType floor,
			BlockType outerWall, BlockType innerWall, BlockType ceiling, BlockType roof){
		super(world);
		this.minX = minX;
		this.minY = minY;
		this.minZ = minZ;
		this.random = random;
		this.floor = floor;
		this.outerWall = outerWall;
		this.innerWall = innerWall;
		this.ceiling = ceiling;
		this.roof = roof;
	}

	public BuildTaskFlat(BlockPlacer world, BlockType floor, BlockType ceiling, BlockType roof, BlockType innerWall, BlockType outerWall, int minX, int minY, int minZ, long seed) {
		super(world);
		this.floor = floor;
		this.ceiling = ceiling;
		this.roof = roof;
		this.outerWall = outerWall;
		this.innerWall = innerWall;
		this.random = new Random(seed);
		this.minX = minX;
		this.minY = minY;
		this.minZ = minZ;
		currentFloorY = minY;
		state = STATE_ENTRANCE;
		door = new Door();
	}
	
	@Override
	protected void placeNext() {
		if(state == STATE_ENTRANCE){
			boolean sideX = random.nextBoolean();
			int width = random.nextInt(3) + 2;
			int height = random.nextInt(2) + 2;
			currentFloorHeight = height + 2;
			totalInnerHeight = currentFloorHeight + 1;
			if(sideX){
				int minDoorX;
				if(confirmedInnerWidth)
					minDoorX = minX + 3 + random.nextInt(totalInnerWidth - 3 - width);
				else {
					minDoorX = minX + 20 + random.nextInt(20);
					totalInnerWidth = minDoorX - minX + width + 20;
				}
				if(!confirmedInnerDepth)
					totalInnerDepth = 20 + random.nextInt(20);
				door.min = minDoorX;
			}
			else {
				int minDoorZ;
				if(confirmedInnerDepth)
					minDoorZ = minZ + 3 + random.nextInt(totalInnerDepth - 3 - width);
				else {
					minDoorZ = minZ + 20 + random.nextInt(20);
					totalInnerDepth = minDoorZ - minZ + width + 20;
				}
				if(!confirmedInnerWidth)
					totalInnerWidth = 20 + random.nextInt(20);
				door.min = minDoorZ;
			}
			door.side = sideX;
			door.height = height;
			door.max = door.min + width - 1;
			setState(STATE_OUTER_WALLS);
		}
		else if(state == STATE_OUTER_WALLS){
			confirmedInnerWidth = true;
			confirmedInnerDepth = true;
			confirmedInnerHeight = true;
			int maxX = minX + totalInnerWidth + 1;
			int maxY = minY + totalInnerHeight + 1;
			int maxZ = minZ + totalInnerDepth + 1;
			if(door.side){
				fill(outerWall, minX, minY + 1, minZ, door.min - 1, minY + door.height, minZ);
				fill(outerWall, door.max + 1, minY + 1, minZ, maxX, minY + door.height, minZ);
				fill(outerWall, minX, minY + door.height + 1, minZ, maxX, maxY, minZ);
			}
			else {
				fill(outerWall, minX, minY + 1, minZ, minX, minY + door.height, door.min - 1);
				fill(outerWall, minX, minY + 1, door.max + 1, minX, minY + door.height, maxZ);
				fill(outerWall, minX, minY + door.height + 1, minZ, minX, maxY, maxZ);
			}
			fill(outerWall, maxX, minY + 1, minZ, maxX, maxY, maxZ);
			fill(outerWall, minX, minY + 1, maxZ, maxX, maxY, maxZ);
			setState(STATE_ENTRANCE_ROOM);
		}
		else if(state == STATE_ENTRANCE_ROOM){
			currentFloor = new CurrentFloor();
			setState(STATE_CORRIDORS);
		}
	}
	
	@Override
	protected boolean isCompleted() {
		return state == STATE_NONE;
	}
	
	@Override
	protected void saveOwn(DataOutputStream output) throws IOException {
		output.writeInt(minX);
		output.writeInt(minY);
		output.writeInt(minZ);
		output.writeLong(getSeed());
		floor.save(output);
		outerWall.save(output);
		innerWall.save(output);
		ceiling.save(output);
		roof.save(output);
		output.writeBoolean(door.side);
		output.writeInt(door.min);
		output.writeInt(door.max);
		output.writeInt(door.height);
		output.writeByte(state);
		output.writeInt(totalInnerWidth);
		output.writeInt(totalInnerDepth);
		output.writeInt(totalInnerHeight);
		output.writeBoolean(confirmedInnerWidth);
		output.writeBoolean(confirmedInnerDepth);
		output.writeBoolean(confirmedInnerHeight);
		output.writeInt(currentFloorY);
		output.writeInt(currentFloorHeight);
		//TODO save and load the currentFloor data
	}
	
	@Override
	protected short getClassID(){
		return CLASS_ID;
	}
	
	private long getSeed(){
		try {
		    Field field = Random.class.getDeclaredField("seed");
		    field.setAccessible(true);
		    AtomicLong scrambledSeed = (AtomicLong) field.get(random);   //this needs to be XOR'd with 0x5DEECE66DL
		    return scrambledSeed.get() ^ 0x5DEECE66DL;
		}
		catch (Exception e) {
			throw new Error("Can't save random seed: " + e.getLocalizedMessage());
		}
	}
	
	private void setState(byte state){
		this.state = state;
	}
	
	private static class Door {
		
		/**
		 * True if the door is in the north wall, false if in the west wall
		 */
		private boolean side;
		
		private int min;
		private int max;
		private int height;
	}
	
	private class CurrentFloor {
		
		private boolean[] walls;
		
		private CurrentFloor(){
			if(!confirmedInnerWidth)
				throw new IllegalStateException("Width should be confirmed by now!");
			if(!confirmedInnerDepth)
				throw new IllegalStateException("Depth should be confirmed by now!");
			walls = new boolean[totalInnerWidth * totalInnerDepth];
		}
		
		/**
		All parameters should be relative to the minX and minZ of the inside building.
		*/
		private boolean isAvailable(int minX, int minZ, int maxX, int maxZ){
			if(minX < 0 || minZ < 0 || maxX >= totalInnerWidth || maxZ >= totalInnerDepth)
				return false;
			for(int x = minX; x<= maxX; x++)
				for(int z = minZ; z <= maxZ; z++)
					if(walls[x + z * totalInnerWidth])
						return false;
			return true;
		}

		/**
		All parameters should be relative to the minX and minZ of the inside building.
		*/
		private void fill(int minX, int minZ, int maxX, int maxZ){
			if(minX < 0 || minZ < 0 || maxX >= totalInnerWidth || maxZ >= totalInnerDepth)
				throw new IllegalArgumentException("Outside bounds : [" + minX + "," + maxX + "] and [" + minZ + "," + maxZ + "]!");
			for(int x = minX; x <= maxX; x++)
				for(int z = minZ; z <= maxZ; z++)
					walls[x + z * totalInnerWidth] = true;
		}
	}
}
