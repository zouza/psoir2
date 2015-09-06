<%@ page import="ec2ImageProcessing.ImageProcessing" %>
<html>
<body>
	<%
		ImageProcessing obj = new ImageProcessing();
		obj.getImgFromSQS("SQS");
	%>
</body>
</html>
