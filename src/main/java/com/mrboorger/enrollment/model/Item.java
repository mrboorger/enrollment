package com.mrboorger.enrollment.model;

import java.util.Date;

public class Item {
    String id;
    String type;
    String name;
    String parentId;
    Long price;
    Date updateDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price)  {
        this.price = price;
    }

    public Date getDate() {
        return updateDate;
    }

    public void setDate(Date date) {
        this.updateDate = date;
    }
}
