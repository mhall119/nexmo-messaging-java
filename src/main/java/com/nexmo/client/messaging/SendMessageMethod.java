package com.nexmo.client.messaging;

import java.io.IOException;

import com.nexmo.client.AbstractMethod;
import com.nexmo.client.HttpWrapper;
import com.nexmo.client.auth.JWTAuthMethod;
import com.nexmo.client.sms.messages.TextMessage;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.BasicResponseHandler;

public class SendMessageMethod extends AbstractMethod<TextMessage, MessageSubmissionResponse> {
    private static final Class[] ALLOWED_AUTH_METHODS = new Class[]{JWTAuthMethod.class};
    private static final String PATH = "/v0.1/messages";

    SendMessageMethod(HttpWrapper httpWrapper) {
        super(httpWrapper);
    }

    @Override
    protected Class[] getAcceptableAuthMethods() {
        return ALLOWED_AUTH_METHODS;
    }

    @Override
    public RequestBuilder makeRequest(TextMessage message) {
        System.out.println("URL: "+httpWrapper.getHttpConfig().getApiBaseUri() + PATH);
        RequestBuilder request = RequestBuilder.post(httpWrapper.getHttpConfig().getApiBaseUri() + PATH);
        // TODO: Hack in message body for now
        message.addParams(request);
        System.out.println(request.getParameters());
        return request;
    }

    @Override
    public MessageSubmissionResponse parseResponse(HttpResponse response) throws IOException {
        return MessageSubmissionResponse.fromJson(new BasicResponseHandler().handleResponse(response));
    }

}