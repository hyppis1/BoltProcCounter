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
	private int specialPercentage = 0;


    boolean acbSpecUsed = false;
	boolean zcbSpecUsed = false;
	boolean HpXpDrop = false;
	boolean soundMuted = true;
	boolean soundMutedB2B = true;
	boolean needAccuracyPass = false;
	boolean shouldLoad = true;
	boolean shouldSave = false;
	private int coolDownTicksRemaining = 0;


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

			// 10% bonus from kandarin hard diary
			if (config.KandarinHardDiary())
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
						coolDownTicksRemaining = 4;

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
		return weaponId == ItemID.ZARYTE_CROSSBOW
				|| weaponId == ItemID.ARMADYL_CROSSBOW
				|| weaponId == ItemID.DRAGON_HUNTER_CROSSBOW_T
				|| weaponId == ItemID.DRAGON_HUNTER_CROSSBOW_B
				|| weaponId == ItemID.DRAGON_HUNTER_CROSSBOW
				|| weaponId == ItemID.DRAGON_CROSSBOW_CR
				|| weaponId == ItemID.DRAGON_CROSSBOW
				|| weaponId == ItemID.RUNE_CROSSBOW_OR
				|| weaponId == ItemID.RUNE_CROSSBOW
				|| weaponId == ItemID.ADAMANT_CROSSBOW
				|| weaponId == ItemID.MITHRIL_CROSSBOW
				|| weaponId == ItemID.STEEL_CROSSBOW
				|| weaponId == ItemID.IRON_CROSSBOW
				|| weaponId == ItemID.BLURITE_CROSSBOW
				|| weaponId == ItemID.BRONZE_CROSSBOW;
	}

	private boolean isQuiverEquipped()
	{
		// Basicly check if any quiver is equipped
		return capeId == ItemID.DIZANAS_MAX_CAPE
				|| capeId == ItemID.DIZANAS_MAX_CAPE_L
				|| capeId == ItemID.BLESSED_DIZANAS_QUIVER
				|| capeId == ItemID.BLESSED_DIZANAS_QUIVER_L
				|| capeId == ItemID.DIZANAS_QUIVER
				|| capeId == ItemID.DIZANAS_QUIVER_L
				|| capeId == ItemID.DIZANAS_QUIVER_UNCHARGED
				|| capeId == ItemID.DIZANAS_QUIVER_UNCHARGED_L;
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
				expectedRate = 0.05; // same rate both pvm and pvp
				boltProcSoundId = 2918;
				ammoName = "Opal";
				needAccuracyPass = false;
				break;
			case ItemID.JADE_BOLTS_E:
			case ItemID.JADE_DRAGON_BOLTS_E:
				arrayIndex = 1;
				expectedRate = 0.06; // same rate both pvm and pvp and nobody will ever use these
				boltProcSoundId = 2916;
				ammoName = "Jade";
				needAccuracyPass = false;
				break;
			case ItemID.PEARL_BOLTS_E:
			case ItemID.PEARL_DRAGON_BOLTS_E:
				arrayIndex = 2;
				expectedRate = 0.06; // same rate both pvm and pvp
				boltProcSoundId = 2920;
				ammoName = "Pearl";
				needAccuracyPass = false;
				break;
			case ItemID.TOPAZ_BOLTS_E:
			case ItemID.TOPAZ_DRAGON_BOLTS_E:
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
			case ItemID.SAPPHIRE_BOLTS_E:
			case ItemID.SAPPHIRE_DRAGON_BOLTS_E:
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
			case ItemID.EMERALD_BOLTS_E:
			case ItemID.EMERALD_DRAGON_BOLTS_E:
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
			case ItemID.RUBY_BOLTS_E:
			case ItemID.RUBY_DRAGON_BOLTS_E:
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
			case ItemID.DIAMOND_BOLTS_E:
			case ItemID.DIAMOND_BOLTS_E_23649:
			case ItemID.DIAMOND_DRAGON_BOLTS_E:
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
			case ItemID.DRAGONSTONE_BOLTS_E:
			case ItemID.DRAGONSTONE_DRAGON_BOLTS_E:
				arrayIndex = 8;
				expectedRate = 0.06; // same rate both pvm and pvp
				boltProcSoundId = 2915;
				ammoName = "Dragonstone";
				needAccuracyPass = true;
				break;
			case ItemID.ONYX_BOLTS_E:
			case ItemID.ONYX_DRAGON_BOLTS_E:
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
			System.err.println("Error saving to file: " + e.getMessage());
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
					System.err.println("Error creating data sample file: " + e.getMessage());
				}
			}

			Files.write(dataFilePath, (savedData + "\n").getBytes(), StandardOpenOption.APPEND);
			
		} catch (IOException e)
		{
			System.err.println("Error saving data sample to file: " + e.getMessage());
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
			System.out.println("9 data points loaded");

		} catch (Exception e) {
			System.err.println("Error loading 9 data points from file: " + e.getMessage());

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
				System.out.println("8 data points loaded");

			} catch (Exception ee) {
				System.err.println("Error loading 8 data points from file: " + ee.getMessage());
			}

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
		// System.out.println("sound id: " + eventSoundId);
	}
	@Provides
	BoltProcCounterConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BoltProcCounterConfig.class);
	}

}
