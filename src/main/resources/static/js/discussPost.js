function like(btn, entityType, entityId) {
    $.ajax({
        url: CONTEXT_PATH + "/like",
        method: "post",
        data: {"entityType": entityType, "entityId": entityId},

        //data为服务器返回的数据
        success: function (data) {
            //将控制器方法返回的json转化为js对象
            data = JSON.parse(data);
            if (data.code == 0) {
                $(btn).children("i").text(data.entityLikeCount);
                $(btn).children("b").text(data.entityLikeStatus == 1 ? "已赞" : "赞");
            } else {
                alert(data.msg);
            }
        },

        error: function () {
            alert("服务器错误");
        }
    })
}