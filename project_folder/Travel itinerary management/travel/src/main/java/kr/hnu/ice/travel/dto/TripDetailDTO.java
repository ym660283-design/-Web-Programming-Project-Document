package kr.hnu.ice.travel.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * 작성자: 김용민
 * 기능 설명: 여행 세부 일정 정보를 화면과 데이터 계층 사이에서 전달하는 DTO 클래스입니다.
 */
public class TripDetailDTO {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private int detailId;
    private int tripId;
    private LocalDate scheduleDate;
    private String placeName;
    private LocalTime visitTime;
    private String memo;
    private int cost;
    private int sortOrder;
    private BigDecimal latitude;
    private BigDecimal longitude;

    public int getDetailId() {
        return detailId;
    }

    public void setDetailId(int detailId) {
        this.detailId = detailId;
    }

    public int getTripId() {
        return tripId;
    }

    public void setTripId(int tripId) {
        this.tripId = tripId;
    }

    public LocalDate getScheduleDate() {
        return scheduleDate;
    }

    public void setScheduleDate(LocalDate scheduleDate) {
        this.scheduleDate = scheduleDate;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public LocalTime getVisitTime() {
        return visitTime;
    }

    public void setVisitTime(LocalTime visitTime) {
        this.visitTime = visitTime;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public String getScheduleDateValue() {
        return scheduleDate == null ? "" : scheduleDate.toString();
    }

    public String getVisitTimeValue() {
        return visitTime == null ? "" : visitTime.format(TIME_FORMATTER);
    }

    public String getCostText() {
        return String.format("%,d원", cost);
    }

    public String getLatitudeValue() {
        return latitude == null ? "" : latitude.stripTrailingZeros().toPlainString();
    }

    public String getLongitudeValue() {
        return longitude == null ? "" : longitude.stripTrailingZeros().toPlainString();
    }

    public boolean hasLocation() {
        return latitude != null && longitude != null;
    }
}
