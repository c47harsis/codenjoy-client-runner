$(function () {
    var repo = localStorage.getItem('repo')
    var server = localStorage.getItem('server')
    if (repo !== null) {
        $("#repo").val(repo)
    }
    if (server !== null) {
        $("#server").val(server)
    }

    $('#btnsubmit').click(function (e) {
        login()
    });
});


function login() {
    localStorage.setItem('repo', $("#repo").val())
    localStorage.setItem('server', $("#server").val())
    $(location).attr('href', 'main.html')
}