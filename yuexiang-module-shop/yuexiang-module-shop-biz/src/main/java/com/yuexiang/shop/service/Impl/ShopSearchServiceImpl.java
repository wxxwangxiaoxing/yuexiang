package com.yuexiang.shop.service.impl;

import co.elastic.clients.json.JsonData;
import com.yuexiang.common.pojo.PageResult;
import com.yuexiang.shop.domain.document.ShopDocument;
import com.yuexiang.shop.domain.vo.ShopListVO;
import com.yuexiang.shop.service.FavoriteService;
import com.yuexiang.shop.service.ShopSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightFieldParameters;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightParameters;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "yuexiang.elasticsearch.enabled", havingValue = "true")
public class ShopSearchServiceImpl implements ShopSearchService {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final FavoriteService favoriteService;

    @Override
    public PageResult<ShopListVO> searchShops(String keyword, Long typeId, String area,
                                              Double longitude, Double latitude,
                                              Integer minPrice, Integer maxPrice,
                                              Integer pageNo, Integer pageSize, Long userId) {
        NativeQuery query = buildSearchQuery(keyword, typeId, area, minPrice, maxPrice,
                longitude, latitude, pageNo, pageSize);

        try {
            var searchHits = elasticsearchTemplate.search(query, ShopDocument.class);
            List<ShopListVO> results = new ArrayList<>();

            for (var hit : searchHits) {
                ShopDocument doc = hit.getContent();
                ShopListVO vo = convertToVO(doc, hit);
                results.add(vo);
            }

            Set<Long> shopIds = results.stream().map(ShopListVO::getId).collect(Collectors.toSet());
            Set<Long> favoriteIds = favoriteService.getFavoriteShopIds(userId, shopIds);
            results.forEach(vo -> vo.setIsFavorite(favoriteIds.contains(vo.getId())));

            return new PageResult<>(results, searchHits.getTotalHits());
        } catch (Exception e) {
            log.error("ES 搜索失败，降级到 DB 查询", e);
            return new PageResult<>(Collections.emptyList(), 0L);
        }
    }

    private NativeQuery buildSearchQuery(String keyword, Long typeId, String area,
                                         Integer minPrice, Integer maxPrice,
                                         Double longitude, Double latitude,
                                         Integer pageNo, Integer pageSize) {
        var builder = new co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery.Builder();

        // 必须条件：未删除
        builder.must(m -> m.term(t -> t.field("deleted").value(0)));

        // 关键词搜索（name + address）
        if (keyword != null && !keyword.trim().isEmpty()) {
            builder.should(s -> s.match(m -> m.field("name").query(keyword).boost(3.0f)));
            builder.should(s -> s.match(m -> m.field("address").query(keyword).boost(1.0f)));
            builder.should(s -> s.matchPhrasePrefix(m -> m.field("name").query(keyword).boost(5.0f)));
            builder.minimumShouldMatch("1");
        }

        // 精确过滤
        if (typeId != null) {
            builder.filter(f -> f.term(t -> t.field("typeName").value(typeId)));
        }
        if (area != null && !area.trim().isEmpty()) {
            builder.filter(f -> f.term(t -> t.field("area").value(area)));
        }
        if (minPrice != null) {
            builder.filter(f -> f.range(r -> r.field("avgPrice").gte(JsonData.of(minPrice))));
        }
        if (maxPrice != null) {
            builder.filter(f -> f.range(r -> r.field("avgPrice").lte(JsonData.of(maxPrice))));
        }

        // 排序
        var sortList = new ArrayList<co.elastic.clients.elasticsearch._types.SortOptions>();
        if (longitude != null && latitude != null) {
            sortList.add(co.elastic.clients.elasticsearch._types.SortOptions.of(s -> s
                    .geoDistance(g -> g
                            .field("location")
                            .location(l -> l.latlon(ll -> ll.lat(latitude).lon(longitude)))
                            .order(co.elastic.clients.elasticsearch._types.SortOrder.Asc)
                    )
            ));
        } else {
            sortList.add(co.elastic.clients.elasticsearch._types.SortOptions.of(s -> s
                    .field(f -> f.field("score").order(co.elastic.clients.elasticsearch._types.SortOrder.Desc))
            ));
            sortList.add(co.elastic.clients.elasticsearch._types.SortOptions.of(s -> s
                    .field(f -> f.field("salesCount").order(co.elastic.clients.elasticsearch._types.SortOrder.Desc))
            ));
        }

        HighlightFieldParameters fieldParams = HighlightFieldParameters.builder()
                .withPreTags("<em>")
                .withPostTags("</em>")
                .build();

        HighlightParameters highlightParams = HighlightParameters.builder()
                .withPreTags("<em>")
                .withPostTags("</em>")
                .build();

        Highlight highlight = new Highlight(highlightParams, List.of(
                new HighlightField("name", fieldParams),
                new HighlightField("address", fieldParams)
        ));

        return NativeQuery.builder()
                .withQuery(builder.build()._toQuery())
                .withSort(sortList)
                .withPageable(org.springframework.data.domain.PageRequest.of(pageNo - 1, pageSize))
                .withHighlightQuery(new HighlightQuery(highlight, ShopDocument.class))
                .build();
    }

    private ShopListVO convertToVO(ShopDocument doc, org.springframework.data.elasticsearch.core.SearchHit<ShopDocument> hit) {
        ShopListVO vo = new ShopListVO();
        vo.setId(doc.getId());
        vo.setName(doc.getName());
        vo.setTypeName(doc.getTypeName());
        vo.setAddress(doc.getAddress());
        vo.setArea(doc.getArea());
        vo.setAvgPrice(doc.getAvgPrice() != null ? Integer.valueOf(String.valueOf(doc.getAvgPrice() / 100.0)) : null);
        vo.setScore(doc.getScore());
        vo.setSalesCount(doc.getSalesCount());

        // 高亮处理
        if (hit.getHighlightFields() != null) {
            Map<String, List<String>> highlights = hit.getHighlightFields();
            if (highlights.containsKey("name")) {
                vo.setName(String.join("", highlights.get("name")));
            }
            if (highlights.containsKey("address")) {
                vo.setAddress(String.join("", highlights.get("address")));
            }
        }

        return vo;
    }
}
