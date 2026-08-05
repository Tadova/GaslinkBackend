//package com.gaslink.api.modules.order.dto;
//import com.gaslink.api.shared.enums.OrderStatus;
//import jakarta.validation.constraints.*;
//import lombok.Data;
//
//@Data
//public class UpdateOrderStatusRequest {
//    @NotNull private OrderStatus status;
//    private String note;
//
//    public @NotNull OrderStatus getStatus() {
//        return status;
//    }
//
//    public void setStatus(@NotNull OrderStatus status) {
//        this.status = status;
//    }
//
//    public String getNote() {
//        return note;
//    }
//
//    public void setNote(String note) {
//        this.note = note;
//    }
//}