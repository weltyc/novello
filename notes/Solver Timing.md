Solver timing
=============

DeepSolverTimer from depth 22
-----------------------------

commit 4a1ec46 (just prior to endgame hashtable synchronization) 

    Total flip count at 22:    2.03 Gn,    0.03 Gevals,    2.20 G$ / 31 s = 64.9 Mn/s
    
commit f13e299 (just added synchronization to endgame hashtables):

    Total flip count at 22:    2.03 Gn,    0.03 Gevals,    2.20 G$ / 32 s = 62.9 Mn/s

DeepSolverTimer at various depths
---------------------------------

commit f13e299, timing to solve 100 positions at various depths. On mac-mini, using 8 threads
each solving a different position.

Depth | time/s
------+--------
   20 |    8
   22 |   32
   24 |  148
   26 |  765
   