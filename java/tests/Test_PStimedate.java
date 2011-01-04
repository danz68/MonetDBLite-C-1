/*
 * The contents of this file are subject to the MonetDB Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://monetdb.cwi.nl/Legal/MonetDBLicense-1.1.html
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is the MonetDB Database System.
 *
 * The Initial Developer of the Original Code is CWI.
 * Portions created by CWI are Copyright (C) 1997-July 2008 CWI.
 * Copyright August 2008-2011 MonetDB B.V.
 * All Rights Reserved.
 */

import java.sql.*;

public class Test_PStimedate {
	public static void main(String[] args) throws Exception {
		Class.forName("nl.cwi.monetdb.jdbc.MonetDriver");
		Connection con = DriverManager.getConnection(args[0]);
		Statement stmt = con.createStatement();
		PreparedStatement pstmt;
		ResultSet rs = null;
		//DatabaseMetaData dbmd = con.getMetaData();

		con.setAutoCommit(false);
		// >> false: auto commit was just switched off
		System.out.println("0. false\t" + con.getAutoCommit());

		try {
			stmt.executeUpdate("CREATE TABLE table_Test_PStimedate (t time, ts timestamp, d date)");
		} catch (SQLException e) {
			System.out.println(e);
			System.out.println("Creation of test table failed! :(");
			System.out.println("ABORTING TEST!!!");
			System.exit(-1);
		}

		try {
			pstmt = con.prepareStatement("INSERT INTO table_Test_PStimedate VALUES (?, ?, ?)");
			System.out.print("1. empty call...");
			try {
				// should fail (no arguments given)
				pstmt.execute();
				System.out.println(" PASSED :(");
				System.out.println("ABORTING TEST!!!");
				System.exit(-1);
			} catch (SQLException e) {
				System.out.println(" failed :)");
			}

			System.out.print("2. inserting a record...");
			java.util.Date d = new java.util.Date();
			pstmt.setTime(1, new java.sql.Time(d.getTime()));
			pstmt.setTimestamp(2, new java.sql.Timestamp(d.getTime()));
			pstmt.setDate(3, new java.sql.Date(d.getTime()));

			pstmt.executeUpdate();
			System.out.println(" passed :)");
			System.out.print("3. closing PreparedStatement...");
			pstmt.close();
			System.out.println(" passed :)");

			System.out.print("4. selecting record...");
			pstmt = con.prepareStatement("SELECT * FROM table_Test_PStimedate");
			rs = pstmt.executeQuery();
			System.out.println(" passed :)");

			while (rs.next()) {
				for (int j = 1; j <= 3; j++) {
					System.out.print((j + 4) + ". retrieving...");
					java.util.Date x = (java.util.Date)(rs.getObject(j));
					boolean matches = false;
					if (x instanceof Time) {
						System.out.print(" (Time)");
						matches = (new Time(d.getTime())).toString().equals(x.toString());
					} else if (x instanceof Date) {
						System.out.print(" (Date)");
						matches = (new Date(d.getTime())).toString().equals(x.toString());
					} else if (x instanceof Timestamp) {
						System.out.print(" (Timestamp)");
						matches = (new Timestamp(d.getTime())).toString().equals(x.toString());
					}
					if (matches) {
						System.out.println(" passed :)");
					} else {
						System.out.println(" FAILED :( (" + x + " is not " + d + ")");
					}
				}
			}

			con.rollback();
		} catch (SQLException e) {
			System.out.println("FAILED :( "+ e.getMessage());
			System.out.println("ABORTING TEST!!!");
		}

		con.close();
	}
}
