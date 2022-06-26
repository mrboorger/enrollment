package com.mrboorger.enrollment.controller;

import com.mrboorger.enrollment.model.Item;
import com.mrboorger.enrollment.service.ItemService;
import com.mrboorger.enrollment.utils.Util;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.*;

@RestController
public class ItemController {
    private final ItemService itemService;

    // TODO: check
    private Item parseItem(JSONObject jItem, Date updatedDate) throws IllegalArgumentException {
        Item item = new Item();

        try {
            if (!jItem.has("type") && jItem.isNull("type") ||
                    (!jItem.getString("type").equals("CATEGORY") && !jItem.getString("type").equals("OFFER"))) {
                throw new IllegalArgumentException("type must be 'CATEGORY' or 'OFFER'");
            }
            item.setType(jItem.getString("type"));
        } catch (JSONException e) {
            throw new IllegalArgumentException("type must be 'CATEGORY' or 'OFFER'");
        }

        try {
            if (!jItem.has("type") || jItem.isNull("name")) {
                throw new IllegalArgumentException("name must be not null string'");
            }
            item.setName(jItem.getString("name"));
        } catch (JSONException e) {
            throw new IllegalArgumentException("name must be not null string");
        }

        try {
            if (!jItem.has("type") || jItem.isNull("id")) {
                throw new IllegalArgumentException("id must be not null string'");
            }
            item.setId(jItem.getString("id"));
        } catch (JSONException e) {
            throw new IllegalArgumentException("id must be not null string");
        }


        try {
//            if (!jItem.has("parentId")) {
//                throw new IllegalArgumentException("parentId must be null or string");
//            }
            if (!jItem.isNull("parentId")) {
                item.setParentId(jItem.getString("parentId"));
            }
        } catch (JSONException e) {
            throw new IllegalArgumentException("parentId must be null or string");
        }

        if (item.getType().equals("CATEGORY")) {
            if (!jItem.isNull("price")) {
                throw new IllegalArgumentException("price must be null for CATEGORY");
            }
        }

        if (item.getType().equals("OFFER")) {
            try {
                if (jItem.isNull("price") || jItem.getLong("price") < 0) {
                    throw new IllegalArgumentException("price must be not null not negative integer for OFFER");
                }
                item.setPrice(jItem.getLong("price"));
            } catch (JSONException e) {
                throw new IllegalArgumentException("price must be not null integer for OFFER");
            }
        }

        if (updatedDate == null) {
            throw new IllegalArgumentException("date must be not null");
        }
        item.setDate(updatedDate);

        return item;
    }

    @Autowired
    public ItemController(ItemService clientService) {
        this.itemService = clientService;
    }

    private void UpdateParent(String parentId, String updateTime) throws SQLException {
        if (parentId == null) {
            return;
        }

        itemService.updateTime(parentId, updateTime);
        UpdateParent(itemService.getOneItem(parentId).getParentId(), updateTime);
    }

    // JSONObject
    @PostMapping(value = "/imports")
    public ResponseEntity<?> importItem(@RequestBody String strParams) {
        JSONObject params = null;
        try {
            params = new JSONObject(strParams);
        } catch (JSONException e) {
//            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        String dateStr;
        try {
            if (!params.has("updateDate") || params.isNull("updateDate")) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            dateStr = params.getString("updateDate");
        } catch (JSONException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (!Util.isIsoTimestamp(dateStr)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Date updateDate = Util.parseIsoDateTime(dateStr);

        JSONArray items;
        try {
            items = params.getJSONArray("items");
        } catch (JSONException e) {
            // items must be not null JSONArray
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        for (int i = 0; i < items.length(); ++i) {
            try {
                Item item = parseItem(items.getJSONObject(i), updateDate);
                itemService.importItem(item);
                UpdateParent(item.getParentId(), Util.IsoDateTimeToString(item.getDate()));
            } catch (JSONException | IllegalArgumentException | SQLException e) {
//                e.printStackTrace();
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping(value = "/delete/{id}")
    public ResponseEntity<?> deleteItem(@PathVariable(name = "id") String itemId) {
        try {
            if (!itemService.deleteItem(itemId)) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (SQLException e) {
//            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(value = "/nodes/{id}")
    public ResponseEntity<?> getInformation(@PathVariable(name = "id") String itemId) {
        try {
            JSONObject ans = itemService.getItem(itemId);
            return ans != null
                    ? new ResponseEntity<>(ans.toString(), HttpStatus.OK)
                    : new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (SQLException | JSONException e) {
//            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

}
