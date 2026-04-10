package com.yuexiang.user.assembler;

import com.yuexiang.user.domain.vo.BrowseHistoryGroupVO;
import com.yuexiang.user.domain.vo.BrowseHistoryPageVO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BrowseHistoryPageAssembler {

    public BrowseHistoryPageVO assemble(Long total, List<BrowseHistoryGroupVO> groups) {
        return BrowseHistoryPageVO.builder()
                .total(total)
                .groups(groups)
                .build();
    }
}
