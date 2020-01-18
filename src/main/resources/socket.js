let liveSocket = new WebSocket("ws://localhost:10101/ws")
liveSocket.onopen = function (event) {
    console.log("Hot reload enabled.")
}
liveSocket.onclose = function (event) {
    console.log("Closed.")
}
liveSocket.onmessage = function (event) {
    let data = JSON.parse(event.data)
    let eventKey = data.event
    if (eventKey === "ping") {
        return
    }
    if (eventKey === "reload") {
        liveSocket.close()
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
