// https://stackoverflow.com/questions/1960240/jquery-ajax-submit-form
$(document).ready(function () {
    $(".like-tweet-form").submit(function (e) {
        e.preventDefault();

        var form = $(this);
        var url = form.attr('action');

        $.ajax({
            type: "POST",
            url: url,
            data: form.serialize(),
            success: function (data) {
                if (data === "added") {
                    form.children('input.alert').removeClass("alert-light").addClass("alert-success")
                } else if (data === "removed") {
                    form.children('input.alert').removeClass("alert-success").addClass("alert-light")
                }
            }
        });
    });
});
