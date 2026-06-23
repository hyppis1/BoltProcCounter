package com.BoltProcCounter;

import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ClientShutdown;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.api.VarPlayer;
import net.runelite.api.GameState;
import net.runelite.api.events.StatChanged;
import net.runelite.api.Skill;
import net.runelite.api.events.FakeXpDrop;

import java.text.DecimalFormat;
import java.io.*;
import java.nio.file.*;

import static net.runelite.client.RuneLite.RUNELITE_DIR;

@Slf4j
@PluginDescriptor(
	name = "Bolt Proc Counter"
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
	public int[] attackDealtDmgArray = new int[10]; // counters to track hits that dealt dmg

	public double overallAccuracy;
	public double procDryRate = 0.0; // Counter to track proc dryness chance
	public double rate = 0.0;
	public double expectedProcs = 0.0; // Counter to track expected  procs
	public double expectedRate;
	private int eventSoundId;
	private int boltProcSoundId;
	private int weaponId;
	private int ammoId;
	private int capeId;

	public String ammoName;

	public String[] ammoNames = {"Opal","Jade","Pearl","Topaz","Sapphire","Emerald","Ruby","Diamond","Dragonstone","Onyx"};
	public int ammoIndex;
	public int wasAmmoIndex = -1;
	public int PlayingAnimationID;
	public int PlayingAnimationFrame;
	private int specialPercentage = 0;


    boolean acbSpecUsed = false;
	boolean zcbSpecUsed = false;
	boolean HpXpDrop = false;
	boolean soundMuted = true;
	boolean soundMutedB2B = true;
	boolean needAccuracyPass = false;
	boolean shouldLoad = true;
	boolean shouldSave = false;



	@Override
	protected void startUp() throws Exception
	{
		config = configManager.getConfig(BoltProcCounterConfig.class);
		overlayManager.add(BoltProcCounterOverlay);
	}

	@Subscribe
	private void onClientShutdown(ClientShutdown e)
	{
		// backup data saving in case client is closed without logging out first
		if (shouldSave && config.dataSaving())
		{
			saveToFile();
		}
	}

	@Subscribe
	public void onGameStateChanged(final GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGING_IN)
		{
			// Make "shouldLoad" true on log in. Only impacts if user played with x account earlier, logged out and logged back in with y account.
			if (config.dataSaving())
			{
				shouldLoad = true;
			}
		}

		if (event.getGameState() == GameState.LOGIN_SCREEN)
		{
			// only save data once. When "shouldSave" is true and data saving is enabled
			if (shouldSave && config.dataSaving())
			{
				saveToFile();
				// "shouldSave" only turns True after data is loaded/game tick has passed. To avoid overwriting existing data with zeros
				shouldSave = false;
			}
		}
	}

	@Override
	protected void shutDown() throws Exception
	{
		// also save data when plugin is turned off
		if (shouldSave && config.dataSaving())
		{
			saveToFile();
		}
		overlayManager.remove(BoltProcCounterOverlay);
	}


	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		// Check if the "dataSaving" toggle was changed
		if (event.getKey().equals("dataSaving"))
		{
			// make "shouldLoad" true to load all the data on next tick
			if (config.dataSaving())
			{
				shouldLoad = true;
			}
		}

		// Check if the "resetCounters" toggle was changed
		else if (event.getKey().equals(("resetCounters")))
		{
			if (wasAmmoIndex != -1)
			{
				attackCounterArray[wasAmmoIndex] = 0;
				attacksSinceLastProcArray[wasAmmoIndex] = 0;
				longestDryStreakArray[wasAmmoIndex] = 0;
				procCounterArray[wasAmmoIndex] = 0;
				acbSpecsUsedArray[wasAmmoIndex] = 0;
				acbSpecsProcsArray[wasAmmoIndex] = 0;
				zcbSpecsUsedArray[wasAmmoIndex] = 0;
				zcbSpecsProcsArray[wasAmmoIndex] = 0;
				attackDealtDmgArray[wasAmmoIndex] = 0;
			}
		}

	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		Client client = this.client;
		Player localPlayer = client.getLocalPlayer();

		if (localPlayer == null)
		{
			return;
		}

		// only load data once. When "shouldLoad" is true and data saving is enabled
		if (shouldLoad && config.dataSaving())
		{
			loadFromFile();
			// "shouldLoad" only turns True if data saving config is toggled back on. Or on client start
			shouldLoad = false;
		}

		// "shouldSave" turns true after data loading has happened/game tick has passed.
		shouldSave = true;

		// get currently equipped weapon and ammo id
		ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
		if (equipment != null)
		{
			int SlotIdx = EquipmentInventorySlot.WEAPON.getSlotIdx();
			Item slotItem = equipment.getItem(SlotIdx);
			if (slotItem != null)
			{
				weaponId = slotItem.getId();
			}
			else
			{
				weaponId = -1;
			}

			SlotIdx = EquipmentInventorySlot.AMMO.getSlotIdx();
			slotItem = equipment.getItem(SlotIdx);
			if (slotItem != null)
			{
				ammoId = slotItem.getId();
			}
			else
			{
				ammoId = -1;
			}

			SlotIdx = EquipmentInventorySlot.CAPE.getSlotIdx();
			slotItem = equipment.getItem(SlotIdx);
			if (slotItem != null)
			{
				capeId = slotItem.getId();
			}
			else
			{
				capeId = -1;
			}
		}

		ammoIndex = ammoIdToArrayIndex();

		// if no ammo was found, check if player has quiver equipped
		if (ammoIndex == -1)
		{
			if (isQuiverEquipped())
			{
				// quiver ammo slot ammo id
				ammoId = client.getVarpValue(VarPlayer.DIZANAS_QUIVER_ITEM_ID);
				ammoIndex = ammoIdToArrayIndex();
			}
		}

		// do nothing if not equipped with bolts that have proc effects
		if (ammoIndex != -1)
		{
			// for overlay data to stay visible when equipping something else, stops flickering when swapping between bolts and arrows etc
			wasAmmoIndex = ammoIndex;

			// check if crossbow is equipped
			if (isCrossbowEquipped())
			{
				// get players animation
				if (localPlayer.getAnimation() == PlayingAnimationID)
				{
					PlayingAnimationFrame = localPlayer.getAnimationFrame();
				}

				// check if its correct attack animation
				if (isAttackAnimation(PlayingAnimationID) && PlayingAnimationFrame < 1)
				{

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

						// Increment the attacks hit counters, if hp xp drop happened
						if (HpXpDrop)
						{
							attackDealtDmgArray[wasAmmoIndex] ++;
						}

						// if bolt bypasses accuracy check, increment since last proc here
						if (!needAccuracyPass)
						{
							attacksSinceLastProcArray[ammoIndex] ++;
						}
						else
						{
							// else, depending on config on rule set, increment since last proc here
							if (!config.AccuracyPassRule())
							{
								attacksSinceLastProcArray[ammoIndex] ++;
							}
							else
							{
								// or here if hp xp drop also happened
								if (HpXpDrop)
								{
									attacksSinceLastProcArray[ammoIndex] ++;
								}
							}
						}
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

					if (config.dataSampleSaving() && attackCounterArray[ammoIndex] % config.sampleSize() == 0)
					{
						// save data sample to a file if attackCounterArray is divisible by config sampleSize
						saveDataSampleToFile();
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

			// 10% bonus from kandarin hard diary
			if (config.KandarinHardDiary())
			{
				expectedRate *= 1.1;
			}

			// calculate proc rates etc. depending on config setting
			if (config.AccuracyPassRule())
			{
				// if config is ON, check if bolt effects bypass accuracy check
				if (needAccuracyPass)
				{
					// if bolt effect needs to pass accuracy check, use attackDealtDmgArray instead of attackCounterArray

					// calculate expected rate of procs
					expectedProcs = attackDealtDmgArray[ammoIndex] * expectedRate;

					// calculate rate of procs
					rate = (double) procCounterArray[ammoIndex] / attackDealtDmgArray[ammoIndex];
				}
				else
				{
					// if bolt effect bypasses accuracy check, do normal calcs

					// calculate expected rate of procs
					expectedProcs = attackCounterArray[ammoIndex] * expectedRate;

					// calculate rate of procs
					rate = (double) procCounterArray[ammoIndex] / attackCounterArray[ammoIndex];
				}
			}
			else
			{
				// if config is OFF, do normal calcs

				// calculate expected rate of procs
				expectedProcs = attackCounterArray[ammoIndex] * expectedRate;

				// calculate rate of procs
				rate = (double) procCounterArray[ammoIndex] / attackCounterArray[ammoIndex];
			}

			// format expected rate of procs
			expectedProcs = Double.parseDouble(new DecimalFormat("#.#").format(expectedProcs));

			// format rate of procs
			rate *= 100.0;
			rate = Double.parseDouble(new DecimalFormat("#.###").format(rate));

			// calculate and format proc dry chance
			procDryRate = 1 - Math.pow(1 - expectedRate, attacksSinceLastProcArray[ammoIndex]);
			procDryRate *= 100.0;
			procDryRate = Double.parseDouble(new DecimalFormat("#.##").format(procDryRate));

			// calculate and format overall accuracy
			overallAccuracy = (double) attackDealtDmgArray[ammoIndex] / attackCounterArray[ammoIndex];
			overallAccuracy *= 100.0;
			overallAccuracy = Double.parseDouble(new DecimalFormat("#.##").format(overallAccuracy));

			zcbSpecUsed = false;
			acbSpecUsed = false;
			// hacky way to track if sound id is being updated or not, aka player has muted sounds
			eventSoundId = -1;

			// make hp xp drop false
			HpXpDrop = false;
		}
	}


	private boolean isCrossbowEquipped()
	{
		// Basicly check if any crossbow is equipped
		return weaponId == net.runelite.api.gameval.ItemID.ZARYTE_XBOW
				|| weaponId == net.runelite.api.gameval.ItemID.ACB
				|| weaponId == net.runelite.api.gameval.ItemID.DRAGONHUNTER_XBOW
				|| weaponId == net.runelite.api.gameval.ItemID.DRAGONHUNTER_XBOW_KBD
				|| weaponId == net.runelite.api.gameval.ItemID.DRAGONHUNTER_XBOW_VORKATH
				|| weaponId == net.runelite.api.gameval.ItemID.XBOWS_CROSSBOW_DRAGON
				|| weaponId == net.runelite.api.gameval.ItemID.BH_XBOWS_CROSSBOW_DRAGON_CORRUPTED
				|| weaponId == net.runelite.api.gameval.ItemID.XBOWS_CROSSBOW_RUNITE
				|| weaponId == net.runelite.api.gameval.ItemID.LEAGUE_3_RUNE_XBOW
				|| weaponId == net.runelite.api.gameval.ItemID.XBOWS_CROSSBOW_ADAMANTITE
				|| weaponId == net.runelite.api.gameval.ItemID.XBOWS_CROSSBOW_MITHRIL
				|| weaponId == net.runelite.api.gameval.ItemID.XBOWS_CROSSBOW_STEEL
				|| weaponId == net.runelite.api.gameval.ItemID.XBOWS_CROSSBOW_IRON
				|| weaponId == net.runelite.api.gameval.ItemID.XBOWS_CROSSBOW_BLURITE
				|| weaponId == net.runelite.api.gameval.ItemID.XBOWS_CROSSBOW_BRONZE;
	}

	private boolean isQuiverEquipped()
	{
		// Basicly check if any quiver is equipped
		return capeId == net.runelite.api.gameval.ItemID.SKILLCAPE_MAX_DIZANAS
				|| capeId == net.runelite.api.gameval.ItemID.SKILLCAPE_MAX_DIZANAS_TROUVER
				|| capeId == net.runelite.api.gameval.ItemID.DIZANAS_QUIVER_INFINITE
				|| capeId == net.runelite.api.gameval.ItemID.DIZANAS_QUIVER_INFINITE_TROUVER
				|| capeId == net.runelite.api.gameval.ItemID.DIZANAS_QUIVER_CHARGED
				|| capeId == net.runelite.api.gameval.ItemID.DIZANAS_QUIVER_CHARGED_TROUVER
				|| capeId == net.runelite.api.gameval.ItemID.DIZANAS_QUIVER_UNCHARGED
				|| capeId == net.runelite.api.gameval.ItemID.DIZANAS_QUIVER_UNCHARGED_TROUVER;
	}

	private int ammoIdToArrayIndex()
	{
		int arrayIndex;
		switch (ammoId)
		{
			case net.runelite.api.gameval.ItemID.XBOWS_CROSSBOW_BOLTS_BRONZE_TIPPED_OPAL_ENCHANTED:
			case net.runelite.api.gameval.ItemID.DRAGON_BOLTS_ENCHANTED_OPAL:
			case net.runelite.api.gameval.ItemID.BR_DRAGON_BOLTS_ENCHANTED_OPAL:
				arrayIndex = 0;
				expectedRate = 0.05; // same rate both pvm and pvp
				boltProcSoundId = 2918;
				ammoName = "Opal";
				needAccuracyPass = false;
				break;
			case net.runelite.api.gameval.ItemID.XBOWS_CROSSBOW_BOLTS_BLURITE_TIPPED_JADE_ENCHANTED:
			case net.runelite.api.gameval.ItemID.DRAGON_BOLTS_ENCHANTED_JADE:
				arrayIndex = 1;
				expectedRate = 0.06; // same rate both pvm and pvp and nobody will ever use these
				boltProcSoundId = 2916;
				ammoName = "Jade";
				needAccuracyPass = false;
				break;
			case net.runelite.api.gameval.ItemID.XBOWS_CROSSBOW_BOLTS_IRON_TIPPED_PEARL_ENCHANTED:
			case net.runelite.api.gameval.ItemID.DRAGON_BOLTS_ENCHANTED_PEARL:
				arrayIndex = 2;
				expectedRate = 0.06; // same rate both pvm and pvp
				boltProcSoundId = 2920;
				ammoName = "Pearl";
				needAccuracyPass = false;
				break;
			case net.runelite.api.gameval.ItemID.XBOWS_CROSSBOW_BOLTS_STEEL_TIPPED_REDTOPAZ_ENCHANTED:
			case net.runelite.api.gameval.ItemID.DRAGON_BOLTS_ENCHANTED_TOPAZ:
				arrayIndex = 3;
				if (config.pvpRates())
				{
					expectedRate = 0.04;
				}
				else
				{
					expectedRate = 0.0;
				}
				boltProcSoundId = 2914;
				ammoName = "Topaz";
				needAccuracyPass = false;
				break;
			case net.runelite.api.gameval.ItemID.XBOWS_CROSSBOW_BOLTS_MITHRIL_TIPPED_SAPPHIRE_ENCHANTED:
			case net.runelite.api.gameval.ItemID.DRAGON_BOLTS_ENCHANTED_SAPPHIRE:
				arrayIndex = 4;
				if (config.pvpRates())
				{
					expectedRate = 0.05;
				}
				else
				{
					expectedRate = 0.25;
				}
				boltProcSoundId = 2912;
				ammoName = "Sapphire";
				needAccuracyPass = true;
				break;
			case net.runelite.api.gameval.ItemID.XBOWS_CROSSBOW_BOLTS_MITHRIL_TIPPED_EMERALD_ENCHANTED:
			case net.runelite.api.gameval.ItemID.DRAGON_BOLTS_ENCHANTED_EMERALD:
				arrayIndex = 5;
				if (config.pvpRates())
				{
					expectedRate = 0.54;
				}
				else
				{
					expectedRate = 0.55;
				}
				boltProcSoundId = 2919;
				ammoName = "Emerald";
				needAccuracyPass = true;
				break;
			case net.runelite.api.gameval.ItemID.XBOWS_CROSSBOW_BOLTS_ADAMANTITE_TIPPED_RUBY_ENCHANTED:
			case net.runelite.api.gameval.ItemID.DRAGON_BOLTS_ENCHANTED_RUBY:
				arrayIndex = 6;
				if (config.pvpRates())
				{
					expectedRate = 0.11;
				}
				else
				{
					expectedRate = 0.06;
				}
				boltProcSoundId = 2911;
				ammoName = "Ruby";
				needAccuracyPass = false;
				break;
			case net.runelite.api.gameval.ItemID.XBOWS_CROSSBOW_BOLTS_ADAMANTITE_TIPPED_DIAMOND_ENCHANTED:
			case net.runelite.api.gameval.ItemID.DRAGON_BOLTS_ENCHANTED_DIAMOND:
			case net.runelite.api.gameval.ItemID.BR_DSTONE_BOLTS_E: // Not sure if these are dstone or diamond bolts. Same item id used to be diamond bolts
				arrayIndex = 7;
				if (config.pvpRates())
				{
					expectedRate = 0.05;
				}
				else
				{
					expectedRate = 0.1;
				}
				boltProcSoundId = 2913;
				ammoName = "Diamond";
				needAccuracyPass = false;
				break;
			case net.runelite.api.gameval.ItemID.XBOWS_CROSSBOW_BOLTS_RUNITE_TIPPED_DRAGONSTONE_ENCHANTED:
			case net.runelite.api.gameval.ItemID.DRAGON_BOLTS_ENCHANTED_DRAGONSTONE:
				arrayIndex = 8;
				expectedRate = 0.06; // same rate both pvm and pvp
				boltProcSoundId = 2915;
				ammoName = "Dragonstone";
				needAccuracyPass = true;
				break;
			case net.runelite.api.gameval.ItemID.XBOWS_CROSSBOW_BOLTS_RUNITE_TIPPED_ONYX_ENCHANTED:
			case net.runelite.api.gameval.ItemID.DRAGON_BOLTS_ENCHANTED_ONYX:
				arrayIndex = 9;
				if (config.pvpRates())
				{
					expectedRate = 0.1;
				}
				else
				{
					expectedRate = 0.11;
				}
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

	private void saveToFile()
	{
		try
		{
			// normal data saving

			Player player = client.getLocalPlayer();
			if (player.getName() == null)
			{
				return;
			}
			// Get the directory path for the player's data
			Path pluginDirectory = Files.createDirectories(Paths.get(RUNELITE_DIR.getPath(),"bolt-proc-counter",player.getName()));

			// Check if the directory already exists
			if (!Files.exists(pluginDirectory)) {
				Files.createDirectories(pluginDirectory); // Create the directory if it doesn't exist
			}

			// Write data to a file within the player's directory, save all ammo data
			for (int i = 0; i < ammoNames.length; i++)
			{
				Path dataFilePath = pluginDirectory.resolve(ammoNames[i] + ".txt");
				String savedData = attackCounterArray[i] + ";" + attacksSinceLastProcArray[i] + ";" +
						longestDryStreakArray[i] + ";" + procCounterArray[i] + ";" + acbSpecsUsedArray[i] + ";" +
						acbSpecsProcsArray[i] + ";" + zcbSpecsUsedArray[i] + ";" + zcbSpecsProcsArray[i] + ";" + attackDealtDmgArray[i];
				Files.write(dataFilePath, savedData.getBytes());
			}

		} catch (IOException e)
		{
			log.error("Error saving to file: " + e.getMessage());
		}
	}

	private void saveDataSampleToFile()
	{
		try
		{
			// data sample saving
			
			Player player = client.getLocalPlayer();
			// Get the directory path for the player's data
			Path pluginDirectory = Files.createDirectories(Paths.get(RUNELITE_DIR.getPath(),"bolt-proc-counter",player.getName()));

			// Check if the directory already exists
			if (!Files.exists(pluginDirectory)) {
				Files.createDirectories(pluginDirectory); // Create the directory if it doesn't exist
			}

			String savedData = "";
			if (config.saveAttacks())
			{
				savedData = (attackCounterArray[ammoIndex] + ";");
			}
			if (config.saveSinceLast())
			{
				savedData += (attacksSinceLastProcArray[ammoIndex] + ";");
			}
			if (config.saveLongestDry())
			{
				savedData += (longestDryStreakArray[ammoIndex] + ";");
			}
			if (config.saveProcs())
			{
				savedData += (procCounterArray[ammoIndex] + ";");
			}
			if (config.saveAcbData())
			{
				savedData += (acbSpecsUsedArray[ammoIndex] + ";");
				savedData += (acbSpecsProcsArray[ammoIndex] + ";");
			}
			if (config.saveZcbData())
			{
				savedData += (zcbSpecsUsedArray[ammoIndex]+ ";");
				savedData += (zcbSpecsProcsArray[ammoIndex]+ ";");
			}
			if (config.saveAttackDealtDmg())
			{
				savedData += (attackDealtDmgArray[ammoIndex]);
			}
			// remove final ; to make it look nicer
			if (savedData.endsWith(";"))
			{
				savedData = savedData.substring(0, savedData.length() -1);
			}

			Path dataFilePath = pluginDirectory.resolve(ammoName + "_data_tracking.txt");
			// check if file exists
			if (!Files.exists(dataFilePath)) {
				// Create the file if it doesn't exist
				try {
					Files.createFile(dataFilePath);
					// make headers for the newly formed file. Only figure out headers once when making the file
					String headerText = "";
					if (config.saveAttacks())
					{
						headerText = "Attacks;";
					}
					if (config.saveSinceLast())
					{
						headerText += "SinceLastProc;";
					}
					if (config.saveLongestDry())
					{
						headerText += "LongestDry;";
					}
					if (config.saveProcs())
					{
						headerText += "Procs;";
					}
					if (config.saveAcbData())
					{
						headerText += "AcbSpecs;AcbProcs;";
					}
					if (config.saveZcbData())
					{
						headerText += "ZcbSpecs;ZcbProcs;";
					}
					if (config.saveAttackDealtDmg())
					{
						headerText += "AttacksHit";
					}
					// remove final ; to make it look nicer
					if (headerText.endsWith(";"))
					{
						headerText = headerText.substring(0, headerText.length() -1);
					}
					Files.write(dataFilePath, (headerText + "\n").getBytes(), StandardOpenOption.APPEND);
				} catch (IOException e) {
					log.error("Error creating data sample file: " + e.getMessage());
				}
			}

			Files.write(dataFilePath, (savedData + "\n").getBytes(), StandardOpenOption.APPEND);
			
		} catch (IOException e)
		{
			log.error("Error saving data sample to file: " + e.getMessage());
		}
	}

	private void loadFromFile()
	{
		// try to load 9 data points first, if it fails try to load old 8 data points
		try
		{
			Player player = client.getLocalPlayer();
			// Get the directory path for the player's data
			// load all data that is tracked and make that the current counters

			String loadedData;
			for (int i = 0; i < ammoNames.length; i++)
			{
				Path dataDirectory = Paths.get(RUNELITE_DIR.getPath(),"bolt-proc-counter",player.getName(),ammoNames[i] + ".txt");
				if (Files.exists(dataDirectory))
				{
					loadedData = (new String(Files.readAllBytes(dataDirectory)));
				}
				else
				{
					loadedData = ("0;0;0;0;0;0;0;0;0");
				}

				// Split the loaded data by semicolons
				String[] parts = loadedData.split(";");

				// add loaded data to arrays
				attackCounterArray[i] = Integer.parseInt(parts[0]);
				attacksSinceLastProcArray[i] = Integer.parseInt(parts[1]);
				longestDryStreakArray[i] = Integer.parseInt(parts[2]);
				procCounterArray[i] = Integer.parseInt(parts[3]);
				acbSpecsUsedArray[i] = Integer.parseInt(parts[4]);
				acbSpecsProcsArray[i] = Integer.parseInt(parts[5]);
				zcbSpecsUsedArray[i] = Integer.parseInt(parts[6]);
				zcbSpecsProcsArray[i] = Integer.parseInt(parts[7]);
				attackDealtDmgArray[i] = Integer.parseInt(parts[8]);
			}
			log.info("9 data points loaded");

		} catch (Exception e) {
			log.error("Error loading 9 data points from file: " + e.getMessage());

			// try old save format of 8 data points
			try
			{
				Player player = client.getLocalPlayer();
				// Get the directory path for the player's data
				// load all data that is tracked and make that the current counters

				String loadedData;
				for (int i = 0; i < ammoNames.length; i++)
				{
					Path dataDirectory = Paths.get(RUNELITE_DIR.getPath(),"bolt-proc-counter",player.getName(),ammoNames[i] + ".txt");
					if (Files.exists(dataDirectory))
					{
						loadedData = (new String(Files.readAllBytes(dataDirectory)));
					}
					else
					{
						loadedData = ("0;0;0;0;0;0;0;0;0");
					}

					// Split the loaded data by semicolons
					String[] parts = loadedData.split(";");

					// add loaded data to arrays
					attackCounterArray[i] = Integer.parseInt(parts[0]);
					attacksSinceLastProcArray[i] = Integer.parseInt(parts[1]);
					longestDryStreakArray[i] = Integer.parseInt(parts[2]);
					procCounterArray[i] = Integer.parseInt(parts[3]);
					acbSpecsUsedArray[i] = Integer.parseInt(parts[4]);
					acbSpecsProcsArray[i] = Integer.parseInt(parts[5]);
					zcbSpecsUsedArray[i] = Integer.parseInt(parts[6]);
					zcbSpecsProcsArray[i] = Integer.parseInt(parts[7]);
				}
				log.info("8 data points loaded");

			} catch (Exception ee) {
				log.error("Error loading 8 data points from file: " + ee.getMessage());
			}

		}
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged event)
	{
		Actor actor = event.getActor();

		// Check if the animation change is for the local player
		if (actor == client.getLocalPlayer())
		{
			PlayingAnimationID = actor.getAnimation();
			PlayingAnimationFrame = 0;

		}
	}
	@Subscribe
	public void onStatChanged(StatChanged event)
	{
		// check if skill is hp
		if (event.getSkill() == Skill.HITPOINTS)
		{
			HpXpDrop = true;
		}
	}
	@Subscribe
	public void onFakeXpDrop(FakeXpDrop event)
	{
		// check if skill is hp
		if (event.getSkill() == Skill.HITPOINTS)
		{
			HpXpDrop = true;
		}
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
			if (weaponId == net.runelite.api.gameval.ItemID.ACB)
			{
				acbSpecUsed = true;
			}

			if (weaponId == net.runelite.api.gameval.ItemID.ZARYTE_XBOW)
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
