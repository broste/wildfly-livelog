<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%! private final Logger logger = LoggerFactory.getLogger(getClass()); %>
<html>
  <head>
    <title>LiveLog TestApp</title>
  </head>
  <body>
    <form action="index.jsp">
        <select name="level">
            <option value="INFO" selected="selected">INFO</option>
            <option value="WARN">WARN</option>
            <option value="ERROR">ERROR</option>
        </select>
        <input type="text" name="message" value="<%= request.getParameter("message") != null ? request.getParameter("message") : "" %>">
        <input type="submit" value="Log">
    </form>
    <%
        String message = request.getParameter("message");
        String levelParam = request.getParameter("level");
        if (levelParam != null) {
            switch (levelParam) {
                case "INFO":
                    logger.info(message);
                    break;
                case "WARN":
                    logger.warn(message);
                    break;
                case "ERROR":
                    logger.error(message);
                    break;
            }
        }
    %>
  </body>
</html>
