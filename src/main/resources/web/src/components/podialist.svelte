<script>

import { onMount } from 'svelte'
import PodiumBlock from './podiumblock.svelte'

export let data = [];

onMount(async () => {
    if(process.browser) {
        if(!(data.length > 0)) {
            fetch("http://localhost:8080/api/podia/get").then(resp => {
                return resp.json()
            }).then(resp => {
                data = resp
                console.log(data)
            })
        }
    }
})

</script>

<div id="podialist">
    {#each data as podium}
        <div>
            <PodiumBlock data={podium}/>
        </div>
    {/each}
</div>

<style>

div#podialist {
    width: 100%;
    display: flex;
    flex-wrap: wrap;
    border: 1px solid rgba(0, 0, 0, .2);
}

</style>