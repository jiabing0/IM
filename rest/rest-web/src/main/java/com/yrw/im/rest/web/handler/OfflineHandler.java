package com.yrw.im.rest.web.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yrw.im.common.domain.ResultWrapper;
import com.yrw.im.common.exception.ImException;
import com.yrw.im.rest.web.filter.TokenManager;
import com.yrw.im.rest.web.service.OfflineService;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.BodyInserters.fromObject;

/**
 * Date: 2019-05-27
 * Time: 09:52
 *
 * @author yrw
 */
@Component
public class OfflineHandler {

    private OfflineService offlineService;
    private TokenManager tokenManager;

    public OfflineHandler(OfflineService offlineService, TokenManager tokenManager) {
        this.offlineService = offlineService;
        this.tokenManager = tokenManager;
    }

    public Mono<ServerResponse> pollOfflineMsg(ServerRequest request) {

        String token = request.headers().header("token").get(0);
        Mono<Long> id = tokenManager.validateToken(token);

        return id.map(i -> {
            try {
                return offlineService.pollOfflineMsg(i);
            } catch (JsonProcessingException e) {
                throw new ImException(e);
            }
        }).map(ResultWrapper::success).flatMap(res ->
            ServerResponse.ok().contentType(APPLICATION_JSON).body(fromObject(res)));
    }
}
