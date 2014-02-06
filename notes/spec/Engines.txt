Engines
-------
The main job of an Engine is to generate a move given a Position and a maximum search depth.
"Engine" is a concept,
not a java interface. The two java interfaces are "SyncEngine" and "AsyncEngine" which accomplish
the job synchronously and asynchronously respectively.

Some things we need:
* External engines need to communicate via NBoard protocol.
* We need a simple (synchronous) way to write simple engines.

AsyncEngine roughly parallels the NBoard protocol. The caller calls requestMove() and, sometime later
and possibly in another thread, the Engine calls engineMove(). All external programs are used as AsyncEngines.

A SyncEngine has "getMove()" and "setDepth()" methods. The caller calls
getMove() and blocks until a move is calculated and returned. This is much simpler to implement
and has lower threading overhead. It is used for (most? all?) internal engines.

A SyncEngine is much simpler to implement and is used in non-interactive situations such as tournaments.
An AsyncEngine is needed to communicate with external programs such as NTest and Edax.

Due to the moderately high startup cost of some Engines, an Engine may be reused from game to game.

All SyncEngines may assume that they will only be called from a single thread over their lifetime.
This allows dramatic code simplification and performance improvements inside the Engine. (AsyncEngines too?)

Engine Users
------------
These are programs that use Engines [Engines.txt] to accomplish a goal.

Some things we need:
* The GUI needs to get moves asynchronously and get responses on the EDT.
* A Tournament needs to get moves synchronously.

These uses are superficially similar but have so many small differences in implementation and requirements
that they are considered two entirely separate concepts within the program. Both users communicate with
adaptors to Engines.

Both synchronous and asynchronous engines have "tracks" that connect the user's required API with
the engine's API.

      Async Track                                Sync Track
      -----------                                ----------
     NBoard Window          User           SelfPlaySet, Tournament
           |                                         |
       EdtEngine       User Adaptor               Player
           |                                         |
      AsyncEngine        Interface              SyncEngine
           |                                         |
       PingEngine                                    |
           |                                         |
      NTest, Edax        Engines            Charlie, Novello


The Gui's adaptor is the EdtEngine; this is an AsyncEngine that only communicates on the EDT.

The Tournament's adaptor is the Player; this is like a SyncEngine but has a fixed search depth.

The PingEngine is designed to allow multiple PingEngines to be combined into a new PingEngine.

On the Sync track, engines expect to receive all commands on a single thread and return their responses
synchronously on that thread.

On the Async track, engines expect to receive all commands on a single thread.
The responses they return are not required to be on a single thread, but all responses are required to
happen-after both any previous requests and any previous responses from their underlying engine.

Engines can switch tracks. In