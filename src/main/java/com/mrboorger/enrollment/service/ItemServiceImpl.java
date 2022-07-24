package com.mrboorger.enrollment.service;

import com.mrboorger.enrollment.model.Item;
import com.mrboorger.enrollment.repositories.ItemsRepository;
import com.mrboorger.enrollment.utils.Util;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.sql.SQLException;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.Objects;

@Service
public class ItemServiceImpl implements ItemService {
    @Autowired
    private ItemsRepository itemsRepository;

    private void deleteSubtree(String itemId) {
        itemsRepository.deleteById(itemId);
        for (var childId : itemsRepository.findAllByParentId(itemId)) {
            deleteSubtree(childId.getId());
        }
    }

    static class Info {
        public JSONObject jItem;
        public Long totalSum;
        public Long offersCnt;

        public Info(JSONObject jItem, Long totalSum, Long offersCnt) {
            this.jItem = jItem;
            this.totalSum = totalSum;
            this.offersCnt = offersCnt;
        }
    }

    private Info getSubtree(String itemId) throws JSONException, NoSuchElementException {
        var item = itemsRepository.findById(itemId).get();
        Info info = new Info(new JSONObject(), 0L, 0L);

        JSONObject jItem = info.jItem;

        jItem.put("id", itemId);
        String parentId = item.getParentId();

        jItem.put("type", item.getType());
        jItem.put("name", item.getName());
        jItem.put("parentId", parentId == null ? JSONObject.NULL : parentId);
        jItem.put("date", Util.IsoDateTimeToString(item.getUpdateDate()));

        if (jItem.getString("type").equals("OFFER")) {
            jItem.put("children", JSONObject.NULL);
            jItem.put("price", item.getPrice());
            return new Info(jItem, jItem.getLong("price"), 1L);
        }

        JSONArray jChildren = new JSONArray();

        var children =  itemsRepository.findAllByParentId(itemId);

        for (Item child : children) {
            Info res = getSubtree(child.getId());
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

    // TODO: another exception
    @Override
    public void importItem(Item item) throws SQLException {
        var optRepItem = itemsRepository.findById(item.getId());
        if (optRepItem.isPresent() && !Objects.equals(item.getType(), optRepItem.get().getType())) {
            throw new SQLException("Forbidden to change type");
        }

        itemsRepository.save(item);
    }

    @Override
    public boolean deleteItem(String itemId) throws SQLException {
        if (!itemsRepository.existsById(itemId)) {
            return false;
        }
        deleteSubtree(itemId);
        return true;
    }

    @Override
    public JSONObject getItem(String itemId) throws JSONException {
        if (!itemsRepository.existsById(itemId)) {
            return null;
        }
        return getSubtree(itemId).jItem;
    }

    @Override
    public void updateTime(String parentId, Date updateDate) {
        itemsRepository.setStatusForItem(updateDate, parentId);
    }

    @Override
    public Item getOneItem(String id) throws NoSuchElementException {
        return itemsRepository.findById(id).get();
    }
}
