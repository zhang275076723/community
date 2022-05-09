$(function () {
    $("#sendBtn").click(send_letter);
    $(".close").click(delete_msg);
});

/**
 * 发送朋友私信
 */
function send_letter() {
    //发送私信框隐藏
    $("#sendModal").modal("hide");

    //获取接收方用户名
    var toName = $("#recipient-name").val();
    //获取私信内容
    var content = $("#message-text").val();

    $.ajax({
        url: CONTEXT_PATH + "/letter/send",
        method: "post",
        data: {"toName": toName, "content": content},//发送的数据为js对象，方法参数使用@RequestParam接收
        // data: JSON.stringify({"toName": toName, "content": content}),//发送的数据为json字符串，方法参数使用@RequestBody接收，还要设置contentType

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
            alert("服务器错误");
        }
    })

    //提示框显示2s后隐藏
    $("#hintModal").modal("show");
    setTimeout(function () {
        $("#hintModal").modal("hide");
    }, 2000);
}

function delete_msg() {
    // TODO 删除数据
    $(this).parents(".media").remove();
}