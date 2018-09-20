package nl.knokko.builder.mod;

import net.minecraft.init.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import nl.knokko.builder.mod.command.CommandBuilder;
import nl.knokko.builder.mod.manager.ForgeBuildManager;

@Mod(modid = BuildHelperMod.MODID, name = BuildHelperMod.NAME, version = BuildHelperMod.VERSION)
public class BuildHelperMod {
    public static final String MODID = "knokkobuilder";
    public static final String NAME = "Build Helper";
    public static final String VERSION = "1.0";
    
    @Instance
    public static BuildHelperMod instance;
    
    @EventHandler
    public void init(FMLInitializationEvent event){
    	MinecraftForge.EVENT_BUS.register(new BuildHelperEventHandler());
    }
    
    @EventHandler
    public void serverStart(FMLServerStartingEvent event){
    	event.registerServerCommand(new CommandBuilder());
    }
}
