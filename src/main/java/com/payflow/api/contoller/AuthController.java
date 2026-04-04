package com.payflow.api.contoller;

import com.payflow.api.dto.request.LoginRequest;
import com.payflow.api.dto.request.RefreshRequest;
import com.payflow.api.dto.request.RegisterRequest;
import com.payflow.api.dto.response.AuthentciationResponse;
import com.payflow.application.command.RegisterCommand;
import com.payflow.application.command.RegisterCommandHandler;
import com.payflow.application.query.AuthQuery;
import com.payflow.application.query.AuthQueryHandler;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RegisterCommandHandler registerCommandHandler;
    private final AuthQueryHandler authentificationQueryHandler;
    @PostMapping("/register")
    public AuthentciationResponse register(
        @Valid @RequestBody RegisterRequest request
    ){
        return registerCommandHandler.handle(new RegisterCommand(
                request.getEmail(),
                request.getPassword(),
                request.getFullName()
        ));
    }

    @PostMapping("/login")
    public AuthentciationResponse login(@Valid @RequestBody LoginRequest request){
        return  authentificationQueryHandler.handle(new AuthQuery(
                request.getEmail(),
                request.getPassword()
        ));
    }
    @PostMapping("/refresh")
    public AuthentciationResponse refresh(@RequestBody RefreshRequest request) {
        return authentificationQueryHandler.handleRefresh(request.getRefreshToken());
    }
}
