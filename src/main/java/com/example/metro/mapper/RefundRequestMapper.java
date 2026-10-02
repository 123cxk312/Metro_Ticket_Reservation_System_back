package com.example.metro.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.metro.domain.entity.RefundRequest;
import com.example.metro.domain.enums.RefundStatus;
import com.example.metro.dto.refund.RefundDetailItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RefundRequestMapper extends BaseMapper<RefundRequest> {

    List<RefundDetailItem> findRefundsByUserId(@Param("userId") Long userId);

    List<RefundDetailItem> findRefundsForAdmin(@Param("status") RefundStatus status);

    RefundDetailItem findRefundDetail(@Param("refundId") Long refundId);
}
