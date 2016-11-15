package com.wirelessmoves;
import java.text.DateFormat;
import java.util.Date;



public class Fingerprint {
	
		
		int cid;
		int rssi;
		int cid1[];
		int rssi1[];
		double lat , lon ;
		String dat;
		String tim;
		
		
		public Fingerprint (int cid ,int rssi ,int cid1[] ,int rssi1[] ,double lat , double lon,String dat,String tim)
		{
			this.cid= cid;
			this.rssi=rssi;
			this.cid1 = new int[6];
			this.rssi1 = new int [6];
			for(int i=0 ; i<6 ; i++)
			{
				this.cid1[i] = cid1[i] ;
				this.rssi1[i] = rssi1[i] ;
			}
			this.lat=lat ;
			this.lon = lon ;
			this.dat=String.valueOf(dat);
			this.tim=tim;
			
		}
		
		public Fingerprint()
		{}
		
		
		
		public int getCid()
		{
			return this.cid;
		}
		public int getRssi()
		{
			return this.rssi;
		}
		public void SetCid(int ID)
		{
			this.cid=ID;
		}
		
		public int[] getNcid ()
		{
			return this.cid1;
		}
		
		public int[] getNrssi ()
		{
			return this.rssi1;
		}
		public double getLat()
		{
			return this.lat;
		}
		
		public double getLon()
		{
			return this.lon;
		}
		public String getDat()
		{
			return this.dat;
		}
		
		public String getTim()
		{
			return this.tim;
		}
		public void setLat(int l)
		{
			this.lat=l;
		}
		
		public void setLon(int l)
		{
			this.lon=l;
		}
		
		public void setTim()
		{
			
			
			
		}
		
		public void setDat()
		{
			
			//this.tim= String.valueOf(DateFormat.getDateInstance().format(new Date()));
			
		}
		
}



