package com.example.internetbanking.dto;

public class FundTransferResponse {
    private String status;
    private String message;
    private String utrNumber;
    public FundTransferResponse() {
		super();
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public String getUtrNumber() { return utrNumber; }
    public void setUtrNumber(String utrNumber) { this.utrNumber = utrNumber; }
	public FundTransferResponse(String status, String message,String utrNumber) {
        this.status = status;
        this.message = message;
        this.utrNumber = utrNumber;
    }

}
