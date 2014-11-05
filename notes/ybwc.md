Parallelism
===========

Parallel search is done by the Younger Brother Wait Concept (YBWC). Probably the best description
of this algorithm is in [Valavan Manohararajah's PhD thesis](http://www.top-5000.nl/ps/Parallel%20Alpha-Beta%20Search%20on%20Shared%20Memory%20Multiprocessors.pdf) 

Novello's implementation is as follows:

1. Worker threads are created, one for each processor core.
2. A single thread is assigned the root node and begins searching it. 
3. A thread adds its current node to the split pool if:
    + One subnode has already been searched.
    + The node is of sufficient depth (~20?)
4. When a node is added to the split pool, idle threads grab a subnode and search it.
5. The thread that began the node is responsible for collecting the values and returning them. 
