
PlayerFactory
-------------
We need:
* To create a Player for a tournament
* To re-use Players where possible to reduce overhead costs
  * Re-use any Player in the same thread.
  * If two Players with the same Engine but different depths are playing each other,
    create two Players rather than call setDepth() between moves. This keeps the shallower player from
    using the deeper player's transposition table.

PlayerFactories therefore will probably be implemented using a ThreadLocal cache of existing Players.