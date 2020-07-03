package com.troy.tco.item;

import com.troy.tco.TCO;
import com.troy.tco.entity.EntityHarpoon;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class ItemHarpoonGun extends ItemBase {
	//private EntityHarpoon projectile = null;

	public ItemHarpoonGun()
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
		if (this.isHarpoon(player.getHeldItem(EnumHand.OFF_HAND)))
		{
			return player.getHeldItem(EnumHand.OFF_HAND);
		}
		else if (this.isHarpoon(player.getHeldItem(EnumHand.MAIN_HAND)))
		{
			return player.getHeldItem(EnumHand.MAIN_HAND);
		}
		else
		{
			for (int i = 0; i < player.inventory.getSizeInventory(); i++)
			{
				ItemStack itemstack = player.inventory.getStackInSlot(i);

				if (this.isHarpoon(itemstack))
				{
					return itemstack;
				}
			}

			return ItemStack.EMPTY;
		}
	}

	protected boolean isHarpoon(ItemStack stack)
	{
		return stack.getItem() instanceof ItemHarpoon;
	}

	public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase entityLiving, int timeLeft)
	{
		int chargeTime = this.getMaxItemUseDuration(stack) - timeLeft;
		ItemStackData data = getItemStackData(stack);
		if (chargeTime > 10 && entityLiving instanceof EntityPlayer && data != null)
		{
			EntityPlayer player = (EntityPlayer)entityLiving;

			float vel = 5.0f;
			//float vel = 2.0f;

			if (!world.isRemote)
			{
				EntityHarpoon projectile = new EntityHarpoon(world, player, new Vec3d(data.x, data.y, data.z));
				projectile.shoot(player, player.rotationPitch, player.rotationYaw, 0.0f, vel, 0.0f);

				stack.damageItem(1, player);
				world.spawnEntity(projectile);
				removeItemStackData(stack);
			}

			world.playSound((EntityPlayer) null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 0.75f, 1.0f / (itemRand.nextFloat() * 0.4f + 2.0f) + 1.0f);


		}
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack)
	{
		return 72000;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack)
	{
		return EnumAction.BOW;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn)
	{
		ItemStack stack = playerIn.getHeldItem(handIn);

		if (getItemStackData(stack) == null)
		{
			//playerIn.sendMessage(new TextComponentString("[TCO]: You cannot shoot a harpoon until you select the anchor point (right click)"));
			return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
		}
		else
		{
			playerIn.setActiveHand(handIn);
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
		}
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		ItemStack stack = player.getHeldItem(hand);
		if (getItemStackData(stack) != null)
		{
			//player.sendMessage(new TextComponentString("[TCO]: You have already selected the source block. Shoot the harpoon now"));
			return EnumActionResult.FAIL;
		}
		else
		{
			ItemStack ammo = findAmmo(player);
			if (ammo == ItemStack.EMPTY && !player.capabilities.isCreativeMode) return EnumActionResult.FAIL;
			if (!player.capabilities.isCreativeMode && !world.isRemote)
			{
				ammo.shrink(1);

				if (ammo.isEmpty())
				{
					player.inventory.deleteStack(ammo);
				}
			}
			ItemStackData data = new ItemStackData(hitX + pos.getX(), hitY + pos.getY(), hitZ + pos.getZ());
			world.playSound((EntityPlayer) null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_LEASHKNOT_PLACE, SoundCategory.PLAYERS, 0.75f, 1.0f);
			setItemStackData(stack, data);
			return EnumActionResult.PASS;
		}
	}

	public int getItemEnchantability()
	{
		return 0;
	}

	//Stores where the non-harpoon end of the line is attached to
	static class ItemStackData
	{
		public double x, y, z;

		public ItemStackData(double x, double y, double z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
	}

	private static final String START_X = "StartX", START_Y = "StartY", START_Z = "StartZ";

	private static void removeItemStackData(ItemStack stack)
	{
		if (stack != null && stack.hasTagCompound())
		{
			NBTTagCompound nbt = stack.getTagCompound();
			nbt.removeTag(START_X);
			nbt.removeTag(START_Y);
			nbt.removeTag(START_Z);
		}
	}
	private static void setItemStackData(ItemStack stack, ItemStackData data)
	{
		NBTTagCompound nbt;
		if (stack.hasTagCompound())
		{
			nbt = stack.getTagCompound();
		}
		else
		{
			nbt = new NBTTagCompound();
		}
		nbt.setDouble(START_X, data.x);
		nbt.setDouble(START_Y, data.y);
		nbt.setDouble(START_Z, data.z);
		stack.setTagCompound(nbt);

	}

	private static ItemStackData getItemStackData(ItemStack stack)
	{
		if (stack.hasTagCompound())
		{
			NBTTagCompound nbt = stack.getTagCompound();

			if (nbt.hasKey(START_X) && nbt.hasKey(START_Y) && nbt.hasKey(START_Z)) {
				double x = nbt.getDouble(START_X);
				double y = nbt.getDouble(START_Y);
				double z = nbt.getDouble(START_Z);
				return new ItemStackData(x, y, z);
			}
		}
		return null;
	}

}
