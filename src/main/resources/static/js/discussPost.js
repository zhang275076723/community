$(function () {
    $("#topBtn").click(setTop);
    $("#wonderfulBtn").click(setWonderful);
    $("#deleteBtn").click(setDelete);
});

/**
 * 点赞和取消点赞
 * @param btn
 * @param entityType
 * @param entityId
 * @param entityUserId
 * @param discussPostId
 */
function like(btn, entityType, entityId, entityUserId, discussPostId) {
    $.ajax({
        url: CONTEXT_PATH + "/like",
        method: "post",
        data: {
            "entityType": entityType, "entityId": entityId,
            "entityUserId": entityUserId, "discussPostId": discussPostId
        },

        //data为服务器返回的数据
        success: function (data) {
            //将控制器方法返回的json转化为js对象
            data = JSON.parse(data);
            //成功
            if (data.code == 0) {
                //更改点赞数量
                $(btn).children("i").text(data.entityLikeCount);
                //更改点赞状态
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

/**
 *  置顶帖子和取消置顶
 */
function setTop() {
    $.ajax({
        url: CONTEXT_PATH + "/discussPost/top",
        method: "post",
        data: {"discussPostId": $("#discussPostId").val()},

        //data为服务器返回的数据
        success: function (data) {
            //将控制器方法返回的json转化为js对象
            data = JSON.parse(data);
            //成功
            if (data.code == 0) {
                //更改帖子置顶状态，使用text而不是val修改button内容
                $("#topBtn").text(data.type == 1 ? "已置顶" : "置顶");
            } else {
                alert(data.msg);
            }
        },

        error: function () {
            alert("服务器错误");
        }
    })
}

/**
 * 帖子加精
 */
function setWonderful() {
    $.ajax({
        url: CONTEXT_PATH + "/discussPost/wonderful",
        method: "post",
        data: {"discussPostId": $("#discussPostId").val()},

        //data为服务器返回的数据
        success: function (data) {
            //将控制器方法返回的json转化为js对象
            data = JSON.parse(data);
            //成功
            if (data.code == 0) {
                //更改帖子加精状态，使用text而不是val修改button内容
                $("#wonderfulBtn").text(data.status == 1 ? "已加精" : "加精");
            } else {
                alert(data.msg);
            }
        },

        error: function () {
            alert("服务器错误");
        }
    })
}

/**
 * 帖子删除
 */
function setDelete() {
    $.ajax({
        url: CONTEXT_PATH + "/discussPost/delete",
        method: "post",
        data: {"discussPostId": $("#discussPostId").val()},

        //data为服务器返回的数据
        success: function (data) {
            //将控制器方法返回的json转化为js对象
            data = JSON.parse(data);
            //成功
            if (data.code == 0) {
                //删除成功，跳转到首页
                location.href = CONTEXT_PATH + "/index";
            } else {
                alert(data.msg);
            }
        },

        error: function () {
            alert("服务器错误");
        }
    })
}