package com.yuexiang.shop.domain.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.GeoPointField;

@Data
@Document(indexName = "shop_index")
public class ShopDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String name;

    @Field(type = FieldType.Keyword)
    private String typeName;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String address;

    @Field(type = FieldType.Keyword)
    private String area;

    @Field(type = FieldType.Double)
    private Double score;

    @Field(type = FieldType.Integer)
    private Integer salesCount;

    @Field(type = FieldType.Integer)
    private Integer avgPrice;

    @Field(type = FieldType.Keyword)
    private String phone;

    @Field(type = FieldType.Keyword)
    private String openHours;

    @GeoPointField
    private String location;

    @Field(type = FieldType.Integer)
    private Integer deleted;
}
