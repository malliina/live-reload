let liveReload = new WebSocket("ws://localhost:@PORT@/ws")
liveReload.onopen = function (event) {
    console.log("Hot reload enabled.")
}
liveReload.onclose = function (event) {
    console.log("Closed.")
}
liveReload.onmessage = function (event) {
    let data = JSON.parse(event.data)
    let eventKey = data.event
    if (eventKey === "ping") {
        return
    }
    if (eventKey === "reload") {
        liveReload.close()
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
