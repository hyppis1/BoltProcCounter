package com.RubyCounter;

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

@Slf4j
@PluginDescriptor(
	name = "Ruby counter"
)
public class RubyCounterPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ConfigManager configManager;

	@Inject
	private RubyCounterConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private RubyCounterOverlay RubyCounterOverlay;




	public int attackCounter = 0; // Counter to track attacks
	public int attacksSinceLastRuby = 0; // Counter to track attacks since last ruby
	public double rubyDryRate = 0.0; // Counter to track ruby proc dryness chance
	public int longestDryStreak = 0; // Counter to track dry streak
	public int rubyCounter = 0; // Counter to track ruby procs
	public int acbSpecsUsed = 0; //counter to track acb spec uses
	public int acbSpecsProcs = 0; // counter to track acb spec procs
	public int zcbSpecsUsed = 0; //counter to track zcb spec uses
	public int zcbSpecsProcs = 0; // counter to track zcb spec procs
	public double rate = 0.0;
	public double expectedProcs = 0.0; // Counter to track expected ruby procs
	private int eventSoundId;
	private int specialPercentage = 0;

    boolean acbSpecUsed = false;
	boolean zcbSpecUsed = false;
	boolean soundMuted = true;
	private static final int coolDownTicks = 4;
	private int coolDownTicksRemaining = 0;


	@Override
	protected void startUp() throws Exception
	{
		config = configManager.getConfig(RubyCounterConfig.class);
		overlayManager.add(RubyCounterOverlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(RubyCounterOverlay);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		// Check if the "Reset Counters" toggle was changed
		if (event.getKey().equals("resetCounters"))
		{
			attackCounter = 0;
			rubyCounter = 0;
			rubyDryRate = 0.0;
			longestDryStreak = 0;
			rate = 0.0;
			expectedProcs = 0.0;
			attacksSinceLastRuby = 0;
			acbSpecsProcs = 0;
			acbSpecsUsed = 0;
			zcbSpecsProcs = 0;
			zcbSpecsUsed = 0;
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		Client client = this.client;
		Player localPlayer = client.getLocalPlayer();

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
					// check if ruby bolts are equipped
					if (hasCorrectAmmo())
					{
						// check if tracking acb separately and if acb spec was used
						if(config.AcbTracking() && acbSpecUsed)
						{
							acbSpecsUsed++;
							// check if it was ruby
							if (eventSoundId == 2911)
							{
								acbSpecsProcs++;
							}
						}
						// check if tracking zcb separately and if zcb spec was used
						else if (config.ZcbTracking() && zcbSpecUsed)
						{
							zcbSpecsUsed++;
							// check if it was ruby
							if (eventSoundId == 2911)
							{
								zcbSpecsProcs++;
							}
						}
						else
						{
							// Increment the attack counters
							attackCounter++;
							attacksSinceLastRuby++;
							if (eventSoundId == 2911)
							{
								// Increment the ruby counters
								rubyCounter++;

								if (attacksSinceLastRuby > longestDryStreak)
								{
									longestDryStreak = attacksSinceLastRuby;
								}
								// reset attacks since last ruby
								attacksSinceLastRuby = 0;

							}


							if (config.KandarinHardDiary())
							{
								expectedProcs = attackCounter * 0.066;
								expectedProcs = Double.parseDouble(new DecimalFormat("#.#").format(expectedProcs));

								rubyDryRate = 1 - Math.pow(1 - 0.066, attacksSinceLastRuby);
								rubyDryRate *= 100.0;
								rubyDryRate = Double.parseDouble(new DecimalFormat("#.##").format(rubyDryRate));
							}
							else
							{
								expectedProcs = attackCounter * 0.06;
								expectedProcs = Double.parseDouble(new DecimalFormat("#.#").format(expectedProcs));

								rubyDryRate = 1 - Math.pow(1 - 0.06, attacksSinceLastRuby) ;
								rubyDryRate *= 100.0;
								rubyDryRate = Double.parseDouble(new DecimalFormat("#.##").format(rubyDryRate));
							}

							// calculate and format rate of ruby procs
							rate = (double) rubyCounter / attackCounter;
							rate *= 100.0;
							rate = Double.parseDouble(new DecimalFormat("#.###").format(rate));
						}

						// hacky way to track if sound id is being updated or not, aka player has muted sounds
						if (eventSoundId == -1)
						{
							soundMuted = true;
						}
					}
				}
			}
		}

		zcbSpecUsed = false;
		acbSpecUsed = false;
		// hacky way to track if sound id is being updated or not, aka player has muted sounds
		eventSoundId = -1;
	}


	private boolean isCrossbowEquipped()
	{
		ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);

		if (equipment != null)
		{
			int weaponSlotIdx = EquipmentInventorySlot.WEAPON.getSlotIdx();
			int weaponId = equipment.getItem(weaponSlotIdx).getId();

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

		return false;
	}

	private boolean hasCorrectAmmo()
	{
		ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);

		if (equipment != null)
		{
			int ammoSlotIdx = EquipmentInventorySlot.AMMO.getSlotIdx();
			int ammoId = equipment.getItem(ammoSlotIdx).getId();

			// Check if the ammo ID matches any of the specified IDs
			return ammoId == ItemID.RUBY_BOLTS_E
					|| ammoId == ItemID.RUBY_DRAGON_BOLTS_E;
		}

		return false;
	}

	private boolean isAttackAnimation(int animationId)
	{
		// Check if the animation ID matches any of the specified attack animations
		// 7552 rcb,dcb,dhcb and acb attack, 9168 zcb attack, 9206 ornament rcb attack
		return animationId == 7552 || animationId == 9168 || animationId == 9206;
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
			ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);

			if (equipment != null)
			{
				int weaponSlotIdx = EquipmentInventorySlot.WEAPON.getSlotIdx();
				int weaponId = equipment.getItem(weaponSlotIdx).getId();

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

	}

	@Subscribe
	public void onSoundEffectPlayed(SoundEffectPlayed event)
	{
		soundMuted = false;
		eventSoundId = event.getSoundId();
	}


	@Provides
	RubyCounterConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(RubyCounterConfig.class);
	}

}
