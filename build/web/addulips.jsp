
<%@page import="java.sql.*;"%>
<%@page import="java.io.*;" %>

<%
    String uname=(String)session.getAttribute("user");
   String company=request.getParameter("comp");
    out.print(company);



 

   
   final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
   final String DB_URL = "jdbc:mysql://localhost/prabhat";

   
   final String USER = "root";
   final String PASS = "root";
   
   
   Connection conn = null;
   Statement stmt = null;
   String query="insert into ulips values(?,?)";
   try{
     
      Class.forName("com.mysql.jdbc.Driver");

      
      
      conn = DriverManager.getConnection(DB_URL, USER, PASS);
             
      PreparedStatement ps=conn.prepareStatement(query);
      ps.setString(1,uname);
      ps.setString(2,company);
      
      ps.executeUpdate();
            
      
   }catch(SQLException se){
      
      se.printStackTrace();
   }catch(Exception e){
     
      e.printStackTrace();
   }finally{
     
      try{
         if(stmt!=null)
            conn.close();
      }catch(SQLException se){
      }
      try{
         if(conn!=null)
            conn.close();
      }catch(SQLException se){
         se.printStackTrace();
      }
   }
  
String site = new String("logged.jsp");
   response.setStatus(response.SC_MOVED_TEMPORARILY);
   response.setHeader("Location", site);


%>