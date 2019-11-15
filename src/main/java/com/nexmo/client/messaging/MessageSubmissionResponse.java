package com.nexmo.client.messaging;

public class MessageSubmissionResponse {
    public String data;

    public static MessageSubmissionResponse fromJson(String json) {
        MessageSubmissionResponse response = new MessageSubmissionResponse();
        response.data = json;
        return response;
    }

}