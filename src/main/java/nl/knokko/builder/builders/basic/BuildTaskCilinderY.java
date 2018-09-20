package nl.knokko.builder.builders.basic;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import nl.knokko.builder.builders.BuildTask;
import nl.knokko.util.blocks.BlockPlacer;
import nl.knokko.util.blocks.BlockType;

public class BuildTaskCilinderY extends BuildTask {
	
	public static final short CLASS_ID = 2;
	
	/*
	private static final String KEY_CENTRE_X = "CentreX";
	private static final String KEY_CENTRE_Z = "CentreZ";
	private static final String KEY_MAX_Y = "MaxY";
	private static final String KEY_RADIUS = "Radius";
	private static final String KEY_X = "CurrentX";
	private static final String KEY_Y = "CurrentY";
	private static final String KEY_Z = "CurrentZ";
	private static final String KEY_BLOCK = "Block";
	*/
	
	private final BlockType block;
	
	private final int centreX;
	private final int centreZ;
	
	private final int maxY;
	
	private final int minX;
	private final int maxX;
	
	private final int minZ;
	private final int maxZ;
	
	private final int radiusSQ;
	private final int radius;
	
	private int x;
	private int y;
	private int z;
	
	public static BuildTaskCilinderY fromData(BlockPlacer world, DataInputStream input) throws IOException {
		return new BuildTaskCilinderY(world, new BlockType(input), input.readInt(), input.readInt(), input.readInt(),
				input.readInt(), input.readInt(), input.readInt(), input.readInt());
	}
	
	/*
	public static BuildTaskCilinderY fromNBT(World world, NBTTagCompound nbt){
		BuildTaskCilinderY task = new BuildTaskCilinderY(world, BlockType.fromNBT(nbt, KEY_BLOCK), nbt.getInteger(KEY_CENTRE_X),
		nbt.getInteger(KEY_CENTRE_Z), nbt.getInteger(KEY_Y), nbt.getInteger(KEY_MAX_Y), nbt.getInteger(KEY_RADIUS));
		task.x = nbt.getInteger(KEY_X);
		task.z = nbt.getInteger(KEY_Z);
		return task;
	}
	*/
	
	private BuildTaskCilinderY(BlockPlacer world, BlockType block, int currentX, int currentY, int currentZ, int centreX, int maxY, int centreZ, int radius){
		super(world);
		this.block = block;
		this.centreX = centreX;
		this.centreZ = centreZ;
		this.maxY = maxY;
		this.radius = radius;
		x = currentX;
		y = currentY;
		z = currentZ;
		
		minX = centreX - radius;
		maxX = centreX + radius;
		minZ = centreZ - radius;
		maxZ = centreZ + radius;
		radiusSQ = radius * radius;
	}

	public BuildTaskCilinderY(BlockPlacer world, BlockType block, int centreX, int centreZ, int minY, int maxY, int radius) {
		super(world);
		this.block = block;
		this.centreX = centreX;
		this.centreZ = centreZ;
		this.maxY = maxY;
		this.radius = radius;

		y = minY;
		minZ = centreZ - radius;
		maxX = centreX + radius;
		maxZ = centreZ + radius;
		radiusSQ = radius * radius;
		minX = centreX - radius;
		x = minX;
		z = minZ;
	}

	@Override
	protected void placeNext() {
		if((centreX - x) * (centreX - x) + (centreZ - z) * (centreZ - z) <= radiusSQ)
			world.place(block, x, y, z);
		x++;
		if(x > maxX){
			x = minX;
			z++;
			if(z > maxZ){
				z = minZ;
				y++;
			}
		}
	}

	@Override
	protected boolean isCompleted() {
		return y > maxY;
	}
	
	/*
	@Override
	protected NBTTagCompound ownNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger(KEY_X, x);
		nbt.setInteger(KEY_Y, y);
		nbt.setInteger(KEY_Z, z);
		nbt.setInteger(KEY_CENTRE_X, centreX);
		nbt.setInteger(KEY_CENTRE_Z, centreZ);
		nbt.setInteger(KEY_MAX_Y, maxY);
		nbt.setInteger(KEY_RADIUS, radius);
		block.save(nbt, KEY_BLOCK);
		return nbt;
	}
	*/

	@Override
	protected short getClassID() {
		return CLASS_ID;
	}

	@Override
	protected void saveOwn(DataOutputStream output) throws IOException {
		block.save(output);
		output.writeInt(x);
		output.writeInt(y);
		output.writeInt(z);
		output.writeInt(centreX);
		output.writeInt(maxY);
		output.writeInt(centreZ);
		output.writeInt(radius);
	}
}
