$(document).ready(function () {
    $.ajax({
        url: '/userList',
        type: 'get',
        success: function (data) {
            $("#datalistOptions").html(data)
        }
    })
})
