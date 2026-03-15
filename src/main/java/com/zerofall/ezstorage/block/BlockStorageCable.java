package com.zerofall.ezstorage.block;

import com.zerofall.ezstorage.Reference;
import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.Block;
import net.minecraft.src.Entity;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.Material;
import net.minecraft.src.MovingObjectPosition;
import net.minecraft.src.Vec3;
import net.minecraft.src.World;

import java.util.List;

public class BlockStorageCable extends StorageMultiblock {

    public static final float CORE_MIN = 6.0F / 16.0F;
    public static final float CORE_MAX = 10.0F / 16.0F;

    public BlockStorageCable(int id) {
        super(id, "storage_cable", Material.wood);
        this.setTextureName(Reference.MOD_ID + ":inventory_cable");
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
    }

    @Override
    public AxisAlignedBB getBlockBoundsFromPoolBasedOnState(IBlockAccess world, int x, int y, int z) {
        float minX = CORE_MIN;
        float minY = CORE_MIN;
        float minZ = CORE_MIN;
        float maxX = CORE_MAX;
        float maxY = CORE_MAX;
        float maxZ = CORE_MAX;

        if (canConnectToBlock(world, x - 1, y, z)) {
            minX = 0.0F;
        }
        if (canConnectToBlock(world, x + 1, y, z)) {
            maxX = 1.0F;
        }
        if (canConnectToBlock(world, x, y - 1, z)) {
            minY = 0.0F;
        }
        if (canConnectToBlock(world, x, y + 1, z)) {
            maxY = 1.0F;
        }
        if (canConnectToBlock(world, x, y, z - 1)) {
            minZ = 0.0F;
        }
        if (canConnectToBlock(world, x, y, z + 1)) {
            maxZ = 1.0F;
        }

        return AxisAlignedBB.getAABBPool().getAABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public void setBlockBoundsForItemRender() {
    }

    @Override
    public AxisAlignedBB getBlockBoundsFromPoolForItemRender(int iItemDamage) {
        return AxisAlignedBB.getAABBPool().getAABB(CORE_MIN, CORE_MIN, CORE_MIN, CORE_MAX, CORE_MAX, CORE_MAX);
    }

    @Override
    public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB mask, List list, Entity entity) {
        addCollisionSegment(mask, list, x, y, z, CORE_MIN, CORE_MIN, CORE_MIN, CORE_MAX, CORE_MAX, CORE_MAX);

        if (canConnectToBlock(world, x, y - 1, z)) {
            addCollisionSegment(mask, list, x, y, z, CORE_MIN, 0.0F, CORE_MIN, CORE_MAX, CORE_MIN, CORE_MAX);
        }
        if (canConnectToBlock(world, x, y + 1, z)) {
            addCollisionSegment(mask, list, x, y, z, CORE_MIN, CORE_MAX, CORE_MIN, CORE_MAX, 1.0F, CORE_MAX);
        }
        if (canConnectToBlock(world, x, y, z - 1)) {
            addCollisionSegment(mask, list, x, y, z, CORE_MIN, CORE_MIN, 0.0F, CORE_MAX, CORE_MAX, CORE_MIN);
        }
        if (canConnectToBlock(world, x, y, z + 1)) {
            addCollisionSegment(mask, list, x, y, z, CORE_MIN, CORE_MIN, CORE_MAX, CORE_MAX, CORE_MAX, 1.0F);
        }
        if (canConnectToBlock(world, x - 1, y, z)) {
            addCollisionSegment(mask, list, x, y, z, 0.0F, CORE_MIN, CORE_MIN, CORE_MIN, CORE_MAX, CORE_MAX);
        }
        if (canConnectToBlock(world, x + 1, y, z)) {
            addCollisionSegment(mask, list, x, y, z, CORE_MAX, CORE_MIN, CORE_MIN, 1.0F, CORE_MAX, CORE_MAX);
        }
    }

    @Override
    public MovingObjectPosition collisionRayTrace(World world, int x, int y, int z, Vec3 start, Vec3 end) {
        MovingObjectPosition closest = traceSegment(x, y, z, start, end, CORE_MIN, CORE_MIN, CORE_MIN, CORE_MAX, CORE_MAX, CORE_MAX);

        if (canConnectToBlock(world, x, y - 1, z)) {
            closest = pickCloser(start, closest, traceSegment(x, y, z, start, end, CORE_MIN, 0.0F, CORE_MIN, CORE_MAX, CORE_MIN, CORE_MAX));
        }
        if (canConnectToBlock(world, x, y + 1, z)) {
            closest = pickCloser(start, closest, traceSegment(x, y, z, start, end, CORE_MIN, CORE_MAX, CORE_MIN, CORE_MAX, 1.0F, CORE_MAX));
        }
        if (canConnectToBlock(world, x, y, z - 1)) {
            closest = pickCloser(start, closest, traceSegment(x, y, z, start, end, CORE_MIN, CORE_MIN, 0.0F, CORE_MAX, CORE_MAX, CORE_MIN));
        }
        if (canConnectToBlock(world, x, y, z + 1)) {
            closest = pickCloser(start, closest, traceSegment(x, y, z, start, end, CORE_MIN, CORE_MIN, CORE_MAX, CORE_MAX, CORE_MAX, 1.0F));
        }
        if (canConnectToBlock(world, x - 1, y, z)) {
            closest = pickCloser(start, closest, traceSegment(x, y, z, start, end, 0.0F, CORE_MIN, CORE_MIN, CORE_MIN, CORE_MAX, CORE_MAX));
        }
        if (canConnectToBlock(world, x + 1, y, z)) {
            closest = pickCloser(start, closest, traceSegment(x, y, z, start, end, CORE_MAX, CORE_MIN, CORE_MIN, 1.0F, CORE_MAX, CORE_MAX));
        }

        return closest;
    }

    @SuppressWarnings("rawtypes")
    private void addCollisionSegment(AxisAlignedBB mask, List list, int x, int y, int z,
                                     float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        AxisAlignedBB.getAABBPool().getAABB(minX, minY, minZ, maxX, maxY, maxZ)
                .offset(x, y, z)
                .addToListIfIntersects(mask, list);
    }

    private MovingObjectPosition traceSegment(int x, int y, int z, Vec3 start, Vec3 end,
                                              float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        AxisAlignedBB box = AxisAlignedBB.getAABBPool().getAABB(minX, minY, minZ, maxX, maxY, maxZ).offset(x, y, z);
        MovingObjectPosition hit = box.calculateIntercept(start, end);

        if (hit != null) {
            hit.blockX = x;
            hit.blockY = y;
            hit.blockZ = z;
        }

        return hit;
    }

    private MovingObjectPosition pickCloser(Vec3 start, MovingObjectPosition first, MovingObjectPosition second) {
        if (first == null) {
            return second;
        }
        if (second == null) {
            return first;
        }

        double firstDistance = first.hitVec.squareDistanceTo(start.xCoord, start.yCoord, start.zCoord);
        double secondDistance = second.hitVec.squareDistanceTo(start.xCoord, start.yCoord, start.zCoord);
        return secondDistance < firstDistance ? second : first;
    }

    public static boolean canConnectToBlock(IBlockAccess world, int x, int y, int z) {
        int blockId = world.getBlockId(x, y, z);

        if (blockId <= 0 || blockId >= Block.blocksList.length) {
            return false;
        }

        Block neighbor = Block.blocksList[blockId];
        return neighbor instanceof StorageMultiblock;
    }

}