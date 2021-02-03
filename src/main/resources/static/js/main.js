var getSolutionsInterval
var logsInterval
var solutionStatusInterval

$(function () {
    var { repo, serverUrl } = getUrls()
    if (repo === null || serverUrl === null) {
        $(location).attr('href', '/');
    }

    $("#serverUrl").text(serverUrl)
    $("#repo").text(repo)

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
            serverUrl: getUrls().serverUrl,
            repo: getUrls().repo,
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
        repo: localStorage.getItem('repo'),
        serverUrl: localStorage.getItem('serverUrl')
    }
}

function goToIndex() {
    $(location).attr('href', '.');
}

function sendSolution() {
    var body = {
        repo: getUrls().repo,
        serverUrl: getUrls().serverUrl
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
        url: "all",
        data: {
            serverUrl: getUrls().serverUrl
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
                row = '<tr class="solutionRow" id="solutionRow-' + id + '">'
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
                $('#solutionRow-' + id).click(function (e) {
                    var solutionId = $(this).find('.sId').text()
                    hideTable()
                    showSolutionInfo(solutionId);
                });
            });
        }
    })
}

function hideTable() {
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

    $('#logField').empty();
    fetchSolutionStatus(solutionId)
    fetchRuntimeLogs(solutionId)

    $('#stopSolutionButton').click(function (e) {
        stopSolution(solutionId)
    });
    $('#closeInfoButton').click(function (e) {
        hideSolutionInfo()
        showTable()
    });

    var status = $('#solStatus').text();
    if (status !== 'ERROR' && status !== 'FINISHED' && status !== 'KILLED') {

        clearInterval(logsInterval)
        logsInterval = setInterval(function () { fetchRuntimeLogs(solutionId); }, 1500)

        clearInterval(solutionStatusInterval)
        solutionStatusInterval = setInterval(function () { fetchSolutionStatus(solutionId); }, 1500)
    }

}

function fetchSolutionStatus(solutionId) {
    $.ajax({
        type: "get",
        url: "summary",
        data: {
            solutionId: solutionId,
            serverUrl: getUrls().serverUrl
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
            } else {
                $('#stopSolutionButton').prop("disabled", false)
            }
        }
    })
}

function fetchRuntimeLogs(solutionId) {
    var linesCount = $('#logField .logLine').length;
    console.log(linesCount);

    $.ajax({
        type: "get",
        url: "runtime_logs",
        data: {
            serverUrl: getUrls().serverUrl,
            solutionId: solutionId,
            offset: linesCount
        },
        dataType: "json",
        contentType: "application/json",
        cache: "false",
        success: function (response) {
            console.log(response);
            $.each(response, function (i, logStr) {
                $('#logField').append('<samp class="logLine">' + logStr + '</samp><br/>');
            });
        }
    })
}