package com.troy.tco.item;

import com.troy.tco.TCO;
import com.troy.tco.entity.EntityHarpoon;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.*;
import net.minecraft.stats.StatList;
import net.minecraft.util.*;
import net.minecraft.util.math.Vec2f;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class HarpoonGun extends ItemBase {
	//private EntityHarpoon projectile = null;

	public HarpoonGun()
	{
		super("harpoon_gun");
		setMaxDamage(100);
		setMaxStackSize(1);
		setCreativeTab(CreativeTabs.COMBAT);

		this.addPropertyOverride(new ResourceLocation("pull"), new IItemPropertyGetter()
		{
			@SideOnly(Side.CLIENT)
			public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn)
			{
				if (entityIn == null)
				{
					return 0.0F;
				}
				else
				{
					return !(entityIn.getActiveItemStack().getItem() instanceof ItemBow) ? 0.0F : (float)(stack.getMaxItemUseDuration() - entityIn.getItemInUseCount()) / 20.0F;
				}
			}
		});
		this.addPropertyOverride(new ResourceLocation("pulling"), new IItemPropertyGetter()
		{
			@SideOnly(Side.CLIENT)
			public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn)
			{
				return entityIn != null && entityIn.isHandActive() && entityIn.getActiveItemStack() == stack ? 1.0F : 0.0F;
			}
		});
	}

	protected ItemStack findAmmo(EntityPlayer player)
	{
		if (this.isArrow(player.getHeldItem(EnumHand.OFF_HAND)))
		{
			return player.getHeldItem(EnumHand.OFF_HAND);
		}
		else if (this.isArrow(player.getHeldItem(EnumHand.MAIN_HAND)))
		{
			return player.getHeldItem(EnumHand.MAIN_HAND);
		}
		else
		{
			for (int i = 0; i < player.inventory.getSizeInventory(); ++i)
			{
				ItemStack itemstack = player.inventory.getStackInSlot(i);

				if (this.isArrow(itemstack))
				{
					return itemstack;
				}
			}

			return ItemStack.EMPTY;
		}
	}

	protected boolean isArrow(ItemStack stack)
	{
		return stack.getItem() instanceof ItemArrow;
	}

	public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase entityLiving, int timeLeft)
	{
		int chargeTime = this.getMaxItemUseDuration(stack) - timeLeft;
		if (chargeTime < 20) return;

		if (entityLiving instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer)entityLiving;
			ItemStack itemstack = this.findAmmo(player);

			if (!itemstack.isEmpty() || player.capabilities.isCreativeMode)
			{
				TCO.logger.info("Charge: " + chargeTime);
				float vel = 5.0f;

				if (!world.isRemote)
				{
					ItemArrow itemarrow = (ItemArrow) (itemstack.getItem() instanceof ItemArrow ? itemstack.getItem() : Items.ARROW);
					EntityHarpoon projectile = new EntityHarpoon(world, player);
					projectile.shoot(player, player.rotationPitch, player.rotationYaw, 0.0f, vel, 0.0f);

					stack.damageItem(1, player);
					world.spawnEntity(projectile);
				}

				world.playSound((EntityPlayer) null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, 1.0f, 1.0f / (itemRand.nextFloat() * 0.4f + 2.0f) + 1.0f);

				if (!player.capabilities.isCreativeMode)
				{
					itemstack.shrink(1);

					if (itemstack.isEmpty())
					{
						player.inventory.deleteStack(itemstack);
					}
				}

			}
		}
	}

	public int getMaxItemUseDuration(ItemStack stack)
	{
		return 72000;
	}

	public EnumAction getItemUseAction(ItemStack stack)
	{
		return EnumAction.BOW;
	}

	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn)
	{
		ItemStack itemstack = playerIn.getHeldItem(handIn);
		boolean hasAmmo = !this.findAmmo(playerIn).isEmpty();

		if (!playerIn.capabilities.isCreativeMode && !hasAmmo)
		{
			return hasAmmo ? new ActionResult<ItemStack>(EnumActionResult.PASS, itemstack) : new ActionResult<ItemStack>(EnumActionResult.FAIL, itemstack);
		}
		else
		{
			playerIn.setActiveHand(handIn);
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
		}
	}

	public int getItemEnchantability()
	{
		return 0;
	}

}
