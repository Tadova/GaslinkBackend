//package com.gaslink.api.modules.admin;
//import com.gaslink.api.modules.admin.dto.*;
//import com.gaslink.api.shared.response.ApiResponse;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/v1/admin")
//@PreAuthorize("hasRole('ADMIN')")
//public class AdminController {
//    private final AdminService adminService;
//
//    public AdminController(AdminService adminService) {
//        this.adminService = adminService;
//    }
//
//    @GetMapping("/stats")
//    public ResponseEntity<ApiResponse<DashboardStatsDto>> stats() {
//        return ResponseEntity.ok(ApiResponse.success(adminService.getDashboardStats()));
//    }
//
//    @PostMapping("/user-action")
//    public ResponseEntity<ApiResponse<Void>> userAction(@Valid @RequestBody AdminUserActionRequest req) {
//        adminService.performUserAction(req);
//        return ResponseEntity.ok(ApiResponse.success("Action performed", null));
//    }
//}