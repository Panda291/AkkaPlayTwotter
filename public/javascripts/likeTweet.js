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
                    form.children('input.btn')
                        .removeClass("btn-secondary")
                        .addClass("btn-success")
                        .attr('value', 'unlike')
                } else if (data === "removed") {
                    form.children('input.btn')
                        .removeClass("btn-success")
                        .addClass("btn-secondary")
                        .attr('value', 'like')
                }
            }
        });
    });
});
