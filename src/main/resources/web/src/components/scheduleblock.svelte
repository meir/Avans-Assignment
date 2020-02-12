<script>

export let data;

const month = {
    0: 'Jan',
    1: 'Feb',
    2: 'Mar',
    3: 'Apr',
    4: 'May',
    5: 'Jun',
    6: 'Jul',
    7: 'Aug',
    8: 'Sep',
    9: 'Oct',
    10: 'Nov',
    11: 'Dec'
}

Number.prototype.pad = function(size) {
    var s = String(this);
    while (s.length < (size || 2)) {s = "0" + s;}
    return s;
}

function Duration(a, b) {
    let timeA = new Date(a).getTime()
    let timeB = new Date(b).getTime()
    let duration = timeB - timeA
    
    const minute = 1000*60

    let hours = Math.floor(duration / (minute * 60))
    let minutes = Math.floor((duration - (hours * (minute * 60))) / minute)
    return `${hours.pad(2)}:${minutes.pad(2)}`
}

function Time(a) {
    let date = new Date(a)
    return `${date.getHours().pad(2)}:${date.getMinutes().pad(2)}`
}

</script>

<div id="main">
    <div id="date">
        <h2>{ new Date(data.start).getDate() }</h2>
        <h4>{ month[new Date(data.start).getMonth()] }</h4>
    </div>
    <div id="content">
        <div>
            <h4>{ data.artist.name }</h4>
            <div>on { data.podium.name }</div>
            <div>Plays from { Time(data.start) }</div>
            <div>for { Duration(data.start, data.end) } hours</div>
        </div>
        <div>
            <div id="operations" on:click={() => {
                fetch("http://localhost:8080/api/performance/delete", {
                    method: "POST",
                    body: JSON.stringify({
                        id: Number(data.id)
                    })
                }).then(resp => {
                    return resp.json()
                }).then(resp => {
                    if(resp.success) {
                        if(process.browser) location.reload()
                    }
                })
            }}>Delete</div>
            <div id="operations" on:click={() => {
                let i = {}
                i.id = data.id
                i.artist = data.artist.id
                i.podium = data.podium.id
                i.start = data.start.replace(' ', 'T')
                i.end = data.end.replace(' ', 'T')
                if(process.browser) window.openModal("Edit", "http://localhost:8080/api/performance/update", i)  
            }}>
                Edit
            </div>
        </div>
    </div>
</div>

<style>

#content {
    display: flex;
    flex: 1;
    padding: .4rem;
    justify-content: space-between;
}

#operations {
    cursor: pointer;
    margin-left: auto;
}

div#main {
    margin: .8rem;
    border: 1px solid rgba(0, 0, 0, .1);
    display: flex;
    width: 50%;
    margin-left: auto;
    margin-right: auto;
}

div#date {
    padding: .2rem;
    width: 60px;
    text-align: center;
    background-color: rgba(255, 255, 255, .5);
}

h2, h4 {
    padding: 0;
    margin: 0;
    letter-spacing: 1.2pt;
    text-transform: uppercase;
}

</style>