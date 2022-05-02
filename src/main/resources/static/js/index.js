$(function () {
    $("#publishBtn").click(publish);
});

function publish() {
    //添加帖子框隐藏
    $("#publishModal").modal("hide");

    //获取标题
    var title = $("#recipient-name").val();
    //获取内容
    var content = $("#message-text").val();

    $.ajax({
        url: CONTEXT_PATH + "/discussPost/add",
        method: "post",
        contentType: "application/json;charset=utf-8",//发送给服务器的数据类型
        data: JSON.stringify({"title": title, "content": content}),//发送的数据为json字符串，方法参数使用@RequestBody接收，还要设置contentType
        // data:{"title": title,"content": content},//发送的数据为js对象，方法参数使用@RequestParam接收

        //data为服务器返回的数据
        success: function (data) {
            //将控制器方法返回的json转化为js对象
            data = JSON.parse(data);
            //提示框显示提示消息
            $("#hintBody").text(data.msg);
            //提示框显示2s后隐藏
            $("#hintModal").modal("show");
            setTimeout(function () {
                $("#hintModal").modal("hide");
                // 刷新页面
                window.location.reload();
            }, 2000);
        },

        error: function () {
            alert("服务器错误！");
        }
    });

    // $.post(
    //     CONTEXT_PATH + "/discussPost/add",
    //     {"title":title,"content":content},
    //     function(data) {
    //         data = $.parseJSON(data);
    //         // 在提示框中显示返回消息
    //         $("#hintBody").text(data.msg);
    //         // 显示提示框
    //         $("#hintModal").modal("show");
    //         // 2秒后,自动隐藏提示框
    //         setTimeout(function(){
    //             $("#hintModal").modal("hide");
    //             // 刷新页面
    //             if(data.code == 0) {
    //                 window.location.reload();
    //             }
    //         }, 2000);
    //     }
    // );
}