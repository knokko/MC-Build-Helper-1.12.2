package nl.knokko.builder.builders;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

import nl.knokko.builder.builders.basic.BuildTaskCilinderY;
import nl.knokko.builder.builders.basic.BuildTaskFill;
import nl.knokko.builder.builders.building.BuildTaskFlat;
import nl.knokko.util.blocks.BlockPlacer;
import nl.knokko.util.blocks.BlockType;

public abstract class BuildTask {
	
	private static final String KEY_CLASS_ID = "ClassID";
	private static final String KEY_OWN = "OwnData";
	private static final String KEY_QUEUE_LENGTH = "QueueLength";
	private static final String KEY_PRE_TASK = "Task";
	
	protected BuildTask[] queuedTasks;
	
	protected BlockPlacer world;
	
	/*
	public static BuildTask fromNBT(World world, NBTTagCompound nbt){
		int queueLength = nbt.getInteger(KEY_QUEUE_LENGTH);
		BuildTask[] tasks = new BuildTask[queueLength];
		for(int i = 0; i < queueLength; i++)
			tasks[i] = fromNBT(world, nbt.getCompoundTag(KEY_PRE_TASK + i));
		if(queueLength == 0)
			tasks = null;
		NBTTagCompound sub = nbt.getCompoundTag(KEY_OWN);
		short classID = nbt.getShort(KEY_CLASS_ID);
		BuildTask task;
		if(classID == BuildTaskFill.CLASS_ID)
			task = BuildTaskFill.fromNBT(world, sub);
		else if(classID == BuildTaskFlat.CLASS_ID)
			task = BuildTaskFlat.fromNBT(world, sub);
		else
			throw new IllegalArgumentException("Invalid class ID: " + classID);
		task.queuedTasks = tasks;
		return task;
	}
	*/
	
	public static BuildTask fromData(BlockPlacer world, DataInputStream input) throws IOException {
		int queueLength = input.readInt();
		BuildTask[] tasks;
		if(queueLength > 0){
			tasks = new BuildTask[queueLength];
			for(int i = 0; i < queueLength; i++)
				tasks[i] = fromData(world, input);
		}
		else
			tasks = null;
		short classID = input.readShort();
		BuildTask task;
		if(classID == BuildTaskFill.CLASS_ID)
			task = BuildTaskFill.fromData(world, input);
		else if(classID == BuildTaskFlat.CLASS_ID)
			task = BuildTaskFlat.fromData(world, input);
		else if(classID == BuildTaskCilinderY.CLASS_ID)
			task = BuildTaskCilinderY.fromData(world, input);
		else
			throw new IllegalArgumentException("Invalid class ID: " + classID);
		task.queuedTasks = tasks;
		return task;
	}
	
	public BuildTask(BlockPlacer world){
		if(world == null)
			throw new NullPointerException("world is null");
		this.world = world;
	}
	
	protected abstract void placeNext();
	
	public boolean completed(){
		return queuedTasks == null && isCompleted();
	}
	
	protected abstract boolean isCompleted();
	
	public void place(){
		if(queuedTasks != null){
			queuedTasks[0].place();
			if(queuedTasks[0].isCompleted()){
				if(queuedTasks.length == 1)
					queuedTasks = null;
				else
					queuedTasks = Arrays.copyOfRange(queuedTasks, 1, queuedTasks.length);
			}
		}
		else
			placeNext();
	}
	
	/*
	public NBTTagCompound toNBT(){
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger(KEY_QUEUE_LENGTH, queuedTasks != null ? queuedTasks.length : 0);
		if(queuedTasks != null){
			for(int i = 0; i < queuedTasks.length; i++){
				nbt.setTag(KEY_PRE_TASK + i, queuedTasks[i].toNBT());
			}
		}
		nbt.setShort(KEY_CLASS_ID, getClassID());
		nbt.setTag(KEY_OWN, ownNBT());
		return nbt;
	}
	*/
	
	public void save(DataOutputStream output) throws IOException {
		output.writeInt(queuedTasks != null ? queuedTasks.length : 0);
		if(queuedTasks != null)
			for(BuildTask task : queuedTasks)
				task.save(output);
		output.writeShort(getClassID());
		saveOwn(output);
	}
	
	//protected abstract NBTTagCompound ownNBT();
	
	protected abstract void saveOwn(DataOutputStream output) throws IOException;
	
	protected abstract short getClassID();
	
	protected void queue(BuildTask...tasks){
		if(queuedTasks == null)
			queuedTasks = tasks;
		else {
			queuedTasks = Arrays.copyOf(queuedTasks, queuedTasks.length + tasks.length);
			System.arraycopy(tasks, 0, queuedTasks, queuedTasks.length - tasks.length, tasks.length);
		}
	}
	
	protected void queueDirectly(BuildTask...tasks){
		if(queuedTasks == null)
			queuedTasks = tasks;
		else {
			BuildTask[] newTasks = new BuildTask[queuedTasks.length + tasks.length];
			System.arraycopy(tasks, 0, newTasks, 0, tasks.length);
			System.arraycopy(queuedTasks, 0, newTasks, tasks.length, queuedTasks.length);
			queuedTasks = newTasks;
		}
	}
	
	protected void fill(BlockType block, int minX, int minY, int minZ, int maxX, int maxY, int maxZ){
		queue(new BuildTaskFill(world, block, minX, minY, minZ, maxX, maxY, maxZ));
	}
}