package com.mrboorger.enrollment.repositories;

import com.mrboorger.enrollment.model.Item;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface ItemsRepository extends CrudRepository<Item, String> {
    Iterable<Item> findAllByParentId(String parentId);

    @Modifying
    @Query("update Item item set item.updateDate = ?1 where item.id = ?2")
    void setStatusForItem(Date updateDate, String id);
}
