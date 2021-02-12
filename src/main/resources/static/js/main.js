var getSolutionsInterval
var logsInterval
var solutionStatusInterval
var currentSolutionId

$(function () {
    var { repo, serverUrl } = getUrls()
    if (repo === null || serverUrl === null) {
        goToIndex();
    }

    $("#serverUrl").text(serverUrl)
    $("#repo").text(repo)

    $("#editButton").click(function (e) {
        goToIndex()
    })

    $('#sendButton').click(function (e) {
        sendSolution()
    })

    $('#runtimeLogButton').click(function (e) { 
        $('#logSelectorBlock>button').addClass('btn-link');
        $(this).removeClass('btn-link');
        showRuntimeLogs();
    });

    $('#buildLogButton').click(function (e) { 
        $('#logSelectorBlock>button').addClass('btn-link');
        $(this).removeClass('btn-link');
        showBuildLogs()
    });

    $('#stopSolutionButton').click(function (e) {
        stopSolution(currentSolutionId)
    });

    $('#closeInfoButton').click(function (e) {
        hideSolutionInfo()
        showTable()
    });

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
        contentType: "application/json",
        cache: "false",
        success: function (response) {
        },
        error: function (jqXHR, exception) {
            var msg = '';
            if (jqXHR.responseText !== "") {
                msg = jqXHR.responseText;
            } else if (jqXHR.status === 0) {
                msg = 'Not connect.\n Verify Network.';
            } else if (jqXHR.status == 404) {
                msg = 'Requested page not found. [404]';
            } else if (jqXHR.status == 500) {
                msg = 'Internal Server Error [500].';
            } else if (exception === 'parsererror') {
                msg = 'Requested JSON parse failed.';
            } else if (exception === 'timeout') {
                msg = 'Time out error.';
            } else if (exception === 'abort') {
                msg = 'Ajax request aborted.';
            } else {
                msg = 'Uncaught Error.\n' + jqXHR.responseText;
            }
            alert(msg);
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

function showRuntimeLogs() {
    clearInterval(logsInterval)
    $('#logField').empty();
    fetchRuntimeLogs(currentSolutionId)
    var status = $('#solStatus').text();
    if (status !== 'ERROR' && status !== 'FINISHED' && status !== 'KILLED') {
        clearInterval(logsInterval)
        logsInterval = setInterval(function () { fetchRuntimeLogs(currentSolutionId); }, 1500)
    }
}

function showBuildLogs() {
    clearInterval(logsInterval)
    $('#logField').empty();
    fetchBuildLogs(currentSolutionId)
    var status = $('#solStatus').text();
    if (status !== 'ERROR' && status !== 'FINISHED' && status !== 'KILLED') {
        clearInterval(logsInterval)
        logsInterval = setInterval(function () { fetchBuildLogs(currentSolutionId); }, 1500)
    }
}

function showSolutionInfo(solutionId) {
    currentSolutionId = solutionId
    $('#solutionInfo').show();
    fetchSolutionStatus(currentSolutionId).then(() => {
        var status = $('#solStatus').text();
        if (status !== 'ERROR' && status !== 'FINISHED' && status !== 'KILLED') {
            clearInterval(solutionStatusInterval)
            solutionStatusInterval = setInterval(function () { fetchSolutionStatus(currentSolutionId); }, 1500)
            clearInterval(logsInterval)
            logsInterval = setInterval(function () { fetchRuntimeLogs(currentSolutionId); }, 1500)
        }
        if (status == 'NEW' || status == 'COMPILING') {
            $('#buildLogButton').click();
        } else {
            $('#runtimeLogButton').click();
        }
    })

}

function fetchSolutionStatus(solutionId) {
    return $.ajax({
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

function fetchBuildLogs(solutionId) {
    var linesCount = $('#logField .logLine').length;
    $.ajax({
        type: "get",
        url: "build_logs",
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