package com.mrboorger.enrollment.service;

import com.mrboorger.enrollment.database.DataBase;
import com.mrboorger.enrollment.model.Item;
import com.mrboorger.enrollment.utils.Util;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;

@Service
public class ItemServiceImpl implements ItemService {
    private final DataBase dataBase = DataBase.getInstance();

    private static String GenImportQuery(Item item) {
        StringBuilder query = new StringBuilder();

        String parentIdStr = item.getParentId();
        if (parentIdStr == null) {
            parentIdStr = "null";
        } else {
            parentIdStr = "'" + parentIdStr + "'";
        }

        query.append("INSERT INTO items (id, type, name, parentId, price, updateDate) ");
//        query.append(String.format("VALUES ('%s', '%s', '%s', %s, %s, STR_TO_DATE('%s','%%Y-%%m-%%dT%%T.%%fZ'))",
        query.append(String.format("VALUES ('%s', '%s', '%s', %s, %s, '%s')",
                item.getId(), item.getType(), item.getName(),
                parentIdStr, item.getPrice(), Util.IsoDateTimeToString(item.getDate())));
        query.append(String.format("ON DUPLICATE KEY UPDATE type='%s', name='%s', parentId=%s, price=%s, updateDate='%s'",
                item.getType(), item.getName(),
                parentIdStr, item.getPrice(), Util.IsoDateTimeToString(item.getDate())));

        return query.toString();
    }

    private void deleteSubtree(String itemId) throws SQLException {
        dataBase.executeUpdate(String.format("DELETE from items WHERE id='%s'", itemId));
        ResultSet resultSet = dataBase.executeQuery(String.format("SELECT id FROM items WHERE parentId = '%s'", itemId));
        ArrayList<String> children = new ArrayList<>();
        while (resultSet.next()) {
            children.add(resultSet.getString(1));
        }
        resultSet.close();

        for (String child : children) {
            deleteSubtree(child);
        }
    }

    class Info {
        public JSONObject jItem;
        public Long totalSum;
        public Long offersCnt;

        public Info(JSONObject jItem, Long totalSum, Long offersCnt) {
            this.jItem = jItem;
            this.totalSum = totalSum;
            this.offersCnt = offersCnt;
        }
    }

    private Info getSubtree(String itemId) throws SQLException, JSONException {
        ResultSet resultSet = dataBase.executeQuery(String.format("SELECT * FROM items WHERE id = '%s'", itemId));
        Info info = new Info(new JSONObject(), 0L, 0L);
        JSONObject jItem = info.jItem;

        if (!resultSet.next()) {
            throw new JSONException("");
        }
        jItem.put("id", itemId);
        String parentId = resultSet.getString("parentId");

        jItem.put("type", resultSet.getString("type"));
        jItem.put("name", resultSet.getString("name"));
        jItem.put("parentId", parentId == null ? JSONObject.NULL : parentId);
        jItem.put("date", resultSet.getString("updateDate"));

        if (jItem.getString("type").equals("OFFER")) {
            jItem.put("children", JSONObject.NULL);
            jItem.put("price", Long.valueOf(resultSet.getString("price")));
            return new Info(jItem, jItem.getLong("price"), 1L);
        }

        JSONArray jChildren = new JSONArray();

        resultSet = dataBase.executeQuery(String.format("SELECT id FROM items WHERE parentId = '%s'", itemId));
        ArrayList<String> children = new ArrayList<>();
        while (resultSet.next()) {
            children.add(resultSet.getString("id"));
        }
        resultSet.close();

        for (String child : children) {
            Info res = getSubtree(child);
            jChildren.put(res.jItem);
            if (res.offersCnt > 0) {
                info.offersCnt += res.offersCnt;
                info.totalSum += res.totalSum;
            }
        }

        jItem.put("children", jChildren);
        if (info.offersCnt > 0) {
            jItem.put("price", info.totalSum / info.offersCnt);
        } else {
            jItem.put("price", JSONObject.NULL);
        }
        return info;
    }

    private boolean ifExist(String itemId) throws SQLException {
        ResultSet resultSet = dataBase.executeQuery(String.format("SELECT id FROM items WHERE id='%s'", itemId));
        return resultSet.next();
    }

    // TODO: another exception
    @Override
    public void importItem(Item item) throws SQLException {
        if (ifExist(item.getId()) && !Objects.equals(item.getType(), getOneItem(item.getId()).getType())) {
            throw new SQLException("Forbidden to change type");
        }

        dataBase.executeUpdate(GenImportQuery(item));
    }

    @Override
    public boolean deleteItem(String itemId) throws SQLException {
        if (!ifExist(itemId)) {
            return false;
        }
        deleteSubtree(itemId);
        return true;
    }

    @Override
    public JSONObject getItem(String itemId) throws SQLException, JSONException {
        if (!ifExist(itemId)) {
            return null;
        }
        return getSubtree(itemId).jItem;
    }

    @Override
    public void updateTime(String parentId, String updateDate) throws SQLException {
        dataBase.executeUpdate(String.format("UPDATE items SET updateDate = '%s' WHERE id='%s'",
                updateDate, parentId));
    }

    @Override
    public Item getOneItem(String id) throws SQLException {
        ResultSet rs = dataBase.executeQuery(String.format("SELECT * from items WHERE id='%s'", id));
        rs.next();

        Item item = new Item();

        item.setId(id);
        item.setType(rs.getString("type"));
        item.setName(rs.getString("name"));
        item.setParentId(rs.getString("parentId"));
        item.setDate(Util.parseIsoDateTime(rs.getString("updateDate")));
        item.setPrice(rs.getLong("price"));

        return item;
    }
}
