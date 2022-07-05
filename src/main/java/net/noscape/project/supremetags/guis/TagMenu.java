package net.noscape.project.supremetags.guis;

import net.noscape.project.supremetags.*;
import net.noscape.project.supremetags.handlers.Tag;
import net.noscape.project.supremetags.handlers.menu.*;
import net.noscape.project.supremetags.storage.*;
import org.bukkit.*;
import org.bukkit.enchantments.*;
import org.bukkit.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;
import org.bukkit.persistence.*;

import java.util.*;

import static net.noscape.project.supremetags.utils.Utils.format;

public class TagMenu extends Paged {

    private final Map<String, Tag> tags;

    public TagMenu(MenuUtil menuUtil) {
        super(menuUtil);

        tags = SupremeTags.getInstance().getTagManager().getTags();
    }

    @Override
    public String getMenuName() {
        return "Tag Menu";
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {

        Player player = (Player) e.getWhoClicked();

        ArrayList<String> tag = new ArrayList<>(tags.keySet());

        if (Objects.requireNonNull(e.getCurrentItem()).getType().equals(Material.NAME_TAG)) {
            if (!ChatColor.stripColor(Objects.requireNonNull(e.getCurrentItem().getItemMeta()).getDisplayName()).startsWith("Active")) {
                String identifier = Objects.requireNonNull(e.getCurrentItem().getItemMeta()).getPersistentDataContainer().get(new NamespacedKey(SupremeTags.getInstance(), "identifier"), PersistentDataType.STRING);
                if (!UserData.getActive(player).equalsIgnoreCase(identifier)) {
                    UserData.setActive(player, identifier);
                    player.closeInventory();
                    new TagMenu(SupremeTags.getMenuUtil(player)).open();
                    menuUtil.setIdentifier(identifier);
                }
            }
        } else if (e.getCurrentItem().getType().equals(Material.BARRIER)) {
            player.closeInventory();
        } else if (e.getCurrentItem().getType().equals(Material.RED_DYE)) {
            UserData.setActive(player, "None");
            player.closeInventory();
            new TagMenu(SupremeTags.getMenuUtil(player)).open();
            menuUtil.setIdentifier("None");
        } else if (e.getCurrentItem().getType().equals(Material.DARK_OAK_BUTTON)) {
            if (ChatColor.stripColor(Objects.requireNonNull(e.getCurrentItem().getItemMeta()).getDisplayName()).equalsIgnoreCase("Back")) {
                if (page != 0) {
                    page = page - 1;
                    super.open();
                }
            } else if (ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName()).equalsIgnoreCase("Next")) {
                if (!((index + 1) >= tag.size())) {
                    page = page + 1;
                    super.open();
                }
            }
        }
    }

    @Override
    public void setMenuItems() {

        addBottom();

        ArrayList<String> tag = new ArrayList<>(tags.keySet());

        if (!tag.isEmpty()) {
            for(int i = 0; i < getMaxItems(); i++) {
                index = getMaxItems() * page + i;
                if(index >= tag.size()) break;
                if (tag.get(index) != null){

                    if (menuUtil.getOwner().hasPermission("playertags.tag." + tag.get(index))) {

                        ItemStack tagItem = new ItemStack(Material.NAME_TAG, 1);
                        ItemMeta tagMeta = tagItem.getItemMeta();
                        assert tagMeta != null;
                        tagMeta.setDisplayName(format("&7Tag: " + tags.get(tag.get(index)).getTag()));

                        tagMeta.getPersistentDataContainer().set(new NamespacedKey(SupremeTags.getInstance(), "identifier"), PersistentDataType.STRING, tags.get(tag.get(index)).getIdentifier());

                        String identifier = Objects.requireNonNull(tagItem.getItemMeta()).getPersistentDataContainer().get(new NamespacedKey(SupremeTags.getInstance(), "identifier"), PersistentDataType.STRING);

                        if (UserData.getActive(menuUtil.getOwner()).equals(identifier)) {
                            tagItem.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 1);
                        }

                        tagMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                        tagMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                        tagMeta.addItemFlags(ItemFlag.HIDE_DYE);
                        tagMeta.addItemFlags(ItemFlag.HIDE_DESTROYS);

                        tagItem.setItemMeta(tagMeta);
                        inventory.addItem(tagItem);

                    }
                }
            }
        }
    }
}
