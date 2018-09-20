package nl.knokko.builder.mod.command;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import nl.knokko.builder.builders.BuildTask;
import nl.knokko.builder.builders.basic.BuildTaskFill;
import nl.knokko.builder.mod.manager.ForgeBuildManager;
import nl.knokko.util.blocks.BlockPlacer;
import nl.knokko.util.blocks.BlockType;
import nl.knokko.util.forge.blocks.ForgeBlockPlacer;
import nl.knokko.util.forge.blocks.ForgeBlockTypes;
import static java.lang.Math.min;
import static java.lang.Math.max;

public class CommandBuilder extends CommandBase {
	
	private static final List<String> ALIASES = Lists.newArrayList("b", "build", "bt");
	
	private static int parseCoord(String coordinate, int location) throws NumberFormatException {
		if(coordinate.startsWith("~"))
			return location + Integer.parseInt(coordinate.substring(1));
		else
			return Integer.parseInt(coordinate);
	}

	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(args.length == 0){
			sender.sendMessage(new TextComponentString("Use " + getUsage(sender)));
			return;
		}
		BlockPos pos = sender.getPosition();
		if(pos == null){
			sender.sendMessage(new TextComponentString("Only command senders with a position can use this command."));
			return;
		}
		World entityworld = sender.getEntityWorld();
		if(entityworld == null){
			sender.sendMessage(new TextComponentString("Only command sender with a world can use this command."));
			return;
		}
		BlockPlacer world = new ForgeBlockPlacer(entityworld);
		String first = args[0];
		if(first.equals("box")){
			if(args.length >= 8){
				try {
					BlockType block = ForgeBlockTypes.fromString(args[1]);
					if(block != null){
						try {
							int x1 = parseCoord(args[2], pos.getX());
							int y1 = parseCoord(args[3], pos.getY());
							int z1 = parseCoord(args[4], pos.getZ());
							int x2 = parseCoord(args[5], pos.getX());
							int y2 = parseCoord(args[6], pos.getY());
							int z2 = parseCoord(args[7], pos.getZ());
							String name = null;
							int blocksPerTick = -1;
							int delay = -1;
							if(args.length >= 9)
								name = args[8];
							if(args.length >= 10){
								try {
									blocksPerTick = Integer.parseInt(args[9]);
								} catch(NumberFormatException nfe){
									sender.sendMessage(new TextComponentString("You should enter an integer instead of '" + args[9] + "'"));
									return;
								}
							}
							if(args.length >= 11){
								try {
									delay = Integer.parseInt(args[10]);
								} catch(NumberFormatException nfe){
									sender.sendMessage(new TextComponentString("You should enter an integer instead of '" + args[10] + "'"));
									return;
								}
							}
							try {
								if(name == null)
									ForgeBuildManager.get(entityworld).add(new BuildTaskFill(world, block, min(x1, x2), min(y1, y2), min(z1, z2), max(x1, x2), max(y1, y2), max(z1, z2)));
								else
									ForgeBuildManager.get(entityworld).add(new BuildTaskFill(world, block, min(x1, x2), min(y1, y2), min(z1, z2), max(x1, x2), max(y1, y2), max(z1, z2)), name, blocksPerTick, delay);
							} catch(IllegalArgumentException ex){
								sender.sendMessage(new TextComponentString(ex.getMessage()));
							}
						} catch(NumberFormatException nfe){
							sender.sendMessage(new TextComponentString("You should type 6 integers right after you specify the block."));
						}
					}
					else
						sender.sendMessage(new TextComponentString("There is no block with name " + args[1]));
				} catch(IllegalArgumentException ex){
					sender.sendMessage(new TextComponentString(ex.getMessage()));
				}
			}
			else
				sender.sendMessage(new TextComponentString("Use /build box <block> <x1> <y1> <z1> <x2> <y2> <z2> [name] [blocks per tick] [delay]"));
		}
	}

	public String getName() {
		return "buildtask";
	}

	public String getUsage(ICommandSender sender) {
		return "/build box";
	}
	
	@Override
	public int getRequiredPermissionLevel(){
		return 2;
	}
	
	@Override
	public List<String> getAliases(){
		return ALIASES;
	}
}
