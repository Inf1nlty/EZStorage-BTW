package com.zerofall.ezstorage.init;

import btw.block.BTWBlocks;
import btw.crafting.recipe.RecipeManager;
import net.minecraft.src.*;

import com.zerofall.ezstorage.block.BlockCondensedStorage;
import com.zerofall.ezstorage.block.BlockCraftingBox;
import com.zerofall.ezstorage.block.BlockHyperStorage;
import com.zerofall.ezstorage.block.BlockInputPort;
import com.zerofall.ezstorage.block.BlockStorage;
import com.zerofall.ezstorage.block.BlockStorageCable;
import com.zerofall.ezstorage.block.BlockStorageCore;
import com.zerofall.ezstorage.block.BlockStoragePanel;
import com.zerofall.ezstorage.item.ItemBlockCraftingBox;
import com.zerofall.ezstorage.item.ItemBlockStorage;
import com.zerofall.ezstorage.tileentity.TileEntityInventoryProxy;
import com.zerofall.ezstorage.tileentity.TileEntityStorageCore;

public class EZBlocks {

    private static final int ID_STORAGE_CORE = 3906;
    private static final int ID_STORAGE_BOX = 3907;
    private static final int ID_CONDENSED_STORAGE_BOX = 3908;
    private static final int ID_HYPER_STORAGE_BOX = 3909;
    private static final int ID_INPUT_PORT = 3910;
    private static final int ID_CRAFTING_BOX = 3911;
    private static final int ID_STORAGE_PANEL = 3912;
    private static final int ID_STORAGE_CABLE = 3913;

    public static Block storage_core;
    public static Block storage_box;
    public static Block condensed_storage_box;
    public static Block hyper_storage_box;
    public static Block input_port;
    public static Block crafting_box;
    public static Block storage_panel;
    public static Block storage_cable;

    public static void registerBlocks() {

        storage_core = new BlockStorageCore(ID_STORAGE_CORE);
        TileEntity.addMapping(TileEntityStorageCore.class, "TileEntityStorageCore");
        Item.itemsList[ID_STORAGE_CORE] = new ItemBlock(storage_core.blockID - 256);

        storage_box = new BlockStorage(ID_STORAGE_BOX);
        Item.itemsList[ID_STORAGE_BOX] = new ItemBlockStorage(storage_box.blockID - 256);

        condensed_storage_box = new BlockCondensedStorage(ID_CONDENSED_STORAGE_BOX);
        Item.itemsList[ID_CONDENSED_STORAGE_BOX] = new ItemBlockStorage(condensed_storage_box.blockID - 256);

        hyper_storage_box = new BlockHyperStorage(ID_HYPER_STORAGE_BOX);
        Item.itemsList[ID_HYPER_STORAGE_BOX] = new ItemBlockStorage(hyper_storage_box.blockID - 256);

        input_port = new BlockInputPort(ID_INPUT_PORT);
        TileEntity.addMapping(TileEntityInventoryProxy.class, "TileEntityInputPort");
        Item.itemsList[ID_INPUT_PORT] = new ItemBlock(input_port.blockID - 256);

        crafting_box = new BlockCraftingBox(ID_CRAFTING_BOX);
        Item.itemsList[ID_CRAFTING_BOX] = new ItemBlockCraftingBox(crafting_box.blockID - 256);

        storage_panel = new BlockStoragePanel(ID_STORAGE_PANEL);
        Item.itemsList[ID_STORAGE_PANEL] = new ItemBlock(storage_panel.blockID - 256);

        storage_cable = new BlockStorageCable(ID_STORAGE_CABLE);
        Item.itemsList[ID_STORAGE_CABLE] = new ItemBlock(storage_cable.blockID - 256);
    }

    public static void registerRecipes() {
        RecipeManager.addRecipe(new ItemStack(storage_core), new Object[] {"BCB", "ABA", 'A', new ItemStack(Block.wood), 'B', new ItemStack(Item.stick), 'C', new ItemStack(BTWBlocks.chest)});
        RecipeManager.addRecipe(new ItemStack(storage_box), new Object[] {"ABA", "BCB", "ABA", 'A', new ItemStack(Block.wood), 'B', new ItemStack(Block.planks), 'C', new ItemStack(BTWBlocks.chest)});
        RecipeManager.addRecipe(new ItemStack(condensed_storage_box), new Object[] {"ACA", "EBE", "DCD", 'A', new ItemStack(Item.ingotIron), 'B', new ItemStack(storage_box), 'C', new ItemStack(Item.ingotGold), 'D', new ItemStack(Item.ingotIron), 'E', new ItemStack(BTWBlocks.chest)});
        RecipeManager.addRecipe(new ItemStack(hyper_storage_box), new Object[] {"ABA", "ACA", "AAA", 'A', new ItemStack(Block.obsidian), 'B', new ItemStack(Item.netherStar), 'C', new ItemStack(condensed_storage_box)});
        RecipeManager.addRecipe(new ItemStack(input_port), new Object[] {" A ", " B ", " C ", 'A', new ItemStack(BTWBlocks.hopper), 'B', new ItemStack(Block.pistonBase), 'C', new ItemStack(Block.blockNetherQuartz)});

        RecipeManager.addRecipe(new ItemStack(crafting_box, 1, 0), new Object[] {" A ", " B ", " C ", 'A', new ItemStack(Item.enderPearl), 'B', new ItemStack(Block.anvil, 1, 0), 'C', new ItemStack(Item.diamond)});


        RecipeManager.addRecipe(new ItemStack(storage_panel), new Object[] {"ABA", "BCB", "ABA", 'A', new ItemStack(Block.wood), 'B', new ItemStack(Item.stick), 'C', new ItemStack(Block.planks)});
        RecipeManager.addRecipe(new ItemStack(storage_cable, 16), new Object[] {"ABA", "BBB", "ABA", 'A', new ItemStack(Block.wood), 'B', new ItemStack(Item.stick)});
    }

}