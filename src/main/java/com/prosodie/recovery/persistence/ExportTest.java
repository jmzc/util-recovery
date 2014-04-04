package com.prosodie.recovery.persistence;

import java.util.List;

import com.prosodie.recovery.persistence.domain.Record;
//import java.util.Random;



public class ExportTest
{


	public static void main(String[] args)
	{
		//Random random = new Random();
		 
		try
		{
		
		UtilDB export = new UtilDB("C:\\tomcat624\\logs\\WSAUTOCALLER\\export\\vacolba.db");

		
		export.create();
		
		//export.save("VACOLBA", "{code:" + random.nextInt(2000) + "}");
		
		List<Record> l = export.query("VACOLBA",7);
		
		for (Record r: l)
		{
			System.out.println("ID-->" + r.getID() + "\npayload -->" + r.getPayload() + "\nretry -->" + r.getRetry());
		}
		
		
		
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		

	}

}
