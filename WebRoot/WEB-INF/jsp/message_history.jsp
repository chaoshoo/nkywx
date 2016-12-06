<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%
	String path = request.getContextPath();
	String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
			+ path + "/";
%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Residents health management service platform</title>
<meta name="meituan_check">
<meta name="description" content="">
<meta name="keywords" content="">
<meta name="applicable-device" content="mobile">
<meta name="viewport" content="initial-scale=1, width=device-width, maximum-scale=1, user-scalable=no">
<meta name="format-detection" content="telephone=no">
<meta name="format-detection" content="address=no">
<meta name="for" content="meituan.com">
<link href="<%=path %>/css/weui.css" rel="stylesheet">
<link rel="stylesheet" href="<%=path %>/jquery-weui/0.8.0/css/jquery-weui.min.css">
<link href="<%=path %>/css/common.css" rel="stylesheet">
<link href="<%=path %>/css/yuyue.css" rel="stylesheet">
<script type="application/javascript" src="<%=path %>/jquery-weui/0.8.0/js/jquery.min.js" ></script>
<script type="application/javascript" src="<%=path %>/jquery-weui/0.8.0/js/jquery-weui.min.js" ></script>

</head>
<body id="account">
<header class="navbar">
  <div class="nav-wrap-left"> <a class="react back" href="<%=path %>/index/homepage.html"  ><i class="text-icon icon-back"></i></a> </div>
  <span class="nav-header h1"> Message list </span> 
  <div class="nav-wrap-right"> <a class="react headSearch" href="javascript:void(0)"> 
  <font style="font-weight:bold;font-style:italic;"></font>
  </a> </div></header>
<div class="cur-control-con" >
  <div class="weui_search_bar" id="search_bar">
    <form class="weui_search_outer">
      <div class="weui_search_inner"> <i class="weui_icon_search"></i>
        <input type="search" class="weui_search_input" id="search_input" placeholder="search" required>
        <a href="javascript:" class="weui_icon_clear" id="search_clear"></a> </div>
      <label for="search_input" class="weui_search_text" id="search_text"> <i class="weui_icon_search"></i> <span>search</span> </label>
    </form>
    <a href="javascript:" class="weui_search_cancel" id="search_cancel">cancel</a> </div>
        <input type="hidden" name="pageno" id="pageno" value="0"/>
    
  <div id="liuyanlist" class="cur-control-con" >
      <!-- 
  <div class=" replay-item">
  <div class="replay-item-title">I Sunday12：50</div>
  <div class="replay-item-info">Hello,，This is Xie Chen.！A few days ago, you had a physical check-up，Feeling can also，When can I come back！</div>
  </div>
  --> 
    </div>
     <div class="weui-infinite-scroll" id="loadingdiv">
	  <div class="infinite-preloader"></div>
	    <!-- Being loaded... -->
	    <span  id="loading">Pull up load more</span>
	</div>
    </div>
<script>
$(document).ready(function(e) {
	 $("#pageno").val(0);
	messagelist();
	var loading = false;  //State mark
	$(document.body).infinite().on("infinite", function() {
	  if(loading) return;
	  loading = true;
	  setTimeout(function() {
		liuyanlist();
	    loading = false;
	  }, 1500);   //Simulation delay
	});
});
function messagelist(){
	var pagenum = parseInt($("#pageno").val());
	pagenum = pagenum+1;
	var title = $("#search_input").val();
	$.ajax({
		url:"<%=path%>/vip/messagelist.json?ttt="+new Date().getTime(),
		type:"get",
		dataType:"json",
		data:"pageNo="+pagenum+"&content="+title,
		success:function(data){
			var liuyanlisthtml = $("#liuyanlist").html();
			var x = 0;
			$.each(data, function(i, item){  
				x = x+1;
				liuyanlisthtml += '<div class=" replay-item">'
					+'<div class="replay-item-title" ><div class="center" style="margin:0  atuo;">'+item.SENDTIMESTR+'</div></div>'
	               +'  <div class="replay-item-info">'+item.CONTENT+'</div>'
	               +'</div>';

			 });
			$("#liuyanlist").html(liuyanlisthtml);
			if(x<15){
				$("#loadingdiv").hide();
			}
			if(x>0){
				$("#pageno").val(pagenum);
			}
		}
	});
}

</script>
</body>
</html>