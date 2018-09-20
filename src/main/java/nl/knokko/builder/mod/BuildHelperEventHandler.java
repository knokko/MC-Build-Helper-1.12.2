package nl.knokko.builder.mod;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import nl.knokko.builder.mod.manager.ForgeBuildManager;

public class BuildHelperEventHandler {

	public BuildHelperEventHandler() {}
	
	@SubscribeEvent
	public void onWorldTick(WorldTickEvent event){
		ForgeBuildManager.get(event.world).update();
	}
}