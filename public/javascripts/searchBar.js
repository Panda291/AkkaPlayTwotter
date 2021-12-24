$(document).ready(function () {
    $.ajax({
        url: '/searchList',
        type: 'get',
        success: function (data) {
            $("#datalistOptions").html(data)
        }
    })
})
