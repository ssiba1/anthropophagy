/*
 * All Rights Reserved (c) MoriyaShiine
 */

package moriyashiine.anthropophagy.common.item;

import moriyashiine.anthropophagy.common.Anthropophagy;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

public class FleshItem extends Item {
	public FleshItem(Settings settings) {
		super(settings);
	}

	@Override
	public Text getName(ItemStack stack) {
		String ownerName = getOwnerName(stack);
		if (!ownerName.isEmpty()) {
			return Text.translatable(getTranslationKey(stack) + "_owned", ownerName);
		}
		return super.getName(stack);
	}

	public static String getOwnerName(ItemStack stack) {
		NbtCompound nbt = stack.getSubNbt(Anthropophagy.MOD_ID);
		if (nbt != null) {
			return nbt.getString("OwnerName");
		}
		return "";
	}

	public static boolean isOwnerPlayer(ItemStack stack) {
		NbtCompound nbt = stack.getSubNbt(Anthropophagy.MOD_ID);
		if (nbt != null) {
			return nbt.getBoolean("FromPlayer");
		}
		return false;
	}

	public static void setOwner(ItemStack stack, String ownerName, boolean fromPlayer) {
		if (ownerName.isEmpty()) {
			return;
		}
		NbtCompound nbt = stack.getOrCreateSubNbt(Anthropophagy.MOD_ID);
		nbt.putString("OwnerName", ownerName);
		nbt.putBoolean("FromPlayer", fromPlayer);
	}

	public static void setOwner(ItemStack stack, Entity entity) {
		setOwner(stack, entity.getName().getString(), entity instanceof PlayerEntity);
	}
}
