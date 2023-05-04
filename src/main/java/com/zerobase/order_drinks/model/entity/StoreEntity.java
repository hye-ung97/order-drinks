package com.zerobase.order_drinks.model.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "STORE")
public class StoreEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "지점 번호", example = "1")
    private int storeId;

    @Column(name = "store_name", unique = true)
    @Schema(description = "지점명", example = "스타벅스 역삼대로점")
    private String storeName;
    @Schema(description = "지점 주소", example = "대한민국 서울특별시 강남구 테헤란로 211")
    private String address;

    @OneToMany(mappedBy = "store")
    private List<ListOrderEntity> list;

}
