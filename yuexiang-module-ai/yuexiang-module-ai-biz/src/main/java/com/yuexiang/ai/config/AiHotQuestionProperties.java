package com.yuexiang.ai.config;

import com.yuexiang.ai.domain.vo.AiHotQuestionVO;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "yuexiang.ai.hot-question")
public class AiHotQuestionProperties {

    private List<AiHotQuestionVO> items = defaultItems();

    private static List<AiHotQuestionVO> defaultItems() {
        List<AiHotQuestionVO> items = new ArrayList<>();
        items.add(AiHotQuestionVO.builder().id(1).text("附近有啥好吃的").icon("food").build());
        items.add(AiHotQuestionVO.builder().id(2).text("人均50聚餐推荐").icon("budget").build());
        items.add(AiHotQuestionVO.builder().id(3).text("安静约会餐厅").icon("date").build());
        items.add(AiHotQuestionVO.builder().id(4).text("今日秒杀推荐").icon("flash-sale").build());
        items.add(AiHotQuestionVO.builder().id(5).text("适合带娃的餐厅").icon("family").build());
        items.add(AiHotQuestionVO.builder().id(6).text("深夜食堂推荐").icon("night").build());
        return items;
    }
}
