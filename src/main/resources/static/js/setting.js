$(function () {
    $("#uploadForm").submit(upload);
});

function upload() {
    $.ajax({
        url: "https://upload.qiniup.com",
        method: "post",
        //不将表单内容转换为字符串，默认提交表单会将内容转换为字符串提交给服务器
        processData: false,
        //jQuery不设置上传文件的类型，让浏览器自动设置
        //提交文件时是二进制形式，jQuery设置的边界有问题，所以边界由浏览器设置边界字符串
        contentType: false,
        //jQuery对象转换为js对象
        //发送的数据为js对象，方法参数使用@RequestParam接收
        data: new FormData($("#uploadForm")[0]),
        //返回json格式数据
        success: function (data) {
            //上传成功
            if (data != null && data.code == 0) {
                //更新用户头像路径
                $.ajax({
                    url: CONTEXT_PATH + "/user/header/url",
                    method: "post",
                    //发送的数据为js对象，方法参数使用@RequestParam接收
                    //如果发送的数据为json字符串，方法参数使用@RequestBody接收，还要设置contentType
                    data: {"fileName": $("input[name='key']").val()},
                    //data为服务器返回的数据
                    success: function (data) {
                        //将控制器方法返回的json转化为js对象
                        data = $.parseJSON(data);
                        //更新用户头像成功
                        if (data.code == 0) {
                            //刷新页面
                            window.location.reload();
                        } else {
                            alert(data.msg);
                        }
                    }
                });
            } else {
                alert("上传失败!");
            }
        }
    });

    //表单不提交，不执行表单的action
    return false;
}