package com.welty.novello.solver;

import com.welty.novello.core.Square;
import org.jetbrains.annotations.NotNull;

/**
 */
class ListOfEmpties {

    /**
     * Fake node at the beginning and end of the list.
     */
    final Node end;

    ListOfEmpties() {
        end = new Node(null);
        end.next = end;
        end.prev = end;
    }

    /**
     * Add an element to the end of the list
     */
    void add(@NotNull Square square) {
        final Node node = new Node(square);
        node.insertBefore(end);
    }

    /**
     * @return first node in the list
     */
    public Node first() {
        return end.next;
    }

    long calcParity() {
        long parity = 0;
        for (Node node = first(); node != end; node = node.next) {
            parity ^= node.square.parityRegion;
        }
        return parity;
    }

    static class Node {
        /**
         * The data. This is only null for the "fake node" at the beginning and end of lists.
         */
        final Square square;

        /**
         * Previous Node in the List, or null if this is the first Node in the List
         */
        @NotNull Node prev;

        /**
         * Next Node in the List, or null if this is the last Node in the List
         */
        @NotNull Node next;

        private Node(Square square) {
            this.square = square;
        }

        /**
         * Inset a node before next
         *
         * @param next node that will go after the inserted node.
         */
        private void insertBefore(Node next) {
            final Node prev = next.prev;
            insert(prev, next);
        }

        /**
         * Insert a node between prev and next.
         * <p/>
         * Precondition: prev.next = next, next.prev = prev.
         *
         * @param prev node that will go before the inserted node.
         * @param next node that will go after the inserted node.
         */
        private void insert(Node prev, Node next) {
            this.prev = prev;
            this.next = next;
            restore();
        }

        /**
         * Remove a Node
         * <p/>
         * This routine does not change node.prev and node.next, so the caller can use these to continue traversing this List.
         *
         */
        void remove() {
            next.prev = prev;
            prev.next = next;
        }

        /**
         * Restore a node to its previous position in the list
         */
        void restore() {
            prev.next = this;
            next.prev = this;
        }
    }
}
