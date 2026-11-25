package io.github.painworks.crysismod.nanosuit.cradle;

import static io.github.painworks.crysismod.nanosuit.cradle.NanosuitCradleBlock.FACING;
import static io.github.painworks.crysismod.nanosuit.cradle.NanosuitCradleBlock.PART;

import io.github.painworks.crysismod.nanosuit.cradle.NanosuitCradleBlock.Part;
import io.github.painworks.crysismod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

public final class RoboticArmAssemblyBlockEntity extends BlockEntity {
   public enum Availability { ABSENT, CONNECTED, UNAVAILABLE }
   public enum State { FOLDED, DEPLOYED, OPERATING }
   public record Installation(Availability availability, RoboticArmAssemblyBlockEntity assembly) {}

   private static final Installation ABSENT = new Installation(Availability.ABSENT, null);
   private static final Installation UNAVAILABLE = new Installation(Availability.UNAVAILABLE, null);
   // Test states are deliberately transient, without any stored intervention to resume.
   private State armState = State.FOLDED;

   public RoboticArmAssemblyBlockEntity(BlockPos pos, BlockState state) {
      super(ModBlocks.ROBOTIC_ARM_ASSEMBLY_ENTITY.get(), pos, state);
   }

   public State armState() { return armState; }

   public boolean transitionTo(State target) {
      if (!(level instanceof ServerLevel server) || !server.getServer().isSameThread() || isRemoved() || armState == target) return false;
      if (target != State.FOLDED) {
         if (target == State.DEPLOYED && armState != State.FOLDED || target == State.OPERATING && armState != State.DEPLOYED) return false;
         if (!isInstalled(server)) return false;
      }
      armState = target;
      return true;
   }

   static void tick(Level level, BlockPos pos, BlockState state, RoboticArmAssemblyBlockEntity assembly) {
      if (assembly.armState != State.FOLDED && level instanceof ServerLevel server && !assembly.isInstalled(server)) {
         assembly.transitionTo(State.FOLDED);
      }
   }

   private boolean isInstalled(ServerLevel level) {
      return installationAt(level, worldPosition.below(2)).assembly == this;
   }

   static Installation installationAt(ServerLevel level, BlockPos cradlePos) {
      var cradleChunk = loadedChunk(level, cradlePos);
      if (cradleChunk == null) return UNAVAILABLE;
      var cradleState = cradleChunk.getBlockState(cradlePos);
      if (!cradleState.is(ModBlocks.NANOSUIT_CRADLE.get()) || cradleState.getValue(PART) != Part.MASTER) return ABSENT;
      if (!(cradleChunk.getBlockEntity(cradlePos) instanceof NanosuitCradleBlockEntity cradle) || cradle.isRemoved()) return UNAVAILABLE;
      var direction = cradleState.getValue(FACING);
      var cradleTail = cradlePos.relative(direction);
      var tailChunk = loadedChunk(level, cradleTail);
      if (tailChunk == null || !NanosuitCradleBlock.isPartner(cradleState, tailChunk.getBlockState(cradleTail))) return UNAVAILABLE;
      if (level.isOutsideBuildHeight(cradlePos.above(2))) return ABSENT;

      var armPos = cradlePos.above(2);
      var armHead = cradleChunk.getBlockState(armPos);
      var armTail = tailChunk.getBlockState(cradleTail.above(2));
      var armBlock = ModBlocks.ROBOTIC_ARM_ASSEMBLY.get();
      if (!armHead.is(armBlock) && !armTail.is(armBlock)) return ABSENT;
      if (!armHead.is(armBlock) || armHead.getValue(PART) != Part.MASTER || armHead.getValue(FACING) != direction
         || !NanosuitCradleBlock.isPartner(armHead, armTail)
         || !cradleChunk.getBlockState(cradlePos.above()).isAir() || !tailChunk.getBlockState(cradleTail.above()).isAir()) return UNAVAILABLE;
      if (!(cradleChunk.getBlockEntity(armPos) instanceof RoboticArmAssemblyBlockEntity assembly) || assembly.isRemoved()) return UNAVAILABLE;
      return new Installation(Availability.CONNECTED, assembly);
   }

   public static RoboticArmAssemblyBlockEntity at(ServerLevel level, BlockPos pos) {
      var chunk = loadedChunk(level, pos);
      if (chunk == null) return null;
      var state = chunk.getBlockState(pos);
      if (!state.is(ModBlocks.ROBOTIC_ARM_ASSEMBLY.get()) || state.getValue(PART) == Part.LEGACY) return null;
      var other = NanosuitCradleBlock.otherPos(pos, state);
      var otherChunk = loadedChunk(level, other);
      if (otherChunk == null || !NanosuitCradleBlock.isPartner(state, otherChunk.getBlockState(other))) return null;
      return (state.getValue(PART) == Part.MASTER ? chunk.getBlockEntity(pos) : otherChunk.getBlockEntity(other))
         instanceof RoboticArmAssemblyBlockEntity assembly && !assembly.isRemoved() ? assembly : null;
   }

   private static LevelChunk loadedChunk(ServerLevel level, BlockPos pos) {
      return level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
   }
}
