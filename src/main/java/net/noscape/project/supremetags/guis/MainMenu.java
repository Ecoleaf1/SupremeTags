package net.noscape.project.supremetags.guis;

import net.noscape.project.supremetags.*;
import net.noscape.project.supremetags.handlers.menu.*;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;

import java.util.*;

import static net.noscape.project.supremetags.utils.Utils.*;

public class MainMenu extends Menu {
    private final List<String> catorgies;
    private final Map<Integer, String> dataItem = new HashMap<>();
    private final Map<String, Integer> categoriesTags;

    public MainMenu(MenuUtil menuUtil) {
        super(menuUtil);
        this.catorgies = SupremeTags.getInstance().getCategoryManager().getCatorgies();
        this.categoriesTags = SupremeTags.getInstance().getCategoryManager().getCatorgiesTags();
    }

    @Override
    public String getMenuName() {
        return format(SupremeTags.getInstance().getConfig().getString("gui.main-menu.title"));
    }

    @Override
    public int getSlots() {
        return SupremeTags.getInstance().getConfig().getInt("gui.main-menu.size");
    }

    @Override
    public void handleMenu(InventoryClickEvent clickEvent) {
        String category = dataItem.get(clickEvent.getSlot());
        final FileConfiguration config = SupremeTags.getInstance().getConfig();
        Material categoryMaterial; {
            String materialName = config.getString("categories." + category + ".material");
            if (materialName == null) return;
            categoryMaterial = Material.valueOf(materialName);
        }
        Player player = (Player) clickEvent.getWhoClicked();
        String permission = config.getString("categories." + category + ".permission");

        if (clickEvent.getCurrentItem() == null) return;

        boolean hasMinTags = false;
        for (String cats : getCatorgies()) {
            if (cats == null) continue;
            if (categoriesTags.get(cats) != null) {
                hasMinTags = true;
                break;
            }
        }
        
        if (category == null) return;

        Material clickedMaterial = clickEvent.getCurrentItem().getType();
        if (!clickedMaterial.equals(categoryMaterial)) return;
        if (hasMinTags == false) {
            clickEvent.setCancelled(true);
            msgPlayer(player, "&cThere are no tags in this category.");
            return;
        }

        if (permission == null || player.hasPermission(permission) == false) {
            msgPlayer(player, "&cYou don't have permission to access these tags.");
            return;
        }

        menuUtil.setCategory(category);
        new CategoryMenu(SupremeTags.getMenuUtil(player, category)).open();
        menuUtil.getOwner().updateInventory();
    }

    @Override
    public void setMenuItems() {
        final FileConfiguration config = SupremeTags.getInstance().getConfig();

        // loop through categories items.
        for (String categories : getCatorgies())
            setupMenuIcon(categories, config);

        if (config.getBoolean("categories-menu-fill-empty"))
            fillEmpty();
    }

    private void setupMenuIcon(String category, FileConfiguration config) {
        if (category == null) return;
        boolean canSee = config.getBoolean("categories." + category + ".permission-see-category");
        String permission = config.getString("categories." + category + ".permission");
        Material material; {
            String materialName = config.getString("categories." + category + ".material");
            if (materialName == null) return;
            material = Material.valueOf(materialName);
        }
        int slot = config.getInt("categories." + category + ".slot");
        String displayname = config.getString("categories." + category + ".id_display");

        Player owner = menuUtil.getOwner();
        if (owner.hasPermission(permission) == false && !canSee) return;

        setCatIcon(slot, category, material, displayname);
    }

    private void setCatIcon(int invSlot, String category, Material material, String categoryDisplayName) {
        ItemStack cat_item = new ItemStack(material, 1);
        cat_item.setItemMeta(createCatIconMeta(cat_item, category, categoryDisplayName));
        dataItem.put(invSlot, category);
        inventory.setItem(invSlot, cat_item);
    }
    
    private ItemMeta createCatIconMeta(ItemStack icon, String category, String categoryDisplayName) {
        ItemMeta itemMeta = icon.getItemMeta();
        assert itemMeta != null;
        itemMeta.setDisplayName(format(categoryDisplayName));
        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemMeta.addItemFlags(ItemFlag.HIDE_DYE);
        itemMeta.addItemFlags(ItemFlag.HIDE_DESTROYS);

        itemMeta.setLore(color(getCategoryLore(categoryDisplayName)));
        return itemMeta;
    }
    
    private List<String> getCategoryLore(String category) {
        ArrayList<String> lore = (ArrayList<String>) SupremeTags.getInstance().getConfig().getStringList("categories." + category + ".lore");
        if (categoriesTags.get(category) != null)
            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s)
                .replaceAll("%tags_amount%", String.valueOf(categoriesTags.get(category))));
        else
            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s)
                .replaceAll("%tags_amount%", String.valueOf(0)));
        return lore;
    }

    public List<String> getCatorgies() {
        return catorgies;
    }

    public Map<Integer, String> getDataItem() {
        return dataItem;
    }
}
