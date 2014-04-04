package com.prosodie.recovery.ws;

public class WSResponse
{

	private int code;
	private String payload;
	
	
	public WSResponse(int code, String payload)
	{
		super();
		this.code = code;
		this.payload = payload;
	}
	
	
	public int getCode()
	{
		return code;
	}
	public String getPayload()
	{
		return payload;
	}
	public void setCode(int code)
	{
		this.code = code;
	}
	public void setPayload(String payload)
	{
		this.payload = payload;
	}
	
	
	
	
	
	
	
	
	
}
