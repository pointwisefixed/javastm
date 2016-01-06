package com.walmart.stm.main;

import com.walmart.stm.dto.Seat;
import com.walmart.stm.dto.SeatHold;
import com.walmart.stm.service.TicketServiceImpl;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * Created by grosal3 on 1/2/16.
 */
public class TicketingTest {

    /**
     * 1l - 250 seats
     * 2l - 400 seats
     * 3l - 500 seats
     * 4l - 700 seats
     *
     * @param args
     */
    public static void main(String[] args) {
        int l1 = 250;
        int l2 = 400;
        int l3 = 500;
        int l4 = 700;
        Map<Integer, List<Seat>> seatsByLevel = new HashMap<>();
        for (int i = 0; i < (l1 + l2 + l3 + l4); i++) {
            int level = i < l1 ? 1 : i >= l1 && i < l2 + l1 ? 2 : i >= l2 + l1 && i < l1 + l2 + l3 ? 3 : 4;
            String id = UUID.randomUUID().toString();
            List<Seat> seatsForLevel = seatsByLevel.get(level);
            if (seatsForLevel == null) {
                seatsForLevel = new ArrayList<>();
                seatsByLevel.put(level, seatsForLevel);
            }
            seatsForLevel.add(new Seat.Builder(id, level, i).build());
        }
        TicketServiceImpl ticketService = new TicketServiceImpl(seatsByLevel);
        int numberOfTransactions = 2000;

        ExecutorService executorService = Executors.newFixedThreadPool(numberOfTransactions);
        ScheduledExecutorService holdFreer = Executors.newScheduledThreadPool(5);
        holdFreer.scheduleAtFixedRate(() -> ticketService.releaseHolds(), 1, 10, TimeUnit.SECONDS);
        AtomicInteger seatsTaken = new AtomicInteger(0);
        IntStream.range(0, numberOfTransactions).forEach(i -> executorService.execute(() -> {
            int maxLevelToUse = (new Random()).nextInt(5);
            while (maxLevelToUse <= 0) {
                maxLevelToUse = (new Random()).nextInt(5);
            }
            int minLevelToUse = (new Random()).nextInt(maxLevelToUse);

            if (minLevelToUse <= 0) {
                minLevelToUse = (new Random()).nextInt(maxLevelToUse);
            }
            int seatsToHold = (new Random()).nextInt(20 - 1) + 1;
            while (seatsToHold <= 0) {
                seatsToHold = (new Random()).nextInt(20 - 1) + 1;
            }
            String customerEmail = (new Random()).nextInt() + "@something.com";
            SeatHold seatHold = ticketService
                .findAndHoldSeats(seatsToHold, Optional.of(minLevelToUse), Optional.of(maxLevelToUse), customerEmail);
            if (seatHold.isHasEnoughSeats()) {
                System.out.println("Got hold id " + seatHold.getId() + " for " + seatHold.getSeatIdsHeld().size()
                    + " seats and for customer email: " + customerEmail + " and expiration time of " + seatHold
                    .getExpirationTime());
                int seconds = (new Random()).nextInt(80);
                System.out.println("thinking  = [" + seconds + " seconds] whether to reserve or not");
                try {
                    Thread.sleep(seconds * 1000l);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                boolean shouldIReserve = (new Random()).nextBoolean();
                System.out.println(
                    "Decided to reserve hold id, customer email, number of seats " + seatHold.getId() + ", "
                        + customerEmail + "," +
                        seatHold.getSeatIdsHeld().size() + ": " + shouldIReserve);
                if (shouldIReserve) {
                    String reservationId = ticketService.reserveSeats(seatHold.getId(), customerEmail);
                    if (reservationId != null) {
                        seatsTaken.set(seatsTaken.addAndGet(seatHold.getSeatIdsHeld().size()));
                        System.out.println("seats taken = [" + seatsTaken + "]");
                    }
                }
            }
        }));


        try {
            executorService.shutdown();
            executorService.awaitTermination(6000, TimeUnit.SECONDS);
            holdFreer.shutdown();
            holdFreer.awaitTermination(12000, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            executorService.shutdownNow();
            holdFreer.shutdownNow();
        }
        ticketService.printResults();
    }
}
