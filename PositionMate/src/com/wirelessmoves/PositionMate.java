package com.wirelessmoves;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

import android.app.Activity;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import android.content.Context;

import android.database.Cursor;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import java.util.Formatter;

//public class PositionMate extends MapActivity {
public class PositionMate extends Activity {
	/* menu item id's */
	private static final int RESET_COUNTER = 0;
	private static final int CURRENT_SAMPLE = 1;
	private static final int DATABASE = 2;
	private static final int MATCH_FINGERPRINT = 3;
	static public GridView grid;
	static public ListView list;
	public boolean isCurrentSamplePressed;
	static public String outText = null;
	Formatter form = new Formatter();

	static final String dbName = "/sdcard/demoDB1";
	static final String userDBName = "/sdcard/userDB1";

	private static final long MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 1; // in
																		// Meters
	private static final long MINIMUM_TIME_BETWEEN_UPDATES = 1000; // in
																	// Milliseconds

	protected LocationManager locationManager;

	private static final int UPDATE_DATABASE = 10;

	private static int mode;
	private static int currentInterval = 0;

	private static int bestMatchLatitudeE6;
	private static int bestMatchLongitudeE6;
	/* name of the output file */
	private String cellLogFilename = "cell-log-data.txt";
	DatabaseHelper d, userD;
	/*
	 * These variables need to be global, so we can used them onResume and
	 * onPause method to stop the listener
	 */
	private TelephonyManager Tel;
	private MyPhoneStateListener MyListener;
	private boolean isListenerActive = false;

	/*
	 * These variables need to be global so they can be saved when the activity
	 * exits and reloaded upon restart.
	 */

	private static int gridCellWithMinFrequencyMatchScore;
	private static int gridCellWithMinSignalStrengthMatchScore;

	private static double minFrequencyMatchScore = Integer.MAX_VALUE;
	private static double minSignalStrengthMatchScore = Integer.MAX_VALUE;

	private static double minWeightedSignalStrengthMatchScore = Integer.MAX_VALUE;
	private static int gridCellWithMinWeightedSignalStrengthMatchScore;

	private static ArrayList<Fingerprint> userFingerprint;
	private long NumberOfSignalStrengthUpdates = 0;

	private long LastCellId = 0;
	private long NumberOfCellChanges = -1;

	private long LastLacId = 0;
	private long NumberOfLacChanges = -1;

	private long PreviousCells[] = new long[4];
	private int PreviousCellsIndex = 0;
	private long NumberOfUniqueCellChanges = -1;

	private boolean outputDebugInfo = false;
	private static String matchOutputText = "\n";
	private static String userLogText = "\n";
	private String userLogFilename = "user-log.txt";
	/* Buffer string to cache file operations */
	private String FileWriteBufferStr = "";
	private MapView mapView;

	/*
	 * a resource required to keep the phone from going to the screen saver
	 * after a timeout
	 */
	private PowerManager.WakeLock wl;

	/* This method is called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		setContentView(R.layout.textview1);
		d = new DatabaseHelper(this, dbName);
		userD = new DatabaseHelper(this, userDBName);
		/* If saved variable state exists from last run, recover it */
		if (savedInstanceState != null) {
			locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, MINIMUM_TIME_BETWEEN_UPDATES,
					MINIMUM_DISTANCE_CHANGE_FOR_UPDATES,
					new MyLocationListener());
			NumberOfSignalStrengthUpdates = savedInstanceState
					.getLong("NumberOfSignalStrengthUpdates");

			LastCellId = savedInstanceState.getLong("LastCellId");
			NumberOfCellChanges = savedInstanceState
					.getLong("NumberOfCellChanges");

			LastLacId = savedInstanceState.getLong("LastLacId");
			NumberOfLacChanges = savedInstanceState
					.getLong("NumberOfLacChanges");

			PreviousCells = savedInstanceState.getLongArray("PreviousCells");
			PreviousCellsIndex = savedInstanceState
					.getInt("PreviousCellsIndex");
			NumberOfUniqueCellChanges = savedInstanceState
					.getLong("NumberOfUniqueCellChanges");

			outputDebugInfo = savedInstanceState.getBoolean("outputDebugInfo");

		} else {
			/* Initialize PreviousCells Array to defined values */
			for (int x = 0; x < PreviousCells.length; x++)
				PreviousCells[x] = 0;
		}

		/* Get a handle to the telephony manager service */
		/* A listener will be installed in the object from the onResume() method */
		MyListener = new MyPhoneStateListener();
		Tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

		/*
		 * get a handle to the power manager and set a wake lock so the screen
		 * saver is not activated after a timeout
		 */
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "DoNotDimScreen");

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, RESET_COUNTER, 0, "Reset Counters");
		menu.add(0, CURRENT_SAMPLE, 0, "Current Sample");
		menu.add(0, DATABASE, 0, "Database");
		menu.add(0, MATCH_FINGERPRINT, 0, "Match");
		menu.add(0, UPDATE_DATABASE, 0, "Update");
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case RESET_COUNTER:

			NumberOfCellChanges = 0;
			NumberOfLacChanges = 0;
			NumberOfSignalStrengthUpdates = 0;

			NumberOfUniqueCellChanges = 0;

			/* Initialize PreviousCells Array to a defined value */
			for (int x = 0; x < PreviousCells.length; x++)
				PreviousCells[x] = 0;
			isCurrentSamplePressed = false;
			return true;

		case CURRENT_SAMPLE: {

			setContentView(R.layout.textview1);
			((TextView) findViewById(R.id.textView2)).setText(outText);
			isCurrentSamplePressed = true;
			return true;

		}

		case DATABASE: {
			View v1 = null;
			isCurrentSamplePressed = false;
			see_data(v1);
			return true;
		}
		case MATCH_FINGERPRINT: {
			mode = MATCH_FINGERPRINT;
			isCurrentSamplePressed = false;
			setContentView(R.layout.textview1);
			((TextView) findViewById(R.id.textView2)).setText(matchOutputText);

			return true;
		}

		case UPDATE_DATABASE: {
			mode = UPDATE_DATABASE;
			setContentView(R.layout.textview1);

			isCurrentSamplePressed = true;
			return true;
		}

		default:
			isCurrentSamplePressed = false;
			setContentView(R.layout.textview1);

			return super.onOptionsItemSelected(item);

		}
	}

	public void see_data(View v) {
		setContentView(R.layout.gridview);
		setContentView(R.layout.list);
		Cursor c = d.getAllSamples();
		startManagingCursor(c);

		grid = (GridView) findViewById(R.id.gridView1);
		list = (ListView) findViewById(R.id.listView1);
		String[] from = new String[] { "_id", DatabaseHelper.rssi,
				DatabaseHelper.cid1, DatabaseHelper.rssi1, DatabaseHelper.cid2,
				DatabaseHelper.rssi2, DatabaseHelper.cid3,
				DatabaseHelper.rssi3, DatabaseHelper.cid4,
				DatabaseHelper.rssi4, DatabaseHelper.cid5,
				DatabaseHelper.rssi5, DatabaseHelper.cid6,
				DatabaseHelper.rssi6, DatabaseHelper.lat, DatabaseHelper.lon };
		int m;
		int[] to = new int[] { R.id.i, R.id.rssi, R.id.cid1, R.id.rssi1,
				R.id.cid2, R.id.rssi2, R.id.cid3, R.id.rssi3, R.id.cid4,
				R.id.rssi4, R.id.cid5, R.id.rssi5, R.id.cid6, R.id.rssi6,
				R.id.lat, R.id.lon };
		m = c.getCount();
		if (m > 0) {
			TextView tv = new TextView(getApplicationContext());
			tv.setText(c.getColumnNames().toString());
			// setContentView(tv);
			SimpleCursorAdapter sca = new SimpleCursorAdapter(this,
					R.layout.tablerow1, c, from, to);
			LayoutInflater l = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
			View v1 = null;
			list.addHeaderView(l.inflate(R.layout.header, (ViewGroup) v1));
			list.setAdapter(sca);
			list.refreshDrawableState();
		}

	}

	@Override
	public void onBackPressed() {
		/*
		 * do nothing to prevent the user from accidentally closing the activity
		 * this way
		 */

	}

	/* Called when the application is minimized */
	@Override
	protected void onPause() {
		super.onPause();

		/*
		 * remove the listener object from the telephony manager as otherwise
		 * several listeners will appear on some Android implementations once
		 * the application is resumed.
		 */
		Tel.listen(MyListener, PhoneStateListener.LISTEN_NONE);
		isListenerActive = false;

		/* let the device activate the screen lock again */
		wl.release();
	}

	/* Called when the application resumes */
	@Override
	protected void onResume() {
		super.onResume();

		if (isListenerActive == false) {
			Tel.listen(MyListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
			isListenerActive = true;
		}

		/* prevent the screen lock after a timeout again */
		wl.acquire();
	}

	/* Called when the activity closes or is sent to the background */
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {

		super.onSaveInstanceState(savedInstanceState);

		/* save variables */
		savedInstanceState.putLong("NumberOfSignalStrengthUpdates",
				NumberOfSignalStrengthUpdates);

		savedInstanceState.putLong("LastCellId", LastCellId);
		savedInstanceState.putLong("NumberOfCellChanges", NumberOfCellChanges);

		savedInstanceState.putLong("LastLacId", LastLacId);
		savedInstanceState.putLong("NumberOfLacChanges", NumberOfLacChanges);

		savedInstanceState.putLongArray("PreviousCells", PreviousCells);
		savedInstanceState.putInt("PreviousCellsIndex", PreviousCellsIndex);
		savedInstanceState.putLong("NumberOfUniqueCellChanges",
				NumberOfUniqueCellChanges);

		savedInstanceState.putBoolean("outputDebugInfo", outputDebugInfo);

		/* save the trace data still in the write buffer into a file */
		saveDataToFile(FileWriteBufferStr, "---in save instance, "
				+ DateFormat.getTimeInstance().format(new Date()) + "\r\n",
				cellLogFilename);
		FileWriteBufferStr = "";

	}

	private void saveDataToFile(String LocalFileWriteBufferStr, String id,
			String filename) {
		/* write measurement data to the output file */
		try {
			File root = Environment.getExternalStorageDirectory();
			if (root.canWrite()) {
				File logfile = new File(root, filename);
				FileWriter logwriter = new FileWriter(logfile, true); /*
																	 * true =
																	 * append
																	 */
				BufferedWriter out = new BufferedWriter(logwriter);

				/* first, save debug info if activated */
				if (outputDebugInfo == true)
					out.write(id);

				/* now save the data buffer into the file */
				out.write(LocalFileWriteBufferStr);
				out.close();
			}
		} catch (IOException e) {
			/* don't do anything for the moment */
		}

	}

	/*
	 * The private PhoneState listener class that overrides the signal strength
	 * change method
	 */
	/* This is where the main activity of the this app */
	private class MyPhoneStateListener extends PhoneStateListener {

		private static final int MAX_FILE_BUFFER_SIZE = 2000;

		/*
		 * Get the Signal strength from the provider each time there is an
		 * update
		 */
		@Override
		public void onSignalStrengthsChanged(SignalStrength signalStrength) {
			int NewCellId = 0;
			int NewLacId = 0;
			Location mLocation;

			String outputText = "\n";

			/*
			 * a try enclosure is necessary as an exception is thrown inside if
			 * the network is currently not available.
			 */
			try {
				// outputText = "Software Version: v20\r\n";

				// if (outputDebugInfo == true) outputText +=
				// "Debug Mode Activated\r\n";

				outputText += "\r\n";

				/*
				 * output signal strength value directly on canvas of the main
				 * activity
				 */
				NumberOfSignalStrengthUpdates += 1;
				//outputText += "Number of updates: "
					//	+ String.valueOf(NumberOfSignalStrengthUpdates)
						//+ "\r\n\r\n";

				outputText += "Network Operator: " + Tel.getNetworkOperator()
						+ " " + Tel.getNetworkOperatorName() + "\r\n";
				outputText += "Network Type: "
						+ String.valueOf(Tel.getNetworkType()) + "\r\n\r\n";

				outputText = outputText
						+ "Signal Strength: "
						+ String.valueOf(-113
								+ (2 * signalStrength.getGsmSignalStrength()))
						+ " dBm\r\n\r\n";

				GsmCellLocation myLocation = null;
				while (myLocation == null)
					myLocation = (GsmCellLocation) Tel.getCellLocation();
				int signalstr = -113
						+ (2 * signalStrength.getGsmSignalStrength());
				if (myLocation != null) {
					NewCellId = myLocation.getCid();
				} else
					NewCellId = 0;
				outputText += "Cell ID: " + String.valueOf(NewCellId) + "\r\n";

				if (myLocation != null) {
					NewLacId = myLocation.getLac();
				} else
					NewLacId = 0;
				//outputText += "LAC: " + String.valueOf(NewLacId) + "\r\n\r\n";

				/*
				 * Check if the current cell has changed and increase counter if
				 * necessary
				 */
				if (NewCellId != LastCellId) {
					NumberOfCellChanges += 1;
					LastCellId = NewCellId;
				}

				boolean IsCellInArray = false;

				for (int x = 0; x < PreviousCells.length; x++) {
					if (PreviousCells[x] == NewCellId) {
						IsCellInArray = true;
						break;
					}
				}

				/* if the cell change was unique */
				if (IsCellInArray == false) {
					/*
					 * increase unique cell change counter and save cell id in
					 * array at current index
					 */
					NumberOfUniqueCellChanges++;
					PreviousCells[PreviousCellsIndex] = NewCellId;

					/*
					 * Increase index and wrap back to 0 in case it is at the
					 * end of the array
					 */
					PreviousCellsIndex++;
					if (PreviousCellsIndex == PreviousCells.length)
						PreviousCellsIndex = 0;
				} /* else: do not increase the counter */

				//outputText += "Number of Unique Cell Changes: "
						//+ String.valueOf(NumberOfUniqueCellChanges) + "\r\n";

				/*
				 * Check if the current LAC has changed and increase counter if
				 * necessary
				 */
				if (NewLacId != LastLacId) {
					NumberOfLacChanges += 1;
					LastLacId = NewLacId;
				}
				//outputText += "Number of LAC Changes: "
						//+ String.valueOf(NumberOfLacChanges) + "\r\n\r\n";

				/* Neighbor Cell Stuff */
				List<NeighboringCellInfo> nbcell = Tel.getNeighboringCellInfo();
				outputText += "Current Neighbors with their RSS values: "
						+ String.valueOf(nbcell.size()) + "\r\n";
				Iterator<NeighboringCellInfo> it = nbcell.iterator();
				// Iterator<NeighboringCellInfo> it1 =nbcell.iterator();
				int cid1[] = { 0, 0, 0, 0, 0, 0 }, rssi1[] = { 0, 0, 0, 0, 0, 0 }, i = 0;
				while (it.hasNext()) {

					
					
					NeighboringCellInfo it1 = it.next();
					
					cid1[i] = it1.getCid();
					form.close();
					form = new Formatter();
					outputText += (form.format("%05d  ",cid1[i]).toString());
					outputText += String.valueOf((rssi1[i] = -113
							+ (2 * it1.getRssi())))
							+ " dBm\n";
					FileWriteBufferStr += String.valueOf(cid1[i]) + ", ";
					FileWriteBufferStr += String.valueOf(rssi1[i]) + ", ";
					i++;
				}

				String dt = DateFormat.getDateInstance().format(new Date())
						+ ", ";
				String tm = DateFormat.getTimeInstance().format(new Date());

				// ArrayList<Fingerprint>
				// if (userFingerprint == null)
				// userFingerprint = new ArrayList<Fingerprint>();
				//
				if (mode == UPDATE_DATABASE) {
					Fingerprint f;
					mLocation = locationManager
							.getLastKnownLocation(LocationManager.GPS_PROVIDER);

					// Long l1=mLocation.getTime();

					if (mLocation != null)
						f = new Fingerprint(NewCellId, signalstr, cid1, rssi1,
								mLocation.getLatitude(),
								mLocation.getLongitude(), dt, tm);

					else
						f = new Fingerprint(NewCellId, signalstr, cid1, rssi1,
								0.0, 0.0, dt, tm);

					d.AddFingerprint(f);

				}

				else

				{
					// f = new Fingerprint(NewCellId, signalstr , cid1 ,
					// rssi1,0.0,0.0,dt,tm) ;
					Fingerprint f;
					f = new Fingerprint(NewCellId, signalstr, cid1, rssi1, 0.0,
							0.0, dt, tm);
					// userFingerprint.add(f);
					userD.AddFingerprint(f);
					mLocation = locationManager
							.getLastKnownLocation(LocationManager.GPS_PROVIDER);

					// Long l1=mLocation.getTime();

					if (mLocation != null) {
						f.lat = mLocation.getLatitude();
						f.lon = mLocation.getLongitude();

					}
					d.AddFingerprint(f);

					if (currentInterval == 0) {
						saveDataToFile(
								FileWriteBufferStr,
								"---in listener, "
										+ DateFormat.getTimeInstance().format(
												new Date()) + "\r\n",
								cellLogFilename);
						FileWriteBufferStr = "";
					}
					currentInterval++;

					matchOutputText += String
							.valueOf("\nEntering user records .....\nCurrent user record being entered = ");
					matchOutputText += String.valueOf(currentInterval);
					if (currentInterval == DatabaseHelper.numIntervals) {
						currentInterval = currentInterval
								% DatabaseHelper.numIntervals;
						d.Matchfingerprint(d.getLastNRecordsFromDatabase());

						// matchOutputText +=
						// String.valueOf("\n Frequency match scores are:");
						// for (int io =0 ; io< d.gridSquares.size(); io ++ )
						// { matchOutputText += String.valueOf("\n GridID") +
						// String.valueOf((Integer)(d.gridSquares.keySet().toArray()[io])+1)
						// + String.valueOf(" = ") +
						// String.valueOf(d.gridSquares.get(d.gridSquares.keySet().toArray()[io]).frequencyMatchScore);
						// if (minFrequencyMatchScore >
						// d.gridSquares.get(d.gridSquares.keySet().toArray()[io]).frequencyMatchScore)
						// {
						// minFrequencyMatchScore =
						// d.gridSquares.get(d.gridSquares.keySet().toArray()[io]).frequencyMatchScore;
						// gridCellWithMinFrequencyMatchScore =
						// (Integer)d.gridSquares.keySet().toArray()[io] ;
						// }
						// }

						matchOutputText += String
								.valueOf("\n SignalStrength match scores are:");
						for (int io = 0; io < d.gridSquares.size(); io++) {
							matchOutputText += String.valueOf("\n GridID")
									+ String.valueOf((Integer) (d.gridSquares
											.keySet().toArray()[io]) + 1)
									+ String.valueOf(" = ")
									+ String.valueOf(d.gridSquares
											.get(d.gridSquares.keySet()
													.toArray()[io]).signalStrengthMatchScore);

							if (minSignalStrengthMatchScore > d.gridSquares
									.get(d.gridSquares.keySet().toArray()[io]).signalStrengthMatchScore) {

								minSignalStrengthMatchScore = d.gridSquares
										.get(d.gridSquares.keySet().toArray()[io]).signalStrengthMatchScore;
								gridCellWithMinSignalStrengthMatchScore = (Integer) d.gridSquares
										.keySet().toArray()[io];
							}
						}

						matchOutputText += String
								.valueOf("\n WeightedSignalStrength match scores are:");
						for (int io = 0; io < d.gridSquares.size(); io++) {
							matchOutputText += String.valueOf("\n GridID")
									+ String.valueOf((Integer) (d.gridSquares
											.keySet().toArray()[io]) + 1)
									+ String.valueOf(" = ")
									+ String.valueOf(d.gridSquares
											.get(d.gridSquares.keySet()
													.toArray()[io]).weightedSignalStrengthMatchScore);

							if (minWeightedSignalStrengthMatchScore > d.gridSquares
									.get(d.gridSquares.keySet().toArray()[io]).weightedSignalStrengthMatchScore) {

								minWeightedSignalStrengthMatchScore = d.gridSquares
										.get(d.gridSquares.keySet().toArray()[io]).weightedSignalStrengthMatchScore;
								gridCellWithMinWeightedSignalStrengthMatchScore = (Integer) d.gridSquares
										.keySet().toArray()[io];
							}
						}

						// matchOutputText +=
						// String.valueOf("\nBest co ordinates with frequency = ");
						// matchOutputText += String.valueOf("Grid ID:");
						// matchOutputText +=
						// String.valueOf(d.gridSquares.get(gridCellWithMinFrequencyMatchScore).gridID+1);
						// matchOutputText += String.valueOf("\nLat : ");
						// matchOutputText +=
						// String.valueOf(d.gridSquares.get(gridCellWithMinFrequencyMatchScore).latitude);
						// matchOutputText += String.valueOf("Lon : ");
						// matchOutputText +=
						// String.valueOf(d.gridSquares.get(gridCellWithMinFrequencyMatchScore).longitude);
						//
						matchOutputText += String
								.valueOf("\nBest co ordinates with signal strength = ");
						matchOutputText += String.valueOf("Grid ID:");
						matchOutputText += String
								.valueOf(d.gridSquares
										.get(gridCellWithMinSignalStrengthMatchScore).gridID + 1);
						matchOutputText += String.valueOf("\nLat : ");
						matchOutputText += String
								.valueOf(d.gridSquares
										.get(gridCellWithMinSignalStrengthMatchScore).latitude);
						matchOutputText += String.valueOf("Lon : ");
						matchOutputText += String
								.valueOf(d.gridSquares
										.get(gridCellWithMinSignalStrengthMatchScore).longitude);

						matchOutputText += String
								.valueOf("\nBest co ordinates with weighted signal strength = ");
						matchOutputText += String.valueOf("Grid ID:");
						matchOutputText += String
								.valueOf(d.gridSquares
										.get(gridCellWithMinWeightedSignalStrengthMatchScore).gridID + 1);
						matchOutputText += String.valueOf("\nLat : ");
						matchOutputText += String
								.valueOf(d.gridSquares
										.get(gridCellWithMinWeightedSignalStrengthMatchScore).latitude);
						matchOutputText += String.valueOf("Lon : ");
						matchOutputText += String
								.valueOf(d.gridSquares
										.get(gridCellWithMinWeightedSignalStrengthMatchScore).longitude);

						bestMatchLatitudeE6 = (int) (d.gridSquares
								.get(gridCellWithMinWeightedSignalStrengthMatchScore).latitude * 1000000);
						bestMatchLongitudeE6 = (int) (d.gridSquares
								.get(gridCellWithMinWeightedSignalStrengthMatchScore).longitude * 1000000);
						// setContentView(R.layout.mapview1);
						// mapView = (MapView)findViewById(R.id.mapview);
						// mapView.getController().setCenter(new
						// GeoPoint(bestMatchLatitudeE6, bestMatchLongitudeE6));
						//
						saveDataToFile(
								FileWriteBufferStr,
								"---in listener, "
										+ DateFormat.getTimeInstance().format(
												new Date()) + "\r\n",
								userLogFilename);
						saveDataToFile(
								matchOutputText,
								"---in listener, "
										+ DateFormat.getTimeInstance().format(
												new Date()) + "\r\n",
								userLogFilename);

					}
				}

				/*
				 * Write the information to a file, too This information is
				 * first buffered in a string buffer and only written to the
				 * file once enough data has accumulated
				 */
				FileWriteBufferStr += String
						.valueOf(NumberOfSignalStrengthUpdates) + ", ";
				FileWriteBufferStr += DateFormat.getDateInstance().format(
						new Date())
						+ ", ";
				FileWriteBufferStr += DateFormat.getTimeInstance().format(
						new Date())
						+ ", ";
				FileWriteBufferStr += String.valueOf(NewLacId) + ", ";
				FileWriteBufferStr += String.valueOf(NewCellId) + ", ";
				FileWriteBufferStr += String.valueOf(-113
						+ (2 * signalStrength.getGsmSignalStrength()));
				FileWriteBufferStr += "\r\n";

				//outputText += "File Buffer Length: "
					//	+ FileWriteBufferStr.length() + "\r\n";

				if (FileWriteBufferStr.length() >= MAX_FILE_BUFFER_SIZE) {

					saveDataToFile(FileWriteBufferStr, "---in listener, "
							+ DateFormat.getTimeInstance().format(new Date())
							+ "\r\n", cellLogFilename);
					if (mode != UPDATE_DATABASE)
						saveDataToFile(
								FileWriteBufferStr,
								"---in listener, "
										+ DateFormat.getTimeInstance().format(
												new Date()) + "\r\n",
								userLogFilename);
					FileWriteBufferStr = "";
				}
				if (mode == MATCH_FINGERPRINT) {
					// setContentView(R.layout.textview1);
					((TextView) findViewById(R.id.textView2))
							.setText(matchOutputText);
				}

				super.onSignalStrengthsChanged(signalStrength);

			} catch (Exception e) {
				outputText = "No network information available...";
			}

			outText = outputText;
			if (isCurrentSamplePressed && mode != MATCH_FINGERPRINT) {
				// setContentView(R.layout.textview1);
				((TextView) findViewById(R.id.textView2)).setText(outText);

			}

			else {

				if(isCurrentSamplePressed==false)
				((TextView) findViewById(R.id.textView2))
						.setText(new String("Determining your current position"));
			}

		}

	};/* End of private Class */

	private class MyLocationListener implements LocationListener {

		public void onLocationChanged(Location location) {
		}

		public void onStatusChanged(String s, int i, Bundle b) {
		}

		public void onProviderDisabled(String s) {
		}

		public void onProviderEnabled(String s) {
		}
	}

	// @Override
	// protected boolean isRouteDisplayed() {
	// // TODO Auto-generated method stub
	// return false;
	// }

}