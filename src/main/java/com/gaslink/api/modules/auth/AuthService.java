package com.gaslink.api.modules.auth;

import com.gaslink.api.config.JwtConfig;
import com.gaslink.api.exception.BusinessException;
import com.gaslink.api.modules.auth.dto.*;
import com.gaslink.api.modules.auth.entity.PasswordResetToken;
import com.gaslink.api.modules.auth.repository.PasswordResetTokenRepository;
import com.gaslink.api.modules.email.EmailService;
import com.gaslink.api.modules.otp.OtpService;
import com.gaslink.api.modules.user.*;
import com.gaslink.api.modules.user.dto.UserProfileDto;
import com.gaslink.api.modules.vendor.Vendor;
import com.gaslink.api.modules.vendor.VendorRepository;
import com.gaslink.api.modules.vendor.VendorService;
import com.gaslink.api.shared.enums.UserRole;
import com.gaslink.api.shared.enums.VendorAccountStatus;
import com.gaslink.api.shared.enums.VerificationStatus;
import com.gaslink.api.shared.exception.*;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final VendorRepository vendorRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, String> redisTemplate;
    private final OtpService otpService;
    private final EmailService emailService;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final VendorService vendorService;

    @Value("${app.otp.expiry-minutes:5}")
    private int otpExpiryMinutes;

    @Value("${app.reset-token.expiry-minutes:30}")
    private int resetTokenExpiryMinutes;

    public AuthService(UserRepository userRepository,
                       VendorRepository vendorRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       JwtTokenProvider jwtTokenProvider,
                       PasswordEncoder passwordEncoder,
                       RedisTemplate<String, String> redisTemplate,
                       OtpService otpService,
                       EmailService emailService,
                       PasswordResetTokenRepository resetTokenRepository,
                       VendorService vendorService) {
        this.userRepository = userRepository;
        this.vendorRepository = vendorRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.redisTemplate = redisTemplate;
        this.otpService = otpService;
        this.emailService = emailService;
        this.resetTokenRepository = resetTokenRepository;
        this.vendorService = vendorService;
    }

    /**
     * Register a new user (Customer or Vendor)
     */
    @Transactional
    public void register(RegisterRequest request) throws BusinessException {
        // Validate role
        UserRole role;
        try {
            role = UserRole.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid role. Must be 'CUSTOMER' or 'VENDOR'");
        }

        // ===== CHECK DUPLICATES =====
        // 1. Check email
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BusinessException("Email already registered. Please use a different email.");
        }

        // 2. Check phone
        if (userRepository.findByPhone(request.getPhone()).isPresent()) {
            throw new BusinessException("Phone number already registered. Please use a different phone number.");
        }

        // 3. If registering as VENDOR, check additional vendor-specific duplicates
        if (role == UserRole.VENDOR) {
            validateVendorRegistration(request);
        }

        // Create user
        User user = User.builder()
                .id(UUID.randomUUID())
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .isActive(false)
                .pushToken(request.getPushToken())
                .build();

        userRepository.save(user);

        // If user is registering as VENDOR, create vendor profile
        if (role == UserRole.VENDOR) {
            createVendorProfile(user, request);
        }

        // Generate and send OTP
        String otp = otpService.generateOtp(user.getEmail());
        otpService.sendOtpEmail(user.getEmail(), otp);

        log.info("✅ User registered as {}: {}", role.name(), user.getEmail());
    }

    /**
     * Validate vendor-specific fields and prevent duplicates
     */
    private void validateVendorRegistration(RegisterRequest request) throws BusinessException {
        // Validate required fields
        if (request.getBusinessName() == null || request.getBusinessName().isEmpty()) {
            throw new BusinessException("Business name is required for vendor registration");
        }
        if (request.getBusinessAddress() == null || request.getBusinessAddress().isEmpty()) {
            throw new BusinessException("Business address is required for vendor registration");
        }
        if (request.getNin() == null || request.getNin().isEmpty()) {
            throw new BusinessException("NIN is required for vendor registration");
        }
        if (request.getLat() == null || request.getLng() == null) {
            throw new BusinessException("Business location (latitude/longitude) is required for vendor registration");
        }
        if (request.getServiceRadiusKm() == null) {
            throw new BusinessException("Service radius is required for vendor registration");
        }

        // Validate NIN format (11 digits)
        if (!request.getNin().matches("^\\d{11}$")) {
            throw new BusinessException("NIN must be exactly 11 digits");
        }

        // ===== DUPLICATE CHECKS FOR VENDORS =====

        // 1. Check NIN (must be unique)
        if (vendorRepository.existsByNin(request.getNin())) {
            throw new BusinessException("NIN already registered with another vendor. Please use a different NIN.");
        }

        // 2. Check business address (must be unique)
        if (vendorRepository.existsByBusinessAddress(request.getBusinessAddress())) {
            throw new BusinessException("Business address already registered. Please use a different address.");
        }

        // 3. Check location (lat/lng combination must be unique)
        if (vendorRepository.existsByLocation(request.getLat(), request.getLng())) {
            throw new BusinessException("A vendor is already registered at this location. Please verify your location.");
        }

        log.info("✅ Vendor validation passed for: {}", request.getEmail());
    }

    /**
     * Create a vendor profile for the user
     */
    private void createVendorProfile(User user, RegisterRequest request) {
        Vendor vendor = Vendor.builder()
                .id(user.getId())
                .businessName(request.getBusinessName())
                .businessAddress(request.getBusinessAddress())
                .nin(request.getNin())
                .lat(request.getLat())
                .lng(request.getLng())
                .serviceRadiusKm(request.getServiceRadiusKm())
                .verificationStatus(VerificationStatus.PENDING)
                .accountStatus(VendorAccountStatus.ENABLED)
                .subscriptionStatus("INACTIVE")
                .isOpen(false)
                .build();

        vendorRepository.save(vendor);
        log.info("🛒 Vendor profile created for user: {} (NIN: {}, Status: PENDING)",
                user.getEmail(), request.getNin());
    }

    /**
     * Send OTP to phone
     */
    public void sendOtp(String phone) {
        String otp = String.format("%06d", new Random().nextInt(999999));
        redisTemplate.opsForValue().set("otp:" + phone, otp, 5, TimeUnit.MINUTES);
        System.out.println("OTP for " + phone + " => " + otp);
    }

    /**
     * Verify OTP for user registration
     */
    @Transactional
    public void verifyOtp(OtpVerifyRequest req) throws BusinessException {
        // Validate OTP format
        if (!otpService.isValidOtpFormat(req.getOtpCode())) {
            throw new BusinessException("Invalid OTP format. OTP must be 6 digits.");
        }

        // Check if OTP exists and is valid
        String storedOtp = redisTemplate.opsForValue().get("otp:" + req.getPhone());
        if (storedOtp == null) {
            throw new BusinessException("OTP has expired. Please request a new one.");
        }

        if (!storedOtp.equals(req.getOtpCode())) {
            throw new BusinessException("Invalid OTP. Please try again.");
        }

        // Find user by phone
        User user = userRepository.findByPhone(req.getPhone())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Activate user
        user.setActive(true);
        userRepository.save(user);

        // Delete OTP from Redis
        redisTemplate.delete("otp:" + req.getPhone());

        log.info("✅ OTP verified successfully for user: {}", user.getEmail());
    }

    /**
     * Login user
     */
    @Transactional
    public AuthResponse login(@Valid LoginRequest req) {
        User user = userRepository.findByPhoneOrEmail(req.getPhoneOrEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        if (!user.isActive()) {
            throw new ForbiddenException("Account not verified. Please verify your OTP.");
        }

        return buildAuthResponse(user);
    }

    /**
     * Refresh token
     */
    @Transactional
    public AuthResponse refreshToken(@Valid RefreshTokenRequest req) {
        RefreshToken rt = refreshTokenRepository.findByToken(req.getRefreshToken())
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (rt.isRevoked() || rt.getExpiresAt().isBefore(Instant.now())) {
            throw new UnauthorizedException("Refresh token expired");
        }

        rt.setRevoked(true);
        refreshTokenRepository.save(rt);

        User user = userRepository.findById(rt.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return buildAuthResponse(user);
    }

    /**
     * Logout user
     */
    @Transactional
    public void logout(UUID userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    /**
     * Build auth response with tokens
     */
    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getRole().name());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        RefreshToken rt = new RefreshToken();
        rt.setUserId(user.getId());
        rt.setToken(refreshToken);
        rt.setExpiresAt(Instant.now().plusMillis(JwtConfig.REFRESH_TOKEN_EXPIRY_MS));
        rt.setRevoked(false);
        refreshTokenRepository.save(rt);

        UserProfileDto profile = new UserProfileDto(
                user.getId(),
                user.getFullName(),
                user.getPhone(),
                user.getEmail(),
                user.getRole(),
                user.getAvatarUrl()
        );

        return new AuthResponse(
                accessToken,
                refreshToken,
                JwtConfig.ACCESS_TOKEN_EXPIRY_MS / 1000,
                profile
        );
    }

    // ========== PASSWORD RESET METHODS ==========

    /**
     * Forgot password - Send reset link
     */
    @Transactional
    public PasswordResetDTOs.PasswordResetResponse forgotPassword(PasswordResetDTOs.ForgotPasswordRequest request) throws BusinessException {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("User not found with email: " + request.getEmail()));

        if (!user.isActive()) {
            throw new BusinessException("Account is inactive. Please contact support.");
        }

        resetTokenRepository.deleteByEmail(user.getEmail());

        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(resetTokenExpiryMinutes);

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .email(user.getEmail())
                .expiryDate(expiryDate)
                .used(false)
                .createdAt(LocalDateTime.now())
                .build();

        resetTokenRepository.save(resetToken);
        emailService.sendPasswordResetEmail(user.getEmail(), token);

        log.info("Password reset token generated for user: {}", user.getEmail());

        return PasswordResetDTOs.PasswordResetResponse.builder()
                .message("Password reset link sent to your email")
                .email(user.getEmail())
                .expiryMinutes(resetTokenExpiryMinutes)
                .build();
    }

    /**
     * Verify reset token
     */
    @Transactional
    public void verifyResetToken(PasswordResetDTOs.VerifyTokenRequest request) throws BusinessException {
        PasswordResetToken resetToken = resetTokenRepository.findByTokenAndUsedFalse(request.getToken())
                .orElseThrow(() -> new BusinessException("Invalid or expired reset token"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Reset token has expired. Please request a new one.");
        }

        log.info("Reset token verified successfully for: {}", resetToken.getEmail());
    }

    /**
     * Reset password
     */
    @Transactional
    public void resetPassword(PasswordResetDTOs.ResetPasswordRequest request) throws BusinessException {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("Passwords do not match");
        }

        PasswordResetToken resetToken = resetTokenRepository.findByTokenAndUsedFalse(request.getToken())
                .orElseThrow(() -> new BusinessException("Invalid or expired reset token"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Reset token has expired. Please request a new one.");
        }

        User user = userRepository.findByEmail(resetToken.getEmail())
                .orElseThrow(() -> new BusinessException("User not found"));

        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new BusinessException("New password must be different from current password");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetTokenRepository.markTokenAsUsed(request.getToken(), LocalDateTime.now());
        emailService.sendPasswordChangedNotification(user.getEmail());

        log.info("Password reset successfully for user: {}", user.getEmail());
    }

    /**
     * Setup password for Super Admin
     */
    @Transactional
    public void setupPassword(PasswordResetDTOs.SetupPasswordRequest request) throws BusinessException {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("Passwords do not match");
        }

        PasswordResetToken resetToken = resetTokenRepository.findByTokenAndUsedFalse(request.getToken())
                .orElseThrow(() -> new BusinessException("Invalid or expired setup token"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Setup token has expired. Please contact support.");
        }

        User user = userRepository.findByEmail(resetToken.getEmail())
                .orElseThrow(() -> new BusinessException("User not found"));

        if (user.getRole() != UserRole.SUPER_ADMIN) {
            throw new BusinessException("This token is only for Super Admin setup");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setActive(true);
        userRepository.save(user);

        resetTokenRepository.markTokenAsUsed(request.getToken(), LocalDateTime.now());
        emailService.sendPasswordChangedNotification(user.getEmail());

        log.info("✅ Password set up successfully for Super Admin: {}", user.getEmail());
    }
}