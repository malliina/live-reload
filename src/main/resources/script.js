let liveReload = new WebSocket("@WS_URL@")
liveReload.onopen = function (event) {
    console.log("Live reload enabled.")
}
liveReload.onclose = function (event) {
    console.log("Closed.")
}
liveReload.onmessage = async function (event) {
    let data = JSON.parse(event.data)
    let eventKey = data.event
    if (eventKey === "ping") {
        return
    }
    if (eventKey === "reload") {
        liveReload.close()
        await reloadWhenServerReady()
        console.log("Reloading...")
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

async function reloadWhenServerReady() {
    return await fetch(location.href, { method: "HEAD"})
        .catch(async (err) => {
            console.log("HEAD didn't work, waiting 200 ms to try again...")
            await delay(200)
            return await reloadWhenServerReady()
        })
}

function delay(ms){
    return new Promise((res,rej) => setTimeout(res, ms));
}
