Novello Opening Book
====================

The Book stores positions that Novello has seen before. It is used both to move instantly in the opening
and during midgame search to check for transpositions into the book.

Design
------

Conceptually, the Book is stored as a map: Board &rarr; Data.

The Book only stores positions with nEmpty >= minEmpties. This keeps the size of the book down.
minEmpties is planned to be 20.

During Novello's searches, each search node within 3 ply of the root is checked by looking up its Board's minimal
 reflection in the Book.

At the root of a search, if the position is in book and nEmpty > minEmpties, all subpositions are searched. If one
is found with the same score as the root score, that move is immediately played. If no subposition matches the score,
the search continues as normal. (No subposition would match, for example, if the book contains a game that was aborted
 before reaching a terminal position).

Details
-------
Unlike NTest, which has one book for each search depth, Novello shares its book among all its instances.

No position with < minEmpties is stored; instead the access routine does a search. This keeps the size of the book down.
Solves from 20 empty currently take on the order of 2 seconds.

### Passes

If a Board has no legal moves (player must pass) it is not stored in the book.

If the book generator is told to add such a position to the book, it passes and then adds the resulting position 
to the book. If the result of the pass is another position with no legal moves (i.e. the game is over),
the position is not physically stored in book but terminal positions are valued correctly in all search routines.

The book's search methods correctly handle positions where the mover must pass.

Node Data
---------

Each book node is categorized as Solved, Unsolved Leaf (ULeaf), or Unsolved Branch (UBranch).

Nodes that have been played in a game are added to the book either as Solved or UBranch nodes. 
Solved and UBranch nodes are therefore called 'played' nodes.
ULeaf nodes only store deviations.
 
UBranch nodes store the square of the best unplayed move. -2 indicates all moves have been played. -1 indicates
the best unplayed move has not been calculated. The position following the best unplayed move must be in book; it is
added when calculating the best unplayed move.

Solved and ULeaf nodes do not store a move square, only a valuation.

Invariants
----------

There are three consistency levels for a book Consistent, Valued, and Negamaxed:

### Consistent

This is the lowest possible level. Games have been added to book, but the book can't be used in games until it is Valued.

Solved and ULeaf nodes must contain a valid value. UBranch nodes with sq==-2 must have all subnodes in the book.
UBranch nodes with sq >= 0 must have the sub-position in the book.
   
### Valued

At this level a value can be assigned to each node in the book, so the book can be used in games.

As Consistent, plus UBranch nodes may not have sq==-1.
As a consequence of the above rules, UBranch nodes with depth == minDepth must be solved.

To get to 'valued' state, any UBranch node with sq==-1 must be
either solved (in which case it is changed to a Solved node) or have its best deviation calculated using
a midgame search of unplayed moves (in which case the successor position is added to book as a ULeaf node).
If no unplayed move exists, the sq is changed to -2. This process is typically performed from lowest #empties to highest,
so that solves can be reused in the search.

### Negamaxed
 
Valued, plus UBranch nodes with sq>=0 must have the successor position be ULeaf.
 
To get to 'Negamaxed' state, all UBranch nodes with sq>=0 whose successor position is Solved or UBranch has its
best deviation calculated using a midgame search of unplayed moves (in which case the successor position is added to book
as a ULeaf node). If no unplayed move exists, the sq is changed to -2.

Adding to book
--------------

A set of games is added to book by adding all positions with >= 20 empties to the book as ULeaf nodes with best
unplayed move set to -1. This is a Consistent book. The book is then valued as described in 'Valued', above.

To Do
-----

Multithreaded book generation
Larger book
Book location GUI
Database - load from folder