# Bolt Proc Counter

This plugin tracks your attacks, procs and rate of bolts procs.
There is options to separate Armadyl/Zaryte cross bow special attacks from the normal counter.
You must have sounds enabled for the plugin to track ruby bolt procs.

## Ruby Reset Protection

Configurable settings to hide Quick-exit and similar menu options while ruby resetting.

When enabled, Quick-exit and similar menu options are hidden for a user-defined number of ticks after a natural ruby proc.
Can also choose to prevent Quick-exit and similar options from being clicked in already open menus after a natural ruby proc

An optional setting restricts this behaviour to ruby resets by only hiding these menu options for a user-defined number of ticks after entering a new instanced region.

## Data saving

Data is saved in .txt files with corresponding bolt name. For example Ruby.txt file contains data for ruby bolt tracking.

Saved data would look like this "27;5;13;3;0;0;0;0,5". 

This example data is "Attacks: 27, Since last proc: 5, Longest dry: 13, Procs: 3, Acb specs: 0, Acb procs: 0, Zcb specs: 0 Zcb procs: 0, AttacksHit: 5".

Save location: %USERPROFILE%/.runelite/bolt-proc-counter/%INGAMENAME%/"bolt type".txt

Sample saving works the same way. Saves data to .txt file but will append the data. 
Sample data can be easily imported to Excel file using Excels in-build features.

Save location: %USERPROFILE%/.runelite/bolt-proc-counter/%INGAMENAME%/"bolt type"_data_tracking.txt

## Change log

- 14.7.2025 Updated plugin to work with quiver ammo slot. And added "attacks that hit" and overall accuracy tracking. Data saving includes "attacks that hit" as last item.
- 30.7.2026 Added Ruby Reset Protection for the plugin. Configurable settings to hide Quick-exit like options when ruby resetting.

