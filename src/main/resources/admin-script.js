var socket;

function initLogWebsocket(webSocketUrl) {
    socket = new ReconnectingWebSocket(webSocketUrl);
    socket.onopen = function (e) {
        console.log("[open] Connection established");
    };

    socket.onmessage = function (event) {
        console.log('[message] Data received from server: ' + event.data);
        let payload = JSON.parse(event.data);
        var content = '';
        payload.changes.forEach(function (dateTime, text) {
            let date = convertDateToLocalTimezone(dateTime);
            content += date + ': ' + text + '\n';
        })
        document.getElementById('log_content').innerHTML = content;
    };

    socket.onclose = function (event) {
        if (event.wasClean) {
            console.log('[close] Connection closed cleanly, code=' + event.code + ' reason=' + event.reason);
        } else {
            console.log('[close] Connection died');
        }
    };

    socket.onerror = function (error) {
        console.log('[error] ' + error.message);
    };
}

function withLeadingZero(number, threeDigits = false) {
    return ((threeDigits && number < 100) ? '0' : '') + (number < 10 ? '0' : '') + number;
}

function convertDateToLocalTimezone(date) {
    let parsedAndConverted = new Date((typeof date === "string" ? new Date(date) : date).toLocaleString());
    parsedAndConverted.setMilliseconds(getMilliseconds(date));
    return parsedAndConverted;
}

function getMilliseconds(date) {
    let split = date.split('.');
    if (split.length > 3) {
        return Math.round(parseInt(split[1]) / 1000);
    } else {
        return Math.round(parseInt(split[1]));
    }
}