package com.trungha.identity_service.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.trungha.identity_service.dto.request.AuthenticationRequest;
import com.trungha.identity_service.dto.request.IntrospectRequest;
import com.trungha.identity_service.dto.response.AuthenticationResponse;
import com.trungha.identity_service.dto.response.IntrospectResponse;
import com.trungha.identity_service.entity.User;
import com.trungha.identity_service.exception.AppException;
import com.trungha.identity_service.exception.ErrorCode;
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
import java.util.Collection;
import java.util.Date;
import java.util.StringJoiner;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);
    UserRepository userRepository;
    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    public IntrospectResponse introspectResponse(IntrospectRequest request)
            throws JOSEException, ParseException {
        var token = request.getToken();
        JWSVerifier jwsVerifier = new MACVerifier(SIGNER_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        var verified = signedJWT.verify(jwsVerifier);
        return IntrospectResponse.builder()
                .valid(verified && expiryTime.after(new Date()))
                .build();
    }


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

    private String generateToken(User user ) {
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512); // header
        // dung claim de build payload
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("trungha.com")
                .issueTime(new Date()) // time create
                .expirationTime(new Date( // time end
                        Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli()
                ))
                .claim("Scope", buildScope(user))
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
       // if(!CollectionUtils.isEmpty(user.getRoles()))
            //user.getRoles().forEach(stringJoiner::add);
        return stringJoiner.toString();
    }

}
