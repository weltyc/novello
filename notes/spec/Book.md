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
If no unplayed move exists, the sq is changed to -2. 
 
The valuation process must be performed from lowest #empties to highest, so that when midgame searches encounter a book
position they use the correct value. This also allows solves to be reused, speeding up the valuation process.

### Negamaxed
 
Valued, plus UBranch nodes with sq>=0 must have the successor position be ULeaf.
 
To get to 'Negamaxed' state, all UBranch nodes with sq>=0 whose successor position is Solved or UBranch has its
best deviation calculated using a midgame search of unplayed moves (in which case the successor position is added to book
as a ULeaf node). If no unplayed move exists, the sq is changed to -2.

Adding to book
--------------

A set of games is added to book by adding all positions with >= 20 empties to the book as ULeaf nodes with best
unplayed move set to -1. This is a Consistent book. The book is then valued as described in 'Valued', above.

Compression
-----------
The book is written in compressed format to disk, as follows:

The write method maintains a set S of positions that have been written to book.
The compressed format is a forest; a set of trees. Each tree writes a root position
and a list of subtrees. Positions that have already been written (and are therefore in S)
are not written again.

The algorithm searches the book in order of nEmpty, from 60 down to 1. Any position found that is not in S
is written as a tree:
 
### Write a tree

+ mover/enemy bitboard, node data, subtree list. 

Node data is stored as follows:

+  1 byte for node type (0=Solved, 1=ULeaf, 2=UBranch).
+  Solved, ULeaf: 1 byte for value.
+  UBranch : 1 byte for best unplayed deviation.  

If nEmpty > minDepth, this is followed by the subtree list, which is written as:

+ 1 byte representing move, or -1 to indicate no more moves. Successor position passes if necessary.
+ Node data for the successor position, subtree list. 

### Reading a tree

This is the opposite of writing a tree, but the book needs to be valued on read (because UBranch nodes
don't store values).

To Do
-----

Larger book
Automatically move book to AppData on startup
Book learning