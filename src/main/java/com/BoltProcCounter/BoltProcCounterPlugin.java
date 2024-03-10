package com.BoltProcCounter;

import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.SoundEffectPlayed;
import net.runelite.api.VarPlayer;

import java.text.DecimalFormat;
import java.util.Objects;

@Slf4j
@PluginDescriptor(
	name = "Bolt proc counter"
)
public class BoltProcCounterPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ConfigManager configManager;

	@Inject
	private BoltProcCounterConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private BoltProcCounterOverlay BoltProcCounterOverlay;



	public int[] attackCounterArray = new int[10]; // Counters to track attacks
	public int[] attacksSinceLastProcArray = new int[10]; // Counters to track attacks since last procs
	public int[] longestDryStreakArray = new int[10]; // Counters to track dry streaks
	public int[] procCounterArray = new int[10]; // Counters to track bolt procs
	public int[] acbSpecsUsedArray = new int[10]; // counters to track acb spec uses
	public int[] acbSpecsProcsArray = new int[10]; // counters to track acb spec procs
	public int[] zcbSpecsUsedArray = new int[10]; // counters to track zcb spec uses
	public int[] zcbSpecsProcsArray = new int[10]; // counters to track zcb spec procs

	public double procDryRate = 0.0; // Counter to track ruby proc dryness chance
	public double rate = 0.0;
	public double expectedProcs = 0.0; // Counter to track expected ruby procs

	public double expectedRate = 0.06; // expected ruby proc rate
	private int eventSoundId;
	private int boltProcSoundId;
	private int weaponId;
	private int ammoId;

	public String ammoName;
	public int ammoIndex;
	public int wasAmmoIndex = 0;
	private int specialPercentage = 0;


    boolean acbSpecUsed = false;
	boolean zcbSpecUsed = false;
	boolean soundMuted = true;
	boolean soundMutedB2B = true;
	boolean needAccuracyPass = false;
	private static final int coolDownTicks = 4;
	private int coolDownTicksRemaining = 0;


	@Override
	protected void startUp() throws Exception
	{
		config = configManager.getConfig(BoltProcCounterConfig.class);
		overlayManager.add(BoltProcCounterOverlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(BoltProcCounterOverlay);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		// Check if the "Reset Counters" toggle was changed
		if (event.getKey().equals("resetCounters"))
		{
			if (wasAmmoIndex != -1)
			{
				attackCounterArray[wasAmmoIndex] = 0;
				procCounterArray[wasAmmoIndex] = 0;
				procDryRate = 0.0;
				longestDryStreakArray[wasAmmoIndex] = 0;
				rate = 0.0;
				expectedProcs = 0.0;
				attacksSinceLastProcArray[wasAmmoIndex] = 0;
				acbSpecsProcsArray[wasAmmoIndex] = 0;
				acbSpecsUsedArray[wasAmmoIndex] = 0;
				zcbSpecsProcsArray[wasAmmoIndex] = 0;
				zcbSpecsUsedArray[wasAmmoIndex] = 0;
			}
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		Client client = this.client;
		Player localPlayer = client.getLocalPlayer();

		// get currently equipped weapon and ammo id
		ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
		if (equipment != null)
		{
			int SlotIdx = EquipmentInventorySlot.WEAPON.getSlotIdx();
			weaponId = Objects.requireNonNull(equipment.getItem(SlotIdx)).getId();

			SlotIdx = EquipmentInventorySlot.AMMO.getSlotIdx();
			ammoId = Objects.requireNonNull(equipment.getItem(SlotIdx)).getId();
		}

		ammoIndex = ammoIdToArrayIndex();

		// do nothing if not equipped with bolts that have proc effects
		if (ammoIndex != -1)
		{
			// for overlay data to stay visible when equipping something else, stops flikering
			wasAmmoIndex = ammoIndex;

			// 10% bonus from kandarin hard diary
			if (config.KandarinHardDiary())
			{
				expectedRate *= 1.1;
			}

			// 10% bonus from zcb
			if (weaponId == ItemID.ZARYTE_CROSSBOW)
			{
				expectedRate *= 1.1;
			}

			// cool down ticks to not track multiple hits from single attack animation
			if (coolDownTicksRemaining > 0)
			{
				coolDownTicksRemaining--;
			}

			if (localPlayer != null && coolDownTicksRemaining == 0)
			{
				// check if crossbow is equipped
				if (isCrossbowEquipped())
				{
					// get players animation
					int animationId = localPlayer.getAnimation();
					// System.out.println("Player is performing animation with ID: " + animationId);

					// check if its correct attack animation
					if (isAttackAnimation(animationId))
					{
						// apply cool down after attack animation
						coolDownTicksRemaining = coolDownTicks;

						// check if tracking acb separately and if acb spec was used
						if(config.AcbTracking() && acbSpecUsed)
						{
							acbSpecsUsedArray[ammoIndex] ++;
							// check if it was proc
							if (eventSoundId == boltProcSoundId)
							{
								acbSpecsProcsArray[ammoIndex] ++;
							}
						}
						// check if tracking zcb separately and if zcb spec was used
						else if (config.ZcbTracking() && zcbSpecUsed)
						{
							zcbSpecsUsedArray[ammoIndex] ++;
							// check if it was proc
							if (eventSoundId == boltProcSoundId)
							{
								zcbSpecsProcsArray[ammoIndex] ++;
							}
						}
						else
						{
							// Increment the attack counters
							attackCounterArray[ammoIndex] ++;
							attacksSinceLastProcArray[ammoIndex] ++;
							if (eventSoundId == boltProcSoundId)
							{
								// Increment the proc counters
								procCounterArray[ammoIndex] ++;

								if (attacksSinceLastProcArray[ammoIndex] > longestDryStreakArray[ammoIndex])
								{
									longestDryStreakArray[ammoIndex] = attacksSinceLastProcArray[ammoIndex];
								}
								// reset attacks since last ruby
								attacksSinceLastProcArray[ammoIndex] = 0;

							}
						}

						// hacky way to track if sound id is being updated or not, aka player has muted sounds
						// was problems in pvp combat for some odd reason so now checking if sound is muted in b2b attacks
						if (soundMuted)
						{
							soundMutedB2B = true;
						}
						if (eventSoundId == -1)
						{
							soundMuted = true;
						}
					}
				}
			}

			// calculate and format expected rate of procs
			expectedProcs = attackCounterArray[ammoIndex] * expectedRate;
			expectedProcs = Double.parseDouble(new DecimalFormat("#.#").format(expectedProcs));

			// calculate and format proc dry chance
			procDryRate = 1 - Math.pow(1 - expectedRate, attacksSinceLastProcArray[ammoIndex]);
			procDryRate *= 100.0;
			procDryRate = Double.parseDouble(new DecimalFormat("#.##").format(procDryRate));

			// calculate and format rate of procs
			rate = (double) procCounterArray[ammoIndex] / attackCounterArray[ammoIndex];
			rate *= 100.0;
			rate = Double.parseDouble(new DecimalFormat("#.###").format(rate));


			zcbSpecUsed = false;
			acbSpecUsed = false;
			// hacky way to track if sound id is being updated or not, aka player has muted sounds
			eventSoundId = -1;
		}
	}


	private boolean isCrossbowEquipped()
	{
		// Check if one of the specified crossbows is equipped
		return weaponId == ItemID.ADAMANT_CROSSBOW
				|| weaponId == ItemID.RUNE_CROSSBOW
				|| weaponId == ItemID.RUNE_CROSSBOW_OR
				|| weaponId == ItemID.DRAGON_CROSSBOW
				|| weaponId == ItemID.DRAGON_CROSSBOW_CR
				|| weaponId == ItemID.DRAGON_HUNTER_CROSSBOW
				|| weaponId == ItemID.DRAGON_HUNTER_CROSSBOW_B
				|| weaponId == ItemID.DRAGON_HUNTER_CROSSBOW_T
				|| weaponId == ItemID.ARMADYL_CROSSBOW
				|| weaponId == ItemID.ZARYTE_CROSSBOW;
	}

	private int ammoIdToArrayIndex()
	{
		int arrayIndex;
		switch (ammoId)
		{
			case ItemID.OPAL_BOLTS_E:
			case ItemID.OPAL_DRAGON_BOLTS_E:
			case ItemID.OPAL_DRAGON_BOLTS_E_27192:
				arrayIndex = 0;
				expectedRate = 0.05;
				boltProcSoundId = 2918;
				ammoName = "Opal";
				needAccuracyPass = false;
				break;
			case ItemID.JADE_BOLTS_E:
			case ItemID.JADE_DRAGON_BOLTS_E:
				arrayIndex = 1;
				expectedRate = 0.06;
				// boltProcSoundId = ???;
				ammoName = "Jade (not supported)";
				needAccuracyPass = false;
				break;
			case ItemID.PEARL_BOLTS_E:
			case ItemID.PEARL_DRAGON_BOLTS_E:
				arrayIndex = 2;
				expectedRate = 0.06;
				boltProcSoundId = 2920;
				ammoName = "Pearl";
				needAccuracyPass = false;
				break;
			case ItemID.TOPAZ_BOLTS_E:
			case ItemID.TOPAZ_DRAGON_BOLTS_E:
				arrayIndex = 3;
				expectedRate = 0.04;
				boltProcSoundId = 2914;
				ammoName = "Topaz";
				needAccuracyPass = false;
				break;
			case ItemID.SAPPHIRE_BOLTS_E:
			case ItemID.SAPPHIRE_DRAGON_BOLTS_E:
				arrayIndex = 4;
				expectedRate = 0.25;
				boltProcSoundId = 2912;
				ammoName = "Sapphire";
				needAccuracyPass = true;
				break;
			case ItemID.EMERALD_BOLTS_E:
			case ItemID.EMERALD_DRAGON_BOLTS_E:
				arrayIndex = 5;
				expectedRate = 0.55;
				boltProcSoundId = 2919;
				ammoName = "Emerald";
				needAccuracyPass = true;
				break;
			case ItemID.RUBY_BOLTS_E:
			case ItemID.RUBY_DRAGON_BOLTS_E:
				arrayIndex = 6;
				expectedRate = 0.06;
				boltProcSoundId = 2911;
				ammoName = "Ruby";
				needAccuracyPass = false;
				break;
			case ItemID.DIAMOND_BOLTS_E:
			case ItemID.DIAMOND_BOLTS_E_23649:
			case ItemID.DIAMOND_DRAGON_BOLTS_E:
				arrayIndex = 7;
				expectedRate = 0.1;
				boltProcSoundId = 2913;
				ammoName = "Diamond";
				needAccuracyPass = false;
				break;
			case ItemID.DRAGONSTONE_BOLTS_E:
			case ItemID.DRAGONSTONE_DRAGON_BOLTS_E:
				arrayIndex = 8;
				expectedRate = 0.06;
				boltProcSoundId = 2915;
				ammoName = "Dragonstone";
				needAccuracyPass = true;
				break;
			case ItemID.ONYX_BOLTS_E:
			case ItemID.ONYX_DRAGON_BOLTS_E:
				arrayIndex = 9;
				expectedRate = 0.11;
				boltProcSoundId = 2917;
				ammoName = "Onyx";
				needAccuracyPass = true;
				break;
			default:
				arrayIndex = -1;
				break;
		}
		return arrayIndex;
	}
	private boolean isAttackAnimation(int animationId)
	{
		// Check if the animation ID matches any of the specified attack animations
		// 7552 rcb,dcb,dhcb and acb attack, 9168 zcb attack, 9206 ornament rcb attack
		// 4230 rcb in pvp, 9205 ornament rcb attack in pvp, 9166 zcb attack in pvp
		return animationId == 7552 || animationId == 9168 || animationId == 9206 || animationId == 4230 || animationId == 9205 || animationId == 9166;
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{

		if (event.getVarpId() != VarPlayer.SPECIAL_ATTACK_PERCENT)
		{
			return;
		}
		// save previous value
		int wasSpecialPercentage = specialPercentage;
		// get new value from event
		specialPercentage = event.getValue();

		// compare if it is less than last time
		if (wasSpecialPercentage > specialPercentage)
		{
			// check for acb and zcb
			if (weaponId == ItemID.ARMADYL_CROSSBOW)
			{
				acbSpecUsed = true;
			}

			if (weaponId == ItemID.ZARYTE_CROSSBOW)
			{
				zcbSpecUsed = true;
			}

		}

	}

	@Subscribe
	public void onSoundEffectPlayed(SoundEffectPlayed event)
	{
		soundMuted = false;
		soundMutedB2B = false;
		eventSoundId = event.getSoundId();
	}


	@Provides
	BoltProcCounterConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BoltProcCounterConfig.class);
	}

}
