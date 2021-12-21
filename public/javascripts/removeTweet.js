// https://stackoverflow.com/questions/1960240/jquery-ajax-submit-form
$(document).ready(function () {
    $(".remove-tweet-form").submit(function (e) {
        e.preventDefault();

        var form = $(this);
        var url = form.attr('action');

        $.ajax({
            type: "POST",
            url: url,
            data: form.serialize(),
            success: function (data) {
                form.closest('li').remove();
            }
        });
    });
});
