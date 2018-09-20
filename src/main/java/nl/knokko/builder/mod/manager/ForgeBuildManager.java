package nl.knokko.builder.mod.manager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import nl.knokko.builder.builders.BuildTask;
import nl.knokko.builder.manager.BuildManager;
import nl.knokko.builder.manager.BuildTaskHolder;
import nl.knokko.builder.mod.BuildHelperMod;
import nl.knokko.util.forge.blocks.ForgeBlockPlacer;

public class ForgeBuildManager extends WorldSavedData {
	
	/*
	private static final String KEY_DEFAULT_DELAY = "DefaultDelay";
	private static final String KEY_DEFAULT_BLOCKS_PER_TICK = "DefaultBlocksPerTick";
	private static final String KEY_TASK_AMOUNT = "Tasks";
	private static final String KEY_NEXT_ID = "NextID";
	private static final String KEY_PRE_TASK = "Task";
	*/
	
	private static final String KEY_DATA = "Data";
	
	public static final String IDENTIFIER = BuildHelperMod.MODID;
	
	private final ForgeBlockPlacer placer = new ForgeBlockPlacer();
	private final BuildManager manager = new BuildManager();
	
	//private NBTTagCompound nbtToLoad;
	
	public static ForgeBuildManager get(World world){
		MapStorage storage = world.getPerWorldStorage();
		ForgeBuildManager manager = (ForgeBuildManager) storage.getOrLoadData(ForgeBuildManager.class, IDENTIFIER);
		if(manager == null){
			manager = new ForgeBuildManager();
			storage.setData(IDENTIFIER, manager);
		}
		manager.setWorld(world);
		return manager;
	}

	public ForgeBuildManager(String identifier) {
		super(identifier);
	}
	
	public ForgeBuildManager(){
		this(IDENTIFIER);
	}
	
	public int getDefaultDelay(){
		return manager.getDefaultDelay();
	}
	
	public int getDefaultBlocksPerTick(){
		return manager.getDefaultBlocksPerTick();
	}
	
	public void update(){
		manager.update();
	}
	
	public void add(BuildTask task){
		manager.add(task);
	}
	
	public void add(BuildTask task, String name, int blocksPerTick, int delay){
		manager.add(task, name, blocksPerTick, delay);
	}
	
	private void setWorld(World world){
		placer.setWorld(world);
	}
	
	public BuildTaskHolder getByName(String name){
		return manager.getByName(name);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		byte[] data = nbt.getByteArray(KEY_DATA);
		if(data == null){
			System.out.println("Failed to load build task data!");
			return;
		}
		try {
			DataInputStream input = new DataInputStream(new ByteArrayInputStream(data));
			manager.load(input, placer);
		} catch(IOException ioex){
			throw new Error("Failed to load build tasks: " + ioex.getMessage());
		}
		/*
		defaultDelay = nbt.getInteger(KEY_DEFAULT_DELAY);
		defaultBlocksPerTick = nbt.getInteger(KEY_DEFAULT_BLOCKS_PER_TICK);
		nextID = nbt.getInteger(KEY_NEXT_ID);
		int taskAmount = nbt.getInteger(KEY_TASK_AMOUNT);
		tasks = new ArrayList<BuildTaskHolder>(taskAmount);
		for(int i = 0; i < taskAmount; i++)
			tasks.add(BuildTaskHolder.fromNBT(this, world, nbt.getCompoundTag(KEY_PRE_TASK + i)));
			*/
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		try {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			DataOutputStream output = new DataOutputStream(bytes);
			manager.save(output);
			nbt.setByteArray(KEY_DATA, bytes.toByteArray());
		} catch(IOException ioex){
			System.out.println("Failed to save build tasks: " + ioex.getMessage());
			System.out.println("This should not happen!");
		}
		/*
		nbt.setInteger(KEY_DEFAULT_DELAY, defaultDelay);
		nbt.setInteger(KEY_DEFAULT_BLOCKS_PER_TICK, defaultBlocksPerTick);
		nbt.setInteger(KEY_NEXT_ID, nextID);
		nbt.setInteger(KEY_TASK_AMOUNT, tasks.size());
		for(int i = 0; i < tasks.size(); i++)
			nbt.setTag(KEY_PRE_TASK + i, tasks.get(i).toNBT());
		*/
		return nbt;
	}
}