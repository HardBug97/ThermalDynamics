package cofh.thermal.dynamics.gametest;

import cofh.thermal.core.common.block.entity.storage.EnergyCellBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import static cofh.lib.api.control.IReconfigurable.SideConfig.SIDE_INPUT;
import static cofh.thermal.core.ThermalCore.BLOCKS;
import static cofh.thermal.dynamics.init.registries.TDynIDs.ID_ENERGY_DUCT;
import static cofh.thermal.lib.util.ThermalIDs.ID_ENERGY_CELL;

public class DuctTests {

    // Energy put into one end of a Fluxduct line reaches a cell at the other end.
    public static void fluxductMovesEnergy(GameTestHelper helper) {

        BlockPos cellPos = new BlockPos(1, 1, 3);
        helper.setBlock(cellPos, BLOCKS.get(ID_ENERGY_CELL));
        EnergyCellBlockEntity cell = helper.getBlockEntity(cellPos, EnergyCellBlockEntity.class);
        cell.reconfigControl().setSideConfig(Direction.EAST, SIDE_INPUT);

        for (int x = 2; x <= 4; ++x) {
            helper.setBlock(new BlockPos(x, 1, 3), BLOCKS.get(ID_ENERGY_DUCT));
        }
        BlockPos inlet = new BlockPos(4, 1, 3);

        helper.startSequence()
                .thenWaitUntil(() -> {
                    EnergyHandler duct = helper.getLevel().getCapability(Capabilities.Energy.BLOCK, helper.absolutePos(inlet), Direction.EAST);
                    helper.assertTrue(duct != null, "The end of the duct line should expose an energy handler");
                    try (Transaction tx = Transaction.openRoot()) {
                        helper.assertTrue(duct.insert(1000, tx) > 0, "The duct line should accept energy");
                        tx.commit();
                    }
                })
                .thenWaitUntil(() -> helper.assertTrue(cell.getEnergyStorage().getEnergyStored() > 0, "Energy should reach the cell through the ducts"))
                .thenSucceed();
    }

}
