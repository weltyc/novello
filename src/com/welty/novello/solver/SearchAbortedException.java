package com.welty.novello.solver;

/**
 * This exception is thrown when an internal engine aborts its search
 * <p/>
 * Using an exception causes the stack to be unwound and ensures no corrupt values are stored in the transposition table.
 */
public class SearchAbortedException extends Exception {
}
