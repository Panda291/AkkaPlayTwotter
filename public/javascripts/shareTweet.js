// https://stackoverflow.com/questions/1960240/jquery-ajax-submit-form
$(document).ready(function () {
    $(".share-tweet-form").submit(function (e) {
        e.preventDefault();

        var form = $(this);
        var url = form.attr('action');

        $.ajax({
            type: "POST",
            url: url,
            data: form.serialize(),
            success: function (data) {
                if (data === "shared") {
                    form.children('input.alert')
                        .removeClass("alert-warning")
                        .addClass("alert-success")
                        .attr('value', 'unshare')
                } else if (data === "unshared") {
                    form.children('input.alert')
                        .removeClass("alert-success")
                        .addClass("alert-warning")
                        .attr('value', 'share')
                }
            }
        });
    });
});
