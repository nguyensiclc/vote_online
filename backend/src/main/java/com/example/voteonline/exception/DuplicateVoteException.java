package com.example.voteonline.exception;

/**
 * Thrown when an IP address tries to vote more than once.
 */
public class DuplicateVoteException extends RuntimeException {
    public DuplicateVoteException(String message) {
        super(message);
    }
}

