<script>

let open = false
let operationType = "none"
let call = ""
let body = null

export function Open(type, c, b) {
    operationType = type
    open = true
    call = c
    body = b
    console.log(body)
}

if(process.browser) window.openModal = Open

function getType(plural = false) {
    if(process.browser) {
        if(document.location.href.includes("schedule")) {
            return plural ? "performances" : "Performance"
        }else if(document.location.href.endsWith("/")) {
            return plural ? "artists" : "Artist"
        }else if(document.location.href.endsWith("/podia")) {
            return plural ? "podia" : "Podium"
        }
    }
    return ""
}

function finish() {
    fetch(call, {
        method: body != null ? "POST" : "GET",
        body: JSON.stringify(body)
    }).then(resp => {
        return resp.json()
    }).then(resp => {
        if(resp.success) {
            open = false;
            if(process.browser) location.reload()
        }
    })
}

let defaultData = {
    artists: {
        name: "name",
        description: "description",
        type: "band/artist",
    },
    podia: {
        name: "podium name",
        description: "description",
        location: "bakerstreet 1"
    },
    performances: {
        artist: 0,
        podium: 0,
        start: "2020/01/01 12:00",
        end: "2020/01/01 13:00"
    }
}

let podia = []
let artists = []

if(process.browser) {
    if(getType() === "Performance") {
        fetch("http://localhost:8080/api/podia/get").then(resp => resp.json()).then(resp => {
            podia = resp
        })
        fetch("http://localhost:8080/api/artists/get").then(resp => resp.json()).then(resp => {
            artists = resp
        })
    }
}

</script>

<div id="create" on:click={() => Open("Create", `http://localhost:8080/api/${getType(true)}/create`, defaultData[getType(true)])}>Create { getType() }</div>
{#if open}
    <div id="modal">
        <div>
            <h1>{ operationType } { getType() }</h1>
            <div id="options">
                {#if getType() === "Performance"}
                    <select bind:value={body.artist}>
                        <option disabled selected>Select an artist</option>
                        {#each artists as artist}
                            <option value={artist.id}>{artist.name}</option>
                        {/each}
                    </select>
                    <select bind:value={body.podium}>
                        <option disabled selected>Select a podium</option>
                        {#each podia as podium}
                            <option value={podium.id}>{podium.name}</option>
                        {/each}
                    </select>
                    <input bind:value={body.start} type="datetime-local">
                    <input bind:value={body.end} type="datetime-local">
                {:else if getType() === "Podium"}
                    <input bind:value={body.name} type="text">
                    <input bind:value={body.description} type="text">
                    <input bind:value={body.location} type="text">
                {:else if getType() === "Artist"}
                    <input bind:value={body.name} type="text">
                    <input bind:value={body.description} type="text">
                    <input bind:value={body.type} type="text">
                {/if}
            </div>
            <div id="operations">
                <div class="button" on:click={() => {open = false}}>Cancel</div>
                <div class="button" on:click={finish}>{ operationType }</div>
            </div>
        </div>
    </div>
{/if}

<style>

div#options * {
    display: block;
    width: 80%;
    margin-left: auto;
    margin-right: auto;
}

div#operations {
    display: flex;
    justify-content: right;
}

div.button {
    padding: 1rem;
    margin: 1rem;
    border-radius: 3px;
    display: flex;
    cursor: pointer;
    border: 1px solid rgba(0, 0, 0, .1);
}

div#create {
    padding: 1rem;
    margin-left: 3rem;
    border-radius: 3px;
    display: flex;
    cursor: pointer;
    border: 1px solid rgba(0, 0, 0, .1);
}

div#modal {
    position: absolute;
    width: 100%;
    height: 100%;
    top: 0;
    left: 0;
    background-color: rgba(0, 0, 0, .2);
    backdrop-filter: blur(3px);
}

div#modal > div {
    width: 50%;
    background-color: white;
    border-radius: 3px;
    margin: 1rem auto;
    padding: .2rem;
}

h1 {
    text-align: center;
}
</style>