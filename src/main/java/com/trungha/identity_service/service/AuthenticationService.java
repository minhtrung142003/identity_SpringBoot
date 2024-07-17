package com.trungha.identity_service.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.trungha.identity_service.dto.request.AuthenticationRequest;
import com.trungha.identity_service.dto.request.IntrospectRequest;
import com.trungha.identity_service.dto.request.LogoutRequest;
import com.trungha.identity_service.dto.request.RefreshRequest;
import com.trungha.identity_service.dto.response.AuthenticationResponse;
import com.trungha.identity_service.dto.response.IntrospectResponse;
import com.trungha.identity_service.entity.InvalidatedToken;
import com.trungha.identity_service.entity.User;
import com.trungha.identity_service.exception.AppException;
import com.trungha.identity_service.exception.ErrorCode;
import com.trungha.identity_service.repository.InvalidatedTokenRepository;
import com.trungha.identity_service.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);
    UserRepository userRepository;
    InvalidatedTokenRepository invalidatedTokenRepository;
    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;

    // hàm verify check token
    public IntrospectResponse introspectResponse(IntrospectRequest request)
            throws JOSEException, ParseException {
        var token = request.getToken();
        boolean isValid = true;
        try {
            verifyToken(token, false); // false vi ko refresh
        }catch (AppException e) {
            isValid = false;
        }
        return IntrospectResponse.builder()
                .valid(isValid)
                .build();
    }

    // login
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        boolean authentication = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if(!authentication) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        var token = generateToken(user);
        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .build();
    }

    // logout
    public void logout(LogoutRequest request) throws ParseException, JOSEException {
       try{
           var signToken = verifyToken(request.getToken(), true); // ky token, true vì khi lay token ma lỡ có refresh thi ko dc
           String jti = signToken.getJWTClaimsSet().getJWTID();
           Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

           InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                   .id(jti)
                   .expiryTime(expiryTime)
                   .build();
           invalidatedTokenRepository.save(invalidatedToken);
       } catch (AppException e) {
           log.info("Token already expired");
       }
    }

    // refresh token
    public  AuthenticationResponse refreshToken(RefreshRequest request)
            throws ParseException, JOSEException {
        // ký token
        var signedJWT = verifyToken(request.getToken(), true); // dung true vi refresh
        // id token
        String jti = signedJWT.getJWTClaimsSet().getJWTID();
        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .id(jti)
                .expiryTime(expiryTime)
                .build();
        invalidatedTokenRepository.save(invalidatedToken);

        var username = signedJWT.getJWTClaimsSet().getSubject();
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));
        var token = generateToken(user);
        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .build();
    }

    private SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        JWSVerifier jwsVerifier = new MACVerifier(SIGNER_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);
        // neu isRefresh true tức là token là 1 refresh token, ngược lại là get token
        Date expiryTime = (isRefresh)
                // signedJWT.getJWTClaimsSet().getIssueTime(): Lấy thời gian phát hành của token
                ? new Date(signedJWT.getJWTClaimsSet().getIssueTime()
                // toInstant(): change Date sang Instant
                // plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS): Thêm REFRESHABLE_DURATION giây vào Instant
                // .toEpochMilli(): change Instant thành milliseconds từ epoch
                        .toInstant().plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS).toEpochMilli())
                // Lấy time hết hạn gốc của token
                : signedJWT.getJWTClaimsSet().getExpirationTime();
        var verified = signedJWT.verify(jwsVerifier);
        if(!(verified && expiryTime.after(new Date()))) // neu chu ky ko dung va time het han
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        if(invalidatedTokenRepository
                .existsById(signedJWT.getJWTClaimsSet().getJWTID()))
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        return  signedJWT;

    }

    private String generateToken(User user ) {
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512); // header
        // dung claim de build payload
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("trungha.com")
                .issueTime(new Date()) // time create
                .expirationTime(new Date( // time end
                        Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli()
                ))
                .jwtID(UUID.randomUUID().toString()) // them id token vao de thuc hien logout
                .claim("scope", buildScope(user))
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(jwsHeader, payload); // thong tin token
        // kí cái token này, tuc la khoá để ký và khoá giải mã phải trùng nhau
        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes())); // no phai can chuoi 32 byte
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("cannot create token", e);
            throw new RuntimeException(e);
        }
    }

    // them roles vao token
    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");
        if(!CollectionUtils.isEmpty(user.getRoles()))
            user.getRoles().forEach(role -> {
                stringJoiner.add("ROLE_" + role.getName()); // add name cua role vao token
                if(!CollectionUtils.isEmpty(role.getPermissions())) // neu permission ko rỗng
                 role.getPermissions()
                         .forEach(permission -> stringJoiner.add(permission.getName())); // add permission vao role trong scope
            });
        return stringJoiner.toString();
    }

}
