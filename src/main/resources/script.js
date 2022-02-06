var socket;

function initWebsocket(webSocketUrl) {
    socket = new ReconnectingWebSocket(webSocketUrl);
    socket.onopen = function(e) {
        console.log("[open] Connection established");
    };

    socket.onmessage = function(event) {
        console.log('[message] Data received from server: ' + event.data);
        document.getElementById('loaded_content').innerHTML = event.data;
    };

    socket.onclose = function(event) {
        if (event.wasClean) {
            console.log('[close] Connection closed cleanly, code=' + event.code + ' reason=' + event.reason);
        } else {
            console.log('[close] Connection died');
        }
    };

    socket.onerror = function(error) {
        console.log('[error] ' + error.message);
    };
}