package com.tomergabel.examples.prometheus;

public class DemoResponse {
    private String status;

    public DemoResponse(String status) {
        this.status = status;
    }
    public DemoResponse() {
        this("ok");
    }

    public String getStatus() {
        return status;
    }

    public static DemoResponse SUCCESS = new DemoResponse();
}

