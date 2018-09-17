package com.svi.database_selector;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.sql.Connection; // Use 'Connection', 'Statement' and 'ResultSet' classes in java.sql package
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.svi.config.AppConfig;

//JDK 1.7 and above
public class DatabaseSelector { // Save as "JdbcSelectTest.java"

	public static void main(String[] args) throws ClassNotFoundException {
		initializeConfig();
		String myDriver = AppConfig.JDBC_DRIVER.value();
		String myUrl = AppConfig.DATABASE_PATH.value();
		String username = AppConfig.USERNAME.value();
		String password = AppConfig.PASSWORD.value();
		String outputPath = AppConfig.OUTPUT_PATH.value();
		Class.forName(myDriver);
		Date date = new Date();
		String start = new SimpleDateFormat("YYYY-MM-DD, HH:mm:ss").format(date.getTime());
		int fileNumber = 1;
    	File tempFile = new File(AppConfig.OUTPUT_PATH.value() + "csv output" + fileNumber + ".csv");
    	while(true){
    		if(tempFile.exists()){
    			fileNumber++;
    			tempFile = new File(AppConfig.OUTPUT_PATH.value() + "csv output" + fileNumber + ".csv");
    		}else{
    			break;
    		}  		
    	}
		String fileName = ("csv output log" + fileNumber + ".txt");
				
		try (
		// Step 1: Allocate a database 'Connection' object

		Connection conn = DriverManager
				.getConnection(myUrl, username, password);
		// MySQL: "jdbc:mysql://hostname:port/databaseName", "username",
		// "password"

				// Step 2: Allocate a 'Statement' object in the Connection
				Statement stmt = conn.createStatement();) {

			// Step 3: Execute a SQL SELECT query, the query result
			// is returned in a 'ResultSet' object.
			String strSelect = AppConfig.QUERY.value();
			System.out.println("The SQL query is: " + strSelect); // Echo For
																	// debugging
			System.out.println();

			ResultSet rset = stmt.executeQuery(strSelect);

			// Step 4: Process the ResultSet by scrolling the cursor forward via
			// next().
			// For each row, retrieve the contents of the cells with
			// getXxx(columnName).

			System.out.println("The records selected are:");
			int rowCount = 0;
			List<String> rowData = new ArrayList<String>();
			List<String[]> resultSet = new ArrayList<String[]>();
			while (rset.next()) { // Move the cursor to the next row, return
									// false if no more row
				int x = 1;
				rowData.clear();
				while (true) {
					try {
						String data = rset.getString(x);
						rowData.add(data);
						x++;
					} catch (SQLException e) {
						String[] temp = rowData.toArray(new String[rowData.size()]); 
						resultSet.add(temp);
						break;
					}
				}
				++rowCount;
			}
			writeData(outputPath, resultSet, tempFile);
			System.out.println("Total number of records = " + rowCount);
			String end = new SimpleDateFormat("YYYY-MM-DD, HH:mm:ss").format(date.getTime());
			writeLogs(start, end, rowCount, fileName);
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		// Step 5: Close the resources - Done automatically by
		// try-with-resources
		System.out.println("Done!");
	}

	private static void initializeConfig() {
		try {
			AppConfig.setContext(new FileInputStream(new File(
					"config/config.properties")));
		} catch (FileNotFoundException e) {
			System.out.println("ConfigFile Not Found");
			e.printStackTrace();
			System.exit(0);
		}

	}
	
	public static void writeData(String filePath, List<String[]> resultSet, File tempFile){
		BufferedWriter writer = null;
        try {
            //create a temporary file
        	
            File logFile = tempFile;

            // This will output the full path where the file will be written to...
            System.out.println("Writing... \n" + logFile.getCanonicalPath());
            writer = new BufferedWriter(new FileWriter(logFile));
            for(String[] row : resultSet){
            	for(String s : row){
            		int i = 0;
            		writer.write((i >= row.length)? s:(s + ", "));
            		i++;
            	}
            	writer.newLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                // Close the writer regardless of what happens...
                writer.close();
            } catch (Exception e) {
            }
        }
	}
	
	public static void writeLogs(String start, String end, int numberOfRows, String fileName){
		BufferedWriter writer = null;
        try {
            //create a temporary file
            String timeLog = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
            File logFile = new File(AppConfig.OUTPUT_PATH.value() + timeLog + " " + fileName);

            // This will output the full path where the file will be written to...

            writer = new BufferedWriter(new FileWriter(logFile));
            writer.write("Started : " + start);
            writer.newLine();
            writer.write("Ended : " + end);
            writer.newLine();
            writer.write("Number of Rows : " + numberOfRows);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                // Close the writer regardless of what happens...
                writer.close();
            } catch (Exception e) {
            }
        }
	}
	
}
