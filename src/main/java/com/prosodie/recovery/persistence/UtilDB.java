package com.prosodie.recovery.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.prosodie.recovery.persistence.domain.Record;

public class UtilDB
{

	private static Object monitor = new Object();

	static
	{
		// Load the sqlite-JDBC driver using the current class loader
		try
		{
			Class.forName("org.sqlite.JDBC");
		}
		catch (ClassNotFoundException e)
		{

		}
	}

	private String db;

	public UtilDB(String db)
	{
		super();
		this.db = db;

	}

	private Connection connection() throws SQLException
	{

		Connection connection = DriverManager.getConnection("jdbc:sqlite:" + this.db);

		return connection;

	}

	private void close(Statement stmt, Connection con)
	{

		this.close(null, stmt, con);
	}

	private void close(ResultSet rs, Statement stmt, Connection con)
	{
		if (rs != null)
		{
			try
			{
				rs.close();
			}
			catch (SQLException e)
			{}
		}
		if (stmt != null)
		{
			try
			{
				stmt.close();
			}
			catch (SQLException e)
			{}
		}
		if (con != null)
		{
			try
			{
				con.close();
			}
			catch (SQLException e)
			{}
		}
	}

	// TEXT. The value is a text string, stored using the database encoding (UTF-8, UTF-16BE or UTF-16LE).
	// TEXT as ISO8601 strings ("YYYY-MM-DD HH:MM:SS.SSS").
	public void create() throws SQLException
	{

		Connection connection = null;

		Statement stmt = null;

		try
		{

			connection = this.connection();

			stmt = connection.createStatement();

			stmt.executeUpdate("DROP TABLE IF EXISTS recovery");
			stmt.executeUpdate("CREATE TABLE recovery (id INTEGER PRIMARY KEY, service TEXT, payload TEXT, datetime TEXT, retry INTEGER)");

		}

		finally
		{
			this.close(stmt, connection);
		}

	}

	private static String SQL1 = "INSERT INTO recovery(service,payload,datetime,retry) values (?,?,datetime('now','localtime') ,?)";

	public void save(String servicio, String payload) throws SQLException
	{

		Connection connection = null;

		PreparedStatement pstmt = null;
		try
		{
			synchronized (monitor)
			{

				connection = this.connection();

				pstmt = connection.prepareStatement(SQL1);

				pstmt.setString(1, servicio);
				pstmt.setString(2, payload);
				pstmt.setInt(3, 0);

				pstmt.execute();
			}

		}

		finally
		{

			this.close(pstmt, connection);

		}

	}

	//private static String SQL2 = "SELECT *  FROM recovery WHERE service = ? AND retry < ? AND strftime('%s','now','localtime') - strftime('%s',datetime) > 60";

	private static String SQL2 = "SELECT * FROM recovery WHERE service = ? AND retry < ?";

	public List<Record> query(String servicio, int max_retry) throws SQLException
	{

		Connection connection = null;

		ResultSet rs = null;

		PreparedStatement pstmt = null;

		List<Record> l = new ArrayList<Record>();

		try
		{

			connection = this.connection();

			pstmt = connection.prepareStatement(SQL2);

			pstmt.setString(1, servicio);
			pstmt.setInt(2, max_retry);

			rs = pstmt.executeQuery();

			if (rs != null)
			{
				while (rs.next())
				{

					Record record = new Record();
					record.setID(rs.getInt("id"));
					record.setPayload(rs.getString("payload"));
					record.setRetry(rs.getInt("retry"));

					l.add(record);

				}

			}

		}

		finally
		{

			this.close(rs, pstmt, connection);

		}

		return l;

	}

	private static String SQL3 = "DELETE FROM recovery WHERE id = ?";

	public void delete(int id) throws SQLException
	{

		Connection connection = null;

		PreparedStatement pstmt = null;
		try
		{

			synchronized (monitor)
			{
				connection = this.connection();

				pstmt = connection.prepareStatement(SQL3);

				pstmt.setInt(1, id);

				pstmt.execute();
			}

		}

		finally
		{

			this.close(pstmt, connection);

		}

	}

	private static String SQL4 = "UPDATE recovery SET retry = retry + 1 WHERE id = ?";

	public void add_retry(int id) throws SQLException
	{

		Connection connection = null;

		PreparedStatement pstmt = null;
		try
		{
			synchronized (monitor)
			{
				connection = this.connection();

				pstmt = connection.prepareStatement(SQL4);

				pstmt.setInt(1, id);

				pstmt.execute();
			}

		}

		finally
		{

			this.close(pstmt, connection);

		}

	}

}