package com.BoltProcCounter;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class BoltProcCounterPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(BoltProcCounterPlugin.class);
		RuneLite.main(args);
	}
}