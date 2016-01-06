package com.walmart.stm.dto;

import java.time.LocalDateTime;
import java.util.List;

public class SeatHold {

    private String id;
    private List<String> seatIdsHeld;
    private LocalDateTime holdTime;
    private LocalDateTime expirationTime;
    private boolean hasEnoughSeats = true;

    public List<String> getSeatIdsHeld() {
        return seatIdsHeld;
    }

    public void setSeatIdsHeld(List<String> seatIdsHeld) {
        this.seatIdsHeld = seatIdsHeld;
    }

    public LocalDateTime getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(LocalDateTime expirationTime) {
        this.expirationTime = expirationTime;
    }

    public LocalDateTime getHoldTime() {
        return holdTime;
    }

    public void setHoldTime(LocalDateTime holdTime) {
        this.holdTime = holdTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "SeatHold id:[" + getId() + "], seatsHeld:[" + getSeatIdsHeld() + "], holdTime: [" +
            getHoldTime() + "], expirationTime: [" + expirationTime + "]" + ", hasEnoughSeats: [" + hasEnoughSeats
            + "]";
    }

    public boolean isHasEnoughSeats() {
        return hasEnoughSeats;
    }

    public void setHasEnoughSeats(boolean hasEnoughSeats) {
        this.hasEnoughSeats = hasEnoughSeats;
    }
}
