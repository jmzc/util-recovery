package com.prosodie.recovery.persistence.domain;

public class Record
{

	
	private Integer id;
	private String payload;
	private int retry;
	
	public Integer getID()
	{
		return id;
	}
	public String getPayload()
	{
		return payload;
	}
	public void setID(Integer id)
	{
		this.id = id;
	}
	public void setPayload(String payload)
	{
		this.payload = payload;
	}
	
	public int getRetry()
	{
		return retry;
	}
	public void setRetry(int retry)
	{
		this.retry = retry;
	}
	
	
	
	

}
