package org.wildfly.plugins.demo;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.annotation.Resource;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

@WebServlet("/datasource")
public class MyServlet extends HttpServlet {

    @Resource(lookup = "java:jboss/datasources/OracleDS")
    DataSource oracleDataSource;

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String schema = null;
        try (Connection conn = oracleDataSource.getConnection()) {
            schema = conn.getSchema();
        } catch (SQLException throwables) {
            throwables.printStackTrace(response.getWriter());
        }
        response.getWriter().println("schema="+schema);
    }
}
