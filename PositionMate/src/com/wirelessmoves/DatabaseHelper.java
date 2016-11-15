package com.wirelessmoves;



import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;




public class DatabaseHelper extends SQLiteOpenHelper {

	static final int numIntervals = 15;
	static final String fingerTable="fingerprint";
	static final String viewFinger="viewFinger";
	static final String i = "_id";
	static final String cid = "cid";
	static final String rssi ="rssi";
	static final String cid1="cid1";
	static final String cid2="cid2";
	static final String cid3="cid3";
	static final String cid4="cid4";
	static final String cid5="cid5";
	static final String cid6="cid6";
	static final String rssi1 ="rssi1";
	static final String rssi2="rssi2";
	static final String rssi3 ="rssi3";
	static final String rssi4 ="rssi4";
	static final String rssi5 ="rssi5";
	static final String rssi6 ="rssi6";
	static final String lat="Latitude";
	static final String lon ="Longitude";
	static final String dat ="Dt";
	static final String tim ="Ti";
	static final int NUM_SQUARES = 8;
	static final int RSSI_MAX = 0;
	static final int RSSI_MIN = -110;
	HashMap<Integer, gridSquareInfo> gridSquares;
	
	static final double [] latMax  = 
	{
		28.66421, 	28.66437, 	28.66459, 	28.66476,28.66504, 28.66498,28.66465,28.66449
	};
	
	static final double [] lonMax  = 
	{
		77.23285,77.23273, 77.23261,77.23243,77.23289,77.23298,77.23308,77.23316 

	};

	static final double [] latMin  = 
	{
		28.66416, 28.66432, 28.66455, 28.66474, 28.66501, 28.66490,28.66462, 28.66446
	};
	
	
	static final double [] lonMin  = 
	{
		77.23281, 77.23269, 77.23255,77.23240,77.23284,77.23293,77.23305,77.23311 

	};
	
	public DatabaseHelper(Context context, String dbName) {
		super(context, dbName, null,1);
		
		// TODO Auto-generated constructor stub
	}

	
	public class cellIdInfo{
		
		public cellIdInfo(Integer integer, float f, float d) {
			// TODO Auto-generated constructor stub
			cellId  = integer;
			frequency = f;
			signalStrength = d;
		
		}
		public Integer cellId;
		public float frequency;
		public float signalStrength;
	}

	
	public class gridSquareInfo {
		
		int gridID;
		double frequencyMatchScore;
		double signalStrengthMatchScore;
		double latitude;
		double longitude;
		ArrayList<Fingerprint> gridSquareRecordList;
		HashMap<Integer,cellIdInfo>  gridSquareCellIDInfo;
		public double weightedSignalStrengthMatchScore;
		
	}
	
	public class MyIntComparable implements Comparator<cellIdInfo>{

		@Override
		public int compare(cellIdInfo lhs, cellIdInfo rhs) {
			// TODO Auto-generated method stub
			return (lhs.frequency>rhs.frequency ? -1 : 1);
			
		}
		}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL("DROP TABLE IF EXISTS "+fingerTable);
		
		db.execSQL("CREATE TABLE "+fingerTable+" ("+
				i+ " INTEGER PRIMARY KEY AUTOINCREMENT , "+
				cid+ " INTEGER , "+
			rssi+" INTEGER ,"	+
			cid1+" INTEGER ,"	+
			rssi1+" INTEGER ," +
			cid2+" INTEGER ,"+
			rssi2+" INTEGER ,"+
			cid3+" INTEGER ,"+
			rssi3+" INTEGER ,"+
			cid4+" INTEGER ,"+
			rssi4+" INTEGER ,"+
			cid5+" INTEGER ,"+
			rssi5+" INTEGER ,"+
			cid6+" INTEGER ,"+
			rssi6+" INTEGER ,"+
			lat+" REAL ,"+
			lon+" REAL ,"+
			dat+" TEXT ,"+
			tim+" TEXT "+ ");");		
		db.execSQL("CREATE VIEW "+viewFinger+
				" AS SELECT "+cid+" AS _id,"+
				" "+rssi+","+
				" "+cid1+","+
				" "+rssi1+","+
				" "+cid2+","+
				" "+rssi2+","+
				" "+cid3+","+
				" "+rssi3+","+
				" "+cid4+","+
				" "+rssi4+","+
				" "+cid5+","+
				" "+rssi5+","+
				" "+cid6+","+
				" "+rssi6+","+
				" "+lat+","+
				" "+lon+","+
				" "+dat+","+
				" "+tim+""+
				" FROM "+fingerTable
				
				);
		
				
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
		db.execSQL("DROP TABLE IF EXISTS "+fingerTable);
		onCreate(db);
	}
	
	HashMap<Integer,cellIdInfo> getFrequencies (ArrayList<Fingerprint> userFingerList)
	{
		
		HashMap<Integer, cellIdInfo> m = new HashMap<Integer, cellIdInfo>();
				
		for (int i = 0 ; i < userFingerList.size() ; i++)
		{
			
			if (userFingerList.get(i).rssi <0)
			m.put(userFingerList.get(i).cid, new cellIdInfo(userFingerList.get(i).cid, 0, 0 ));
			if (userFingerList.get(i).rssi1[0] <0)
			m.put(userFingerList.get(i).cid1[0], new cellIdInfo(userFingerList.get(i).cid1[0], 0, 0 ));
			if (userFingerList.get(i).rssi1[1] <0)
			m.put(userFingerList.get(i).cid1[1], new cellIdInfo(userFingerList.get(i).cid1[1], 0, 0 ));
			if (userFingerList.get(i).rssi1[2] <0)
			m.put(userFingerList.get(i).cid1[2], new cellIdInfo(userFingerList.get(i).cid1[2], 0, 0 ));
			if (userFingerList.get(i).rssi1[3] <0)
			m.put(userFingerList.get(i).cid1[3],new cellIdInfo(userFingerList.get(i).cid1[3], 0, 0 ));
			if (userFingerList.get(i).rssi1[4] <0)
			m.put(userFingerList.get(i).cid1[4],new cellIdInfo(userFingerList.get(i).cid1[4], 0, 0 ));
			if (userFingerList.get(i).rssi1[5] <0)
			m.put(userFingerList.get(i).cid1[5],new cellIdInfo(userFingerList.get(i).cid1[5], 0, 0 ));
		}
		
		
		for (int i = 0; i< userFingerList.size(); i++ )
		{
			if (userFingerList.get(i).rssi <0)
			{
				m.get(userFingerList.get(i).cid).frequency++;
				m.get(userFingerList.get(i).cid).signalStrength+= (RSSI_MAX - RSSI_MIN)+userFingerList.get(i).rssi;
			}
			
			if (userFingerList.get(i).rssi1[0] <0)
			{
				m.get(userFingerList.get(i).cid1[0]).frequency++;
				m.get(userFingerList.get(i).cid1[0]).signalStrength+= (RSSI_MAX - RSSI_MIN)+userFingerList.get(i).rssi1[0];
			}

			if (userFingerList.get(i).rssi1[1] <0)
			{
				m.get(userFingerList.get(i).cid1[1]).frequency++;
				m.get(userFingerList.get(i).cid1[1]).signalStrength+= (RSSI_MAX - RSSI_MIN)+userFingerList.get(i).rssi1[1];
			}
			
			if (userFingerList.get(i).rssi1[2] <0)
			{
				m.get(userFingerList.get(i).cid1[2]).frequency++;
				m.get(userFingerList.get(i).cid1[2]).signalStrength+= (RSSI_MAX - RSSI_MIN)+userFingerList.get(i).rssi1[2];	
			}
				
			if (userFingerList.get(i).rssi1[3] <0)
			{
				m.get(userFingerList.get(i).cid1[3]).frequency++;
				m.get(userFingerList.get(i).cid1[3]).signalStrength+= (RSSI_MAX - RSSI_MIN)+userFingerList.get(i).rssi1[3];
			}
				
			if (userFingerList.get(i).rssi1[4] <0)
			{
				m.get(userFingerList.get(i).cid1[4]).frequency++;
				m.get(userFingerList.get(i).cid1[4]).signalStrength+= (RSSI_MAX - RSSI_MIN)+userFingerList.get(i).rssi1[4];
			}
			
			if (userFingerList.get(i).rssi1[5] <0)
			{
				m.get(userFingerList.get(i).cid1[5]).frequency++;
				m.get(userFingerList.get(i).cid1[5]).signalStrength+= (RSSI_MAX - RSSI_MIN)+userFingerList.get(i).rssi1[5];
			}

		
		}
		Integer[] hashMapKeyArray = new Integer [m.size()];
		m.keySet().toArray(hashMapKeyArray);
		for (int i = 0; i< m.keySet().size(); i++ )
		{
			m.get(hashMapKeyArray[i]).frequency/=userFingerList.size();
			m.get(hashMapKeyArray[i]).signalStrength/=(userFingerList.size()*m.get(hashMapKeyArray[i]).frequency);
		}
		
	
		//ArrayList<cellIdInfo> cellIDs = new ArrayList<cellIdInfo>(m.values());
		

//		MyIntComparable comp  = new MyIntComparable();
//		Collections.sort(cellIDs, comp);
//		
		return m;
		
		//return null;
		
		
	}
		
	void Matchfingerprint (ArrayList<Fingerprint> userFingerList)
	{
		
		SQLiteDatabase db= this.getWritableDatabase();
//		Cursor cur= db.rawQuery("SELECT * FROM "+fingerTable ,null);
	//ArrayList<Fingerprint> userFingerList = new ArrayList<Fingerprint>();
		
	//	for (int i = 0 ; i < numIntervals; i++)
		//{
//			cur.moveToPosition(i);
//			userFingerprint[i] = new Fingerprint();
//			userFingerprint[i].cid = cur.getInt(1);
//			userFingerprint[i].rssi = cur.getInt(2);
//			userFingerprint[i].cid1 = new int[6];
//			userFingerprint[i].rssi1 = new int [6];
//			userFingerprint[i].cid1[0] = cur.getInt(3);
//			userFingerprint[i].rssi1[0] = cur.getInt(4);
//			userFingerprint[i].cid1[1] = cur.getInt(5);
//			userFingerprint[i].rssi1[1] = cur.getInt(6);
//			userFingerprint[i].cid1[2] = cur.getInt(7);
//			userFingerprint[i].rssi1[2] = cur.getInt(8);
//			userFingerprint[i].cid1[3] = cur.getInt(9);
//			userFingerprint[i].rssi1[3] = cur.getInt(10);
//			userFingerprint[i].cid1[4] = cur.getInt(11);
//			userFingerprint[i].rssi1[4] = cur.getInt(12);
//			userFingerprint[i].cid1[5] = cur.getInt(13);
//			userFingerprint[i].rssi1[5] = cur.getInt(14);
//			userFingerprint[i].lat = cur.getDouble(15);
//			userFingerprint[i].lon = cur.getDouble(16);
			
	//		userFingerList.add(userFingerList[i]);
		//}
		
		HashMap<Integer, cellIdInfo> userCellIds = getFrequencies(userFingerList);
				
		
		
		Fingerprint databaseFingerprints[] = getRecordsFromDatabase(userFingerList.get(0).cid);
		
		gridSquares = new HashMap<Integer,gridSquareInfo>();
		int numSquares = NUM_SQUARES;
		for (int i = 0 ; i< databaseFingerprints.length; i++)
			for (int j = 0 ; j< numSquares ; j++)
			{if (inRange (databaseFingerprints[i].lat, databaseFingerprints[i].lon , j ))
				{if (gridSquares.get(j) == null )
					{
						gridSquares.put(j,new gridSquareInfo());
						gridSquares.get(j).gridID = j;
						gridSquares.get(j).latitude = databaseFingerprints[i].lat;
						gridSquares.get(j).longitude = databaseFingerprints[i].lon;
					}
				if (gridSquares.get(j).gridSquareRecordList == null)
					gridSquares.get(j).gridSquareRecordList = new ArrayList<Fingerprint>();
				gridSquares.get(j).gridSquareRecordList.add(databaseFingerprints[i]);
				}	
			}
		
		//ArrayList<HashMap<Integer,cellIdInfo>> candidateSquareGridCells = new ArrayList<HashMap<Integer,cellIdInfo>>();
		Integer[] hashMapKeyArray = new Integer [gridSquares.size()];
		gridSquares.keySet().toArray(hashMapKeyArray);
		
		for (int i= 0 ; i < gridSquares.size(); i ++ )
		{
			gridSquares.get(hashMapKeyArray[i]).gridSquareCellIDInfo = getFrequencies(gridSquares.get(hashMapKeyArray[i]).gridSquareRecordList);
					
		}
		
	
		
		for (int i =0 ; i< gridSquares.size(); i ++ )
		{
			gridSquares.get(hashMapKeyArray[i]).frequencyMatchScore = frequencyMatch (gridSquares.get(hashMapKeyArray[i]).gridSquareCellIDInfo, userCellIds );
			
		}
		
		for (int i =0 ; i< gridSquares.size(); i ++ )
		{
			gridSquares.get(hashMapKeyArray[i]).signalStrengthMatchScore = signalStrengthMatch (gridSquares.get(hashMapKeyArray[i]).gridSquareCellIDInfo, userCellIds );
			
		}
		
		for (int i =0 ; i< gridSquares.size(); i ++ )
		{
			gridSquares.get(hashMapKeyArray[i]).weightedSignalStrengthMatchScore = weightedSignalStrengthMatch (gridSquares.get(hashMapKeyArray[i]).gridSquareCellIDInfo, userCellIds );
			
		}
		
		
		
	}
	
	
	
	private double signalStrengthMatch(HashMap<Integer, cellIdInfo> hashMap,
			HashMap<Integer, cellIdInfo> userCellIds) {
		// TODO Auto-generated method stub

		double score = 0;
		Integer[] hashMapKeyArray = new Integer [userCellIds.size()];
		userCellIds.keySet().toArray(hashMapKeyArray);

		for (int i = 0 ; i < userCellIds.size(); i++ )
		{
			if (hashMap.containsKey(hashMapKeyArray[i] ) )
			{
				score += Math.pow(( ((cellIdInfo)(userCellIds.get(hashMapKeyArray[i]))).signalStrength
				-((cellIdInfo)(hashMap.get(hashMapKeyArray[i]))).signalStrength),2);
			}
			else
				score += Math.pow(((cellIdInfo)(userCellIds.get(hashMapKeyArray[i]))).signalStrength, 2);
				
			
		}
		// TODO Auto-generated method stub
		return score;
		
	}

	private double weightedSignalStrengthMatch(HashMap<Integer, cellIdInfo> hashMap,
			HashMap<Integer, cellIdInfo> userCellIds) {
		// TODO Auto-generated method stub

		double score = 0;
		Integer[] hashMapKeyArray = new Integer [userCellIds.size()];
		userCellIds.keySet().toArray(hashMapKeyArray);

		for (int i = 0 ; i < userCellIds.size(); i++ )
		{
			if (hashMap.containsKey(hashMapKeyArray[i] ) )
			{
				score += Math.abs(( ((cellIdInfo)(userCellIds.get(hashMapKeyArray[i]))).signalStrength
				-((cellIdInfo)(hashMap.get(hashMapKeyArray[i]))).signalStrength)) * ((cellIdInfo)(userCellIds.get(hashMapKeyArray[i]))).signalStrength;
			}
			else
				score += Math.abs(((cellIdInfo)(userCellIds.get(hashMapKeyArray[i]))).signalStrength) * ((cellIdInfo)(userCellIds.get(hashMapKeyArray[i]))).signalStrength;
				
			
		}
		// TODO Auto-generated method stub
		return score;
		
	}
	
	
	private double frequencyMatch(HashMap<Integer, cellIdInfo> hashMap,
			HashMap<Integer, cellIdInfo> userCellIds) {
		
		double score = 0;
		Integer[] hashMapKeyArray = new Integer [userCellIds.size()];
		userCellIds.keySet().toArray(hashMapKeyArray);

		for (int i = 0 ; i < userCellIds.size(); i++ )
		{
			if (hashMap.containsKey(hashMapKeyArray[i] ) )
			{
				score += Math.abs( ((cellIdInfo)(userCellIds.get(hashMapKeyArray[i]))).frequency
				-((cellIdInfo)(hashMap.get(hashMapKeyArray[i]))).frequency);
			}
			else
				score += Math.abs(((cellIdInfo)(userCellIds.get(hashMapKeyArray[i]))).frequency);
				
			
		}
		// TODO Auto-generated method stub
		return score;
	}

	private boolean inRange(double lat2, double lon2, int j) {
		
		// TODO Auto-generated method stub
		if (lat2 <= latMax[j] && lat2 >= latMin[j] &&
		                                        lon2 <= lonMax[j] && lon2 >= lonMin[j])
			return true;
		return false;
	}

	Fingerprint[] getRecordsFromDatabase (Integer cellid)
	{
		
		SQLiteDatabase db= this.getWritableDatabase();
		db.rawQuery("DELETE FROM "+fingerTable+ " WHERE "+ lat + " < " + 0.1  ,null);
		Cursor cur= db.rawQuery("SELECT * FROM "+fingerTable+ " WHERE "+ cid + " = " + cellid  ,null);
		Fingerprint databaseRecoreds [] = new Fingerprint[cur.getCount()];
		for (int i = 0 ; i < cur.getCount(); i++)
		{
			cur.moveToPosition(i);
			databaseRecoreds[i] = new Fingerprint();
			databaseRecoreds[i].cid = cur.getInt(1);
			databaseRecoreds[i].rssi = cur.getInt(2);
			databaseRecoreds[i].cid1 = new int [6];
			databaseRecoreds[i].rssi1 = new int[6];
			
			databaseRecoreds[i].cid1[0] = cur.getInt(3);
			databaseRecoreds[i].rssi1[0] = cur.getInt(4);
			databaseRecoreds[i].cid1[1] = cur.getInt(5);
			databaseRecoreds[i].rssi1[1] = cur.getInt(6);
			databaseRecoreds[i].cid1[2] = cur.getInt(7);
			databaseRecoreds[i].rssi1[2] = cur.getInt(8);
			databaseRecoreds[i].cid1[3] = cur.getInt(9);
			databaseRecoreds[i].rssi1[3] = cur.getInt(10);
			databaseRecoreds[i].cid1[4] = cur.getInt(11);
			databaseRecoreds[i].rssi1[4] = cur.getInt(12);
			databaseRecoreds[i].cid1[5] = cur.getInt(13);
			databaseRecoreds[i].rssi1[5] = cur.getInt(14);
			databaseRecoreds[i].lat = cur.getDouble(15);
			databaseRecoreds[i].lon = cur.getDouble(16);
			
//			for (int j=0 ; j<6;j++)
//				if (databaseRecoreds[i].rssi1[j]>=0)
//					databaseRecoreds[i].rssi1[j] = RSSI_MIN;
			
		
		}
		if (cur.getCount() > 0)
			return databaseRecoreds;
		else
		return null;
		
		
	}
	
	 
	ArrayList<Fingerprint> getLastNRecordsFromDatabase ()
	{
		
		SQLiteDatabase db= this.getWritableDatabase();
		Cursor cur= db.rawQuery("SELECT * FROM "+fingerTable, null);
		ArrayList<Fingerprint> databaseRecoreds = new ArrayList<Fingerprint>();
		int i =0, co =0;
		for (co = 0; co< cur.getCount() - numIntervals ; co++);
		for ( i =0 ; i +co< cur.getCount(); i++)
		{
			cur.moveToPosition(i+co);
			databaseRecoreds.add(new Fingerprint());
			
			databaseRecoreds.get(i).cid = cur.getInt(1);
			databaseRecoreds.get(i).rssi = cur.getInt(2);
			databaseRecoreds.get(i).cid1 = new int [6];
			databaseRecoreds.get(i).rssi1 = new int[6];
			
			databaseRecoreds.get(i).cid1[0] = cur.getInt(3);
			databaseRecoreds.get(i).rssi1[0] = cur.getInt(4);
			databaseRecoreds.get(i).cid1[1] = cur.getInt(5);
			databaseRecoreds.get(i).rssi1[1] = cur.getInt(6);
			databaseRecoreds.get(i).cid1[2] = cur.getInt(7);
			databaseRecoreds.get(i).rssi1[2] = cur.getInt(8);
			databaseRecoreds.get(i).cid1[3] = cur.getInt(9);
			databaseRecoreds.get(i).rssi1[3] = cur.getInt(10);
			databaseRecoreds.get(i).cid1[4] = cur.getInt(11);
			databaseRecoreds.get(i).rssi1[4] = cur.getInt(12);
			databaseRecoreds.get(i).cid1[5] = cur.getInt(13);
			databaseRecoreds.get(i).rssi1[5] = cur.getInt(14);
			databaseRecoreds.get(i).lat = cur.getDouble(15);
			databaseRecoreds.get(i).lon = cur.getDouble(16);
			
//			for (int j=0 ; j<6;j++)
//				if (databaseRecoreds.get(i).rssi1[j]>=0)
//					databaseRecoreds.get(i).rssi1[j] = RSSI_MIN;
//			
			
		
		}
		if (i < numIntervals && cur.getCount()> 0)
		{
			cur.moveToPosition(0);
			for (; i< numIntervals; i++)
			{
				databaseRecoreds.add( new Fingerprint());
				databaseRecoreds.get(i).cid = cur.getInt(1);
				databaseRecoreds.get(i).rssi = cur.getInt(2);
				databaseRecoreds.get(i).cid1 = new int [6];
				databaseRecoreds.get(i).rssi1 = new int[6];
				
				databaseRecoreds.get(i).cid1[0] = cur.getInt(3);
				databaseRecoreds.get(i).rssi1[0] = cur.getInt(4);
				databaseRecoreds.get(i).cid1[1] = cur.getInt(5);
				databaseRecoreds.get(i).rssi1[1] = cur.getInt(6);
				databaseRecoreds.get(i).cid1[2] = cur.getInt(7);
				databaseRecoreds.get(i).rssi1[2] = cur.getInt(8);
				databaseRecoreds.get(i).cid1[3] = cur.getInt(9);
				databaseRecoreds.get(i).rssi1[3] = cur.getInt(10);
				databaseRecoreds.get(i).cid1[4] = cur.getInt(11);
				databaseRecoreds.get(i).rssi1[4] = cur.getInt(12);
				databaseRecoreds.get(i).cid1[5] = cur.getInt(13);
				databaseRecoreds.get(i).rssi1[5] = cur.getInt(14);
				databaseRecoreds.get(i).lat = cur.getDouble(15);
				databaseRecoreds.get(i).lon = cur.getDouble(16);
				
//				for (int j=0 ; j<6;j++)
//					if (databaseRecoreds.get(i).rssi1[j]>=0)
//						databaseRecoreds.get(i).rssi1[j] = RSSI_MIN;
			}
			
			}
		
		if (cur.getCount() > 0)
			return databaseRecoreds;
		else
		return null;
		
		
	}
	
	
	void AddFingerprint(Fingerprint f )
	{
		 
		 
		 SQLiteDatabase db= this.getWritableDatabase();
		 
		
		ContentValues cv=new ContentValues();
		
		int ncids[]=f.getNcid();
		int nrssi[]=f.getNrssi();
		cv.put(cid, f.getCid());
		cv.put(rssi,f.getRssi());
		cv.put(cid1, ncids[0]);
		cv.put(rssi1,nrssi[0]);
		cv.put(cid2, ncids[1]);
		cv.put(rssi2,nrssi[1]);
		cv.put(cid3, ncids[2]);
		cv.put(rssi3,nrssi[2]);
		cv.put(cid4, ncids[3]);
		cv.put(rssi4,nrssi[3]);
		cv.put(cid5, ncids[4]);
		cv.put(rssi5,nrssi[4]);
		cv.put(cid6, ncids[5]);
		cv.put(rssi6,nrssi[5]);
		cv.put(lat,f.getLat());
		cv.put(lon,f.getLon());
		cv.put(dat,f.getDat());
		cv.put(tim,f.getTim());
		
		db.insert(fingerTable,cid, cv);
		db.close();
		
		
	}
	 
	 int getSampleCount()
	 {
		SQLiteDatabase db=this.getWritableDatabase();
		Cursor cur= db.rawQuery("Select * from "+fingerTable, null);
		int x= cur.getCount();
		cur.close();
		return x;
	 }
	 
	 Cursor getAllSamples()
	 {
		 SQLiteDatabase db=this.getWritableDatabase();
		 
		 //String[] selec = {i, "AS", "_id"};
		 Cursor cur= db.rawQuery("SELECT * FROM "+viewFinger,null);
		 //SQLiteCursor cur= (SQLiteCursor) db.query(viewFinger,null,null,null,null,null, null);
		 return cur;
		 
	 }
	 
	
	  
	public int updateSample(Fingerprint f)
	 {
		 SQLiteDatabase db=this.getWritableDatabase();
		 ContentValues cv=new ContentValues();
		 int ncids[]=f.getNcid();
		 int nrssi[]=f.getNrssi();
		 cv.put(cid, f.getCid());
		 cv.put(rssi,f.getRssi());
		 cv.put(cid1, ncids[0]);
		 cv.put(rssi1,nrssi[0]);
		 cv.put(cid2, ncids[1]);
		 cv.put(rssi2,nrssi[1]);
		 cv.put(cid3, ncids[2]);
		 cv.put(rssi3,nrssi[2]);
		 cv.put(cid4, ncids[3]);
		 cv.put(rssi4,nrssi[3]);
		 cv.put(cid5, ncids[4]);
		 cv.put(rssi5,nrssi[4]);
		 cv.put(cid6, ncids[5]);
		 cv.put(rssi6,nrssi[5]);
		 cv.put(lat,f.getLat());
		 cv.put(lon,f.getLon());
		 cv.put(dat,f.getDat());
		 cv.put(tim,f.getTim());
		 String str[]={""};	
		 return db.update(fingerTable, cv, "NULL",str);
		 
	 }
		 

}
