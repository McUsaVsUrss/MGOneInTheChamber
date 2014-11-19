package com.comze_instancelabs.oitc;

import org.bukkit.plugin.java.JavaPlugin;

import com.comze_instancelabs.minigamesapi.config.ClassesConfig;

public class IClassesConfig extends ClassesConfig {

	public IClassesConfig(JavaPlugin plugin) {
		super(plugin, true);
		this.getConfig().options().header("Used for saving classes. Default class:");
		this.getConfig().addDefault("config.kits.default.name", "Default");
		this.getConfig().addDefault("config.kits.default.items", "261*1;262*1;275*1");
		this.getConfig().addDefault("config.kits.default.icon", "262*1");
		this.getConfig().addDefault("config.kits.default.lore", "The Default class.");
		this.getConfig().addDefault("config.kits.default.requires_money", false);
		this.getConfig().addDefault("config.kits.default.requires_permission", false);
		this.getConfig().addDefault("config.kits.default.money_amount", 100);
		this.getConfig().addDefault("config.kits.default.permission_node", "minigames.kits.default");

		this.getConfig().addDefault("config.kits.pro.name", "Pro");
		this.getConfig().addDefault("config.kits.pro.items", "261*1;262*2;258*1");
		this.getConfig().addDefault("config.kits.pro.icon", "262*2");
		this.getConfig().addDefault("config.kits.pro.lore", "The Pro class.");
		this.getConfig().addDefault("config.kits.pro.requires_money", true);
		this.getConfig().addDefault("config.kits.pro.requires_permission", false);
		this.getConfig().addDefault("config.kits.pro.money_amount", 100);
		this.getConfig().addDefault("config.kits.pro.permission_node", "minigames.kits.pro");

		this.getConfig().addDefault("config.kits.uber.name", "Uber");
		this.getConfig().addDefault("config.kits.uber.items", "261*1;262*3;258*1");
		this.getConfig().addDefault("config.kits.uber.icon", "262*3");
		this.getConfig().addDefault("config.kits.uber.lore", "The Uber class.");
		this.getConfig().addDefault("config.kits.uber.requires_money", true);
		this.getConfig().addDefault("config.kits.uber.requires_permission", false);
		this.getConfig().addDefault("config.kits.uber.money_amount", 100);
		this.getConfig().addDefault("config.kits.uber.permission_node", "minigames.kits.uber");
		
		this.getConfig().options().copyDefaults(true);
		this.saveConfig();
	}

}
