package com.walmart.stm.service;

import com.walmart.stm.dto.SeatHold;

import java.util.List;
import java.util.Optional;

/**
 * Created by grosal3 on 1/2/16.
 */
public interface TicketService {


    long numSeatsAvailable(Optional<Integer> venueLevel);

    /**
     * Find and hold the best available seats for a customer
     *
     * @param numSeats      the number of seats to find and hold
     * @param minLevel      the minimum venue level - minimum meaning better
     * @param maxLevel      the maximum venue level - maximum meaning worse
     * @param customerEmail unique identifier for the customer
     * @return a SeatHold object identifying the specific seats and related
     * information
     */
    SeatHold findAndHoldSeats(int numSeats, Optional<Integer> minLevel, Optional<Integer> maxLevel,
        String customerEmail);

    /**
     * Commit seats held for a specific customer
     * If the hold has expired or the seat hold cannot be found, then we return null
     *
     * @param seatHoldId    the seat hold identifier
     * @param customerEmail the email address of the customer to which the seat hold is
     *                      assigned
     * @return a reservation confirmation code
     */
    String reserveSeats(String seatHoldId, String customerEmail);
}
