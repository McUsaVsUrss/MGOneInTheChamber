package com.comze_instancelabs.oitc;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import com.comze_instancelabs.minigamesapi.Arena;
import com.comze_instancelabs.minigamesapi.ArenaType;
import com.comze_instancelabs.minigamesapi.MinigamesAPI;
import com.comze_instancelabs.minigamesapi.util.Util;

public class IArena extends Arena {

	public static Main m;

	HashMap<String, Integer> kills = new HashMap<String, Integer>();

	public IArena(Main m, String arena_id) {
		super(m, arena_id, ArenaType.DEFAULT);
		this.m = m;
	}

	@Override
	public void spectate(String playername) {
		//
	}
	
	@Override
	public void stop(){
		kills.clear();
		super.stop();
	}

}
