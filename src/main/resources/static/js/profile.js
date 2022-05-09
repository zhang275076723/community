$(function () {
    $(".follow-btn").click(follow);
});

/**
 * 关注用户、取关用户
 */
function follow() {
    var btn = this;
    if ($(btn).hasClass("btn-info")) {
        // 关注TA
        $.ajax({
            url: CONTEXT_PATH + "/follow",
            method: "post",
            //entityType为3，说明关注实体是用户；entityId通过当前btn的前一个节点获取
            data: {"entityType": 3, "entityId": $(btn).prev().val()},//发送的数据为js对象，方法参数使用@RequestParam接收

            //data为服务器返回的数据
            success: function (data) {
                //将控制器方法返回的json转化为js对象
                data = JSON.parse(data);
                if (data.code == 0) {
                    //刷新页面
                    window.location.reload();
                } else {
                    alert(data.msg);
                }
            },

            error: function () {
                alert("服务器错误");
            }
        });
        //$(btn).text("已关注").removeClass("btn-info").addClass("btn-secondary");
    } else {
        // 取消关注
        $.ajax({
            url: CONTEXT_PATH + "/unfollow",
            method: "post",
            //entityType为3，说明关注实体是用户；entityId通过当前btn的前一个节点获取
            data: {"entityType": 3, "entityId": $(btn).prev().val()},//发送的数据为js对象，方法参数使用@RequestParam接收

            //data为服务器返回的数据
            success: function (data) {
                //将控制器方法返回的json转化为js对象
                data = JSON.parse(data);
                if (data.code == 0) {
                    //刷新页面
                    window.location.reload();
                } else {
                    alert(data.msg);
                }
            },

            error: function () {
                alert("服务器错误");
            }
        });
        //$(btn).text("关注TA").removeClass("btn-secondary").addClass("btn-info");
    }
}