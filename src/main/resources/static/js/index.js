$(function () {
    var gitUrl = localStorage.getItem('gitUrl')
    var codenjoyUrl = localStorage.getItem('codenjoyUrl')
    if (gitUrl !== null) {
        $("#gitUrl").val(gitUrl)
    }
    if (codenjoyUrl !== null) {
        $("#codenjoyUrl").val(codenjoyUrl)
    }

    $('#btnsubmit').click(function (e) {
        login()
    });
});


function login() {
    localStorage.setItem('gitUrl', $("#gitUrl").val())
    localStorage.setItem('codenjoyUrl', $("#codenjoyUrl").val())
    $(location).attr('href', 'main.html')
}