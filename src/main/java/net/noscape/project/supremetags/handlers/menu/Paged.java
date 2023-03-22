package net.noscape.project.supremetags.handlers.menu;

import de.tr7zw.nbtapi.NBTItem;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import net.noscape.project.supremetags.*;
import net.noscape.project.supremetags.handlers.Tag;
import net.noscape.project.supremetags.storage.*;
import org.bukkit.Material;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

import static net.noscape.project.supremetags.utils.Utils.color;
import static net.noscape.project.supremetags.utils.Utils.format;

public abstract class Paged extends Menu {

    protected int page = 0;
    protected int maxItems = 35;
    protected int index = 0;

    public Paged(MenuUtil menuUtil) {
        super(menuUtil);
    }

    public void applyLayout() {
        if (SupremeTags.getLayout().equalsIgnoreCase("layout1")) {
            inventory.setItem(48, makeItem(Material.valueOf(Objects.requireNonNull(SupremeTags.getInstance().getConfig().getString("gui.layout.back-next-material")).toUpperCase()), ChatColor.GRAY + "Back"));

            inventory.setItem(49, makeItem(Material.valueOf(Objects.requireNonNull(SupremeTags.getInstance().getConfig().getString("gui.layout.close-menu-material")).toUpperCase()), ChatColor.RED + "Close"));

            inventory.setItem(50, makeItem(Material.valueOf(Objects.requireNonNull(SupremeTags.getInstance().getConfig().getString("gui.layout.back-next-material")).toUpperCase()), ChatColor.GRAY + "Next"));

            if (!SupremeTags.getInstance().getConfig().getBoolean("settings.forced-tag")) {
                inventory.setItem(46, makeItem(Material.valueOf(Objects.requireNonNull(SupremeTags.getInstance().getConfig().getString("gui.layout.reset-tag-material")).toUpperCase()), ChatColor.RED + "Reset Tag"));
            }

            inventory.setItem(45, makeItem(Material.valueOf(Objects.requireNonNull(SupremeTags.getInstance().getConfig().getString("gui.layout.refresh-material")).toUpperCase()), ChatColor.GREEN + "Refresh Menu"));

            inventory.setItem(52, makeItem(Material.valueOf(Objects.requireNonNull(SupremeTags.getInstance().getConfig().getString("gui.layout.tag-material")).toUpperCase()), format("&7Active: &6" + UserData.getActive(menuUtil.getOwner().getUniqueId()))));

            inventory.setItem(36, super.GLASS);
            inventory.setItem(37, super.GLASS);
            inventory.setItem(38, super.GLASS);
            inventory.setItem(39, super.GLASS);
            inventory.setItem(40, super.GLASS);
            inventory.setItem(41, super.GLASS);
            inventory.setItem(42, super.GLASS);
            inventory.setItem(43, super.GLASS);
            inventory.setItem(44, super.GLASS);
        } else if (SupremeTags.getLayout().equalsIgnoreCase("layout2")) {
            inventory.setItem(3, makeItem(Material.valueOf(Objects.requireNonNull(SupremeTags.getInstance().getConfig().getString("gui.layout.back-next-material")).toUpperCase()), ChatColor.GRAY + "Back"));

            inventory.setItem(4, makeItem(Material.valueOf(Objects.requireNonNull(SupremeTags.getInstance().getConfig().getString("gui.layout.close-menu-material")).toUpperCase()), ChatColor.RED + "Close"));

            inventory.setItem(5, makeItem(Material.valueOf(Objects.requireNonNull(SupremeTags.getInstance().getConfig().getString("gui.layout.back-next-material")).toUpperCase()), ChatColor.GRAY + "Next"));

            if (!SupremeTags.getInstance().getConfig().getBoolean("settings.forced-tag")) {
                inventory.setItem(1, makeItem(Material.valueOf(Objects.requireNonNull(SupremeTags.getInstance().getConfig().getString("gui.layout.reset-tag-material")).toUpperCase()), ChatColor.RED + "Reset Tag"));
            } else {
                inventory.setItem(1, super.GLASS);
            }

            inventory.setItem(0, makeItem(Material.valueOf(Objects.requireNonNull(SupremeTags.getInstance().getConfig().getString("gui.layout.refresh-material")).toUpperCase()), ChatColor.GREEN + "Refresh Menu"));

            inventory.setItem(7, makeItem(Material.valueOf(Objects.requireNonNull(SupremeTags.getInstance().getConfig().getString("gui.layout.tag-material")).toUpperCase()), format("&7Active: &6" + UserData.getActive(menuUtil.getOwner().getUniqueId()))));

            inventory.setItem(2, super.GLASS);
            inventory.setItem(6, super.GLASS);
            inventory.setItem(8, super.GLASS);
            inventory.setItem(9, super.GLASS);
            inventory.setItem(10, super.GLASS);
            inventory.setItem(11, super.GLASS);
            inventory.setItem(12, super.GLASS);
            inventory.setItem(13, super.GLASS);
            inventory.setItem(14, super.GLASS);
            inventory.setItem(15, super.GLASS);
            inventory.setItem(16, super.GLASS);
            inventory.setItem(17, super.GLASS);
        }
    }

    protected int getPage() {
        return page + 1;
    }

    public int getMaxItems() {
        return maxItems;
    }

    public void getTagItemsCost() {
        Map<String, Tag> tags = SupremeTags.getInstance().getTagManager().getTags();

        ArrayList<Tag> tag = new ArrayList<>(tags.values());

        if (!tag.isEmpty()) {
            int maxItemsPerPage = 36;
            int startIndex = page * maxItemsPerPage;
            int endIndex = Math.min(startIndex + maxItemsPerPage, tag.size());

            for (int i = startIndex; i < endIndex; i++) {
                Tag t = tag.get(i);
                if (t == null) continue;

                String permission = SupremeTags.getInstance().getConfig().getString("tags." + t.getIdentifier() + ".permission");
                double cost = SupremeTags.getInstance().getConfig().getDouble("tags." + t.getIdentifier() + ".cost");

                String displayname;

                if (SupremeTags.getInstance().getConfig().getString("tags." + t.getIdentifier() + ".displayname") != null) {
                    displayname = Objects.requireNonNull(SupremeTags.getInstance().getConfig().getString("tags." + t.getIdentifier() + ".displayname")).replace("%tag%", t.getTag());
                } else {
                    displayname = format("&7Tag: " + t.getTag());
                }

                String material;

                if (SupremeTags.getInstance().getConfig().getString("tags." + t.getIdentifier() + ".display-item") != null) {
                    material = SupremeTags.getInstance().getConfig().getString("tags." + t.getIdentifier() + ".display-item");
                } else {
                    material = "NAME_TAG";
                }

                assert permission != null;

                // toggle if they don't have permission
                if (menuUtil.getOwner().hasPermission(permission) && !permission.equalsIgnoreCase("none") || t.getCost() == 0) {
                    if (UserData.getActive(menuUtil.getOwner().getUniqueId()).equalsIgnoreCase(t.getIdentifier())) {

                        if (material.contains("hdb-")) {

                            HeadDatabaseAPI api = new HeadDatabaseAPI();

                            int id = Integer.parseInt(material.replaceAll("hdb-", ""));

                            ItemStack tagItem = api.getItemHead(String.valueOf(id));
                            ItemMeta tagMeta = tagItem.getItemMeta();
                            assert tagMeta != null;

                            NBTItem nbt = new NBTItem(tagItem);

                            nbt.setString("identifier", tags.get(tag.get(index)).getIdentifier());

                            tagMeta.setDisplayName(format(displayname));

                            if (SupremeTags.getInstance().getConfig().getBoolean("settings.active-tag-glow")) {
                                nbt.getItem().addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
                            }

                            tagMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                            tagMeta.addItemFlags(ItemFlag.HIDE_DYE);
                            tagMeta.addItemFlags(ItemFlag.HIDE_DESTROYS);
                            tagMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

                            // set lore
                            ArrayList<String> lore = (ArrayList<String>) SupremeTags.getInstance().getConfig().getStringList("gui.tag-menu-none-categories.tag-item.unlocked-lore");
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%description%", format(Objects.requireNonNull(SupremeTags.getInstance().getConfig().getString("tags." + t.getIdentifier() + ".description")))));
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%identifier%", t.getIdentifier()));
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%tag%", t.getTag()));
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%cost%", String.valueOf(t.getCost())));

                            tagMeta.setLore(color(lore));

                            nbt.getItem().setItemMeta(tagMeta);

                            nbt.setString("identifier", t.getIdentifier());
                            inventory.addItem(nbt.getItem());
                        } else {
                            ItemStack tagItem = new ItemStack(Material.valueOf(material.toUpperCase()), 1);
                            ItemMeta tagMeta = tagItem.getItemMeta();
                            assert tagMeta != null;

                            NBTItem nbt = new NBTItem(tagItem);

                            nbt.setString("identifier", t.getIdentifier());

                            tagMeta.setDisplayName(format(displayname));
                            tagMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                            tagMeta.addItemFlags(ItemFlag.HIDE_DYE);
                            tagMeta.addItemFlags(ItemFlag.HIDE_DESTROYS);
                            tagMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

                            // set lore
                            ArrayList<String> lore = (ArrayList<String>) SupremeTags.getInstance().getConfig().getStringList("gui.tag-menu-none-categories.tag-item.unlocked-lore");
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%description%", format(Objects.requireNonNull(SupremeTags.getInstance().getConfig().getString("tags." + t.getIdentifier() + ".description")))));
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%identifier%", t.getIdentifier()));
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%tag%", t.getTag()));
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%cost%", String.valueOf(t.getCost())));
                            tagMeta.setLore(color(lore));

                            nbt.getItem().setItemMeta(tagMeta);

                            nbt.setString("identifier", t.getIdentifier());;
                            inventory.addItem(nbt.getItem());
                        }
                    } else {
                        if (material.contains("hdb-")) {

                            int id = Integer.parseInt(material.replaceAll("hdb-", ""));

                            HeadDatabaseAPI api = new HeadDatabaseAPI();

                            ItemStack tagItem = api.getItemHead(String.valueOf(id));
                            ItemMeta tagMeta = tagItem.getItemMeta();
                            assert tagMeta != null;

                            NBTItem nbt = new NBTItem(tagItem);

                            nbt.setString("identifier", t.getIdentifier());

                            tagMeta.setDisplayName(format(displayname));

                            tagMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                            tagMeta.addItemFlags(ItemFlag.HIDE_DYE);
                            tagMeta.addItemFlags(ItemFlag.HIDE_DESTROYS);
                            tagMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

                            // set lore
                            ArrayList<String> lore = (ArrayList<String>) SupremeTags.getInstance().getConfig().getStringList("gui.tag-menu-none-categories.tag-item.unlocked-lore");
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%description%", format(Objects.requireNonNull(SupremeTags.getInstance().getConfig().getString("tags." + t.getIdentifier() + ".description")))));
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%identifier%", t.getIdentifier()));
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%tag%", t.getTag()));
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%cost%", String.valueOf(t.getCost())));

                            tagMeta.setLore(color(lore));

                            nbt.getItem().setItemMeta(tagMeta);

                            nbt.setString("identifier", t.getIdentifier());
                            inventory.addItem(nbt.getItem());
                        } else {
                            ItemStack tagItem = new ItemStack(Material.valueOf(material.toUpperCase()), 1);
                            ItemMeta tagMeta = tagItem.getItemMeta();
                            assert tagMeta != null;

                            NBTItem nbt = new NBTItem(tagItem);

                            nbt.setString("identifier", t.getIdentifier());

                            tagMeta.setDisplayName(format(displayname));
                            tagMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                            tagMeta.addItemFlags(ItemFlag.HIDE_DYE);
                            tagMeta.addItemFlags(ItemFlag.HIDE_DESTROYS);
                            tagMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

                            // set lore
                            ArrayList<String> lore = (ArrayList<String>) SupremeTags.getInstance().getConfig().getStringList("gui.tag-menu-none-categories.tag-item.unlocked-lore");
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%description%", format(Objects.requireNonNull(SupremeTags.getInstance().getConfig().getString("tags." + t.getIdentifier() + ".description")))));
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%identifier%", t.getIdentifier()));
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%tag%", t.getTag()));
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%cost%", String.valueOf(t.getCost())));

                            tagMeta.setLore(color(lore));

                            nbt.getItem().setItemMeta(tagMeta);

                            nbt.setString("identifier", t.getIdentifier());
                            inventory.addItem(nbt.getItem());
                        }
                    }


                    // if permission == none
                } else if (!menuUtil.getOwner().hasPermission(permission) && permission.equalsIgnoreCase("none")) {
                    if (UserData.getActive(menuUtil.getOwner().getUniqueId()).equalsIgnoreCase(t.getIdentifier())) {

                        if (material.contains("hdb-")) {

                            int id = Integer.parseInt(material.replaceAll("hdb-", ""));

                            HeadDatabaseAPI api = new HeadDatabaseAPI();

                            ItemStack tagItem = api.getItemHead(String.valueOf(id));
                            ItemMeta tagMeta = tagItem.getItemMeta();
                            assert tagMeta != null;

                            NBTItem nbt = new NBTItem(tagItem);

                            nbt.setString("identifier", t.getIdentifier());

                            tagMeta.setDisplayName(format(displayname));

                            if (SupremeTags.getInstance().getConfig().getBoolean("settings.active-tag-glow")) {
                                nbt.getItem().addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
                            }

                            ArrayList<String> lore = (ArrayList<String>) SupremeTags.getInstance().getConfig().getStringList("gui.tag-menu-none-categories.tag-item.locked-lore");
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%description%", format(Objects.requireNonNull(SupremeTags.getInstance().getConfig().getString("tags." + t.getIdentifier() + ".description")))));
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%identifier%", t.getIdentifier()));
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%tag%", t.getTag()));
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%cost%", String.valueOf(t.getCost())));

                            tagMeta.setLore(color(lore));

                            nbt.getItem().setItemMeta(tagMeta);

                            nbt.setString("identifier", t.getIdentifier());
                            inventory.addItem(nbt.getItem());
                        } else {
                            ItemStack tagItem = new ItemStack(Material.valueOf(material.toUpperCase()), 1);
                            ItemMeta tagMeta = tagItem.getItemMeta();
                            assert tagMeta != null;

                            NBTItem nbt = new NBTItem(tagItem);

                            nbt.setString("identifier", t.getIdentifier());

                            tagMeta.setDisplayName(format(displayname));
                            tagMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                            tagMeta.addItemFlags(ItemFlag.HIDE_DYE);
                            tagMeta.addItemFlags(ItemFlag.HIDE_DESTROYS);
                            tagMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

                            // set lore
                            ArrayList<String> lore = (ArrayList<String>) SupremeTags.getInstance().getConfig().getStringList("gui.tag-menu-none-categories.tag-item.locked-lore");
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%description%", format(Objects.requireNonNull(SupremeTags.getInstance().getConfig().getString("tags." + t.getIdentifier() + ".description")))));
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%identifier%", t.getIdentifier()));
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%tag%", t.getTag()));
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%cost%", String.valueOf(t.getCost())));

                            tagMeta.setLore(color(lore));

                            nbt.getItem().setItemMeta(tagMeta);

                            nbt.setString("identifier", t.getIdentifier());
                            inventory.addItem(nbt.getItem());
                        }
                    } else {
                        if (material.contains("hdb-")) {

                            int id = Integer.parseInt(material.replaceAll("hdb-", ""));

                            HeadDatabaseAPI api = new HeadDatabaseAPI();

                            ItemStack tagItem = api.getItemHead(String.valueOf(id));
                            ItemMeta tagMeta = tagItem.getItemMeta();
                            assert tagMeta != null;

                            NBTItem nbt = new NBTItem(tagItem);

                            nbt.setString("identifier", t.getIdentifier());

                            tagMeta.setDisplayName(format(displayname));

                            tagMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                            tagMeta.addItemFlags(ItemFlag.HIDE_DYE);
                            tagMeta.addItemFlags(ItemFlag.HIDE_DESTROYS);
                            tagMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

                            // set lore
                            ArrayList<String> lore = (ArrayList<String>) SupremeTags.getInstance().getConfig().getStringList("gui.tag-menu-none-categories.tag-item.locked-lore");
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%description%", format(Objects.requireNonNull(SupremeTags.getInstance().getConfig().getString("tags." + t.getIdentifier() + ".description")))));
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%identifier%", t.getIdentifier()));
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%tag%", t.getTag()));
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%cost%", String.valueOf(t.getCost())));

                            tagMeta.setLore(color(lore));

                            nbt.getItem().setItemMeta(tagMeta);

                            nbt.setString("identifier", t.getIdentifier());
                            inventory.addItem(nbt.getItem());
                        } else {
                            ItemStack tagItem = new ItemStack(Material.valueOf(material.toUpperCase()), 1);
                            ItemMeta tagMeta = tagItem.getItemMeta();
                            assert tagMeta != null;

                            NBTItem nbt = new NBTItem(tagItem);

                            nbt.setString("identifier", t.getIdentifier());

                            tagMeta.setDisplayName(format(displayname));
                            tagMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                            tagMeta.addItemFlags(ItemFlag.HIDE_DYE);
                            tagMeta.addItemFlags(ItemFlag.HIDE_DESTROYS);
                            tagMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

                            // set lore
                            ArrayList<String> lore = (ArrayList<String>) SupremeTags.getInstance().getConfig().getStringList("gui.tag-menu-none-categories.tag-item.locked-lore");
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%description%", format(Objects.requireNonNull(SupremeTags.getInstance().getConfig().getString("tags." + t.getIdentifier() + ".description")))));
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%identifier%", t.getIdentifier()));
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%tag%", t.getTag()));
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%cost%", String.valueOf(t.getCost())));
                            tagMeta.setLore(color(lore));

                            nbt.getItem().setItemMeta(tagMeta);

                            nbt.setString("identifier", t.getIdentifier());
                            inventory.addItem(nbt.getItem());
                        }
                    }
                } else if (!menuUtil.getOwner().hasPermission(permission)) {
                    if (UserData.getActive(menuUtil.getOwner().getUniqueId()).equalsIgnoreCase(t.getIdentifier())) {

                        if (material.contains("hdb-")) {

                            int id = Integer.parseInt(material.replaceAll("hdb-", ""));

                            HeadDatabaseAPI api = new HeadDatabaseAPI();

                            ItemStack tagItem = api.getItemHead(String.valueOf(id));
                            ItemMeta tagMeta = tagItem.getItemMeta();
                            assert tagMeta != null;

                            NBTItem nbt = new NBTItem(tagItem);

                            nbt.setString("identifier", t.getIdentifier());

                            tagMeta.setDisplayName(format(displayname));

                            if (SupremeTags.getInstance().getConfig().getBoolean("settings.active-tag-glow")) {
                                nbt.getItem().addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
                            }

                            ArrayList<String> lore = (ArrayList<String>) SupremeTags.getInstance().getConfig().getStringList("gui.tag-menu-none-categories.tag-item.locked-lore");
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%description%", format(Objects.requireNonNull(SupremeTags.getInstance().getConfig().getString("tags." + t.getIdentifier() + ".description")))));
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%identifier%", t.getIdentifier()));
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%tag%", t.getTag()));
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%cost%", String.valueOf(t.getCost())));

                            tagMeta.setLore(color(lore));

                            nbt.getItem().setItemMeta(tagMeta);

                            nbt.setString("identifier", t.getIdentifier());
                            inventory.addItem(nbt.getItem());
                        } else {
                            ItemStack tagItem = new ItemStack(Material.valueOf(material.toUpperCase()), 1);
                            ItemMeta tagMeta = tagItem.getItemMeta();
                            assert tagMeta != null;

                            NBTItem nbt = new NBTItem(tagItem);

                            nbt.setString("identifier", t.getIdentifier());

                            tagMeta.setDisplayName(format(displayname));
                            tagMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                            tagMeta.addItemFlags(ItemFlag.HIDE_DYE);
                            tagMeta.addItemFlags(ItemFlag.HIDE_DESTROYS);
                            tagMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

                            // set lore
                            ArrayList<String> lore = (ArrayList<String>) SupremeTags.getInstance().getConfig().getStringList("gui.tag-menu-none-categories.tag-item.locked-lore");
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%description%", format(Objects.requireNonNull(SupremeTags.getInstance().getConfig().getString("tags." + t.getIdentifier() + ".description")))));
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%identifier%", t.getIdentifier()));
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%tag%", t.getTag()));
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%cost%", String.valueOf(t.getCost())));

                            tagMeta.setLore(color(lore));

                            nbt.getItem().setItemMeta(tagMeta);

                            nbt.setString("identifier", t.getIdentifier());
                            inventory.addItem(nbt.getItem());
                        }
                    } else {
                        if (material.contains("hdb-")) {

                            int id = Integer.parseInt(material.replaceAll("hdb-", ""));

                            HeadDatabaseAPI api = new HeadDatabaseAPI();

                            ItemStack tagItem = api.getItemHead(String.valueOf(id));
                            ItemMeta tagMeta = tagItem.getItemMeta();
                            assert tagMeta != null;

                            NBTItem nbt = new NBTItem(tagItem);

                            nbt.setString("identifier", t.getIdentifier());

                            tagMeta.setDisplayName(format(displayname));

                            tagMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                            tagMeta.addItemFlags(ItemFlag.HIDE_DYE);
                            tagMeta.addItemFlags(ItemFlag.HIDE_DESTROYS);
                            tagMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

                            // set lore
                            ArrayList<String> lore = (ArrayList<String>) SupremeTags.getInstance().getConfig().getStringList("gui.tag-menu-none-categories.tag-item.locked-lore");
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%description%", format(Objects.requireNonNull(SupremeTags.getInstance().getConfig().getString("tags." + t.getIdentifier() + ".description")))));
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%identifier%", t.getIdentifier()));
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%tag%", t.getTag()));
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%cost%", String.valueOf(t.getCost())));

                            tagMeta.setLore(color(lore));

                            nbt.getItem().setItemMeta(tagMeta);

                            nbt.setString("identifier", t.getIdentifier());
                            inventory.addItem(nbt.getItem());
                        } else {
                            ItemStack tagItem = new ItemStack(Material.valueOf(material.toUpperCase()), 1);
                            ItemMeta tagMeta = tagItem.getItemMeta();
                            assert tagMeta != null;

                            NBTItem nbt = new NBTItem(tagItem);

                            nbt.setString("identifier", t.getIdentifier());

                            tagMeta.setDisplayName(format(displayname));
                            tagMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                            tagMeta.addItemFlags(ItemFlag.HIDE_DYE);
                            tagMeta.addItemFlags(ItemFlag.HIDE_DESTROYS);
                            tagMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

                            // set lore
                            ArrayList<String> lore = (ArrayList<String>) SupremeTags.getInstance().getConfig().getStringList("gui.tag-menu-none-categories.tag-item.locked-lore");
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%description%", format(Objects.requireNonNull(SupremeTags.getInstance().getConfig().getString("tags." + t.getIdentifier() + ".description")))));
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%identifier%", t.getIdentifier()));
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%tag%", t.getTag()));
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%cost%", String.valueOf(t.getCost())));
                            tagMeta.setLore(color(lore));

                            nbt.getItem().setItemMeta(tagMeta);

                            nbt.setString("identifier", t.getIdentifier());
                            inventory.addItem(nbt.getItem());
                        }
                    }
                }
            }
        }
    }


    // ===================================================================================

    public void getTagItemsNoneCost() {
        Map<String, Tag> tags = SupremeTags.getInstance().getTagManager().getTags();

        ArrayList<Tag> tag = new ArrayList<>(tags.values());

        if (!tag.isEmpty()) {
            int maxItemsPerPage = 36;
            int startIndex = page * maxItemsPerPage;
            int endIndex = Math.min(startIndex + maxItemsPerPage, tag.size());

            for (int i = startIndex; i < endIndex; i++) {
                Tag t = tag.get(i);
                if (t == null) continue;

                String permission = SupremeTags.getInstance().getConfig().getString("tags." + t.getIdentifier() + ".permission");

                String displayname;

                if (SupremeTags.getInstance().getConfig().getString("tags." + t.getIdentifier() + ".displayname") != null) {
                    displayname = Objects.requireNonNull(SupremeTags.getInstance().getConfig().getString("tags." + t.getIdentifier() + ".displayname")).replace("%tag%", t.getTag());
                } else {
                    displayname = format("&7Tag: " + t.getTag());
                }

                String material;

                if (SupremeTags.getInstance().getConfig().getString("tags." + t.getIdentifier() + ".display-item") != null) {
                    material = SupremeTags.getInstance().getConfig().getString("tags." + t.getIdentifier() + ".display-item");
                } else {
                    material = "NAME_TAG";
                }

                assert permission != null;

                // toggle if they don't have permission
                if (menuUtil.getOwner().hasPermission(permission) && !permission.equalsIgnoreCase("none")) {
                    if (UserData.getActive(menuUtil.getOwner().getUniqueId()).equalsIgnoreCase(t.getIdentifier())) {

                        if (material.contains("hdb-")) {

                            HeadDatabaseAPI api = new HeadDatabaseAPI();

                            int id = Integer.parseInt(material.replaceAll("hdb-", ""));

                            ItemStack tagItem = api.getItemHead(String.valueOf(id));
                            ItemMeta tagMeta = tagItem.getItemMeta();
                            assert tagMeta != null;

                            NBTItem nbt = new NBTItem(tagItem);

                            nbt.setString("identifier", tags.get(tag.get(index)).getIdentifier());

                            tagMeta.setDisplayName(format(displayname));

                            if (SupremeTags.getInstance().getConfig().getBoolean("settings.active-tag-glow")) {
                                nbt.getItem().addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
                            }

                            tagMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                            tagMeta.addItemFlags(ItemFlag.HIDE_DYE);
                            tagMeta.addItemFlags(ItemFlag.HIDE_DESTROYS);
                            tagMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

                            // set lore
                            ArrayList<String> lore = (ArrayList<String>) SupremeTags.getInstance().getConfig().getStringList("gui.tag-menu-none-categories.tag-item.lore");
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%description%", format(Objects.requireNonNull(SupremeTags.getInstance().getConfig().getString("tags." + t.getIdentifier() + ".description")))));
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%identifier%", t.getIdentifier()));
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%tag%", t.getTag()));

                            tagMeta.setLore(color(lore));

                            nbt.getItem().setItemMeta(tagMeta);

                            nbt.setString("identifier", t.getIdentifier());
                            inventory.addItem(nbt.getItem());
                        } else {
                            ItemStack tagItem = new ItemStack(Material.valueOf(material.toUpperCase()), 1);
                            ItemMeta tagMeta = tagItem.getItemMeta();
                            assert tagMeta != null;

                            NBTItem nbt = new NBTItem(tagItem);

                            nbt.setString("identifier", t.getIdentifier());

                            tagMeta.setDisplayName(format(displayname));
                            tagMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                            tagMeta.addItemFlags(ItemFlag.HIDE_DYE);
                            tagMeta.addItemFlags(ItemFlag.HIDE_DESTROYS);
                            tagMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

                            // set lore
                            ArrayList<String> lore = (ArrayList<String>) SupremeTags.getInstance().getConfig().getStringList("gui.tag-menu-none-categories.tag-item.lore");
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%description%", format(Objects.requireNonNull(SupremeTags.getInstance().getConfig().getString("tags." + t.getIdentifier() + ".description")))));
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%identifier%", t.getIdentifier()));
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%tag%", t.getTag()));

                            tagMeta.setLore(color(lore));

                            nbt.getItem().setItemMeta(tagMeta);

                            nbt.setString("identifier", t.getIdentifier());;
                            inventory.addItem(nbt.getItem());
                        }
                    } else {
                        if (material.contains("hdb-")) {

                            int id = Integer.parseInt(material.replaceAll("hdb-", ""));

                            HeadDatabaseAPI api = new HeadDatabaseAPI();

                            ItemStack tagItem = api.getItemHead(String.valueOf(id));
                            ItemMeta tagMeta = tagItem.getItemMeta();
                            assert tagMeta != null;

                            NBTItem nbt = new NBTItem(tagItem);

                            nbt.setString("identifier", t.getIdentifier());

                            tagMeta.setDisplayName(format(displayname));

                            tagMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                            tagMeta.addItemFlags(ItemFlag.HIDE_DYE);
                            tagMeta.addItemFlags(ItemFlag.HIDE_DESTROYS);
                            tagMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

                            // set lore
                            ArrayList<String> lore = (ArrayList<String>) SupremeTags.getInstance().getConfig().getStringList("gui.tag-menu-none-categories.tag-item.lore");
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%description%", format(Objects.requireNonNull(SupremeTags.getInstance().getConfig().getString("tags." + t.getIdentifier() + ".description")))));
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%identifier%", t.getIdentifier()));
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%tag%", t.getTag()));

                            tagMeta.setLore(color(lore));

                            nbt.getItem().setItemMeta(tagMeta);

                            nbt.setString("identifier", t.getIdentifier());
                            inventory.addItem(nbt.getItem());
                        } else {
                            ItemStack tagItem = new ItemStack(Material.valueOf(material.toUpperCase()), 1);
                            ItemMeta tagMeta = tagItem.getItemMeta();
                            assert tagMeta != null;

                            NBTItem nbt = new NBTItem(tagItem);

                            nbt.setString("identifier", t.getIdentifier());

                            tagMeta.setDisplayName(format(displayname));
                            tagMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                            tagMeta.addItemFlags(ItemFlag.HIDE_DYE);
                            tagMeta.addItemFlags(ItemFlag.HIDE_DESTROYS);
                            tagMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

                            // set lore
                            ArrayList<String> lore = (ArrayList<String>) SupremeTags.getInstance().getConfig().getStringList("gui.tag-menu-none-categories.tag-item.lore");
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%description%", format(Objects.requireNonNull(SupremeTags.getInstance().getConfig().getString("tags." + t.getIdentifier() + ".description")))));
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%identifier%", t.getIdentifier()));
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%tag%", t.getTag()));

                            tagMeta.setLore(color(lore));

                            nbt.getItem().setItemMeta(tagMeta);

                            nbt.setString("identifier", t.getIdentifier());
                            inventory.addItem(nbt.getItem());
                        }
                    }


                    // if permission == none
                } else if (!menuUtil.getOwner().hasPermission(permission) && permission.equalsIgnoreCase("none")) {
                    if (UserData.getActive(menuUtil.getOwner().getUniqueId()).equalsIgnoreCase(t.getIdentifier())) {

                        if (material.contains("hdb-")) {

                            int id = Integer.parseInt(material.replaceAll("hdb-", ""));

                            HeadDatabaseAPI api = new HeadDatabaseAPI();

                            ItemStack tagItem = api.getItemHead(String.valueOf(id));
                            ItemMeta tagMeta = tagItem.getItemMeta();
                            assert tagMeta != null;

                            NBTItem nbt = new NBTItem(tagItem);

                            nbt.setString("identifier", t.getIdentifier());

                            tagMeta.setDisplayName(format(displayname));

                            if (SupremeTags.getInstance().getConfig().getBoolean("settings.active-tag-glow")) {
                                nbt.getItem().addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
                            }

                            ArrayList<String> lore = (ArrayList<String>) SupremeTags.getInstance().getConfig().getStringList("gui.tag-menu-none-categories.tag-item.lore");
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%description%", format(Objects.requireNonNull(SupremeTags.getInstance().getConfig().getString("tags." + t.getIdentifier() + ".description")))));
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%identifier%", t.getIdentifier()));
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%tag%", t.getTag()));

                            tagMeta.setLore(color(lore));

                            nbt.getItem().setItemMeta(tagMeta);

                            nbt.setString("identifier", t.getIdentifier());
                            inventory.addItem(nbt.getItem());
                        } else {
                            ItemStack tagItem = new ItemStack(Material.valueOf(material.toUpperCase()), 1);
                            ItemMeta tagMeta = tagItem.getItemMeta();
                            assert tagMeta != null;

                            NBTItem nbt = new NBTItem(tagItem);

                            nbt.setString("identifier", t.getIdentifier());

                            tagMeta.setDisplayName(format(displayname));
                            tagMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                            tagMeta.addItemFlags(ItemFlag.HIDE_DYE);
                            tagMeta.addItemFlags(ItemFlag.HIDE_DESTROYS);
                            tagMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

                            // set lore
                            ArrayList<String> lore = (ArrayList<String>) SupremeTags.getInstance().getConfig().getStringList("gui.tag-menu-none-categories.tag-item.lore");
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%description%", format(Objects.requireNonNull(SupremeTags.getInstance().getConfig().getString("tags." + t.getIdentifier() + ".description")))));
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%identifier%", t.getIdentifier()));
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%tag%", t.getTag()));

                            tagMeta.setLore(color(lore));

                            nbt.getItem().setItemMeta(tagMeta);

                            nbt.setString("identifier", t.getIdentifier());
                            inventory.addItem(nbt.getItem());
                        }
                    } else {
                        if (material.contains("hdb-")) {

                            int id = Integer.parseInt(material.replaceAll("hdb-", ""));

                            HeadDatabaseAPI api = new HeadDatabaseAPI();

                            ItemStack tagItem = api.getItemHead(String.valueOf(id));
                            ItemMeta tagMeta = tagItem.getItemMeta();
                            assert tagMeta != null;

                            NBTItem nbt = new NBTItem(tagItem);

                            nbt.setString("identifier", t.getIdentifier());

                            tagMeta.setDisplayName(format(displayname));

                            tagMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                            tagMeta.addItemFlags(ItemFlag.HIDE_DYE);
                            tagMeta.addItemFlags(ItemFlag.HIDE_DESTROYS);
                            tagMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

                            // set lore
                            ArrayList<String> lore = (ArrayList<String>) SupremeTags.getInstance().getConfig().getStringList("gui.tag-menu-none-categories.tag-item.lore");
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%description%", format(Objects.requireNonNull(SupremeTags.getInstance().getConfig().getString("tags." + t.getIdentifier() + ".description")))));
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%identifier%", t.getIdentifier()));
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%tag%", t.getTag()));

                            tagMeta.setLore(color(lore));

                            nbt.getItem().setItemMeta(tagMeta);

                            nbt.setString("identifier", t.getIdentifier());
                            inventory.addItem(nbt.getItem());
                        } else {
                            ItemStack tagItem = new ItemStack(Material.valueOf(material.toUpperCase()), 1);
                            ItemMeta tagMeta = tagItem.getItemMeta();
                            assert tagMeta != null;

                            NBTItem nbt = new NBTItem(tagItem);

                            nbt.setString("identifier", t.getIdentifier());

                            tagMeta.setDisplayName(format(displayname));
                            tagMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                            tagMeta.addItemFlags(ItemFlag.HIDE_DYE);
                            tagMeta.addItemFlags(ItemFlag.HIDE_DESTROYS);
                            tagMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

                            // set lore
                            ArrayList<String> lore = (ArrayList<String>) SupremeTags.getInstance().getConfig().getStringList("gui.tag-menu-none-categories.tag-item.lore");
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%description%", format(Objects.requireNonNull(SupremeTags.getInstance().getConfig().getString("tags." + t.getIdentifier() + ".description")))));
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%identifier%", t.getIdentifier()));
                            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%tag%", t.getTag()));

                            tagMeta.setLore(color(lore));

                            nbt.getItem().setItemMeta(tagMeta);

                            nbt.setString("identifier", t.getIdentifier());
                            inventory.addItem(nbt.getItem());
                        }
                    }
                }
            }
        }
    }


    // ==================================================================================================

    public void getTagItemsCostCategory() {
        Map<String, Tag> tags = SupremeTags.getInstance().getTagManager().getTags();

        ArrayList<Tag> tag = new ArrayList<>(tags.values());

        if (!tag.isEmpty()) {
            int maxItemsPerPage = 43;
            int startIndex = page * maxItemsPerPage;
            int endIndex = Math.min(startIndex + maxItemsPerPage, tag.size());

            for (int i = startIndex; i < endIndex; i++) {
                Tag t = tag.get(i);
                if (t == null) continue;

                if (t.getCategory().equalsIgnoreCase(menuUtil.getCategory())) {

                    String permission = t.getPermission();

                    String displayname;

                    if (SupremeTags.getInstance().getConfig().getString("tags." + t.getIdentifier() + ".displayname") != null) {
                        displayname = Objects.requireNonNull(SupremeTags.getInstance().getConfig().getString("tags." + t.getIdentifier() + ".displayname")).replace("%tag%", t.getTag());
                    } else {
                        displayname = format("&7Tag: " + t.getTag());
                    }

                    String material;

                    if (SupremeTags.getInstance().getConfig().getString("tags." + t.getIdentifier() + ".display-item") != null) {
                        material = SupremeTags.getInstance().getConfig().getString("tags." + t.getIdentifier() + ".display-item");
                    } else {
                        material = "NAME_TAG";
                    }

                    assert permission != null;

                    // toggle if they don't have permission
                    if (menuUtil.getOwner().hasPermission(permission) && !permission.equalsIgnoreCase("none") || t.getCost() == 0) {
                        if (UserData.getActive(menuUtil.getOwner().getUniqueId()).equalsIgnoreCase(t.getIdentifier())) {

                            if (material.contains("hdb-")) {

                                int id = Integer.parseInt(material.replaceAll("hdb-", ""));

                                HeadDatabaseAPI api = new HeadDatabaseAPI();

                                ItemStack tagItem = api.getItemHead(String.valueOf(id));
                                ItemMeta tagMeta = tagItem.getItemMeta();
                                assert tagMeta != null;

                                NBTItem nbt = new NBTItem(tagItem);

                                nbt.setString("identifier", t.getIdentifier());

                                tagMeta.setDisplayName(format(displayname));

                                tagMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                                tagMeta.addItemFlags(ItemFlag.HIDE_DYE);
                                tagMeta.addItemFlags(ItemFlag.HIDE_DESTROYS);
                                tagMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

                                // set lore
                                ArrayList<String> lore = (ArrayList<String>) SupremeTags.getInstance().getConfig().getStringList("gui.tag-menu-none-categories.tag-item.unlocked-lore");
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%description%", format(Objects.requireNonNull(SupremeTags.getInstance().getConfig().getString("tags." + t.getIdentifier() + ".description")))));
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%identifier%", t.getIdentifier()));
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%tag%", t.getTag()));

                                tagMeta.setLore(color(lore));

                                if (SupremeTags.getInstance().getConfig().getBoolean("settings.active-tag-glow")) {
                                    nbt.getItem().addUnsafeEnchantment(Enchantment.DURABILITY, 1);
                                }

                                nbt.getItem().setItemMeta(tagMeta);

                                nbt.setString("identifier", t.getIdentifier());
                                inventory.addItem(nbt.getItem());
                            } else {
                                ItemStack tagItem = new ItemStack(Material.valueOf(material.toUpperCase()), 1);
                                ItemMeta tagMeta = tagItem.getItemMeta();
                                assert tagMeta != null;

                                NBTItem nbt = new NBTItem(tagItem);

                                nbt.setString("identifier", t.getIdentifier());

                                tagMeta.setDisplayName(format(displayname));
                                tagMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                                tagMeta.addItemFlags(ItemFlag.HIDE_DYE);
                                tagMeta.addItemFlags(ItemFlag.HIDE_DESTROYS);
                                tagMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

                                // set lore
                                ArrayList<String> lore = (ArrayList<String>) SupremeTags.getInstance().getConfig().getStringList("gui.tag-menu-none-categories.tag-item.unlocked-lore");
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%description%", format(Objects.requireNonNull(SupremeTags.getInstance().getConfig().getString("tags." + t.getIdentifier() + ".description")))));
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%identifier%", t.getIdentifier()));
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%tag%", t.getTag()));

                                tagMeta.setLore(color(lore));

                                if (SupremeTags.getInstance().getConfig().getBoolean("settings.active-tag-glow")) {
                                    nbt.getItem().addUnsafeEnchantment(Enchantment.DURABILITY, 1);
                                }

                                nbt.getItem().setItemMeta(tagMeta);

                                nbt.setString("identifier", t.getIdentifier());
                                inventory.addItem(nbt.getItem());
                            }
                        } else {
                            if (material.contains("hdb-")) {

                                int id = Integer.parseInt(material.replaceAll("hdb-", ""));

                                HeadDatabaseAPI api = new HeadDatabaseAPI();

                                ItemStack tagItem = api.getItemHead(String.valueOf(id));
                                ItemMeta tagMeta = tagItem.getItemMeta();
                                assert tagMeta != null;

                                NBTItem nbt = new NBTItem(tagItem);

                                nbt.setString("identifier", t.getIdentifier());

                                tagMeta.setDisplayName(format(displayname));

                                tagMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                                tagMeta.addItemFlags(ItemFlag.HIDE_DYE);
                                tagMeta.addItemFlags(ItemFlag.HIDE_DESTROYS);
                                tagMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

                                // set lore
                                ArrayList<String> lore = (ArrayList<String>) SupremeTags.getInstance().getConfig().getStringList("gui.tag-menu-none-categories.tag-item.unlocked-lore");
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%description%", format(Objects.requireNonNull(SupremeTags.getInstance().getConfig().getString("tags." + t.getIdentifier() + ".description")))));
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%identifier%", t.getIdentifier()));
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%tag%", t.getTag()));
                                tagMeta.setLore(color(lore));

                                nbt.getItem().setItemMeta(tagMeta);

                                nbt.setString("identifier", t.getIdentifier());
                                inventory.addItem(nbt.getItem());
                            } else {
                                ItemStack tagItem = new ItemStack(Material.valueOf(material.toUpperCase()), 1);
                                ItemMeta tagMeta = tagItem.getItemMeta();
                                assert tagMeta != null;

                                NBTItem nbt = new NBTItem(tagItem);

                                nbt.setString("identifier", t.getIdentifier());

                                tagMeta.setDisplayName(format(displayname));
                                tagMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                                tagMeta.addItemFlags(ItemFlag.HIDE_DYE);
                                tagMeta.addItemFlags(ItemFlag.HIDE_DESTROYS);
                                tagMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

                                // set lore
                                ArrayList<String> lore = (ArrayList<String>) SupremeTags.getInstance().getConfig().getStringList("gui.tag-menu-none-categories.tag-item.unlocked-lore");
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%description%", format(Objects.requireNonNull(SupremeTags.getInstance().getConfig().getString("tags." + t.getIdentifier() + ".description")))));
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%identifier%", t.getIdentifier()));
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%tag%", t.getTag()));

                                tagMeta.setLore(color(lore));

                                nbt.getItem().setItemMeta(tagMeta);

                                nbt.setString("identifier", t.getIdentifier());
                                inventory.addItem(nbt.getItem());
                            }
                        }


                        // if permission == none
                    } else if (!menuUtil.getOwner().hasPermission(permission) && permission.equalsIgnoreCase("none")) {
                        if (UserData.getActive(menuUtil.getOwner().getUniqueId()).equalsIgnoreCase(t.getIdentifier())) {

                            if (material.contains("hdb-")) {

                                int id = Integer.parseInt(material.replaceAll("hdb-", ""));

                                HeadDatabaseAPI api = new HeadDatabaseAPI();

                                ItemStack tagItem = api.getItemHead(String.valueOf(id));
                                ItemMeta tagMeta = tagItem.getItemMeta();
                                assert tagMeta != null;

                                NBTItem nbt = new NBTItem(tagItem);

                                nbt.setString("identifier", t.getIdentifier());

                                tagMeta.setDisplayName(format(displayname));

                                tagMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                                tagMeta.addItemFlags(ItemFlag.HIDE_DYE);
                                tagMeta.addItemFlags(ItemFlag.HIDE_DESTROYS);
                                tagMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

                                // set lore
                                ArrayList<String> lore = (ArrayList<String>) SupremeTags.getInstance().getConfig().getStringList("gui.tag-menu-none-categories.tag-item.unlocked-lore");
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%description%", format(Objects.requireNonNull(SupremeTags.getInstance().getConfig().getString("tags." + t.getIdentifier() + ".description")))));
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%identifier%", t.getIdentifier()));
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%tag%", t.getTag()));
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%cost%", String.valueOf(t.getCost())));

                                tagMeta.setLore(color(lore));

                                if (SupremeTags.getInstance().getConfig().getBoolean("settings.active-tag-glow")) {
                                    nbt.getItem().addUnsafeEnchantment(Enchantment.DURABILITY, 1);
                                }

                                nbt.getItem().setItemMeta(tagMeta);

                                nbt.setString("identifier", t.getIdentifier());
                                inventory.addItem(nbt.getItem());
                            } else {
                                ItemStack tagItem = new ItemStack(Material.valueOf(material.toUpperCase()), 1);
                                ItemMeta tagMeta = tagItem.getItemMeta();
                                assert tagMeta != null;

                                NBTItem nbt = new NBTItem(tagItem);

                                nbt.setString("identifier", t.getIdentifier());

                                tagMeta.setDisplayName(format(displayname));
                                tagMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                                tagMeta.addItemFlags(ItemFlag.HIDE_DYE);
                                tagMeta.addItemFlags(ItemFlag.HIDE_DESTROYS);

                                // set lore
                                ArrayList<String> lore = (ArrayList<String>) SupremeTags.getInstance().getConfig().getStringList("gui.tag-menu-none-categories.tag-item.unlocked-lore");
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%description%", format(Objects.requireNonNull(SupremeTags.getInstance().getConfig().getString("tags." + t.getIdentifier() + ".description")))));
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%identifier%", t.getIdentifier()));
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%tag%", t.getTag()));
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%cost%", String.valueOf(t.getCost())));

                                tagMeta.setLore(color(lore));

                                if (SupremeTags.getInstance().getConfig().getBoolean("settings.active-tag-glow")) {
                                    nbt.getItem().addUnsafeEnchantment(Enchantment.DURABILITY, 1);
                                }

                                nbt.getItem().setItemMeta(tagMeta);

                                nbt.setString("identifier", t.getIdentifier());
                                inventory.addItem(nbt.getItem());
                            }
                        } else {
                            if (material.contains("hdb-")) {

                                int id = Integer.parseInt(material.replaceAll("hdb-", ""));

                                HeadDatabaseAPI api = new HeadDatabaseAPI();

                                ItemStack tagItem = api.getItemHead(String.valueOf(id));
                                ItemMeta tagMeta = tagItem.getItemMeta();
                                assert tagMeta != null;

                                NBTItem nbt = new NBTItem(tagItem);

                                nbt.setString("identifier", t.getIdentifier());

                                tagMeta.setDisplayName(format(displayname));

                                tagMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                                tagMeta.addItemFlags(ItemFlag.HIDE_DYE);
                                tagMeta.addItemFlags(ItemFlag.HIDE_DESTROYS);
                                tagMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

                                // set lore
                                ArrayList<String> lore = (ArrayList<String>) SupremeTags.getInstance().getConfig().getStringList("gui.tag-menu-none-categories.tag-item.unlocked-lore");
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%description%", format(Objects.requireNonNull(SupremeTags.getInstance().getConfig().getString("tags." + t.getIdentifier() + ".description")))));
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%identifier%", t.getIdentifier()));
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%tag%", t.getTag()));
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%cost%", String.valueOf(t.getCost())));


                                tagMeta.setLore(color(lore));

                                nbt.getItem().setItemMeta(tagMeta);

                                nbt.setString("identifier", t.getIdentifier());
                                inventory.addItem(nbt.getItem());
                            } else {
                                ItemStack tagItem = new ItemStack(Material.valueOf(material.toUpperCase()), 1);
                                ItemMeta tagMeta = tagItem.getItemMeta();
                                assert tagMeta != null;

                                NBTItem nbt = new NBTItem(tagItem);

                                nbt.setString("identifier", t.getIdentifier());

                                tagMeta.setDisplayName(format(displayname));
                                tagMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                                tagMeta.addItemFlags(ItemFlag.HIDE_DYE);
                                tagMeta.addItemFlags(ItemFlag.HIDE_DESTROYS);
                                tagMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

                                // set lore
                                ArrayList<String> lore = (ArrayList<String>) SupremeTags.getInstance().getConfig().getStringList("gui.tag-menu-none-categories.tag-item.unlocked-lore");
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%description%", format(Objects.requireNonNull(SupremeTags.getInstance().getConfig().getString("tags." + t.getIdentifier() + ".description")))));
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%identifier%", t.getIdentifier()));
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%tag%", t.getTag()));
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%cost%", String.valueOf(t.getCost())));


                                tagMeta.setLore(color(lore));

                                nbt.getItem().setItemMeta(tagMeta);

                                nbt.setString("identifier", t.getIdentifier());
                                inventory.addItem(nbt.getItem());
                            }
                        }
                    } else if (!menuUtil.getOwner().hasPermission(permission)) {
                        if (UserData.getActive(menuUtil.getOwner().getUniqueId()).equalsIgnoreCase(t.getIdentifier())) {

                            if (material.contains("hdb-")) {

                                int id = Integer.parseInt(material.replaceAll("hdb-", ""));

                                HeadDatabaseAPI api = new HeadDatabaseAPI();

                                ItemStack tagItem = api.getItemHead(String.valueOf(id));
                                ItemMeta tagMeta = tagItem.getItemMeta();
                                assert tagMeta != null;

                                NBTItem nbt = new NBTItem(tagItem);

                                nbt.setString("identifier", t.getIdentifier());

                                tagMeta.setDisplayName(format(displayname));

                                tagMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                                tagMeta.addItemFlags(ItemFlag.HIDE_DYE);
                                tagMeta.addItemFlags(ItemFlag.HIDE_DESTROYS);
                                tagMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

                                // set lore
                                ArrayList<String> lore = (ArrayList<String>) SupremeTags.getInstance().getConfig().getStringList("gui.tag-menu-none-categories.tag-item.locked-lore");
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%description%", format(Objects.requireNonNull(SupremeTags.getInstance().getConfig().getString("tags." + t.getIdentifier() + ".description")))));
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%identifier%", t.getIdentifier()));
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%tag%", t.getTag()));
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%cost%", String.valueOf(t.getCost())));

                                tagMeta.setLore(color(lore));

                                if (SupremeTags.getInstance().getConfig().getBoolean("settings.active-tag-glow")) {
                                    nbt.getItem().addUnsafeEnchantment(Enchantment.DURABILITY, 1);
                                }

                                nbt.getItem().setItemMeta(tagMeta);

                                nbt.setString("identifier", t.getIdentifier());
                                inventory.addItem(nbt.getItem());
                            } else {
                                ItemStack tagItem = new ItemStack(Material.valueOf(material.toUpperCase()), 1);
                                ItemMeta tagMeta = tagItem.getItemMeta();
                                assert tagMeta != null;

                                NBTItem nbt = new NBTItem(tagItem);

                                nbt.setString("identifier", t.getIdentifier());

                                tagMeta.setDisplayName(format(displayname));
                                tagMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                                tagMeta.addItemFlags(ItemFlag.HIDE_DYE);
                                tagMeta.addItemFlags(ItemFlag.HIDE_DESTROYS);

                                // set lore
                                ArrayList<String> lore = (ArrayList<String>) SupremeTags.getInstance().getConfig().getStringList("gui.tag-menu-none-categories.tag-item.locked-lore");
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%description%", format(Objects.requireNonNull(SupremeTags.getInstance().getConfig().getString("tags." + t.getIdentifier() + ".description")))));
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%identifier%", t.getIdentifier()));
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%tag%", t.getTag()));
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%cost%", String.valueOf(t.getCost())));

                                tagMeta.setLore(color(lore));

                                if (SupremeTags.getInstance().getConfig().getBoolean("settings.active-tag-glow")) {
                                    nbt.getItem().addUnsafeEnchantment(Enchantment.DURABILITY, 1);
                                }

                                nbt.getItem().setItemMeta(tagMeta);

                                nbt.setString("identifier", t.getIdentifier());
                                inventory.addItem(nbt.getItem());
                            }
                        } else {
                            if (material.contains("hdb-")) {

                                int id = Integer.parseInt(material.replaceAll("hdb-", ""));

                                HeadDatabaseAPI api = new HeadDatabaseAPI();

                                ItemStack tagItem = api.getItemHead(String.valueOf(id));
                                ItemMeta tagMeta = tagItem.getItemMeta();
                                assert tagMeta != null;

                                NBTItem nbt = new NBTItem(tagItem);

                                nbt.setString("identifier", t.getIdentifier());

                                tagMeta.setDisplayName(format(displayname));

                                tagMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                                tagMeta.addItemFlags(ItemFlag.HIDE_DYE);
                                tagMeta.addItemFlags(ItemFlag.HIDE_DESTROYS);
                                tagMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

                                // set lore
                                ArrayList<String> lore = (ArrayList<String>) SupremeTags.getInstance().getConfig().getStringList("gui.tag-menu-none-categories.tag-item.locked-lore");
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%description%", format(Objects.requireNonNull(SupremeTags.getInstance().getConfig().getString("tags." + t.getIdentifier() + ".description")))));
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%identifier%", t.getIdentifier()));
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%tag%", t.getTag()));
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%cost%", String.valueOf(t.getCost())));


                                tagMeta.setLore(color(lore));

                                nbt.getItem().setItemMeta(tagMeta);

                                nbt.setString("identifier", t.getIdentifier());
                                inventory.addItem(nbt.getItem());
                            } else {
                                ItemStack tagItem = new ItemStack(Material.valueOf(material.toUpperCase()), 1);
                                ItemMeta tagMeta = tagItem.getItemMeta();
                                assert tagMeta != null;

                                NBTItem nbt = new NBTItem(tagItem);

                                nbt.setString("identifier", t.getIdentifier());

                                tagMeta.setDisplayName(format(displayname));
                                tagMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                                tagMeta.addItemFlags(ItemFlag.HIDE_DYE);
                                tagMeta.addItemFlags(ItemFlag.HIDE_DESTROYS);
                                tagMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

                                // set lore
                                ArrayList<String> lore = (ArrayList<String>) SupremeTags.getInstance().getConfig().getStringList("gui.tag-menu-none-categories.tag-item.locked-lore");
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%description%", format(Objects.requireNonNull(SupremeTags.getInstance().getConfig().getString("tags." + t.getIdentifier() + ".description")))));
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%identifier%", t.getIdentifier()));
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%tag%", t.getTag()));
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%cost%", String.valueOf(t.getCost())));

                                tagMeta.setLore(color(lore));

                                nbt.getItem().setItemMeta(tagMeta);

                                nbt.setString("identifier", t.getIdentifier());
                                inventory.addItem(nbt.getItem());
                            }
                        }
                    }
                }
            }
        }
    }


    // ===========================================================

    public void getTagItemsNoneCostCategory() {
        Map<String, Tag> tags = SupremeTags.getInstance().getTagManager().getTags();

        ArrayList<Tag> tag = new ArrayList<>(tags.values());

        if (!tag.isEmpty()) {
            int maxItemsPerPage = 43;
            int startIndex = page * maxItemsPerPage;
            int endIndex = Math.min(startIndex + maxItemsPerPage, tag.size());

            for (int i = startIndex; i < endIndex; i++) {
                Tag t = tag.get(i);
                if (t == null) continue;

                if (t.getCategory().equalsIgnoreCase(menuUtil.getCategory())) {

                    String permission = t.getPermission();

                    String displayname;

                    if (SupremeTags.getInstance().getConfig().getString("tags." + t.getIdentifier() + ".displayname") != null) {
                        displayname = Objects.requireNonNull(SupremeTags.getInstance().getConfig().getString("tags." + t.getIdentifier() + ".displayname")).replace("%tag%", t.getTag());
                    } else {
                        displayname = format("&7Tag: " + t.getTag());
                    }

                    String material;

                    if (SupremeTags.getInstance().getConfig().getString("tags." + t.getIdentifier() + ".display-item") != null) {
                        material = SupremeTags.getInstance().getConfig().getString("tags." + t.getIdentifier() + ".display-item");
                    } else {
                        material = "NAME_TAG";
                    }

                    assert permission != null;

                    // toggle if they don't have permission
                    if (menuUtil.getOwner().hasPermission(permission) && !permission.equalsIgnoreCase("none")) {
                        if (UserData.getActive(menuUtil.getOwner().getUniqueId()).equalsIgnoreCase(t.getIdentifier())) {

                            if (material.contains("hdb-")) {

                                int id = Integer.parseInt(material.replaceAll("hdb-", ""));

                                HeadDatabaseAPI api = new HeadDatabaseAPI();

                                ItemStack tagItem = api.getItemHead(String.valueOf(id));
                                ItemMeta tagMeta = tagItem.getItemMeta();
                                assert tagMeta != null;

                                NBTItem nbt = new NBTItem(tagItem);

                                nbt.setString("identifier", t.getIdentifier());

                                tagMeta.setDisplayName(format(displayname));

                                tagMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                                tagMeta.addItemFlags(ItemFlag.HIDE_DYE);
                                tagMeta.addItemFlags(ItemFlag.HIDE_DESTROYS);
                                tagMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

                                // set lore
                                ArrayList<String> lore = (ArrayList<String>) SupremeTags.getInstance().getConfig().getStringList("gui.tag-menu-none-categories.tag-item.lore");
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%description%", format(Objects.requireNonNull(SupremeTags.getInstance().getConfig().getString("tags." + t.getIdentifier() + ".description")))));
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%identifier%", t.getIdentifier()));
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%tag%", t.getTag()));

                                tagMeta.setLore(color(lore));

                                if (SupremeTags.getInstance().getConfig().getBoolean("settings.active-tag-glow")) {
                                    nbt.getItem().addUnsafeEnchantment(Enchantment.DURABILITY, 1);
                                }

                                nbt.getItem().setItemMeta(tagMeta);

                                nbt.setString("identifier", t.getIdentifier());
                                inventory.addItem(nbt.getItem());
                            } else {
                                ItemStack tagItem = new ItemStack(Material.valueOf(material.toUpperCase()), 1);
                                ItemMeta tagMeta = tagItem.getItemMeta();
                                assert tagMeta != null;

                                NBTItem nbt = new NBTItem(tagItem);

                                nbt.setString("identifier", t.getIdentifier());

                                tagMeta.setDisplayName(format(displayname));
                                tagMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                                tagMeta.addItemFlags(ItemFlag.HIDE_DYE);
                                tagMeta.addItemFlags(ItemFlag.HIDE_DESTROYS);
                                tagMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

                                // set lore
                                ArrayList<String> lore = (ArrayList<String>) SupremeTags.getInstance().getConfig().getStringList("gui.tag-menu-none-categories.tag-item.lore");
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%description%", format(Objects.requireNonNull(SupremeTags.getInstance().getConfig().getString("tags." + t.getIdentifier() + ".description")))));
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%identifier%", t.getIdentifier()));
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%tag%", t.getTag()));

                                tagMeta.setLore(color(lore));

                                if (SupremeTags.getInstance().getConfig().getBoolean("settings.active-tag-glow")) {
                                    nbt.getItem().addUnsafeEnchantment(Enchantment.DURABILITY, 1);
                                }

                                nbt.getItem().setItemMeta(tagMeta);

                                nbt.setString("identifier", t.getIdentifier());
                                inventory.addItem(nbt.getItem());
                            }
                        } else {
                            if (material.contains("hdb-")) {

                                int id = Integer.parseInt(material.replaceAll("hdb-", ""));

                                HeadDatabaseAPI api = new HeadDatabaseAPI();

                                ItemStack tagItem = api.getItemHead(String.valueOf(id));
                                ItemMeta tagMeta = tagItem.getItemMeta();
                                assert tagMeta != null;

                                NBTItem nbt = new NBTItem(tagItem);

                                nbt.setString("identifier", t.getIdentifier());

                                tagMeta.setDisplayName(format(displayname));

                                tagMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                                tagMeta.addItemFlags(ItemFlag.HIDE_DYE);
                                tagMeta.addItemFlags(ItemFlag.HIDE_DESTROYS);
                                tagMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

                                // set lore
                                ArrayList<String> lore = (ArrayList<String>) SupremeTags.getInstance().getConfig().getStringList("gui.tag-menu-none-categories.tag-item.lore");
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%description%", format(Objects.requireNonNull(SupremeTags.getInstance().getConfig().getString("tags." + t.getIdentifier() + ".description")))));
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%identifier%", t.getIdentifier()));
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%tag%", t.getTag()));
                                tagMeta.setLore(color(lore));

                                nbt.getItem().setItemMeta(tagMeta);

                                nbt.setString("identifier", t.getIdentifier());
                                inventory.addItem(nbt.getItem());
                            } else {
                                ItemStack tagItem = new ItemStack(Material.valueOf(material.toUpperCase()), 1);
                                ItemMeta tagMeta = tagItem.getItemMeta();
                                assert tagMeta != null;

                                NBTItem nbt = new NBTItem(tagItem);

                                nbt.setString("identifier", t.getIdentifier());

                                tagMeta.setDisplayName(format(displayname));
                                tagMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                                tagMeta.addItemFlags(ItemFlag.HIDE_DYE);
                                tagMeta.addItemFlags(ItemFlag.HIDE_DESTROYS);
                                tagMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

                                // set lore
                                ArrayList<String> lore = (ArrayList<String>) SupremeTags.getInstance().getConfig().getStringList("gui.tag-menu-none-categories.tag-item.lore");
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%description%", format(Objects.requireNonNull(SupremeTags.getInstance().getConfig().getString("tags." + t.getIdentifier() + ".description")))));
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%identifier%", t.getIdentifier()));
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%tag%", t.getTag()));

                                tagMeta.setLore(color(lore));

                                nbt.getItem().setItemMeta(tagMeta);

                                nbt.setString("identifier", t.getIdentifier());
                                inventory.addItem(nbt.getItem());
                            }
                        }


                        // if permission == none
                    } else if (!menuUtil.getOwner().hasPermission(permission) && permission.equalsIgnoreCase("none")) {
                        if (UserData.getActive(menuUtil.getOwner().getUniqueId()).equalsIgnoreCase(t.getIdentifier())) {

                            if (material.contains("hdb-")) {

                                int id = Integer.parseInt(material.replaceAll("hdb-", ""));

                                HeadDatabaseAPI api = new HeadDatabaseAPI();

                                ItemStack tagItem = api.getItemHead(String.valueOf(id));
                                ItemMeta tagMeta = tagItem.getItemMeta();
                                assert tagMeta != null;

                                NBTItem nbt = new NBTItem(tagItem);

                                nbt.setString("identifier", t.getIdentifier());

                                tagMeta.setDisplayName(format(displayname));

                                tagMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                                tagMeta.addItemFlags(ItemFlag.HIDE_DYE);
                                tagMeta.addItemFlags(ItemFlag.HIDE_DESTROYS);
                                tagMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

                                // set lore
                                ArrayList<String> lore = (ArrayList<String>) SupremeTags.getInstance().getConfig().getStringList("gui.tag-menu-none-categories.tag-item.lore");
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%description%", format(Objects.requireNonNull(SupremeTags.getInstance().getConfig().getString("tags." + t.getIdentifier() + ".description")))));
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%identifier%", t.getIdentifier()));
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%tag%", t.getTag()));

                                tagMeta.setLore(color(lore));

                                if (SupremeTags.getInstance().getConfig().getBoolean("settings.active-tag-glow")) {
                                    nbt.getItem().addUnsafeEnchantment(Enchantment.DURABILITY, 1);
                                }

                                nbt.getItem().setItemMeta(tagMeta);

                                nbt.setString("identifier", t.getIdentifier());
                                inventory.addItem(nbt.getItem());
                            } else {
                                ItemStack tagItem = new ItemStack(Material.valueOf(material.toUpperCase()), 1);
                                ItemMeta tagMeta = tagItem.getItemMeta();
                                assert tagMeta != null;

                                NBTItem nbt = new NBTItem(tagItem);

                                nbt.setString("identifier", t.getIdentifier());

                                tagMeta.setDisplayName(format(displayname));
                                tagMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                                tagMeta.addItemFlags(ItemFlag.HIDE_DYE);
                                tagMeta.addItemFlags(ItemFlag.HIDE_DESTROYS);

                                // set lore
                                ArrayList<String> lore = (ArrayList<String>) SupremeTags.getInstance().getConfig().getStringList("gui.tag-menu-none-categories.tag-item.lore");
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%description%", format(Objects.requireNonNull(SupremeTags.getInstance().getConfig().getString("tags." + t.getIdentifier() + ".description")))));
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%identifier%", t.getIdentifier()));
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%tag%", t.getTag()));

                                tagMeta.setLore(color(lore));

                                if (SupremeTags.getInstance().getConfig().getBoolean("settings.active-tag-glow")) {
                                    nbt.getItem().addUnsafeEnchantment(Enchantment.DURABILITY, 1);
                                }

                                nbt.getItem().setItemMeta(tagMeta);

                                nbt.setString("identifier", t.getIdentifier());
                                inventory.addItem(nbt.getItem());
                            }
                        } else {
                            if (material.contains("hdb-")) {

                                int id = Integer.parseInt(material.replaceAll("hdb-", ""));

                                HeadDatabaseAPI api = new HeadDatabaseAPI();

                                ItemStack tagItem = api.getItemHead(String.valueOf(id));
                                ItemMeta tagMeta = tagItem.getItemMeta();
                                assert tagMeta != null;

                                NBTItem nbt = new NBTItem(tagItem);

                                nbt.setString("identifier", t.getIdentifier());

                                tagMeta.setDisplayName(format(displayname));

                                tagMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                                tagMeta.addItemFlags(ItemFlag.HIDE_DYE);
                                tagMeta.addItemFlags(ItemFlag.HIDE_DESTROYS);
                                tagMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

                                // set lore
                                ArrayList<String> lore = (ArrayList<String>) SupremeTags.getInstance().getConfig().getStringList("gui.tag-menu-none-categories.tag-item.lore");
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%description%", format(Objects.requireNonNull(SupremeTags.getInstance().getConfig().getString("tags." + t.getIdentifier() + ".description")))));
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%identifier%", t.getIdentifier()));
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%tag%", t.getTag()));

                                tagMeta.setLore(color(lore));

                                nbt.getItem().setItemMeta(tagMeta);

                                nbt.setString("identifier", t.getIdentifier());
                                inventory.addItem(nbt.getItem());
                            } else {
                                ItemStack tagItem = new ItemStack(Material.valueOf(material.toUpperCase()), 1);
                                ItemMeta tagMeta = tagItem.getItemMeta();
                                assert tagMeta != null;

                                NBTItem nbt = new NBTItem(tagItem);

                                nbt.setString("identifier", t.getIdentifier());

                                tagMeta.setDisplayName(format(displayname));
                                tagMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                                tagMeta.addItemFlags(ItemFlag.HIDE_DYE);
                                tagMeta.addItemFlags(ItemFlag.HIDE_DESTROYS);
                                tagMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

                                // set lore
                                ArrayList<String> lore = (ArrayList<String>) SupremeTags.getInstance().getConfig().getStringList("gui.tag-menu-none-categories.tag-item.lore");
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%description%", format(Objects.requireNonNull(SupremeTags.getInstance().getConfig().getString("tags." + t.getIdentifier() + ".description")))));
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%identifier%", t.getIdentifier()));
                                lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s).replaceAll("%tag%", t.getTag()));

                                tagMeta.setLore(color(lore));

                                nbt.getItem().setItemMeta(tagMeta);

                                nbt.setString("identifier", t.getIdentifier());
                                inventory.addItem(nbt.getItem());
                            }
                        }
                    }
                }
            }
        }
    }

}
