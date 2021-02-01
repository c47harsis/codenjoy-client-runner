var getSolutionsInterval
var logsInterval
var solutionStatusInterval

$(function () {
    var { gitUrl, codenjoyUrl } = getUrls()
    if (gitUrl === null || codenjoyUrl === null) {
        $(location).attr('href', '/');
    }

    $("#codenjoyUrl").text(codenjoyUrl)
    $("#gitUrl").text(gitUrl)

    $("#editButton").click(function (e) {
        goToIndex()
    })

    $('#sendButton').click(function (e) {
        sendSolution()
    })

    showTable()
})

function stopSolution(solutionId) {
    $.ajax({
        type: "get",
        url: "stop",
        data: {
            codenjoyUrl: getUrls().codenjoyUrl,
            repoUrl: getUrls().gitUrl,
            solutionId: solutionId
        },
        dataType: "json",
        contentType: "application/json",
        cache: "false",
        success: function (response) {
            $.each(response, function (i, logStr) {
                $('#logField').append('<samp>Has been killed by player.</samp><br/>');
            });
        }
    })
}

function getUrls() {
    return {
        gitUrl: localStorage.getItem('gitUrl'),
        codenjoyUrl: localStorage.getItem('codenjoyUrl')
    }
}

function goToIndex() {
    $(location).attr('href', '/');
}

function sendSolution() {
    var body = {
        repoUrl: getUrls().gitUrl,
        codenjoyUrl: getUrls().codenjoyUrl
    }
    $.ajax({
        type: "post",
        url: "check",
        data: JSON.stringify(body),
        dataType: "json",
        contentType: "application/json",
        cache: "false",
        success: function (response) {
        }
    })
}

function getSolutions() {
    $.ajax({
        type: "get",
        url: "get_all",
        data: {
            codenjoyUrl: getUrls().codenjoyUrl
        },
        dataType: "json",
        contentType: "application/json",
        cache: "false",
        success: function (response) {
            $("#solutionsTable tr").remove();
            $('#solutionsCount').text(response.length);
            if (response.length === 0) {
                $('#tablePlaceholder').show();
            } else {
                $('#tablePlaceholder').hide();
            }
            $.each(response, function (i, solution) {
                var { id, status, created, started, finished } = solution
                row = '<tr class="solutionRow">'
                row += '<th class="sId">' + id + '</th>'
                row += '<td>' + (created || "-") + '</td>'
                row += '<td>' + (started || "-") + '</td>'
                row += '<td>' + (finished || "-") + '</td>'
                if (status === 'COMPILING' || status === 'NEW') {
                    row += '<td class="table-info">' + status + '</td>'
                }
                if (status === 'RUNNING') {
                    row += '<td class="table-warning">' + status + '</td>'
                }
                if (status === 'FINISHED') {
                    row += '<td class="table-success">' + status + '</td>'
                }
                if (status === 'ERROR') {
                    row += '<td class="table-danger">' + status + '</td>'
                }
                if (status === 'KILLED') {
                    row += '<td class="table-dark">' + status + '</td>'
                }
                row += '</tr>'
                $('#solutionsTable').append(row);
                $('.solutionRow').click(function (e) {
                    var solutionId = $(this).find('.sId').text()
                    hideTable()
                    showSolutionInfo(solutionId);
                });
            });
        }
    })
}

function hideTable() {
    console.log(getSolutionsInterval)
    clearInterval(getSolutionsInterval)
    $('#table').hide();
}

function showTable() {
    getSolutions()
    clearInterval(getSolutionsInterval)
    getSolutionsInterval = setInterval(getSolutions, 1500)
    $('#table').show();
}

function hideSolutionInfo() {
    clearInterval(logsInterval)

    clearInterval(solutionStatusInterval)
    $('#solutionInfo').hide();
}

function showSolutionInfo(solutionId) {
    $('#solutionInfo').show();

    fetchSolutionStatus(solutionId)
    fetchLogs(solutionId)

    $('#stopSolutionButton').click(function (e) {
        stopSolution(solutionId)
    });
    $('#closeInfoButton').click(function (e) {
        console.log("BUTTON CLICKED")
        hideSolutionInfo()
        showTable()
    });

    var status = $('#solStatus').text();
    if (status !== 'ERROR' && status !== 'FINISHED' && status !== 'KILLED') {

        clearInterval(logsInterval)
        logsInterval = setInterval(function () { fetchLogs(solutionId); }, 1500)

        clearInterval(solutionStatusInterval)
        solutionStatusInterval = setInterval(function () { fetchSolutionStatus(solutionId); }, 1500)
    }

}

function fetchSolutionStatus(solutionId) {
    $.ajax({
        type: "get",
        url: "get_sol",
        data: {
            codenjoyUrl: getUrls().codenjoyUrl,
            repoUrl: getUrls().gitUrl,
            solutionId: solutionId
        },
        dataType: "json",
        contentType: "application/json",
        cache: "false",
        success: function (response) {
            $('#solId').text(response['id']);
            $('#solStatus').text(response['status']);
            $('#solReceived').text(response['created'] || '-');
            $('#solStarted').text(response['started'] || '-');
            $('#solFinished').text(response['finished'] || '-');
            var status = response['status'];
            if (status === 'FINISHED' || status === 'ERROR' || status === 'KILLED') {
                $('#stopSolutionButton').prop("disabled", true)
            }
        }
    })
}

function fetchLogs(solutionId) {
    $.ajax({
        type: "get",
        url: "get_logs",
        data: {
            codenjoyUrl: getUrls().codenjoyUrl,
            repoUrl: getUrls().gitUrl,
            solutionId: solutionId
        },
        dataType: "json",
        contentType: "application/json",
        cache: "false",
        success: function (response) {
            $('#logField').empty();
            $.each(response, function (i, logStr) {
                $('#logField').append('<samp>' + logStr + '</samp><br/>');
            });
        }
    })
}