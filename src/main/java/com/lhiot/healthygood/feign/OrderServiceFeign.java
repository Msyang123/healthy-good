package com.lhiot.healthygood.feign;

import com.leon.microx.web.result.Pages;
import com.leon.microx.web.result.Tuple;
import com.lhiot.healthygood.feign.model.*;
import com.lhiot.healthygood.feign.type.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

@Component
@FeignClient("BASE-ORDER-SERVICE-V1-0")
public interface OrderServiceFeign {

    //创建订单
    @RequestMapping(value = "/orders", method = RequestMethod.POST)
    ResponseEntity<OrderDetailResult> createOrder(@RequestBody CreateOrderParam orderParam);

    //创建一个已支付订单
    @RequestMapping(value = "/orders/paid", method = RequestMethod.POST)
    ResponseEntity<OrderDetailResult> createPaidOrder(@RequestBody CreateOrderParam orderParam);

    //海鼎备货回调，送货上门订单修改订单状态为WAIT_DISPATCHING，且会主动发送配送
    @RequestMapping(value = "/orders/{orderCode}/delivery", method = RequestMethod.PUT)
    ResponseEntity updateOrderToDelivery(@PathVariable("orderCode") String orderCode, @RequestBody DeliveryParam deliverParam);

    //修改订单状态为已支付
    @RequestMapping(value = "/orders/{orderCode}/payed", method = RequestMethod.PUT)
    ResponseEntity updateOrderToPayed(@PathVariable("orderCode") String orderCode, @RequestBody Payed payed);

    //发送海鼎，修改订单状态
    @RequestMapping(value = "/orders/{orderCode}/hd-status", method = RequestMethod.PUT)
    ResponseEntity sendOrderToHd(@PathVariable("orderCode") String orderCode);

    //修改订单状态(DISPATCHING,RECEIVED,FAILURE,其它状态请使用特定接口)
    @RequestMapping(value = "/orders/{orderCode}/status", method = RequestMethod.PUT)
    ResponseEntity updateOrderStatus(@PathVariable("orderCode") String orderCode, @RequestParam("orderStatus") OrderStatus orderStatus);

    //订单详细信息
    @RequestMapping(value = "/orders/{orderCode}", method = RequestMethod.GET)
    ResponseEntity<OrderDetailResult> orderDetail(@PathVariable("orderCode") String orderCode, @RequestParam("needProductList") boolean needProductList,
                                                  @RequestParam("needOrderFlowList") boolean needOrderFlowList);


    @Deprecated
    /**
     * 建议使用 ordersPages
     * @see ordersPages
     */
    @RequestMapping(value = "/orders/user/{userId}", method = RequestMethod.GET)
    ResponseEntity<Tuple<OrderDetailResult>> ordersByUserId(@PathVariable("userId") Long userId,
                                                            @RequestParam(value = "orderType", required = false) OrderType orderType,
                                                            @RequestParam(value = "orderStatus", required = false) OrderStatus orderStatus);

    //分页查询订单列表
    @RequestMapping(value = "/orders/pages", method = RequestMethod.POST)
    ResponseEntity<Pages<OrderDetailResult>> ordersPages(@RequestBody BaseOrderParam baseOrderParam);

    /*********************订单退款***************************************************/

    //订单未发送海鼎，退款
    @RequestMapping(value = "/orders/{orderCode}/not-send-hd/refund", method = RequestMethod.PUT)
    ResponseEntity notSendHdRefundOrder(@PathVariable("orderCode") String orderCode, @RequestBody ReturnOrderParam returnOrderParam);

    //备货退货，确认收到货，进行退款
    @RequestMapping(value = "/orders/{orderCode}/refund", method = RequestMethod.PUT)
    ResponseEntity refundOrder(@PathVariable("orderCode") String orderCode, @RequestBody ReturnOrderParam returnOrderParam);

    //海鼎备货后提交退货
    @RequestMapping(value = "/orders/{orderCode}/returns", method = RequestMethod.PUT)
    ResponseEntity returnsRefundOrder(@PathVariable("orderCode") String orderCode, @RequestBody ReturnOrderParam returnOrderParam);

    //订单发送海鼎，未备货退货
    @RequestMapping(value = "/orders/{orderCode}/send-hd/refund", method = RequestMethod.PUT)
    ResponseEntity sendHdRefundOrder(@PathVariable("orderCode") String orderCode, @RequestBody ReturnOrderParam returnOrderParam);

    //退款确认，修改订单状态
    @RequestMapping(value = "/orders/{payId}/refund/confirmation", method = RequestMethod.PUT)
    ResponseEntity refundConfirmation(@PathVariable("payId") String payId, @RequestParam("refundStatus") OrderRefundStatus refundStatus);

    //订单实际未支付退货（无需退款）
    @RequestMapping(value = "orders/{orderCode}/not-payed/refund", method = RequestMethod.PUT)
    ResponseEntity notPayedRefund(@PathVariable("orderCode") String orderCode, @RequestParam("notPayRefundWay") NotPayRefundWay notPayRefundWay);

    //查询退款费用
    @RequestMapping(value = "orders/{orderCode}/refund/fee", method = RequestMethod.GET)
    ResponseEntity<Fee> refundFee(@PathVariable("orderCode") String orderCode, @RequestParam("productIds") String productIds, @RequestParam("refundType") RefundType refundType);
}