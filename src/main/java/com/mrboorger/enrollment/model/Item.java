package com.mrboorger.enrollment.model;

import com.mrboorger.enrollment.controller.IsCorrectItem;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;


@Data
@Entity
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@IsCorrectItem
public class Item {
    @Id
    @EqualsAndHashCode.Include
    String id;
    String type;
    String name;
    String parentId;
    Long price;
    Date updateDate;
}
