package com.artfactory.project01.todayart.service;

import com.artfactory.project01.todayart.controller.PaymentController;
import com.artfactory.project01.todayart.entity.*;
import com.artfactory.project01.todayart.exception.VerificateFailException;
import com.artfactory.project01.todayart.model.ChangeOrderDetail;
import com.artfactory.project01.todayart.model.OrderForm;
import com.artfactory.project01.todayart.model.Period;
import com.artfactory.project01.todayart.repository.CartRepository;
import com.artfactory.project01.todayart.repository.OrderedDetailRepository;
import com.artfactory.project01.todayart.repository.OrderedRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class OrderService {
    @Autowired
    OrderedRepository orderedRepository;
    @Autowired
    OrderedDetailRepository orderedDetailRepository;
    @Autowired
    CartRepository cartRepository;
    @Autowired
    PaymentController paymentController;


    /*
      작성자: 국화
      1. orderForm 을 받아 ordered을 생성한다
      2. 각 ordered 와 연결되는 shipping 레코드를 생성한다
      3. orderForm 의 CartIdList 에 따라 orderedDetail 를 생성한다
      4. 각 orderedDetail 과 연결되는 payment 레코드를 생성한다
      5. 주문서와 DB 사이의 값을 검증
      6. paymentMethod에 따라 paymentController 를 호출한다 (결제 api 호출은 paymentService에서 실행)
      7. paymentController의 반환값(결제성공, 결제실패)에 따라 결과를 return 한다
        (결제성공시 Ordered return, 실패시 error return)
      @param Member
      @param OrderedForm
      @return Ordered
    */
    @Transactional(rollbackFor = VerificateFailException.class)
    public Ordered createOrder(Member member, OrderForm orderForm) throws VerificateFailException{

        int totalShippingFee=0;
        int totalPrice=0;

        Ordered ordered = new Ordered();
        ordered.setMemberId(member.getMemberId());
        ordered.setTotalPrice(orderForm.getTotalPrice());
        ordered.setShippingFee(orderForm.getShippingFee());
        ordered = orderedRepository.save(ordered);
        ArrayList<Integer> cartIdList = orderForm.getCartIdList();

        for(Integer cartId : cartIdList){
            Payment payment = orderForm.getPayment();
            Cart cart = cartRepository.findById(cartId).get();
            Product product = cart.getProduct();
            OrderedDetail orderedDetail = setOrderDetail(ordered, product, cart);
            orderedDetailRepository.save(orderedDetail);
            totalShippingFee+=orderedDetail.getShippingFee();
            totalPrice+=orderedDetail.getTotalPrice();
            cart.setIsDeleted(1);
            cartRepository.save(cart);
            paymentController.createPayment(payment, ordered.getOrderId(),orderedDetail.getOrderDetailId(), orderedDetail.getTotalPrice());
        }

        try{
            if(ordered.getShippingFee()==totalShippingFee
                    &&ordered.getTotalPrice()==totalPrice){
            } else{
                throw new VerificateFailException("값 검증 실패");
            }
            return ordered;
        }catch(VerificateFailException e) {
            e.printStackTrace();
            throw new VerificateFailException();
        }

    }


    /*
      작성자: 국화
      OrderedDetail setting
      @param Ordered
      @param Product
      @param Cart
      @return OrderedDetail
    */
    public OrderedDetail setOrderDetail(Ordered ordered, Product product, Cart cart){
        OrderedDetail orderedDetail = new OrderedDetail();
        orderedDetail.setOrderId(ordered.getOrderId());
        orderedDetail.setProductId(product.getProductId());
        orderedDetail.setCartId(cart.getCartId());
        orderedDetail.setProductName(product.getProductName());
        orderedDetail.setProductPrice(cart.getProductPrice());
        orderedDetail.setProductSize(cart.getProductSize());
        orderedDetail.setShippingFee(cart.getShippingFee());
        orderedDetail.setQuantity(cart.getQuantity());
        orderedDetail.setTotalProductPrice(cart.getProductPrice()*cart.getQuantity());
        orderedDetail.setTotalPrice(orderedDetail.getTotalProductPrice()+cart.getShippingFee());
        orderedDetailRepository.save(orderedDetail);

        return orderedDetail;
    }


    /*
      작성자: 국화
      모든 주문을 조회한다
      @param null
      @return List<Ordered>
    */
    @Transactional
    public List<Ordered> retrieveOrders(){
        List<Ordered> orders = orderedRepository.findAll();
        return orders;
    }

    /*
      작성자: 국화
      멤버 Id 로 모든 주문을 조회한다
      @param Member
      @return ArrayList<Ordered>
    */
    @Transactional
    public List<Ordered> retrieveOrdersByMemberId(int id){
        List<Ordered> orders = orderedRepository.findByMemberId(id);
        return orders;
    }



    /*
      작성자: 국화
      기간에 따라 주문을 조회한다
      @param Period
      @return ArrayList<Ordered>
    */
    @Transactional
    public ArrayList<Ordered> retrieveOrdersByPeriod(Period period){
        Date startDate = period.getStartDate();
        Date endDate = period.getEndDate();
        ArrayList<Ordered> orderedList = orderedRepository.findByMemberIdWithTerm(startDate, endDate);
        return orderedList;
    }

    /*
      작성자: 국화
      멤버 ID 와 기간으로 주문을 조회한다
      @param int
      @param Period
      @return ArrayList<Ordered>
    */
    @Transactional
    public ArrayList<Ordered> retrieveOrdersByMemberIdAndPeriod(int id, Period period){
        Date startDate = period.getStartDate();
        Date endDate = period.getEndDate();
        ArrayList<Ordered> orderedList = orderedRepository.findByMemberIdWithTerm(id, startDate, endDate);
        return orderedList;
    }


    /*
      작성자: 국화
      주문자가 마이페이지에서 특정 주문을 클릭하면 상세정보를 조회할 수 있다
      @param int
      @param Member
      @return List<OrderedDetail>
    */
    @Transactional
    public List<OrderedDetail> retrieveOrderedDetails(int orderId, Member member){
        int memberId = member.getMemberId();
        Ordered order = orderedRepository.findByOrderIdAndMemberId(orderId,memberId);
        if(order!=null){
            List<OrderedDetail> orderedDetailList = orderedDetailRepository.findAllByOrderId(orderId);
            return orderedDetailList;
        }else{
            return null;
        }
    }

    /*
      작성자: 국화
      관리자가 특정 주문에 대한 상세정보를 조회할 수 있다
      @param int
      @return List<OrderedDetail>
    */
    @Transactional
    public List<OrderedDetail> retrieveOrderedDetailsForAdmin(int orderId){
        List<OrderedDetail> orderedDetailList = orderedDetailRepository.findAllByOrderId(orderId);
        return orderedDetailList;

    }


    /*
      작성자: 국화
      1. 구매자가 주문상태를 조건으로 자신의 주문을 조회한다
      2. 관리자가 주문상태를 조건으로 모든 주문을 조회한다
      @param String, int, String
      @return List<OrderedDetail>
    */
    @Transactional
    public List<Ordered> retrieveOrdersByStatus(String role, int memberId, String status){
        List<Ordered> orderedList;

        if(role.equals("ROLE_ADMIN")){
            orderedList = orderedRepository.findAllByOrderDetails_Status(status);
        }
        else {
            orderedList = orderedRepository.findAllByOrderDetails_StatusAndMemberId(status, memberId);
        }
        return orderedList;
    }

    /*
      작성자: 국화
      각 주문상세의 상태를 변경한다
      @param Member
      @param ChangeOrderDetail
      @return OrderedDetail
    */
    @Transactional
    public OrderedDetail updateStatus(Member member, ChangeOrderDetail item){
        String code = item.getChangeCode();
        OrderedDetail orderedDetail = orderedDetailRepository.findByOrderDetailId(item.getOrderDetailId());

        if(code.equals("CUSTOMER")){
            int memberId = member.getMemberId();
            int orderId = orderedDetail.getOrderId();
            Ordered ordered = orderedRepository.findByOrderId(orderId);
            if(memberId==ordered.getMemberId()){
                if(orderedDetail.getStatus().equals("배송준비")){
                    orderedDetail.setStatus("주문취소");
                    return orderedDetailRepository.save(orderedDetail);
                }
            }
            return null;
        }
        else if(code.equals("SELLER")){
            if(item.getStatus().equals("배송중")){
                orderedDetail.setStatus(item.getStatus());
                orderedDetail.setTrackingNumber(item.getTrackingNumber());
                return orderedDetailRepository.save(orderedDetail);
            }else if(item.getStatus().equals("주문취소")){
                orderedDetail.setStatus(item.getStatus());
                return orderedDetailRepository.save(orderedDetail);
            }

        }
        else if(code.equals("ADMIN")){
            orderedDetail.setStatus(item.getStatus());
            return orderedDetailRepository.save(orderedDetail);

        }
        return null;
    }

    /*
      작성자: 국화
      특정 주문을 감춘다(삭제한다)
      @param int
      @return null
    */
    @Transactional
    public void deleteOrdered(int orderedId){
        Ordered ordered = orderedRepository.findByOrderId(orderedId);
        ordered.setIsHidden(1);
        orderedRepository.save(ordered);
    }

}