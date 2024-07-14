# Bolt Proc Counter

This plugin tracks your attacks, procs and rate of bolts procs.
There is options to separate Armadyl/Zaryte cross bow special attacks from the normal counter.
You must have sounds enabled for the plugin to track ruby bolt procs.

## Data saving

Data is saved in .txt files with corresponding bolt name. For example Ruby.txt file contains data for ruby bolt tracking.

Saved data would look like this "27;5;13;3;0;0;0;0,5". 

This example data is "Attacks: 27, Since last proc: 5, Longest dry: 13, Procs: 3, Acb specs: 0, Acb procs: 0, Zcb specs: 0 Zcb procs: 0, AttacksHit: 5".

Save location: %USERPROFILE%/.runelite/bolt-proc-counter/%INGAMENAME%/"bolt type".txt

Sample saving works the same way. Saves data to .txt file but will append the data. 
Sample data can be easily imported to Excel file using Excels in-build features.

Save location: %USERPROFILE%/.runelite/bolt-proc-counter/%INGAMENAME%/"bolt type"_data_tracking.txt

## Change log

- 14.7 Updated plugin to work with quiver ammo slot. And added "attacks that hit" and overall accuracy tracking. Data saving includes "attacks that hit" as last item.

