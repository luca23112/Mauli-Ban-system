package de.mauli.ban.model;

import org.bukkit.Material;

import java.time.Duration;

public record BanDurationOption(String id, String display, Material material, String rawInput, Duration duration, boolean permanent) {}
