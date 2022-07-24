package com.mrboorger.enrollment.controller;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mrboorger.enrollment.model.Item;
import com.mrboorger.enrollment.repositories.ItemsRepository;
import com.mrboorger.enrollment.service.ItemService;
import com.mrboorger.enrollment.utils.Util;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.sql.SQLException;
import java.util.*;

@RestController
public class ItemController {
    private final ItemService itemService;

    @ExceptionHandler
    public ResponseEntity<?> handleException(MethodArgumentNotValidException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

//    @ExceptionHandler
//    public ResponseEntity<?> handleException(InvalidDataAccessApiUsageException ex) {
//        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
//    }

    @Autowired
    public ItemController(ItemService clientService) {
        this.itemService = clientService;
    }

    private void UpdateParent(String parentId, Date updateDate) throws SQLException {
        if (parentId == null) {
            return;
        }

        var item = itemService.getOneItem(parentId);
        item.setUpdateDate(updateDate);

        System.out.println(item);

        itemService.importItem(item);
        UpdateParent(itemService.getOneItem(parentId).getParentId(), updateDate);
    }

    // TODO: change
    static class Request {
        private List<@Valid Item> items;
        @NotNull
        private @DateTimeFormat(
                iso = DateTimeFormat.ISO.DATE_TIME) Date updateDate;

        public List<Item> getItems() {
            return items;
        }

        public void setItems(List<Item> items) {
            this.items = items;
        }

        public Date getUpdateDate() {
            return updateDate;
        }

        public void setUpdateDate(Date updateDate) {
            this.updateDate = updateDate;
        }
    }

    @PostMapping(value = "/imports")
    public ResponseEntity<?> importItem(@Valid @RequestBody Request req) {
        for(var item : req.getItems()) {
            item.setUpdateDate(req.getUpdateDate());
            try {
                item.setUpdateDate(req.getUpdateDate());
                itemService.importItem(item);
            } catch (SQLException e) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }
        for(var item : req.getItems()) {
            System.out.println(item);
            try {
                UpdateParent(item.getParentId(), req.getUpdateDate());
            } catch (SQLException e) {
                e.printStackTrace();
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
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

}
