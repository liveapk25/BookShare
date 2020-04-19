package com.se.bpgc.bookshare.ui.notifications;


public class NotificationModel {

    public String notifID;

    public String isbn;
    public String title;
    public String author;
    public String thumbnail;
    public String category;
    public double averageRating;
    public String description;

    public String requestUid;
    public String requestName;
    public String requestPhone;
    public String requestEmail;


    public String responseUid;
    public String responseName;
    public String responsePhone;
    public String responseEmail;

    public String isPending;
    public String type;
    public String scheduleDelete;
    public long count;

    public String userUid;

    public NotificationModel(String notifID, String isbn, String title, String author, String thumbnail, String category, double averageRating, String description, String requestUid, String requestName, String requestPhone, String requestEmail, String responseUid, String responseName, String responsePhone, String responseEmail, String isPending, String type, String scheduleDelete, long count, String userUid) {
        this.notifID = notifID;
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.thumbnail = thumbnail;
        this.category = category;
        this.averageRating = averageRating;
        this.description = description;
        this.requestUid = requestUid;
        this.requestName = requestName;
        this.requestPhone = requestPhone;
        this.requestEmail = requestEmail;
        this.responseUid = responseUid;
        this.responseName = responseName;
        this.responsePhone = responsePhone;
        this.responseEmail = responseEmail;
        this.isPending = isPending;
        this.type = type;
        this.scheduleDelete = scheduleDelete;
        this.count = count;
        this.userUid = userUid;
    }

    public long getCount() {
        return count;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getRequestUid() {
        return requestUid;
    }

    public String getResponseUid() {
        return responseUid;
    }

    public String getRequestName() {
        return requestName;
    }

    public String getResponseName() {
        return responseName;
    }

    public String getIsPending() {
        return isPending;
    }

    public String getType() {
        return type;
    }

    public String getUserUid() {
        return userUid;
    }

    public String getRequestPhone() {
        return requestPhone;
    }

    public String getResponsePhone() {
        return responsePhone;
    }

    public String getRequestEmail() {
        return requestEmail;
    }

    public String getResponseEmail() {
        return responseEmail;
    }

    public String getCategory() {
        return category;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public String getDescription() {
        return description;
    }

    public String getNotifID() {
        return notifID;
    }

    public void setResponseUid(String responseUid) {
        this.responseUid = responseUid;
    }

    public void setResponseName(String responseName) {
        this.responseName = responseName;
    }

    public void setResponsePhone(String responsePhone) {
        this.responsePhone = responsePhone;
    }

    public void setResponseEmail(String responseEmail) {
        this.responseEmail = responseEmail;
    }

    public void setIsPending(String isPending) {
        this.isPending = isPending;
    }

    public String getScheduleDelete() {
        return scheduleDelete;
    }

    public void setScheduleDelete(String scheduleDelete) {
        this.scheduleDelete = scheduleDelete;
    }
}
