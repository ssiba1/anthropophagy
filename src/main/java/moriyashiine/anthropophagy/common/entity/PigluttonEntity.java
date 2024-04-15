/*
 * All Rights Reserved (c) MoriyaShiine
 */

package moriyashiine.anthropophagy.common.entity;

import moriyashiine.anthropophagy.common.ModConfig;
import moriyashiine.anthropophagy.common.entity.ai.EatFleshGoal;
import moriyashiine.anthropophagy.common.init.ModEntityTypes;
import moriyashiine.anthropophagy.common.init.ModSoundEvents;
import moriyashiine.anthropophagy.common.init.ModTags;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameRules;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;

public class PigluttonEntity extends HostileEntity {
	public PigluttonEntity(EntityType<? extends HostileEntity> entityType, World world) {
		super(entityType, world);
		setStepHeight(1);
		experiencePoints = 30;
	}

	public static DefaultAttributeContainer.Builder createAttributes() {
		return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 120 * (ModConfig.strongerPiglutton ? 2 : 1)).add(EntityAttributes.GENERIC_ARMOR, 14).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 10 * (ModConfig.strongerPiglutton ? 2 : 1)).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.5).add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.5).add(EntityAttributes.GENERIC_FOLLOW_RANGE, 48);
	}

	public static boolean canSpawn(EntityType<PigluttonEntity> type, ServerWorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
		return random.nextInt(8) == 0 && HostileEntity.canSpawnInDark(type, world, spawnReason, pos, random);
	}

	@Override
	public void tickMovement() {
		super.tickMovement();
		if (!getWorld().isClient && horizontalCollision && getWorld().getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
			Box box = getBoundingBox().expand(0.2);
			for (BlockPos pos : BlockPos.iterate(MathHelper.floor(box.minX), MathHelper.floor(box.minY), MathHelper.floor(box.minZ), MathHelper.floor(box.maxX), MathHelper.floor(box.maxY), MathHelper.floor(box.maxZ))) {
				float hardness = getWorld().getBlockState(pos).getHardness(getWorld(), pos);
				if (hardness >= 0 && hardness < 0.5F) {
					getWorld().breakBlock(pos, true);
				}
			}
		}
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return ModSoundEvents.ENTITY_PIGLUTTON_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSoundEvents.ENTITY_PIGLUTTON_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSoundEvents.ENTITY_PIGLUTTON_DEATH;
	}

	@Override
	public boolean tryAttack(Entity target) {
		boolean flag = super.tryAttack(target);
		if (flag) {
			swingHand(Hand.MAIN_HAND);
		}
		return flag;
	}

	@Override
	public boolean cannotDespawn() {
		return true;
	}

	@Override
	public boolean disablesShield() {
		return true;
	}

	@Override
	protected void initGoals() {
		goalSelector.add(0, new SwimGoal(this));
		goalSelector.add(1, new EatFleshGoal(this));
		goalSelector.add(2, new MeleeAttackGoal(this, 1, true));
		goalSelector.add(3, new WanderAroundFarGoal(this, 1));
		goalSelector.add(4, new LookAtEntityGoal(this, PlayerEntity.class, 8));
		goalSelector.add(4, new LookAroundGoal(this));
		targetSelector.add(0, new RevengeGoal(this));
		targetSelector.add(1, new ActiveTargetGoal<>(this, LivingEntity.class, 10, true, false, living -> living.getType().isIn(ModTags.EntityTypes.PIGLUTTON_TARGETS)));
	}

	public static void attemptSpawn(LivingEntity living, int cannibalLevel, boolean ownFlesh) {
		if (living.getWorld().isClient) {
			return;
		}
		float chance = (Math.min(90, cannibalLevel) - 40) / 800F;
		if (ownFlesh) {
			chance *= 3;
		}
		if (living.getRandom().nextFloat() < chance) {
			PigluttonEntity piglutton = ModEntityTypes.PIGLUTTON.create(living.getWorld());
			if (piglutton != null) {
				final int minH = 8, maxH = 16;
				for (int i = 0; i < 8; i++) {
					int dX = living.getRandom().nextBetween(minH, maxH) * (living.getRandom().nextBoolean() ? 1 : -1);
					int dY = living.getRandom().nextBetween(-6, 6);
					int dZ = living.getRandom().nextBetween(minH, maxH) * (living.getRandom().nextBoolean() ? 1 : -1);
					if (piglutton.teleport(living.getX() + dX, living.getY() + dY, living.getZ() + dZ, false)) {
						living.getWorld().spawnEntity(piglutton);
						piglutton.setTarget(living);
						living.getWorld().playSoundFromEntity(null, piglutton, ModSoundEvents.ENTITY_PIGLUTTON_SPAWN, SoundCategory.HOSTILE, 1, 1);
						return;
					}
				}
			}
		}
	}
}
