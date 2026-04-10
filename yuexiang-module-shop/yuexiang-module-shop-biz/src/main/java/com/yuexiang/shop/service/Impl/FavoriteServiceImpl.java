package com.yuexiang.shop.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yuexiang.common.exception.BadRequestException;
import com.yuexiang.common.exception.NotFoundException;
import com.yuexiang.shop.constants.ShopFavoriteConstants;
import com.yuexiang.shop.domain.entity.Favorite;
import com.yuexiang.shop.domain.entity.Shop;
import com.yuexiang.shop.mapper.ShopFavoriteMapper;
import com.yuexiang.shop.mapper.ShopMapper;
import com.yuexiang.shop.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service("shopFavoriteService")
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final ShopFavoriteMapper favoriteMapper;
    private final ShopMapper shopMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleFavorite(Long userId, Long shopId) {
        validateParams(userId, shopId);
        validateShop(shopId);

        Favorite existing = getFavoriteRecord(userId, shopId);

        if (existing == null) {
            tryInsertFavorite(userId, shopId);
            log.info("收藏商户成功: userId={}, shopId={}", userId, shopId);
            return;
        }

        Integer deleted = existing.getDeleted();
        if (deleted != null && deleted.equals(ShopFavoriteConstants.NOT_DELETED)) {
            cancelFavorite(existing.getId());
            log.info("取消收藏商户成功: userId={}, shopId={}", userId, shopId);
        } else {
            recoverFavorite(existing.getId());
            log.info("恢复收藏商户成功: userId={}, shopId={}", userId, shopId);
        }
    }

    @Override
    public boolean isFavorite(Long userId, Long shopId) {
        validateParams(userId, shopId);

        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
                .eq(Favorite::getBizType, ShopFavoriteConstants.BIZ_TYPE_SHOP)
                .eq(Favorite::getBizId, shopId)
                .eq(Favorite::getDeleted, ShopFavoriteConstants.NOT_DELETED);

        return favoriteMapper.selectCount(wrapper) > 0;
    }

    @Override
    public Set<Long> getFavoriteShopIds(Long userId, Set<Long> shopIds) {
        if (userId == null || shopIds == null || shopIds.isEmpty()) {
            return Collections.emptySet();
        }

        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
                .eq(Favorite::getBizType, ShopFavoriteConstants.BIZ_TYPE_SHOP)
                .in(Favorite::getBizId, shopIds)
                .eq(Favorite::getDeleted, ShopFavoriteConstants.NOT_DELETED)
                .select(Favorite::getBizId);

        List<Favorite> favorites = favoriteMapper.selectList(wrapper);
        return favorites.stream()
                .map(Favorite::getBizId)
                .collect(Collectors.toSet());
    }

    private void validateParams(Long userId, Long shopId) {
        if (userId == null) {
            throw new BadRequestException("用户ID不能为空");
        }
        if (shopId == null) {
            throw new BadRequestException("商户ID不能为空");
        }
    }

    private void validateShop(Long shopId) {
        Shop shop = shopMapper.selectById(shopId);
        if (shop == null || (shop.getDeleted() != null && shop.getDeleted() == ShopFavoriteConstants.DELETED)) {
            throw new NotFoundException("商户不存在");
        }
    }

    /**
     * 查询收藏记录，不区分 deleted 状态。
     * 同一个 userId + bizType + bizId 理论上只应有一条。
     */
    private Favorite getFavoriteRecord(Long userId, Long shopId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
                .eq(Favorite::getBizType, ShopFavoriteConstants.BIZ_TYPE_SHOP)
                .eq(Favorite::getBizId, shopId)
                .last("limit 1");

        return favoriteMapper.selectOne(wrapper);
    }

    /**
     * 插入收藏记录。
     * 若并发下唯一索引冲突，则说明记录已被其他线程插入，此时转为恢复收藏。
     */
    private void tryInsertFavorite(Long userId, Long shopId) {
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setBizType(ShopFavoriteConstants.BIZ_TYPE_SHOP);
        favorite.setBizId(shopId);
        favorite.setCreateTime(LocalDateTime.now());
        favorite.setDeleted(ShopFavoriteConstants.NOT_DELETED);

        try {
            favoriteMapper.insert(favorite);
        } catch (DuplicateKeyException e) {
            log.warn("收藏记录并发插入冲突，转为恢复处理: userId={}, shopId={}", userId, shopId);
            Favorite dbRecord = getFavoriteRecord(userId, shopId);
            if (dbRecord == null) {
                throw e;
            }
            recoverFavorite(dbRecord.getId());
        }
    }

    /**
     * 取消收藏（逻辑删除）
     */
    private void cancelFavorite(Long favoriteId) {
        LambdaUpdateWrapper<Favorite> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Favorite::getId, favoriteId)
                .set(Favorite::getDeleted, ShopFavoriteConstants.DELETED);
        favoriteMapper.update(null, updateWrapper);
    }

    /**
     * 恢复收藏
     */
    private void recoverFavorite(Long favoriteId) {
        LambdaUpdateWrapper<Favorite> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Favorite::getId, favoriteId)
                .set(Favorite::getDeleted, ShopFavoriteConstants.NOT_DELETED);
        favoriteMapper.update(null, updateWrapper);
    }
}
