Novello - a Java othello engine
===============================

Novello is a Java othello engine. It is the internal engine for
NBoard, a Java Othello GUI.

Building
--------

Building Novello requires two things in addition to its source code:
 
1. The othello-utils library, available at <https://github.com/weltyc/othello>, and 
2. the evaluation coefficients, available [here] (https://www.dropbox.com/sh/ybneczo8zpihq9m/AAAo_TB3E7rBfvF1RtgDmrGRa).

The coefficient folder ('j') must be placed in {project-home}/coefficients/ 
 to make {project-home}/coefficients/j
 
Search
------
Novello uses a 'Search object' to perform a game-tree search. Search objects are single-threaded but may be
reused after they have completed a search. There are two kinds of Search objects: MidgameSearch,
which performs a midgame tree search with evaluation and MPC; and Solver, which performs a full-width search
until the end of the game.

When doing a midgame search, and also when doing a probable solve, Novello calls MidgameSearcher. 
When doing a probable solve the MidgameSearcher will, in fact, call the Solver for the last few nodes 
(it's almost as quick to do a solve as it is to do a mid game evaluation, and it's obviously more accurate).
 
For a proven endgame search (no mpc, search every line to the end of the game), Novello calls Solver. 
Solver in turn creates a MidgameSearcher which is used to evaluate moves for use in the move-sorting algorithm.