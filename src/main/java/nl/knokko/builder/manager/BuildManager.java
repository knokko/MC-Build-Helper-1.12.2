package nl.knokko.builder.manager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nl.knokko.builder.builders.BuildTask;
import nl.knokko.util.blocks.BlockPlacer;

public class BuildManager {
	
	private int defaultDelay;
	private int defaultBlocksPerTick;
	
	private int nextID;
	
	private List<BuildTaskHolder> tasks;

	public BuildManager() {
		tasks = new ArrayList<BuildTaskHolder>();
		defaultDelay = 0;
		defaultBlocksPerTick = 5;
	}
	
	public int getDefaultDelay(){
		return defaultDelay;
	}
	
	public int getDefaultBlocksPerTick(){
		return defaultBlocksPerTick;
	}
	
	public void update(){
		if(!tasks.isEmpty()){
			for(int i = 0; i < tasks.size(); i++){
				tasks.get(i).update();
				if(tasks.get(i).isCompleted()){
					tasks.remove(i);
					i--;
				}
			}
		}
	}
	
	public void add(BuildTask task){
		add(task, "task" + nextID++, -1, -1);
	}
	
	public void add(BuildTask task, String name, int blocksPerTick, int delay){
		if(getByName(name) != null)
			throw new IllegalArgumentException("There is already a task with name " + name);
		tasks.add(new BuildTaskHolder(this, task, name, blocksPerTick, delay));
	}
	
	public BuildTaskHolder getByName(String name){
		for(BuildTaskHolder holder : tasks)
			if(holder.getName().equals(name))
				return holder;
		return null;
	}
	
	public void load(DataInputStream input, BlockPlacer placer) throws IOException {
		defaultDelay = input.readInt();
		defaultBlocksPerTick = input.readInt();
		nextID = input.readInt();
		int taskAmount = input.readInt();
		tasks = new ArrayList<BuildTaskHolder>(taskAmount);
		for(int i = 0; i < taskAmount; i++)
			tasks.add(BuildTaskHolder.fromData(this, placer, input));
	}
	
	public void save(DataOutputStream output) throws IOException {
		output.writeInt(defaultDelay);
		output.writeInt(defaultBlocksPerTick);
		output.writeInt(nextID);
		output.writeInt(tasks.size());
		for(BuildTaskHolder task : tasks)
			task.save(output);
	}
}
