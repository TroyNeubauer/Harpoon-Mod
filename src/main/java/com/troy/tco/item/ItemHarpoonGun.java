package com.troy.tco.item;

import com.troy.tco.entity.EntityFixedJoinable;
import com.troy.tco.entity.EntityHarpoon;
import com.troy.tco.entity.EntityHarpoonWire;
import com.troy.tco.util.MathUtils;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

import static com.troy.tco.TCO.logger;

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
			logger.info("Read data: " + data.block.toString() + " - " + world.getBlockState(data.block).getBlock().getLocalizedName() + " pos " + data.pos);
			EntityPlayer player = (EntityPlayer)entityLiving;

			float vel = 5.0f;
			//float vel = 2.0f;

			if (!world.isRemote)
			{
				EntityFixedJoinable anchor = new EntityFixedJoinable(world, data.block, data.pos);
				EntityHarpoon harpoon = new EntityHarpoon(world, player);
				harpoon.shoot(player, player.rotationPitch, player.rotationYaw, 0.0f, vel, 0.0f);

				world.spawnEntity(anchor);
				world.spawnEntity(harpoon);

				EntityHarpoonWire wire = new EntityHarpoonWire(world, anchor, harpoon);
				world.spawnEntity(wire);

				stack.damageItem(1, player);
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
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos block, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		ItemStack stack = player.getHeldItem(hand);
		if (getItemStackData(stack) != null)
		{
			trySendMessage(player, "You must select an achor point for the harpoon before shooting it!", 1);
			return EnumActionResult.FAIL;
		}
		if (!canAnchorToBlock(block, player))
		{
			trySendMessage(player, world.getBlockState(block).getBlock().getLocalizedName() + "Is too weak to be the base for a harpoon zipline. Try a harder block", 2);
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
			ItemStackData data = new ItemStackData(new Vec3d(hitX + block.getX(), hitY + block.getY(), hitZ + block.getZ()), block);
			logger.info("Anchor set to block " + block.toString() + " - " + world.getBlockState(block).getBlock().getLocalizedName() + " pos " + data.pos);
			world.playSound((EntityPlayer) null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_LEASHKNOT_PLACE, SoundCategory.PLAYERS, 0.75f, 1.0f);
			setItemStackData(stack, data);
			return EnumActionResult.PASS;
		}
	}

	private long lastMessageTime = 0;
	private int lastMessageKind = -1;
	private void trySendMessage(EntityPlayer player, String message, int kind)
	{
		if (kind != lastMessageKind || System.currentTimeMillis() - lastMessageTime > 1000)
		{
			lastMessageTime = System.currentTimeMillis();
			lastMessageKind = kind;
			player.sendMessage(new TextComponentString("[TCO]: " + message));
		}
	}

	public int getItemEnchantability()
	{
		return 0;
	}

	//Stores where the non-harpoon end of the line is attached to
	static class ItemStackData
	{
		public Vec3d pos;
		public BlockPos block;

		public ItemStackData(Vec3d pos, BlockPos block) {
			this.pos = pos;
			this.block = block;
		}
	}

	private static final String START_X = "TCOStartX", START_Y = "TCOStartY", START_Z = "TCOStartZ";
	private static final String BLOCK_X = "TCOBlockX", BLOCK_Y = "TCOBlockY", BLOCK_Z = "TCOBlockZ";

	private static void removeItemStackData(ItemStack stack)
	{
		if (stack != null && stack.hasTagCompound())
		{
			NBTTagCompound nbt = stack.getTagCompound();
			nbt.removeTag(START_X);
			nbt.removeTag(START_Y);
			nbt.removeTag(START_Z);
			nbt.removeTag(BLOCK_X);
			nbt.removeTag(BLOCK_Y);
			nbt.removeTag(BLOCK_Z);
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
		nbt.setDouble(START_X, data.pos.x);
		nbt.setDouble(START_Y, data.pos.y);
		nbt.setDouble(START_Z, data.pos.z);
		nbt.setInteger(BLOCK_X, data.block.getX());
		nbt.setInteger(BLOCK_Y, data.block.getY());
		nbt.setInteger(BLOCK_Z, data.block.getZ());
		logger.info("Set nbt data to: " + nbt.toString());
		stack.setTagCompound(nbt);

	}

	private static ItemStackData getItemStackData(ItemStack stack)
	{
		if (stack.hasTagCompound())
		{
			NBTTagCompound nbt = stack.getTagCompound();

			if (nbt.hasKey(START_X) && nbt.hasKey(START_Y) && nbt.hasKey(START_Z) && nbt.hasKey(BLOCK_X) && nbt.hasKey(BLOCK_Y) && nbt.hasKey(BLOCK_Z))
			{
				ItemStackData result = new ItemStackData(new Vec3d(nbt.getDouble(START_X), nbt.getDouble(START_Y), nbt.getDouble(START_Z)), new BlockPos(nbt.getInteger(BLOCK_X), nbt.getInteger(BLOCK_Y), nbt.getInteger(BLOCK_Z)));
				logger.info("Reading nbt data: " + result.pos);
				return result;
			}
		}
		return null;
	}

	public static boolean canAnchorToBlock(BlockPos pos, Entity entity)
	{
		return EntityHarpoon.getBlockBlastResistance(pos, entity) >= 0.5f;
	}


}
