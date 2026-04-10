package com.yuexiang.voucher.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuexiang.voucher.domain.entity.SeckillVoucher;
import com.yuexiang.voucher.domain.vo.SeckillVoucherVO;
import com.yuexiang.voucher.domain.dto.SeckillVoucherWithInfo;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 秒杀券数据访问接口
 * <p>
 * 继承 MyBatis-Plus 的 BaseMapper，提供基础的CRUD操作，
 * 并扩展了秒杀业务所需的定制化查询和更新方法。
 * </p>
 */
@Mapper
public interface SeckillVoucherMapper extends BaseMapper<SeckillVoucher> {

    /**
     * 查询秒杀券列表（仅取前 pageSize 条记录）
     * <p>
     * 本方法默认从第一条开始，按一定规则（如开始时间倒序）获取指定数量的秒杀券。
     * 如需分页查询，建议使用 {@link #selectVouchersBySessionWithPage}。
     * </p>
     *
     * @param pageSize 需要获取的记录数量，必须大于0
     * @return 秒杀券视图对象列表，若无数据则返回空列表
     */
    List<SeckillVoucherVO> selectSeckillList(@Param("pageSize") Integer pageSize);

    /**
     * 扣减指定秒杀券的库存
     * <p>
     * 该方法对应数据库更新操作：UPDATE seckill_voucher SET stock = stock - 1 WHERE voucher_id = #{voucherId} AND stock > 0
     * 通过 stock > 0 条件保证库存不会扣减为负数，是秒杀场景下保证超卖控制的关键操作。
     * </p>
     *
     * @param voucherId 秒杀券ID
     * @return 受影响的行数，通常为1表示扣减成功，0表示库存不足或券不存在
     */
    int decrStock(@Param("voucherId") Long voucherId);

    int incrStock(@Param("voucherId") Long voucherId);

    /**
     * 根据场次ID列表统计各场次下的秒杀券数量
     * <p>
     * 返回的每个 Map 包含两个键值对：
     * <ul>
     *     <li>"session_id" : 场次ID (Long)</li>
     *     <li>"count" : 该场次下的秒杀券数量 (Long)</li>
     * </ul>
     * </p>
     *
     * @param sessionIds 场次ID列表，不可为空
     * @return 统计结果列表，若列表为空则返回空列表
     */
    /**
     * 根据场次ID列表统计各场次下的秒杀券数量，返回以场次ID为键的Map
     *
     * @param sessionIds 场次ID列表
     * @return Map<场次ID, 秒杀券数量>
     */
    @MapKey("session_id")
    List<Map<String, Object>> countVouchersBySessionIds(@Param("sessionIds") List<Long> sessionIds);

    /**
     * 分页查询指定场次下的秒杀券及关联信息
     * <p>
     * 此方法用于秒杀券列表的分页展示，返回结果中包含秒杀券的详细信息以及关联的优惠券信息（通过 {@link SeckillVoucherWithInfo} 封装）。
     * </p>
     *
     * @param sessionId 场次ID
     * @param offset    偏移量，从0开始（例如第一页 offset=0，第二页 offset=pageSize）
     * @param pageSize  每页记录数
     * @return 秒杀券及关联信息列表
     */
    List<SeckillVoucherWithInfo> selectVouchersBySessionWithPage(
            @Param("sessionId") Long sessionId,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize);

    /**
     * 统计指定场次下的秒杀券总数
     * <p>
     * 用于配合 {@link #selectVouchersBySessionWithPage} 进行分页计算。
     * </p>
     *
     * @param sessionId 场次ID
     * @return 秒杀券总数，若场次不存在或没有券则返回0
     */
    Long countBySessionId(@Param("sessionId") Long sessionId);
}
