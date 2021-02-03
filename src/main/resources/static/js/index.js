$(function () {
    var gitUrl = localStorage.getItem('gitUrl')
    var server = localStorage.getItem('server')
    if (gitUrl !== null) {
        $("#gitUrl").val(gitUrl)
    }
    if (server !== null) {
        $("#server").val(server)
    }

    $('#btnsubmit').click(function (e) {
        login()
    });
});


function login() {
    localStorage.setItem('gitUrl', $("#gitUrl").val())
    localStorage.setItem('server', $("#server").val())
    $(location).attr('href', 'main.html')
}