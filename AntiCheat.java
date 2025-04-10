package com.judith.anticheat;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class AntiCheat extends JavaPlugin implements Listener {

    private final Map<UUID, Location> lastLocations = new HashMap<>();
    private final Map<UUID, Long> lastMoveTime = new HashMap<>();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("SimpleAntiCheat geladen.");
    }

    @Override
    public void onDisable() {
        getLogger().info("SimpleAntiCheat deaktiviert.");
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player p = event.getPlayer();
        UUID uuid = p.getUniqueId();
        Location from = event.getFrom();
        Location to = event.getTo();

        if (to == null || from.getWorld() != to.getWorld()) return;

        double distance = to.distance(from);
        long now = System.currentTimeMillis();

        // Speed Check
        if (distance > 0.7 && !p.isFlying() && !p.getAllowFlight()) {
            getLogger().warning("[Speed] " + p.getName() + " bewegt sich zu schnell: " + distance);
        }

        // Timer Check
        if (lastMoveTime.containsKey(uuid)) {
            long timeDiff = now - lastMoveTime.get(uuid);
            if (timeDiff < 30) {
                getLogger().warning("[Timer] " + p.getName() + " bewegt sich zu häufig: " + timeDiff + "ms");
            }
        }
        lastMoveTime.put(uuid, now);

        // Fly Check
        if (!p.getAllowFlight() && p.getLocation().getY() - from.getY() > 1.2 && !p.getLocation().subtract(0,1,0).getBlock().getType().isSolid()) {
            getLogger().warning("[Fly] " + p.getName() + " fliegt verdächtig.");
        }

        lastLocations.put(uuid, p.getLocation());
    }

    @EventHandler
    public void onFallDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player p) {
            if (event.getCause() == EntityDamageEvent.DamageCause.FALL && event.getDamage() == 0) {
                getLogger().warning("[NoFall] " + p.getName() + " hat keinen Fallschaden erhalten.");
            }
        }
    }
}
