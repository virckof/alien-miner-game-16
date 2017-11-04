package edu.ualberta.phydsl.data;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import android.os.*;

/**
* This class exports events to a simple csv file
*/
public class PersistenceManager {

	public static final String FILENAME = "phydsldata.csv";

	public static void printEvents(String data){
		 printEventsWithHeader(data, null);
	}

	public static void printEventsWithHeader(String data, String header)
	{
		try
        {
            File root = new File(Environment.getExternalStorageDirectory(), "phydsl");

            if (!root.exists()) {
                root.mkdirs();
            }

            File gpxfile = new File(root, FILENAME);
            if (gpxfile.length() == 0 && (header != null)) {
            	data = header + "\n" + data;
            }

            BufferedWriter bW = new BufferedWriter(new FileWriter(gpxfile, true));
            bW.write(data);
            bW.newLine();
            bW.flush();
            bW.close();
        }
        catch(IOException e)
        {
             e.printStackTrace();
        }
	}
}
