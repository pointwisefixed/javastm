package com.walmart.stm.service;

import com.walmart.stm.dto.Seat;
import com.walmart.stm.dto.SeatHold;
import com.walmart.stm.transaction.Ref;
import com.walmart.stm.transaction.STM;
import com.walmart.stm.transaction.TransactionBlock;
import com.walmart.stm.transaction.TransactionWithResultBlock;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class TicketServiceImpl implements TicketService, HoldReleaser {

    private Map<Integer, List<Ref<Seat>>> seatsByLevel;

    public TicketServiceImpl(Map<Integer, List<Seat>> seatsById) {
        Map<Integer, List<Ref<Seat>>> seats = seatsById.entrySet().stream().
            collect(Collectors
                .toMap(Map.Entry::getKey, e -> e.getValue().stream().map(Ref::new).collect(Collectors.toList())));
        this.seatsByLevel = seats;
    }

    @Override
    public long numSeatsAvailable(Optional<Integer> venueLevel) {
        Long count = STM.transactionWithResult(new TransactionWithResultBlock<>((tx) -> {
            if (venueLevel.isPresent()) {
                return seatsByLevel.get(venueLevel.get()).stream().filter(sm -> {
                    Seat s = sm.getValue(tx);
                    return !s.isReserved() && !s.isOnHold() && s.getHoldExpirationTime() == null;
                }).count();

            }
            return seatsByLevel.values().stream().flatMap(Collection::stream).filter(sm -> {
                Seat s = sm.getValue(tx);
                return !s.isReserved() && !s.isOnHold() && s.getHoldExpirationTime() == null;
            }).count();
        }));
        return count;
    }

    @Override
    public SeatHold findAndHoldSeats(int numSeats, Optional<Integer> minLevel, Optional<Integer> maxLevel,
        String customerEmail) {
        if (numSeats <= 0)
            throw new IllegalArgumentException("Number of seats must be > 0");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiryDate = now.plus(60, ChronoUnit.SECONDS);
        Integer ml = minLevel.orElse(Integer.MIN_VALUE);
        Integer max = maxLevel.orElse(Integer.MAX_VALUE);
        return STM.transactionWithResult(new TransactionWithResultBlock<>((tx) -> {
            SeatHold hold = new SeatHold();
            final String holdId = UUID.randomUUID().toString();

            Set<Ref<Seat>> seatSet = seatsByLevel.values().stream().flatMap(Collection::stream).filter(sm -> {
                Seat s = sm.getValue(tx);
                return s.getCurrentSeatHoldId() == null && !s.isReserved() && !s.isOnHold()
                    && s.getHoldExpirationTime() == null && s.getLevel() >= ml && s.getLevel() <= max;
            }).sorted((o1, o2) -> {
                Seat o1s = o1.getValue(tx);
                Seat o2s = o2.getValue(tx);
                return o1s.getSeatNumber() < o2s.getSeatNumber() ?
                    -1 :
                    o1s.getSeatNumber() > o2s.getSeatNumber() ? 1 : 0;
            }).limit(numSeats).collect(Collectors.toSet());
            if (seatSet.size() == numSeats) {
                hold.setId(holdId);
                hold.setHoldTime(now);
                hold.setExpirationTime(expiryDate);
                seatSet.stream().forEach(sm -> {
                    Seat s = sm.getValue(tx);
                    Seat ns = new Seat.Builder(s.getId(), s.getLevel(), s.getSeatNumber()).onHold(true)
                        .holdExpirationTime(expiryDate).currentSeatHoldId(holdId).currentSeatHoldEmail(holdId)
                        .lastTimeReleased(s.getLastTimeReleased()).build();
                    sm.setValue(ns, tx);
                });
                hold.setSeatIdsHeld(seatsByLevel.values().stream().flatMap(Collection::stream).filter(sm -> {
                    Seat s = sm.getValue(tx);
                    return holdId.equals(s.getCurrentSeatHoldId());
                }).map(sm -> {
                    Seat s = sm.getValue(tx);
                    return s.getId();
                }).collect(Collectors.toList()));
            } else {
                hold.setHasEnoughSeats(false);
            }
            return hold;
        }));
    }

    @Override
    public String reserveSeats(String seatHoldId, String customerEmail) {
        return STM.transactionWithResult(new TransactionWithResultBlock<>((tx) -> {
            Set<Ref<Seat>> seatSet = seatsByLevel.values().stream().flatMap(Collection::stream).filter(sm -> {
                Seat s = sm.getValue(tx);
                return seatHoldId.equals(s.getCurrentSeatHoldId());
            }).collect(Collectors.toSet());

            if (seatSet.size() == 0) {
                // expired already :(
                return null;
            }

            seatSet.stream().forEach(sm -> {
                Seat s = sm.getValue(tx);
                Seat ns = new Seat.Builder(s.getId(), s.getLevel(), s.getSeatNumber()).reserve(true)
                    .reserveEmail(customerEmail).lastTimeReleased(s.getLastTimeReleased()).build();
                sm.setValue(ns, tx);
            });

            return UUID.randomUUID().toString();
        }));
    }


    @Override
    public void releaseHolds() {
        final LocalDateTime now = LocalDateTime.now();
        STM.transaction(new TransactionBlock((tx) -> {
            Set<Ref<Seat>> toRelease = seatsByLevel.values().stream().flatMap(Collection::stream).filter(sm -> {
                Seat s = sm.getValue(tx);
                return s.isOnHold() && s.getHoldExpirationTime() != null &&
                    now.isAfter(s.getHoldExpirationTime());
            }).collect(Collectors.toSet());
            System.out.println("Releasing " + toRelease.size() + " seats.");
            toRelease.stream().forEach(sm -> {
                Seat s = sm.getValue(tx);
                Seat ns = new Seat.Builder(s.getId(), s.getLevel(), s.getSeatNumber()).lastTimeReleased(now).build();
                sm.setValue(ns, tx);
            });
        }));
    }

    public void printResults() {
        STM.transaction(new TransactionBlock((tx) -> seatsByLevel.forEach((key, value) -> {
            long reserved = value.stream().filter((sm) -> {
                Seat s = sm.getValue(tx);
                return s.isReserved();
            }).count();
            long hold = value.stream().filter((sm) -> {
                Seat s = sm.getValue(tx);
                return s.isOnHold();
            }).count();
            System.out.printf("For level %s: Reserved: %s, On-Hold: %s \n", key, reserved, hold);
        })));
    }
}
