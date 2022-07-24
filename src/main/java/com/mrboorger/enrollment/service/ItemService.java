package com.mrboorger.enrollment.service;

import com.mrboorger.enrollment.model.Item;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.Date;

public interface ItemService {
    void importItem(Item item) throws SQLException;

    boolean deleteItem(String itemId) throws SQLException;

    JSONObject getItem(String itemId) throws SQLException, JSONException;

    void updateTime(String id, Date updateDate) throws SQLException;

    Item getOneItem(String id) throws SQLException;
}
