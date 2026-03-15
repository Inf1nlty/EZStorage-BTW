package com.zerofall.ezstorage.init;

import btw.crafting.recipe.RecipeManager;
import btw.item.BTWItems;
import com.zerofall.ezstorage.enums.PortableStoragePanelTier;
import com.zerofall.ezstorage.item.ItemPortableStoragePanel;
import net.minecraft.src.Block;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;

public class EZItems {

	private static final int ID_PORTABLE_STORAGE_PANEL = 23857;

	public static Item portable_storage_panel;

	public static void registerItems()
	{
		portable_storage_panel = new ItemPortableStoragePanel(ID_PORTABLE_STORAGE_PANEL);
	}

	public static void registerRecipes()
	{
		RecipeManager.addRecipe(new ItemStack(portable_storage_panel, 1, PortableStoragePanelTier.TIER_1.meta), new Object[] {"ABA", "BCB", "DBD", 'A', new ItemStack(Block.torchRedstoneActive), 'B', new ItemStack(Block.woodSingleSlab, 1, Short.MAX_VALUE), 'C', new ItemStack(EZBlocks.storage_core), 'D', new ItemStack(Item.ingotGold)});

		RecipeManager.addSoulforgeRecipe(new ItemStack(portable_storage_panel, 1, PortableStoragePanelTier.TIER_INFINITY.meta), new Object[] {"EEEE", "EPPE", "EPPE", "EEEE", 'E', new ItemStack(BTWItems.redstoneEye), 'P', new ItemStack(portable_storage_panel, 1, PortableStoragePanelTier.TIER_1.meta)});
	}
}