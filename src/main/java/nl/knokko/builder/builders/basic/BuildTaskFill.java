package nl.knokko.builder.builders.basic;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import nl.knokko.builder.builders.BuildTask;
import nl.knokko.util.blocks.BlockPlacer;
import nl.knokko.util.blocks.BlockType;

public class BuildTaskFill extends BuildTask {
	
	public static final short CLASS_ID = 0;
	
	/*
	private static final String KEY_MIN_X = "MinX";
	private static final String KEY_MIN_Y = "MinY";
	private static final String KEY_MIN_Z = "MinZ";
	private static final String KEY_MAX_X = "MaxX";
	private static final String KEY_MAX_Y = "MaxY";
	private static final String KEY_MAX_Z = "MaxZ";
	private static final String KEY_CURRENT_X = "X";
	private static final String KEY_CURRENT_Y = "Y";
	private static final String KEY_CURRENT_Z = "Z";
	private static final String KEY_BLOCK = "Block";
	*/
	
	private final BlockType block;
	
	private final int minX;
	private final int minZ;
	private final int maxX;
	private final int maxY;
	private final int maxZ;
	
	private int x;
	private int y;
	private int z;
	
	/*
	public static BuildTaskFill fromNBT(World world, NBTTagCompound nbt){
		BuildTaskFill task = new BuildTaskFill(world, BlockType.fromNBT(nbt, KEY_BLOCK), nbt.getInteger(KEY_MIN_X),
				nbt.getInteger(KEY_MIN_Y), nbt.getInteger(KEY_MIN_Z), nbt.getInteger(KEY_MAX_X),
				nbt.getInteger(KEY_MAX_Y), nbt.getInteger(KEY_MAX_Z));
		task.x = nbt.getInteger(KEY_CURRENT_X);
		task.y = nbt.getInteger(KEY_CURRENT_Y);
		task.z = nbt.getInteger(KEY_CURRENT_Z);
		return task;
	}
	*/
	
	public static BuildTaskFill fromData(BlockPlacer world, DataInputStream input) throws IOException {
		return new BuildTaskFill(world, new BlockType(input), input.readInt(), input.readInt(), input.readInt(),
				input.readInt(), input.readInt(), input.readInt(), input.readInt(), input.readInt());
	}
	
	private BuildTaskFill(BlockPlacer world, BlockType block, int x, int y, int z, int minX, int minZ, int maxX, int maxY, int maxZ){
		super(world);
		this.block = block;
		this.x = x;
		this.y = y;
		this.z = z;
		this.minX = minX;
		this.minZ = minZ;
		this.maxX = maxX;
		this.maxY = maxY;
		this.maxZ = maxZ;
	}

	public BuildTaskFill(BlockPlacer world, BlockType block, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
		super(world);
		this.block = block;
		this.minX = minX;
		this.minZ = minZ;
		this.maxX = maxX;
		this.maxY = maxY;
		this.maxZ = maxZ;
		x = minX;
		y = minY;
		z = minZ;
	}

	@Override
	protected void placeNext() {
		world.place(block, x, y, z);
		x++;
		if(x > maxX){
			x = minX;
			z++;
		}
		if(z > maxZ){
			z = minZ;
			y++;
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
		nbt.setInteger(KEY_MIN_X, minX);
		nbt.setInteger(KEY_MIN_Y, minY);
		nbt.setInteger(KEY_MIN_Z, minZ);
		nbt.setInteger(KEY_MAX_X, maxX);
		nbt.setInteger(KEY_MAX_Y, maxY);
		nbt.setInteger(KEY_MAX_Z, maxZ);
		nbt.setInteger(KEY_CURRENT_X, x);
		nbt.setInteger(KEY_CURRENT_Y, y);
		nbt.setInteger(KEY_CURRENT_Z, z);
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
		output.writeInt(minX);
		output.writeInt(minZ);
		output.writeInt(maxX);
		output.writeInt(maxY);
		output.writeInt(maxZ);
	}
}