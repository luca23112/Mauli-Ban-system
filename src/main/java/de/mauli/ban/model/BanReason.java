package de.mauli.ban.model;

import org.bukkit.Material;

import java.util.List;

public record BanReason(String id, String displayName, Material material, List<String> description, String defaultReason) {}
