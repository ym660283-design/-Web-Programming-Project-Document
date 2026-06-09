package kr.hnu.ice.travel.dto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

public class TripDTO {
    private static final DateTimeFormatter PERIOD_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    private int tripId;
    private int userId;
    private String ownerName;
    private String tripTitle;
    private String destination;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
    private String shareCode;
    private boolean owner;

    public int getTripId() {
        return tripId;
    }

    public void setTripId(int tripId) {
        this.tripId = tripId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getTripTitle() {
        return tripTitle;
    }

    public void setTripTitle(String tripTitle) {
        this.tripTitle = tripTitle;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getShareCode() {
        return shareCode;
    }

    public void setShareCode(String shareCode) {
        this.shareCode = shareCode;
    }

    public boolean isOwner() {
        return owner;
    }

    public void setOwner(boolean owner) {
        this.owner = owner;
    }

    public String getStartDateValue() {
        return startDate == null ? "" : startDate.toString();
    }

    public String getEndDateValue() {
        return endDate == null ? "" : endDate.toString();
    }

    public String getTripPeriod() {
        if (startDate == null || endDate == null) {
            return "";
        }
        return startDate.format(PERIOD_FORMATTER) + " - " + endDate.format(PERIOD_FORMATTER);
    }

    public String getTripDuration() {
        if (startDate == null || endDate == null) {
            return "";
        }

        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        if (days <= 1) {
            return "당일치기";
        }
        return (days - 1) + "박 " + days + "일";
    }

    public String getStartMonthShort() {
        if (startDate == null) {
            return "";
        }
        return startDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toUpperCase(Locale.ENGLISH);
    }

    public String getStartDayString() {
        if (startDate == null) {
            return "";
        }
        return String.format("%02d", startDate.getDayOfMonth());
    }

    public int getStartYear() {
        return startDate == null ? 0 : startDate.getYear();
    }
}
