$(function () {
    var repo = localStorage.getItem('repo')
    var serverUrl = localStorage.getItem('serverUrl')
    if (repo !== null) {
        $("#repo").val(repo)
    }
    if (serverUrl !== null) {
        $("#serverUrl").val(serverUrl)
    }

    $('#btnsubmit').click(function (e) {
        login()
    });
});


function login() {
    localStorage.setItem('repo', $("#repo").val())
    localStorage.setItem('serverUrl', $("#serverUrl").val())
    $(location).attr('href', 'main.html')
}