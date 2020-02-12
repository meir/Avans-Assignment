<script>

import { onMount } from 'svelte'
import ScheduleBlock from './scheduleblock.svelte'

export let data = [];

onMount(async () => {
    if(process.browser) {
        if(!(data.length > 0)) {
            fetch("http://localhost:8080/api/performances/get").then(resp => {
                return resp.json()
            }).then(resp => {
                data = resp
                console.log(data)
            })
        }
    }
})

</script>

<div id="schedule">
    {#each data.sort((a, b) => {
        return new Date(a.start).getTime() - new Date(b.start).getTime()
    }) as performance}
        <div>
            <ScheduleBlock data={performance}/>
        </div>
    {/each}
</div>

<style>

div#schedule {
    width: 100%;
    border: 1px solid rgba(0, 0, 0, .2);
}

</style>