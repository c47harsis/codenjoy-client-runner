$(function () {
    console.log("Hello")

    $('#btnsubmit').click(function (e) {
        sendSolution()
    });
});



function sendSolution() {

    var body = {}
    body["repoUrl"] = $("#gitUrl").val()
    body["codenjoyUrl"] = $("#codenjoyUrl").val()

    console.log(body)

    $.ajax({
        type: "post",
        url: "/check",
        data: JSON.stringify(body),
        dataType: "json",
        contentType: "application/json",
        cache: "false",
        success: function (response) {
            alert("Hooray!")
        }
    })
}