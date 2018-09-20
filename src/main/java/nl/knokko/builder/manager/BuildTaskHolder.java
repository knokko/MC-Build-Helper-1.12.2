package nl.knokko.builder.manager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import nl.knokko.builder.builders.BuildTask;
import nl.knokko.util.blocks.BlockPlacer;

public class BuildTaskHolder {
	
	/*
	private static final String KEY_DELAY = "Delay";
	private static final String KEY_BLOCKS_PER_TICK = "BlocksPerTick";
	private static final String KEY_COUNTER = "Counter";
	private static final String KEY_TASK = "Task";
	private static final String KEY_NAME = "TaskName";
	*/
	
	private final BuildTask task;
	private final BuildManager manager;
	
	private String name;
	
	private int delay;
	private int blocksPerTick;
	
	private int counter;
	
	/*
	public static BuildTaskHolder fromNBT(BuildManager manager, World world, NBTTagCompound nbt){
		BuildTask task = BuildTask.fromNBT(world, nbt.getCompoundTag(KEY_TASK));
		int delay = nbt.getInteger(KEY_DELAY);
		int blocksPerTick = nbt.getInteger(KEY_BLOCKS_PER_TICK);
		int counter = nbt.getInteger(KEY_COUNTER);
		String name = nbt.getString(KEY_NAME);
		BuildTaskHolder holder = new BuildTaskHolder(manager, task, name, delay, blocksPerTick);
		holder.counter = counter;
		return holder;
	}
	*/
	
	public static BuildTaskHolder fromData(BuildManager manager, BlockPlacer world, DataInputStream input) throws IOException {
		int delay = input.readInt();
		int blocksPerTick = input.readInt();
		int counter = input.readInt();
		String name = input.readUTF();
		BuildTask task = BuildTask.fromData(world, input);
		return new BuildTaskHolder(manager, task, name, delay, blocksPerTick, counter);
	}

	public BuildTaskHolder(BuildManager manager, BuildTask task, String name, int delay, int blocksPerTick, int counter) {
		this.manager = manager;
		this.task = task;
		this.delay = delay;
		this.blocksPerTick = blocksPerTick;
		this.name = name;
		this.counter = counter;
	}
	
	public BuildTaskHolder(BuildManager manager, BuildTask task, String name, int delay, int blocksPerTick){
		this(manager, task, name, delay, blocksPerTick, 0);
	}
	
	public BuildTaskHolder(BuildManager manager, BuildTask task, String name){
		this(manager, task, name, -1, -1);
	}
	
	public void update(){
		int del = getDelay();
		if(counter <= del){
			counter = 0;
			int bpt = getBlocksPerTick();
			for(int i = 0; i < bpt; i++){
				task.place();
				if(task.completed())
					return;
			}
		}
		else
			counter++;
	}
	
	public void save(DataOutputStream output) throws IOException {
		output.writeInt(delay);
		output.writeInt(blocksPerTick);
		output.writeInt(counter);
		output.writeUTF(name);
		task.save(output);
	}
	
	public int getDelay(){
		return delay == -1 ? manager.getDefaultDelay() : delay;
	}
	
	public int getBlocksPerTick(){
		return blocksPerTick == -1 ? manager.getDefaultBlocksPerTick() : blocksPerTick;
	}
	
	public boolean isCompleted(){
		return task.completed();
	}
	
	public String getName(){
		return name;
	}
	
	/*
	public NBTTagCompound toNBT(){
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger(KEY_DELAY, delay);
		nbt.setInteger(KEY_BLOCKS_PER_TICK, blocksPerTick);
		nbt.setInteger(KEY_COUNTER, counter);
		nbt.setString(KEY_NAME, name);
		nbt.setTag(KEY_TASK, task.toNBT());
		return nbt;
	}
	*/
}