# Bolt Proc Counter

This plugin tracks your attacks, procs and rate of bolts procs.
There is options to separate Armadyl/Zaryte cross bow special attacks from the normal counter.
You must have sounds enabled for the plugin to track ruby bolt procs.

For bolts effects that dont bypass accuracy check, the plugin assumes 100% accuracy for expected rates tracking.

## Data saving

Data is saved in .txt files with corresponding bolt name. For example Ruby.txt file contains data for ruby bolt tracking.

Saved data would look like this "27;5;13;3;0;0;0;0". 

This example data is "Attacks: 27, Since last proc: 5, Longest dry: 13, Procs: 3, Acb specs: 0, Acb procs: 0, Zcb specs: 0 Zcb procs: 0".

Save location: %USERPROFILE%/.runelite/bolt-proc-counter/%INGAMENAME%/"bolt type".txt



