let hotSocket = new WebSocket("ws://localhost:8080/ws")
hotSocket.onopen = function (event) {
    console.log("Hot reload enabled.")
}
hotSocket.onclose = function (event) {
    console.log("Closed.")
}
hotSocket.onmessage = function (event) {
    let data = JSON.parse(event.data)
    let eventKey = data.event
    if (eventKey === "ping") {
        return
    }
    if (eventKey === "reload") {
        hotSocket.close()
        location.reload()
    } else if (eventKey === "log") {
        let level = data.level
        let message = data.message
        if (level === "info") console.info(message)
        else if (level === "log") console.log(message)
        else if (level === "warn") console.warn(message)
        else if (level === "error") console.error(message)
        else console.log(message)
    } else {
        console.log("Unknown message: " + data)
    }
}
